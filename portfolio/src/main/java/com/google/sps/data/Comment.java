// Copyright 2020 Google LLC
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

package com.google.sps.data;

/** A comment on a project. */
public final class Comment {

  private final long id;
  private final String message;
  private final String projectId;
  private final long timestamp;

  public Comment(long id, String message, String projectId, long timestamp) {
    this.id = id;
    this.message = message;
    this.projectId = projectId;
    this.timestamp = timestamp;
  }
}