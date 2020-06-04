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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

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

    ArrayList<Event> sortedEvents = new ArrayList<>(events);
    sortedEvents.sort(EVENT_COMPARATOR);

    return timeSlots;
  }
}

