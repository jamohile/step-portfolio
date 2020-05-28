import projectsObject from "./projects-object.js";

import {Comment} from "./comments.js";

/**
 * To get the displayed project, must recieve a URL param with desired project id.
 * @type {string}
 */
const projectId = new URLSearchParams(location.search).get("projectId");

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


document.querySelector("#delete-comments").addEventListener("click", () => Comment.deleteAll(projectId));

/** Methods to hide/show new comment form and related buttons. */
class NewCommentForm {
    /** @const */
    static formNode = document.querySelector("#new-comment");
    /** @const */
    static showFormButtonNode = document.querySelector("#show-comment-form-button");
    /** @const */
    static hideFormButtonNode = document.querySelector("#hide-comment-form-button");

    constructor(){
        NewCommentForm.showFormButtonNode.addEventListener("click", this.show);
        NewCommentForm.hideFormButtonNode.addEventListener("click", this.hide);

        /** Add current project ID to the form. */
        NewCommentForm.formNode.querySelector("input[name=projectId]").value = projectId;
    }

    hide(){
        NewCommentForm.formNode.classList.add("hidden");

        NewCommentForm.showFormButtonNode.classList.remove("hidden");
        NewCommentForm.hideFormButtonNode.classList.add("hidden");
    }

    show(){
        NewCommentForm.formNode.classList.remove("hidden");

        NewCommentForm.showFormButtonNode.classList.add("hidden");
        NewCommentForm.hideFormButtonNode.classList.remove("hidden");
    }
}

const newCommentForm = new NewCommentForm();

 Comment.loadAll(projectId);