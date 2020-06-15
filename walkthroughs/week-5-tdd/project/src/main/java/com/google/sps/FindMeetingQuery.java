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

import com.google.sps.TimeRange;
import java.lang.Math;
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

    ArrayList<Event> sortedEvents = new ArrayList<>(events);
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

    // Whether any events exist for mandatory and optional attendees respectively.
    boolean hasMandatoryEvents = false;
    boolean hasOptionalEvents = false;

    // Whether we should consider attendees of a given class.
    // This is used to handle cases like all optional no mandatory, no attendees, etc.
    boolean considerMandatoryAttendees = request.getAttendees().size() > 0;
    boolean considerOptionalAttendees = request.getOptionalAttendees().size() > 0;

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

      if (containsMandatoryAttendees) {
        hasMandatoryEvents = true;
      }
      if (containsOptionalAttendees) {
        hasOptionalEvents = true;
      }

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

    // At this point there are no more events, so remaining might be a contiguous potential window.
    boolean potentialMandatoryWindowExists = windowIsLongEnough(mandatoryBlockExtent, TimeRange.END_OF_DAY, request);
    // It is only acceptable for the extent to still be 0 IF there are no events that could move that extent.
    boolean remainingMandatoryExtentAcceptable = mandatoryBlockExtent > 0 || hasMandatoryEvents == false;

    // Same as above, for optional.
    boolean potentialOptionalWindowExists = windowIsLongEnough(optionalBlockExtent, TimeRange.END_OF_DAY, request);
    boolean remainingOptionalExtentAcceptable = optionalBlockExtent > 0 || hasOptionalEvents == false;

    // This could be a mandatory-window.
    if (potentialMandatoryWindowExists && remainingMandatoryExtentAcceptable) {
      // Per test FindMeetingQueryTest.optionsForNoAttendees, having no attendees should still return availability.
      boolean noAttendeesToConsider = !considerMandatoryAttendees && !considerOptionalAttendees;
      if (considerMandatoryAttendees || noAttendeesToConsider) {
        timeSlots.add(
          TimeRange.fromStartEnd(mandatoryBlockExtent, TimeRange.END_OF_DAY, true)
        );
      }
      // As above, we can only consider the optional window IF the mandatory window also exists.
      if (considerOptionalAttendees && potentialOptionalWindowExists && remainingOptionalExtentAcceptable) {
        timeSlotsWithOptional.add(
          TimeRange.fromStartEnd(
            Math.max(mandatoryBlockExtent, optionalBlockExtent), // Only use the overlap between optional/mandatory availability.
            TimeRange.END_OF_DAY, true
          )
        );
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

