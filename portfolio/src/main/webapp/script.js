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

/**
 * The color theme currently being used for the website. 
 * This is used to set a html-level class, which is used to enable css variables used by all elements.
 * Defaults to light.
 * @type {"dark" | "light"}
 */
let colorTheme = localStorage.getItem("color-theme") || "light";

/**
 * Switch the current color theme.
 * @return {undefined}
 */
function toggleTheme(){
    colorTheme = colorTheme === "light" ? "dark" : "light";
    setTheme(colorTheme);
}

/**
 * Set color theme based on current colorTheme state.
 * Sets theme by toggling "dark" class on html element.
 * Also persists theme to localStorage.
 * @return {undefined}
 */
function setTheme(){
    const htmlNode = document.querySelector("html");

    if(colorTheme === "dark"){
        htmlNode.classList.add("dark");
    }else{
        htmlNode.classList.remove("dark");
    }

    localStorage.setItem("color-theme", colorTheme);
}

/** Initialize color theme. */
setTheme();