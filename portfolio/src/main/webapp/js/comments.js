
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
     */
    constructor(id, message, projectId, timestamp){
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
    }

    /**
     * JSON data for a comment.
     * @typedef {{id: string, message: string, projectId: string, timestamp: number}} CommentData
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
            const {id, message, projectId, timestamp} = comment;
            Comment.comments.push(new Comment(id, message, projectId, timestamp));
        }
    }
}
