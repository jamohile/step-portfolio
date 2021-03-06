
/**
 * Handle displaying (and maintaining state for) comments on individual projects.
 */
export class Comment {
    /** A list of all comments currently being displayed. */
    static comments = [];

    /** Template element */
    static template = document.querySelector("#comment-template");
    /** UI to hold individual comments. */
    static container = document.querySelector("#comments-list");

    /**
     * @param {string} id
     * @param {string} message
     * @param {string} projectId
     * @param {number} timestamp
     * @param {string} displayName
     */
    constructor(id, message, projectId, timestamp, displayName){
        /** Get root HTML node */
        this.node = Comment.template.content.cloneNode(true).querySelector(".comment");

        /** @private @const {string} */
        this.id = id;
        /** @private @const {string} */
        this.message = message;
        /** @private @const {string} */
        this.projectId = projectId;      
        /** @private @const {number} */
        this.timestamp = timestamp;
        /** @private @const {string} */
        this.displayName = displayName;

        this.render();
        
        /** Add to UI */
        Comment.container.appendChild(this.node);
    }

    /**
     * Set all child nodes and their visual properties based on state.
     * @return {undefined}
     */
    render(){
        this.node.querySelector(".message").innerHTML = this.message;
        this.node.querySelector(".displayName").innerHTML = this.displayName;
    }

    /**
     * JSON data for a comment.
     * @typedef {{id: string, message: string, projectId: string, timestamp: number, displayName: string}} CommentData
     */
    /** 
     * Add all comments to the UI, replacing any existing.
     * @param {CommentData[]} comments
     * @return {undefined}
     */
    static populateAll(comments){
        Comment.container.innerHTML = "";
        Comment.comments = [];

        for(let comment of comments){
            const {id, message, projectId, timestamp, displayName} = comment;
            Comment.comments.push(new Comment(id, message, projectId, timestamp, displayName));
        }
    }

    /**
     * Load comments from the server, then display.
     * @return {Promise<undefined>}
    */
    static async loadAll(projectId, commentsCount, languageCode){
        /** Get comments for the current project. */
        const response = await fetch(`/comments?projectId=${projectId}&commentsCount=${commentsCount}&languageCode=${languageCode}`);
        /** @type {CommentData} */
        const comments = await response.json();

        /** Only show delete comments button if comments exist. */
        const deleteCommentsFormNode = document.querySelector("#delete-comments");
        if(comments.length > 0){
            deleteCommentsFormNode.classList.remove("hidden");
        } else {
            deleteCommentsFormNode.classList.add("hidden");
        }

        Comment.populateAll(comments);
    }

    /** Delete all comments for this project, then reload. */
    static async deleteAll(projectId){
        await fetch(`/comments?projectId=${projectId}`, {
            method: "DELETE"
        });
    }
}

