import projectsObject from "./projects-object.js";

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
 * A simple example of loading data from the server.
 * This is a proof-of-concept, and will be replaced by a more complicated and useful method.
 */
async function loadServerMessage(){
    /** For now we espect to get response <h1>Hello Jay!</h1>*/
    const response = await fetch("/data");
    const message = await response.text();

    /** This is just a temporary place to put this message. */
    document.querySelector("#comments-list").innerHTML = message;
}

loadServerMessage();