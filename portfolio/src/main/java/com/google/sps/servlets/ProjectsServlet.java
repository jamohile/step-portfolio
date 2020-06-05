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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sps.data.Project;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns one or many projects.*/
@WebServlet("/projects")
public class ProjectsServlet extends HttpServlet {

  /** Get one or many projects. (based on whether projectId query param present)*/
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = "";
    /** May be null */
    String projectId = request.getParameter("projectId");

    /** Presence of projectId flag determines whether to return one or many projects. */
    if(projectId == null){
        json = this.getAllProjectsJson();
    } else {
        try {
            json = this.getOneProjectJson(Long.parseLong(projectId));
        } catch (Exception badRequest) {
            /** Client sent bad projectId */
            response.setStatus(404);
            response.getWriter().println("Invalid project ID.");
            return;
        }
    }
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /* Get JSON for a single project by id. */
  private String getOneProjectJson(long projectId) throws EntityNotFoundException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key projectKey = KeyFactory.createKey("Project", projectId);
    Entity projectEntity = datastore.get(projectKey);

    Gson gson = new Gson();
    return gson.toJson(this.getProjectFromEntity(projectEntity));
  }

  /** Get JSON for all projects. */
  private String getAllProjectsJson() {

    /** 
     * Get projects from datastore. 
    */
    Query projectsQuery = new Query("Project");

    /** Load query. */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery projectResults = datastore.prepare(projectsQuery);

    /** Use query to populate a list of Project objects. */
    ArrayList<Project> projects = new ArrayList<Project>();
    for (Entity entity : projectResults.asIterable()) {
        projects.add(this.getProjectFromEntity(entity));
    }

    Gson gson = new Gson();
    return gson.toJson(projects);
  }

  /** Get a project class from a datastore entity. */
  private Project getProjectFromEntity(Entity entity) {
    Gson gson = new Gson();

    long id = entity.getKey().getId();

    String name = (String) entity.getProperty("name");
    String description = (String) entity.getProperty("description");
    /** The remaining properties are lists. GSON needs additional type information to properly decode these. */
    List<String> tags = gson.fromJson(
        (String) entity.getProperty("tags"), 
        new TypeToken<ArrayList<String>>(){}.getType()
    );
    List<String> details = gson.fromJson(
        (String) entity.getProperty("details"), 
        new TypeToken<ArrayList<String>>(){}.getType()
    );
    List<Project.ProjectLink> links = gson.fromJson(
        (String) entity.getProperty("links"), 
        new TypeToken<ArrayList<Project.ProjectLink>>(){}.getType()
    );

    return new Project(id, name, description, tags, details, links);
  }
}
