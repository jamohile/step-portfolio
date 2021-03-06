const tagNames = ["Personal", "Google", "JS", "C++", "Work", "Python", "Soundhound"];

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
    /** All projects currently being displayed. */
    static projects = [];
    /** Raw data for all projects. This will be loaded from the server.*/
    static projectData = [];
    /** Template element */
    static template = document.querySelector("#project-template");
    /** UI to hold individual all projects */
    static container = document.querySelector("#projects-list");

    /**
     * @param {string} id
     * @param {string} name
     * @param {string} description
     * @param {string[]} tags
     */
    constructor(id, name, description, tags){
        /** Get root HTML node */
        this.node = Project.template.content.cloneNode(true).querySelector(".project");

        /** @private @const {string} */
        this.id = id;
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
        this.node.querySelector(".name").href = `/project-detail.html?projectId=${this.id}`;
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
            /* Add style to indicate that this tag is part of filtered tags if applicable */
            if (Tag.tags.get(tagName).selected === true){
                tagNode.classList.add("selected");
            }

            tagNode.addEventListener("click", () => Tag.tags.get(tagName).toggleSelected());

            tagsContainer.appendChild(tagNode);
        }
    }

    /** Add all projects to the UI, replacing all existing. */
    static populateAll(){
        Project.container.innerHTML = "";
        Project.projects = [];

        const tagsMap = Tag.tags;
        const noTagsSelected = ![...tagsMap.values()].some(tag => tag.selected === true);

        for(let project of Project.projectData){
            const {id, name, description, tags} = project;
            /**
             * Display a project if either
                (1) no tags selected, or
                (2) project contains any selected tag (logical OR)
             */
             /** Naively check tag membership right now, efficiency not a big problem. */
             const tagIncluded = tags.some(tag => tagsMap.get(tag).selected === true);
            if (noTagsSelected || tagIncluded){
                Project.projects.push(name, new Project(id, name, description, tags));
            }
        }
    }
    
    /** Load projects from server, then populate to UI */
    static async loadAndPopulateAll() {
        const response = await fetch('/projects');
        Project.projectData = await response.json();
        Project.populateAll();
    }
}

/** Initially add all tags to screen, unselected. */
Tag.populateAll();

/**  
 * Add all projects to screen.
 * This must come after tag population since projects filter based on tags.
 */

Project.loadAndPopulateAll();
