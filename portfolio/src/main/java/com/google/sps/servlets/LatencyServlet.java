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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet waits for a get request, then runs a large number of  steps before returning.
 * It exists only to test cloud debugger latency.
 */
@WebServlet("/latency")
public class LatencyServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long startTime = System.currentTimeMillis();
    killTime();
    long elapsedTime = System.currentTimeMillis() - startTime;
    response.getWriter().println(elapsedTime);
  }

  private void doSomething() {
    // Count to 10 Million
    for(long i = 0L; i < 10000000L; i++){
        // do nothing.
    }
  }

  private void killTime() {
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 10
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 20
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 30
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 40
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 50
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 60
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 70
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 80
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 90
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 100
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 110
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 120
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 130
        doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 140
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 150
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 160
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 170
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 180
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 190
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething();
    doSomething(); // 200
  }
}

