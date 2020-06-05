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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
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

/** 
 * Handles serving and creating comments through GET and POST respectively. 
 * All requests must be accompanied by a projectId query-param, as comments are many-to-one to projects.
 * Only comments for the requested project will be served.
 */
@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {

  /**
   * Serve a JSON array of all comments for the specified projectId.
   * TODO: Add error response if no projectId is provided.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String projectId = request.getParameter("projectId");
    /**
     * The number of comments we should load. This might be "undefined", indicating load all.
     * We keep it as a string to allow for this possibility.
     */
    String commentsCount = request.getParameter("commentsCount");

    /* 
     * Get comments from datastore. 
     * Filter to only show comments for a particular project.
    */
    Query commentsQuery = new Query("Comment")
                            .addSort("timestamp", Query.SortDirection.DESCENDING)
                            .addFilter("projectId", Query.FilterOperator.EQUAL, projectId);
    /* Load query. */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery commentResults = datastore.prepare(commentsQuery);

    /**
     * Apply limit if necessary. 
     * Use offset 0 so that we can have a default options if try/catch fails.
     */
    FetchOptions options = FetchOptions.Builder.withOffset(0);
    try {
        Integer limit = Integer.parseInt(commentsCount);
        options = options.limit(limit);
    } catch (NumberFormatException expectedIfNoLimit){
        /** Don't set limit if not needed. */
    }

    /** Use query to populate a list of Comment objects. */
    ArrayList<Comment> comments = new ArrayList<Comment>();
    
    for (Entity entity : commentResults.asIterable(options)) {
        long id = entity.getKey().getId();
        String message = (String) entity.getProperty("message");
        long timestamp = (long) entity.getProperty("timestamp");

        Comment comment = new Comment(id, message, projectId, timestamp);
        comments.add(comment);
    }

    /* Send JSON encoded list of comments. */
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /**
   * Takes a projectId and message (as query params) and stores a comment.
   * Upon successful save, the client is redirected back to the relevant project detail page.
   * TODO: Add error responses.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /* Extract comment properties from request. */
    String message = request.getParameter("message");
    String projectId = request.getParameter("projectId");
    long timestamp = System.currentTimeMillis();

    /* Create and set properties for a new comment in datastore. */
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("message", message);
    commentEntity.setProperty("projectId", projectId);
    commentEntity.setProperty("timestamp", timestamp);

    /* Save to datastore. */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    
    /* Redirect client back to original project page. */
    String redirectUrl = "/project-detail.html?projectId=" + projectId; 
    response.sendRedirect(redirectUrl);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String projectId = request.getParameter("projectId");
    
    /** 
     * Get keys for all comments related to this projectId.
     */
    Query commentsQuery = new Query("Comment")
                            .setKeysOnly()
                            .addFilter("projectId", Query.FilterOperator.EQUAL, projectId);
    /** Load query. */
    PreparedQuery commentResults = datastore.prepare(commentsQuery);

    /** Extract keys and delete. */
    ArrayList<Key> commentKeys = new ArrayList<Key>();
    for (Entity comment : commentResults.asIterable()) {
        commentKeys.add(comment.getKey());
    }

    /** Convert commentKeys to a normal array so it can be used by datastore.delete varargs. */
    datastore.delete(commentKeys.toArray(new Key[commentKeys.size()]));
  }
  
}

