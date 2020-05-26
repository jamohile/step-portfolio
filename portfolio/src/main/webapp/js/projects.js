const tagNames = ["Personal", "Google", "JS", "C++", "Work", "Python"];
const projectObjects = [
    {name: "Recall", description: "Hospital Scheduling", tags: ["Personal", "JS"]},
    {name: "Vectorly", description: "3D Math Calculator", tags: ["Personal", "JS"]},
    {name: "Houndify React Native", description: "Official React Native port of Houndify SDK", tags: ["Work", "JS"]},
    {name: "Parlex", description: "Language agnostic zero-dependency AST parser", tags: ["Personal", "C++"]},
    {name: "StereoCloud", description: "Stereoscopic image to point cloud generation", tags: ["Personal", "Python"]}
];


/**
 * Tags help group projects.
 * The Tag class is used to display (and keep state) for
 * toggle-able tag elements at the bottom of the page.
 */

class Tag {
    /** All tags currently being displayed, by name. */
    static tags = new Map();
    /** Template element */
    static template = document.querySelector("#tag-template");
    /** UI to hold individual all tags */
    static container = document.querySelector("#tags-list");

    /**
     * @param {string} name
     */
    constructor(name){
        /** Get root HTML node */
        this.node = Tag.template.content.cloneNode(true).querySelector(".tag");

        /** @private @const {string} */
        this.name = name;
        /** @type {boolean} */
        this.selected = false;

        this.node.addEventListener("click", () => this.toggleSelected());

        this.render();
        
        /** Add to UI */
        Tag.container.appendChild(this.node);
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

        /** Rerender projects if tag selection is changed */
        Project.populateAll();
    }

    /** Add all tags to the UI, replacing all existing. */
    static populateAll(){
        Tag.container.innerHTML = "";
        Tag.tags.clear();

        for(let tagName of tagNames){
            Tag.tags.set(tagName, new Tag(tagName));
        }
    }
}

/**
 * A project is a single portfolio item.
 * The Project class handles displaying projects while applying necessary filters.
 */

class Project {
    /** All projects currently being displayed, by name. */
    static projects = new Map();
    /** Template element */
    static template = document.querySelector("#project-template");
    /** UI to hold individual all projects */
    static container = document.querySelector("#projects-list");

    /**
     * @param {string} name
     * @param {string} description
     * @param {string[]} tags
     */
    constructor(name, description, tags){
        /** Get root HTML node */
        this.node = Project.template.content.cloneNode(true).querySelector(".project");

        /** @private @const {string} */
        this.name = name;
        /** @private @const {string} */
        this.description = description;
        /** @private @const {string[]} */
        this.tags = tags;

        this.render();
        
        /** Add to UI */
        Project.container.appendChild(this.node);
    }

    /**
     * Set all child nodes and their visual properties based on state.
     * @return {undefined}
     */
    render(){
        this.node.querySelector(".name").innerHTML = this.name;
        this.node.querySelector(".description").innerHTML = this.description;

        /** Show a list of all tags this project has. */
        const tagsContainer = this.node.querySelector(".tags");
        const tagTemplate = tagsContainer.querySelector(".project-tag-template");
        /** Clear any existing tags from UI. */
        tagsContainer.innerHTML = "";
        /** Add all tags to UI */
        for(let tagName of this.tags){
            const tagNode = tagTemplate.content.cloneNode(true).querySelector(".tag");
            tagNode.innerHTML = tagName;
            tagsContainer.appendChild(tagNode);
        }
    }

    /** Add all projects to the UI, replacing all existing. */
    static populateAll(){
        Project.container.innerHTML = "";
        Project.projects.clear();

        const tagsMap = Tag.tags;
        const noTagsSelected = ![...tagsMap.values()].some(tag => tag.selected === true);

        for(let {name, description, tags} of projectObjects){
            /**
             * Display a project if either
                (1) no tags selected, or
                (2) project contains any selected tag (logical OR)
             */
             /** Naively check tag membership right now, efficiency not a big problem. */
             const tagIncluded = tags.some(tag => tagsMap.get(tag).selected === true);
            if (noTagsSelected || tagIncluded){
                Project.projects.set(name, new Project(name, description, tags));
            }
        }
    }
}

/** Initially add all tags to screen, unselected. */
Tag.populateAll();

/**  
 * Add all projects to screen.
 * This must come after tag population since projects filter based on tags.
 */
Project.populateAll();