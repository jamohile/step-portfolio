import projectsObject from "./projects-object.js";

/**
 * To get the displayed project, must recieve a URL param with desired project id.
 * @type {string}
 */
const projectId = new URLSearchParams(location.search).get("id");

/**
 * All data about the current project.
 * @typedef {{name: string, href: string}} ProjectLink
 * @type {{id: string, name: string, description: string, detail: string[], tags: string[], links: ProjectLink[]}}
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

/** Similar to the list of details, create a list of links but only if present. */
const linkSectionNode = document.querySelector("#project-links");
if (project.links && project.links.length > 0) {
    linkSectionNode.classList.remove("hidden");

    const linkListNode = linkSectionNode.querySelector("#links");

    for(let link of project.links){
        const {name, href} = link;

        /** Create link element. */
        const linkNode = document.createElement("a");
        linkNode.href = href;
        linkNode.innerHTML = name;

        /** Add link to UI. */
        const listElementNode = document.createElement("li");
        listElementNode.appendChild(linkNode);
        linkListNode.appendChild(listElementNode);
    }
} else{
    linkSectionNode.classList.add("hidden");
}