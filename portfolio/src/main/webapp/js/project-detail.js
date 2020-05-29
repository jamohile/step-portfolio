import {Comment} from "./comments.js";

/**
 * To get the displayed project, must recieve a URL param with desired project id.
 * @type {string}
 */
const projectId = new URLSearchParams(location.search).get("projectId");

/** Load data about this project from the server and populate UI items. */
async function populateDetails() {
    const response = await fetch(`/projects?projectId=${projectId}`);
    /**
    * All data about the current project.
    * @type {{id: string, name: string, description: string, detail: string[], tags: string[]}}
    */
    const project = await response.json();

    console.assert(project !== undefined);

    /** Fill in information for this project. */
    document.querySelector("#name").innerHTML = project.name;
    document.querySelector("#description").innerHTML = project.description;

    /** A bullet-list of details about this project. */
    const detailListNode = document.querySelector("#detail");

    /** Create a new list element (detail) for each detail in the project. */
    for(let detail of project.details){
        const detailNode = document.createElement("li");
        detailNode.innerHTML = detail;
        detailListNode.appendChild(detailNode);
    }
}

/**
 * Load comments from the server, then display.
 * @return {Promise<undefined>}
 */
 async function loadComments(){
     /** Get comments for the current project. */
     const response = await fetch(`/comments?projectId=${projectId}`);
     /** @type {CommentData} */
     const comments = await response.json();

     Comment.populateAll(comments);
 }


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

populateDetails();
loadComments();