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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = "";
    String projectId = request.getParameter("projectId");
    System.out.println(projectId);
    if(projectId == null){
        json = this.getAllProjectsJson();
    } else {
        //json = this.getProjectJson(projectId);
    }
    response.getWriter().println(json);
    // /**
    //  * The number of comments we should load. This might be "undefined", indicating load all.
    //  * We keep it as a string to allow for this possibility.
    //  */
    // String commentsCount = request.getParameter("commentsCount");

    // /** 
    //  * Get comments from datastore. 
    //  * Filter to only show comments for a particular project.
    // */
    // Query commentsQuery = new Query("Comment")
    //                         .addSort("timestamp", Query.SortDirection.DESCENDING)
    //                         .addFilter("projectId", Query.FilterOperator.EQUAL, projectId);
    // /** Load query. */
    // DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // PreparedQuery commentResults = datastore.prepare(commentsQuery);

    // /**
    //  * Apply limit if necessary. 
    //  * Use offset 0 so that we can have a default options if try/catch fails.
    //  */
    // FetchOptions options = FetchOptions.Builder.withOffset(0);
    // try {
    //     Integer limit = Integer.parseInt(commentsCount);
    //     options = options.limit(limit);
    // } catch (NumberFormatException expectedIfNoLimit){
    //     /** Don't set limit if not needed. */
    // }

    // /** Use query to populate a list of Comment objects. */
    // ArrayList<Comment> comments = new ArrayList<Comment>();
    
    // for (Entity entity : commentResults.asIterable(options)) {
    //     long id = entity.getKey().getId();
    //     String message = (String) entity.getProperty("message");
    //     long timestamp = (long) entity.getProperty("timestamp");

    //     Comment comment = new Comment(id, message, projectId, timestamp);
    //     comments.add(comment);
    // }

    // /** Send JSON encoded list of comments. */
    // Gson gson = new Gson();
    // String json = gson.toJson(comments);

    // response.setContentType("application/json");
    // response.getWriter().println(json);
  }

  String getAllProjectsJson() {

    /** 
     * Get projects from datastore. 
    */
    Query projectsQuery = new Query("Project");

    /** Load query. */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery projectResults = datastore.prepare(projectsQuery);

    Gson gson = new Gson();

    /** Use query to populate a list of Project objects. */
    ArrayList<Project> projects = new ArrayList<Project>();
    
    for (Entity entity : projectResults.asIterable()) {
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

        Project project = new Project(id, name, description, tags, details, links);
        projects.add(project);
    }
    return gson.toJson(projects);
  }
}