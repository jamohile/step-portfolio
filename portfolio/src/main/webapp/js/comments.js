
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
     * @param {string} message
     */
    constructor(message){
        /** Get root HTML node */
        this.node = Comment.template.content.cloneNode(true).querySelector(".comment");

        /** @private @const {string} */
        this.message = message;

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
     * Add all comments to the UI, replacing any existing.
     * @param {string[]} comments
     * @return {undefined}
     */
    static populateAll(comments){
        Comment.container.innerHTML = "";
        Comment.comments = [];

        for(let comment of comments){
            Comment.comments.push(new Comment(comment));
        }
    }
}
