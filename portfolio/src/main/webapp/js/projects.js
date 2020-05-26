const tagNames = ["Personal", "Google", "JS", "C++"];
const tagTemplate = document.querySelector("#tag-template");
const tagContainer = document.querySelector("#tags-list");

/**
 * Tags help group projects.
 * The Tag class is used to display (and keep state) for
 * toggle-able tag elements at the bottom of the page.
 */

class Tag {
    /** All tags currently being displayed, by name. */
    static tags = new Map();

    /**
     * @param {string} name
     */
    constructor(name){
        /** Get root HTML node */
        this.node = tagTemplate.content.cloneNode(true).querySelector(".tag");

        /** @private @const {string} */
        this.name = name;
        /** @type {boolean} */
        this.selected = false;

        this.node.addEventListener("click", () => this.toggleSelected());

        this.render();
        
        /** Add to UI */
        tagContainer.appendChild(this.node);
    }

    /**
     * Set all child nodes and their visual properties based on state.
     * @return {undefined}
     */
    render(){
        this.node.innerHTML = this.name;

        if (this.selected) {
            this.node.classList.add("selected");
        } else {
            this.node.classList.remove("selected");
        }

    }

    /**
     * Toggle whether selected (in local state only) and rerender this tag.
     * @return {void}
     */
    toggleSelected(){
        this.selected = !this.selected;
        this.render();
    }

    /** Add all tags to the UI, replacing all existing. */
    static populateAll(){
        tagContainer.innerHTML = "";
        Tag.tags.clear();

        for(let tagName of tagNames){
            Tag.tags.set(tagName, new Tag(tagName));
        }
    }
}

/** Initially add all tags to screen, unselected. */
Tag.populateAll();