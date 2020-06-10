/**
 * Load header object from the server and inject into UI.
 */
async function loadHeader(){
    const response = await fetch("/partials/header.html");
    const html = await response.text();
    document.querySelector("header").outerHTML = html;
}

loadHeader();