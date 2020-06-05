import {Comment} from "./comments.js";


/**
 * To get the displayed project,
 * must recieve a URL param with desired project id.
 */
const query = new URLSearchParams(location.search);
/** @type {string} */
const projectId = query.get("projectId");
/** @type {number | undefined} */
let commentsCount = 5;

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
  }

document.querySelector("#delete-comments").addEventListener("click", () => Comment.deleteAll(projectId, commentsCount));

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

/** Attach click handlers to all show comments buttons. */
function getCommentButtonClickHandler (newCommentCount){
    return e => {
        /** Make all show comments buttons non-selected */
        document.querySelectorAll("#controls > .control").forEach(e => e.classList.remove("selected"));
        /** Make current comment button selected, and reload comments. */
        e.currentTarget.classList.add("selected");
        commentsCount = newCommentCount;
        Comment.loadAll(projectId, commentsCount);
    }
}
document.querySelector("#show-5").addEventListener("click", getCommentButtonClickHandler(5));
document.querySelector("#show-15").addEventListener("click", getCommentButtonClickHandler(15));
document.querySelector("#show-all").addEventListener("click", getCommentButtonClickHandler(undefined));

Comment.loadAll(projectId, commentsCount);
populateDetails();
