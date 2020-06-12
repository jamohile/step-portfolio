import {Comment} from "./comments.js";

import languages from "./languages.js";

/**
 * To get the displayed project,
 * must recieve a URL param with desired project id.
 */
const query = new URLSearchParams(location.search);
/** @type {string} */
const projectId = query.get("projectId");

/**
 * How many comments to display.
 * @type {number | undefined}
 */
let commentsCount = 5;

/**
 * What language to display comments in.
 * @type {string}
 */
let languageCode = "en";

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

/** Fill in information for this project, using CDN loaded XSS prevention library.*/
document.querySelector("#name").innerHTML = window.DOMPurify.sanitize(project.name, {ALLOWED_TAGS: []});
document.querySelector("#description").innerHTML = window.DOMPurify.sanitize(project.description, {ALLOWED_TAGS: []});

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

document.querySelector("#delete-comments").addEventListener("click", async () => {
    await Comment.deleteAll(projectId);
    Comment.loadAll(projectId, commentsCount, languageCode);
});

/** Handle state of Comments Form.
 *  Should only be shown when the user is logged in.
 */
const formNode = document.querySelector("#new-comment");
const authButtonNode = document.querySelector("#auth-button");

/** Get user's auth state. Only show form if logged in. */
async function initializeCommentForm() {
    const response = await fetch(`/auth?redirectUrl=${window.location.href}`);
    const json = await response.json();

    if(response.status === 401){
        // User is not logged in.
        authButtonNode.href = json.loginUrl;
        authButtonNode.innerHTML = "Login to comment";
        formNode.classList.add("hidden");
    } else if (response.status === 200) {
        // User is logged in.
        authButtonNode.href = json.logoutUrl;
        authButtonNode.innerHTML = "Logout";
        formNode.classList.remove("hidden");
	    formNode.querySelector("input[name=projectId]").value = projectId;
    }
}

/** Attach click handlers to all show comments buttons. */
function getCommentButtonClickHandler (newCommentCount){
    return e => {
        /** Make all show comments buttons non-selected */
        document.querySelectorAll("#controls > .control").forEach(e => e.classList.remove("selected"));
        /** Make current comment button selected, and reload comments. */
        e.currentTarget.classList.add("selected");
        commentsCount = newCommentCount;
        Comment.loadAll(projectId, commentsCount, languageCode);
    }
}
document.querySelector("#show-5").addEventListener("click", getCommentButtonClickHandler(5));
document.querySelector("#show-15").addEventListener("click", getCommentButtonClickHandler(15));
document.querySelector("#show-all").addEventListener("click", getCommentButtonClickHandler(undefined));

/** Add language options. */
const languageSelectNode = document.querySelector("#set-language");
for(let language of languages) {
    const {name, code} = language;
    const optionNode = document.createElement("option");
    optionNode.innerHTML = name;
    optionNode.value = code;
    optionNode.selected = code === languageCode;
    languageSelectNode.appendChild(optionNode);
}
languageSelectNode.addEventListener("change", e => {
    languageCode = e.currentTarget.value;
    Comment.loadAll(projectId, commentsCount, languageCode);
});

Comment.loadAll(projectId, commentsCount, languageCode);
initializeCommentForm();
populateDetails();

