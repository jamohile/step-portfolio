// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.sps.Event;
import com.google.sps.TimeRange;
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Determines potential meeting times based on a set of pre-existing events
 * and mandatory/optional attendees who must be present.
 */
public final class FindMeetingQuery {
  /** Used to sort events by start time. */
  public static final Comparator<Event> EVENT_COMPARATOR = new Comparator<Event>() {

    @Override
    public int compare(Event a, Event b) {
      return TimeRange.ORDER_BY_START.compare(a.getWhen(), b.getWhen());
    }
  };

  /** Finds potential meeting times */
  public Collection<TimeRange> query(
    Collection<Event> events,
    MeetingRequest request
  ) {
    // Potential windows that only include mandatory attendees.
    ArrayList<TimeRange> timeSlots = new ArrayList<>();
    // Potential windows that include ALL mandatory + optional attendees.
    ArrayList<TimeRange> timeSlotsWithOptional = new ArrayList<>();

    // A list of all attendees, would be nice if this could be done in one line,
    // but not a big deal.
    HashSet<String> allAttendees = new HashSet<String>();
    allAttendees.addAll(request.getAttendees());
    allAttendees.addAll(request.getOptionalAttendees());

    // This algorithm assumes events are sorted by start time.
    ArrayList<Event> sortedEvents = new ArrayList<>(events);

    // This algorithm uses the time between two events to find out when there meeting slots.
    // This means that if the last event ends at 6pm, it is unable to consider the window
    // from 6pm-12am.
    // To handle this without a special case, we add a 'fake' event with everyone present at midnight,
    // artificially making a window.
    Event endOfDayEvent = new Event("END_OF_DAY", TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 1), allAttendees);
    sortedEvents.add(endOfDayEvent);

    sortedEvents.sort(EVENT_COMPARATOR);

    /*
     * We consider this problem in terms of 'windows' and 'blocks'.
     * |-----A-----|                  |------A-----|
     *       |----------B-----|       |---B---|
     *
     * |______________________||______|
     *        block              window
     *
     * We iterate shifts to identify the right-most extent of a block.
     * If we find a shift that start *after* this point, the difference is a window.
     * That window is a possible meeting spot, if long enough.
     */
    int mandatoryBlockExtent = TimeRange.START_OF_DAY; // Based on people who *must* attend
    int optionalBlockExtent = TimeRange.START_OF_DAY; // Based on people who *may* attend

    // Whether we should consider attendees of a given class.
    // This is used to handle cases like all optional no mandatory, no attendees, etc.
    boolean considerMandatoryAttendees = request.getAttendees().size() > 0;
    boolean considerOptionalAttendees = request.getOptionalAttendees().size() > 0;

    if (!(considerMandatoryAttendees || considerOptionalAttendees)) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    for (Event event : sortedEvents) {
      TimeRange when = event.getWhen();
      // If true, the meeting request cannot overlap with this.
      boolean containsMandatoryAttendees = anyAttendeesAreMandatory(
        event,
        request
      );
      // If true, meeting requests that try to include optional attendees cannot overlap with this.
      boolean containsOptionalAttendees = anyAttendeesAreOptional(
        event,
        request
      );

      // As mentioned above, we track 'mandatory' people and 'optional' people as two classes.
      // For a given class, a window exists if there is time between current event involving that class, 
      // And the end of the last event involving that class (_____BlockExtent)
      boolean optionalWindowExists = when.start() > optionalBlockExtent;
      boolean mandatoryWindowExists = when.start() > mandatoryBlockExtent;

      // Makes sure the requested meeting length fits within the window.
      boolean optionalWindowLongEnough = optionalWindowExists && windowIsLongEnough(optionalBlockExtent, when.start(), request);
      boolean mandatoryWindowLongEnough = mandatoryWindowExists && windowIsLongEnough(mandatoryBlockExtent, when.start(), request);

      // Optional-based windows must also include all mandatory attendees.
      if (considerOptionalAttendees && optionalWindowLongEnough && mandatoryWindowLongEnough) {
        timeSlotsWithOptional.add(
          /* Add non-inclusive window because it can't overlap with the start of this event. */
          TimeRange.fromStartEnd(
            Math.max(optionalBlockExtent, mandatoryBlockExtent), // Only use overlap between optional/mandatory availability.
            when.start(), false)
        );
      }
      

      if (containsMandatoryAttendees && mandatoryWindowLongEnough) {
        timeSlots.add(
          /* Add non-inclusive window because it can't overlap with the start of this event. */
          TimeRange.fromStartEnd(mandatoryBlockExtent, when.start(), false)
        );
      }

      // If this window contains members from a certain class, update their respective block extent.
      if (containsOptionalAttendees) { // We group all optional attendees as one lump for now.
        optionalBlockExtent = Math.max(optionalBlockExtent, when.end());
      }
      if (containsMandatoryAttendees) {
        mandatoryBlockExtent = Math.max(mandatoryBlockExtent, when.end());
      }
    }
    // If possible, return the windows that include all optional. Otherwise, just return mandatory.
    return timeSlotsWithOptional.size() > 0 ? timeSlotsWithOptional : timeSlots;
  }

  /**
   * Ensure a potential window is long enough to fit meet request.
   * @param from     start of window in minutes.
   * @param end      end of window in minutes.
   * @param request  target meeting request.
   * @return         whether window is long enough.
   */
  private boolean windowIsLongEnough(int from, int to, MeetingRequest request) {
    return (to - from) >= (int) request.getDuration();
  }

  /** Whether this event contains people who must be present at the meeting.*/
  private boolean anyAttendeesAreMandatory(Event event, MeetingRequest request) {
    Set<String> intersection = new HashSet<>(event.getAttendees());
    /* Find only people present in both sets. */
    intersection.retainAll(request.getAttendees());
    return intersection.size() > 0;
  }

  /** Whether this event contains people who may be present at the meeting.*/
  private boolean anyAttendeesAreOptional(Event event, MeetingRequest request) {
    Set<String> intersection = new HashSet<>(event.getAttendees());
    /* Find only people present in both sets. */
    intersection.retainAll(request.getOptionalAttendees());
    return intersection.size() > 0;
  }
}

