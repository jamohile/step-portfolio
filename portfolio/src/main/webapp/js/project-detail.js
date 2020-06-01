import projectsObject from "./projects-object.js";

import {Comment} from "./comments.js";

/**
 * To get the displayed project, must recieve a URL param with desired project id.
 * @type {string}
 */
const projectId = new URLSearchParams(location.search).get("id");

/**
 * All data about the current project.
 * @type {{id: string, name: string, description: string, detail: string[], tags: string[]}}
 */
const project = projectsObject[projectId];

console.assert(project !== undefined);

/** Fill in information for this project. */
document.querySelector("#name").innerHTML = project.name;
document.querySelector("#description").innerHTML = project.description;

/** A bullet-list of details about this project. */
const detailListNode = document.querySelector("#detail");

/** Create a new list element (detail) for each detail in the project. */
for(let detail of project.detail){
    const detailNode = document.createElement("li");
    detailNode.innerHTML = detail;
    detailListNode.appendChild(detailNode);
}

/**
 * Load comments from the server, then display.
 * @return {Promise<undefined>}
 */
 async function loadComments(){
     const response = await fetch("/data");
     const comments = await response.json();
     Comment.populateAll(comments);
 }

 loadComments();
