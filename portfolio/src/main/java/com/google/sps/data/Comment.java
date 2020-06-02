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
 * This is used only for JSON serialization when sending datastore comments to the user.
 */
public final class Comment {
  
  /** Unique ID as assigned automatically by datastore. */
  private final long id;
  /** The main text content of this comment */
  private final String message;
  /** The project this comment is associated with. */
  private final String projectId;
  /** The time this comment was created, in ms since Epoch. */
  private final long timestamp;

  /**
   * Get a new Comment for JSON serialization.
   * This should only be used to serialize existing datastore entities.
   * 
   * @param id         datastore-generated unique id for this comment.
   * @param message    main text of this message, in any language.
   * @param projectId  projectId this comment is associated with.
   * @param timestamp  comment creation date in MS since Epoch.
   */
  public Comment(long id, String message, String projectId, long timestamp) {
    this.id = id;
    this.message = message;
    this.projectId = projectId;
    this.timestamp = timestamp;
  }
}
