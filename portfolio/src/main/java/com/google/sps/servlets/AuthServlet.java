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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles AJAX authentication requests from the client.
 * While Google Auth handles actual authentication, this lets the client query
 * the current auth state, and supplies login/logout URLs.
*/
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    /** Used to get and Gson serialize a login link */
    class LoginRedirect {
        /** Fully qualified login redirect url from Appengine Users api */
        private final String loginUrl;
        
        public LoginRedirect(String redirectUrl) {
            this.loginUrl = AuthServlet.this.userService.createLoginURL(redirectUrl);
        }
    }
    /** Used to get and Gson serialize a logout link */
    class LogoutRedirect {
        /** Fully qualified logout redirect url from Appengine Users api */
        private final String logoutUrl;
        
        public LogoutRedirect(String redirectUrl) {
            this.logoutUrl = AuthServlet.this.userService.createLogoutURL(redirectUrl);
        }
    }
    
    private UserService userService = UserServiceFactory.getUserService();
    
    /**
     * Returns either status code 200 and json logout url, indicating user is authenticated,
     * or status code 401 and a json login url.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException { 
        String redirectUrl = request.getParameter("redirectUrl");
        if(redirectUrl == null){
            response.setStatus(400);
            return;
        }

        Gson gson = new Gson();
        
        // Unauthenticated, send login url.
        if(request.getUserPrincipal() == null){
            // Send back a login url.
            LoginRedirect loginRedirect = new LoginRedirect(redirectUrl);
            
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().println(
                gson.toJson(loginRedirect)
            );
            return;
        }

        // Authenticated, send logout url.
        LogoutRedirect logoutRedirect = new LogoutRedirect(redirectUrl);
        response.setStatus(200);
        response.setContentType("application/json");
        response.getWriter().println(
            gson.toJson(logoutRedirect)
        );
    }
}

