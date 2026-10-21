(function () {
    "use strict";

    var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

    function mainContent() {
        var main = document.querySelector("main");
        if (!main) {
            return null;
        }
        if (!main.id) {
            main.id = "main-content";
        }
        return main;
    }

    function onClick(event) {
        var target = mainContent();
        if (!target) {
            return;
        }
        event.preventDefault();
        target.scrollIntoView({
            behavior: reduceMotion.matches ? "auto" : "smooth",
            block: "start"
        });
        if (window.history && window.history.pushState) {
            window.history.pushState(null, "", "#main-content");
        }
    }

    function init() {
        mainContent();
        var links = document.querySelectorAll(".cmp-teaser__back");
        Array.prototype.forEach.call(links, function (link) {
            link.setAttribute("href", "#main-content");
            link.addEventListener("click", onClick);
        });
    }

    if (document.readyState !== "loading") {
        init();
    } else {
        document.addEventListener("DOMContentLoaded", init);
    }
}());
