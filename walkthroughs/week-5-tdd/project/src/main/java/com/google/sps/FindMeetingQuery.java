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

public final class FindMeetingQuery {
  /** Used to sort events by start time. */
  public static final Comparator<Event> EVENT_COMPARATOR = new Comparator<Event>() {

    @Override
    public int compare(Event a, Event b) {
      return TimeRange.ORDER_BY_START.compare(a.getWhen(), b.getWhen());
    }
  };

  public Collection<TimeRange> query(
    Collection<Event> events,
    MeetingRequest request
  ) {
    ArrayList<TimeRange> timeSlots = new ArrayList<>();
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

    int numMandatoryEvents = 0;
    int numOptionalEvents = 0;

    boolean considerMandatoryAttendees = request.getAttendees().size() > 0;
    boolean considerOptionalAttendees = request.getOptionalAttendees().size() > 0;

    for (Event event : sortedEvents) {
      TimeRange when = event.getWhen();
      /** If true, this event can't be ignored. */
      boolean containsMandatoryAttendees = anyAttendeesAreMandatory(
        event,
        request
      );
      boolean containsOptionalAttendees = anyAttendeesAreOptional(
        event,
        request
      );

      if (containsMandatoryAttendees) { numMandatoryEvents++; }
      if (containsOptionalAttendees) { numOptionalEvents++; }

      boolean optionalWindowExists = when.start() > optionalBlockExtent;
      boolean mandatoryWindowExists = when.start() > mandatoryBlockExtent;

      boolean optionalWindowLongEnough = optionalWindowExists && windowIsLongEnough(optionalBlockExtent, when.start(), request);
      boolean mandatoryWindowLongEnough = mandatoryWindowExists && windowIsLongEnough(mandatoryBlockExtent, when.start(), request);

      if (considerOptionalAttendees && optionalWindowExists) { // This is a potential window.
        if (optionalWindowLongEnough && mandatoryWindowLongEnough) {
          timeSlotsWithOptional.add(
            /* Add non-inclusive window because it can't overlap with the start of this event. */
            TimeRange.fromStartEnd(
              Math.max(optionalBlockExtent, mandatoryBlockExtent),
              when.start(), false)
          );
        }
      }
      

      if (containsMandatoryAttendees && mandatoryWindowExists) { // This is a potential window.
        if (mandatoryWindowLongEnough) {
          timeSlots.add(
            /* Add non-inclusive window because it can't overlap with the start of this event. */
            TimeRange.fromStartEnd(mandatoryBlockExtent, when.start(), false)
          );
        }
      }

      if (containsOptionalAttendees) { // We group all optional attendees as one lump for now.
        optionalBlockExtent = Math.max(optionalBlockExtent, when.end());
      }
      
      if (containsMandatoryAttendees) {
        mandatoryBlockExtent = Math.max(mandatoryBlockExtent, when.end());
      }
    }

    /* At this point there are no more events, so remaining must be a potential window. */
    if (windowIsLongEnough(mandatoryBlockExtent, TimeRange.END_OF_DAY, request) && (numMandatoryEvents == 0 || mandatoryBlockExtent != 0)) {
      if (considerMandatoryAttendees || (!considerMandatoryAttendees && !considerOptionalAttendees)) {
        timeSlots.add(
          TimeRange.fromStartEnd(mandatoryBlockExtent, TimeRange.END_OF_DAY, true)
        );
      }
      if (considerOptionalAttendees && windowIsLongEnough(optionalBlockExtent, TimeRange.END_OF_DAY, request) && (numOptionalEvents == 0 || optionalBlockExtent != 0)) {
        timeSlotsWithOptional.add(
          TimeRange.fromStartEnd(
            Math.max(mandatoryBlockExtent, optionalBlockExtent), 
            TimeRange.END_OF_DAY, true
          )
        );
      }
    }

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

