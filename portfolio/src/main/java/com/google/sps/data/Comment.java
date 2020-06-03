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

/**
 * A comment on a project.
 * Used only for Gson serialization of existing datastore data.
 */
public final class Comment {
  /** The unique datastore assigned id */
  private final long id;
  /** The main written content of this comment. */
  private final String message;
  /** The associated project Id */
  private final String projectId;
  /** The time in ms since Epoch that this comment was created. Server generated. */
  private final long timestamp;
  /** The email of the user who made this comment, server assigned based on auth state.*/
  private final String email;

  public Comment(long id, String message, String projectId, long timestamp, String email) {
    this.id = id;
    this.message = message;
    this.projectId = projectId;
    this.timestamp = timestamp;
    this.email = email;
  }
}
