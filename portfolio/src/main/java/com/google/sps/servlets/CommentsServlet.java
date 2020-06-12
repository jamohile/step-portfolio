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
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
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
   * Get N comments for a given project. 
   * Each comment is translated to the language indicated by query param languageCode, per ISO-639-1
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String projectId = request.getParameter("projectId");
    /*
     * The number of comments we should load. This might be "undefined", indicating load all.
     * We keep it as a string to allow for this possibility.
     */
    String commentsCount = request.getParameter("commentsCount");

    /*
     * The desired output language code in ISO-639-1 format.
     * Final because it is used in a lambda below.
     */
    final String languageCode = request.getParameter("languageCode");

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

    /*
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

    /* Use query to populate a list of Comment objects. */
    ArrayList<Comment> comments = new ArrayList<Comment>();
    ArrayList<Entity> commentEntities = new ArrayList<Entity>();

    /* Add entities to a list for convenient access later */
    for (Entity entity: commentResults.asIterable(options)) {
      commentEntities.add(entity);
    }

    /* Create a threadpool to perform multiple translations in parallel */
    ExecutorService executor = Executors.newFixedThreadPool(10);
    /* A list to hold pending translations in the order they were added. */
    CompletableFuture<String>[] translationFutures = new CompletableFuture[commentEntities.size()];

    /* Iterate entities and start a translation async for each */
    for (int i = 0; i < commentEntities.size(); i++) {
      Entity entity = commentEntities.get(i);
      
      /* Lambda expression that captures the entity, performs translation, returns translated message. */
      Supplier<String> translationTask = () -> {
        String message = (String) entity.getProperty("message"); 
        
        /* A new translation service is created each time because Translate may not be threadsafe.*/
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation = translate.translate(
          message,
          Translate.TranslateOption.targetLanguage(languageCode == null ? "en" : languageCode)
        );
        return translation.getTranslatedText();
      };

      translationFutures[i] = CompletableFuture.supplyAsync(translationTask, executor);
    }

    /* Block until all translations are complete.
     * This isn't ideal but I'm willing to bet traffic isn't a huge problem. 
     * For this site, fast load for one >>> handle many users.
     */
    CompletableFuture.allOf(translationFutures).join();

    /* Add translated queries for response */
    for (int i = 0; i < commentEntities.size(); i++) {
      Entity entity = commentEntities.get(i);
      long id = entity.getKey().getId();
      long timestamp = (long) entity.getProperty("timestamp");
      String email = (String) entity.getProperty("email");

      String translatedMessage = "";
      try {
        translatedMessage = translationFutures[i].get();
      } catch (Exception unexpected) {
        /* Really the only thing that can cause an error here is 
         * .get() being called before the task is complete.
         * That is taken care of by the .join() block above.
         */
      }

      Comment comment = new Comment(id, translatedMessage, projectId, timestamp, email);
      comments.add(comment);
    }

    /* Send JSON encoded list of comments. */
    Gson gson = new Gson();
    String json = gson.toJson(comments);

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /** 
   * Create a new comment for a given project. Requires a user to be authenticated.
   * The comment can be in any language, since they are all translated on the get-step.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /* Only allow comment creation when logged in. **/
    if(request.getUserPrincipal() == null) {
        response.setStatus(401);
        return;
    }
    
    /* Extract comment properties from request. */
    String message = request.getParameter("message");
    String projectId = request.getParameter("projectId");
    String displayName = request.getParameter("displayName");
    long timestamp = System.currentTimeMillis();
    String email = request.getUserPrincipal().getName();

    /* Create and set properties for a new comment in datastore. */
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("message", message);
    commentEntity.setProperty("projectId", projectId);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("email", email);
    commentEntity.setProperty("displayName", displayName);

    /* Save to datastore. */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    
    /* Redirect client back to original project page. */
    String redirectUrl = "/project-detail.html?projectId=" + projectId; 
    response.sendRedirect(redirectUrl);
  }

  /** Delete all comments for a given project. */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String projectId = request.getParameter("projectId");
    
    /* 
     * Get keys for all comments related to this projectId.
     */
    Query commentsQuery = new Query("Comment")
                            .setKeysOnly()
                            .addFilter("projectId", Query.FilterOperator.EQUAL, projectId);
    PreparedQuery commentResults = datastore.prepare(commentsQuery);

    /* Extract keys and delete. */
    ArrayList<Key> commentKeys = new ArrayList<Key>();
    for (Entity comment : commentResults.asIterable()) {
        commentKeys.add(comment.getKey());
    }

    /* Convert commentKeys to a normal array so it can be used by datastore.delete varargs. */
    datastore.delete(commentKeys.toArray(new Key[commentKeys.size()]));
  }
  
}

