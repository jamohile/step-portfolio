import projectsObject from "./projects-object.js";

/**
 * To get the displayed project, must recieve a URL param with desired project id.
 * @type {string}
 */
const projectId = new URLSearchParams(location.search).get("id");

/**
 * All data about the current project.
 * @type {{id: string, name: string, description: string, detail: string, tags: string[]}}
 */
const project = projectsObject[projectId];

console.assert(project !== undefined);

/** Fill in information for this project. */
document.querySelector("#name").innerHTML = project.name;
document.querySelector("#description").innerHTML = project.description;
document.querySelector("#detail").innerHTML = project.detail;