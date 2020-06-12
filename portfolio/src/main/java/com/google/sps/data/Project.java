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

import java.util.List;

/** 
 * A project that is part of the portfolio.
 * This class is only used to Gson Serialize existing projects from datastore.
 */
public final class Project {

  /** A link from a project to a third party site. */
  public final class ProjectLink {
    /** Displayed name of site/link */
    private final String name;
    /** Actual fully qualified link */
    private final String href;

    public ProjectLink(String name, String href) {
        this.name = name;
        this.href = href;
    }
  }

  /** The unique data-store assigned id */
  private final long id;
  /** The headline-displayed project name */
  private final String name;
  /** A short < 1 sentence description of the project. **/
  private final String description;
  /** All tags that this project is searchable under. */
  private final List<String> tags;
  /** A list of additional 1+ sentence descriptions, shown in detail view. */
  private final List<String> details;
  /** A list of links to third-party websites. (e.g Github, demo) */
  private final List<ProjectLink> links;

  public Project(long id, String name, String description, List<String> tags, List<String> details, List<ProjectLink> links) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.tags = tags;
    this.details = details;
    this.links = links;
  }
}
