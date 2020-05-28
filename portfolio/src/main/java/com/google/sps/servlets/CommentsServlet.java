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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    /** Get comments from datastore. **/
    Query commentsQuery = new Query("Comment")
                            .addSort("timestamp", Query.SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery commentResults = datastore.prepare(commentsQuery);

    ArrayList<Comment> comments = new ArrayList<Comment>();
    for (Entity entity : commentResults.asIterable()) {
        long id = entity.getKey().getId();
        String message = (String) entity.getProperty("message");
        String projectId = (String) entity.getProperty("projectId");
        long timestamp = (long) entity.getProperty("timestamp");

        Comment comment = new Comment(id, message, projectId, timestamp);
        comments.add(comment);
    }

    /** Send JSON encoded list of comments. */
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /** Extract comment properties from request. */
    String message = request.getParameter("message");
    String projectId = request.getParameter("projectId");
    long timestamp = System.currentTimeMillis();

    /** Create and set properties for a new comment in datastore. */
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("message", message);
    commentEntity.setProperty("projectId", projectId);
    commentEntity.setProperty("timestamp", timestamp);

    /** Save to datastore. */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    
    /** Redirect client back to original project page. */
    String redirectUrl = "/project-detail.html?projectId=" + projectId; 
    response.sendRedirect(redirectUrl);
  }
}