(function () {
    "use strict";

    var selectors = {
        self: "[data-cmp-is='infantbooking']",
        item: "[data-cmp-hook-infantbooking='item']",
        button: "[data-cmp-hook-infantbooking='button']",
        toggle: "[data-cmp-hook-infantbooking='toggle']"
    };

    var classes = {
        buttonExpanded: "infantbooking__button--expanded",
        panelExpanded: "infantbooking__panel--expanded",
        panelHidden: "infantbooking__panel--hidden"
    };

    function isSingleExpansion(accordion) {
        return accordion.hasAttribute("data-cmp-single-expansion");
    }

    function itemsOf(accordion) {
        return [].slice.call(accordion.querySelectorAll(selectors.item)).filter(function (item) {
            return item.closest(".infantbooking__list") === accordion;
        });
    }

    function isExpanded(item) {
        return item.hasAttribute("data-cmp-expanded");
    }

    function setExpanded(item, expanded) {
        var button = item.querySelector(selectors.button);
        var panel = item.querySelector("[data-cmp-hook-infantbooking='panel']");
        if (!button || !panel) {
            return;
        }
        button.classList.toggle(classes.buttonExpanded, expanded);
        button.setAttribute("aria-expanded", expanded ? "true" : "false");
        panel.classList.toggle(classes.panelExpanded, expanded);
        panel.classList.toggle(classes.panelHidden, !expanded);
        panel.setAttribute("aria-hidden", expanded ? "false" : "true");
        if (expanded) {
            item.setAttribute("data-cmp-expanded", "");
        } else {
            item.removeAttribute("data-cmp-expanded");
        }
    }

    function allExpanded(accordion) {
        var items = itemsOf(accordion);
        if (!items.length) {
            return false;
        }
        return items.every(function (item) {
            return isExpanded(item);
        });
    }

    function updateToggle(root, accordion) {
        var toggle = root.querySelector(selectors.toggle);
        if (!toggle) {
            return;
        }
        if (isSingleExpansion(accordion)) {
            toggle.hidden = true;
            return;
        }
        var expanded = allExpanded(accordion);
        var label = expanded ? toggle.getAttribute("data-label-collapse") : toggle.getAttribute("data-label-expand");
        if (label) {
            toggle.textContent = label;
        }
        toggle.setAttribute("aria-expanded", expanded ? "true" : "false");
    }

    function toggleItem(accordion, item) {
        var expand = !isExpanded(item);
        if (expand && isSingleExpansion(accordion)) {
            itemsOf(accordion).forEach(function (other) {
                if (other !== item) {
                    setExpanded(other, false);
                }
            });
        }
        setExpanded(item, expand);
    }

    function moveFocus(items, index) {
        var button = items[index] && items[index].querySelector(selectors.button);
        if (button) {
            button.focus();
        }
    }

    function onItemKeyDown(event, items, index) {
        var next = null;
        if (event.key === "ArrowUp" || event.key === "ArrowLeft") {
            next = index > 0 ? index - 1 : index;
        } else if (event.key === "ArrowDown" || event.key === "ArrowRight") {
            next = index < items.length - 1 ? index + 1 : index;
        } else if (event.key === "Home") {
            next = 0;
        } else if (event.key === "End") {
            next = items.length - 1;
        }
        if (next === null) {
            return;
        }
        event.preventDefault();
        moveFocus(items, next);
    }

    function InfantBooking(config) {
        function init(root) {
            root.removeAttribute("data-cmp-is");
            var accordion = root.querySelector(".infantbooking__list");
            if (!accordion) {
                return;
            }
            var items = itemsOf(accordion);
            items.forEach(function (item, index) {
                var button = item.querySelector(selectors.button);
                if (!button) {
                    return;
                }
                button.addEventListener("click", function () {
                    toggleItem(accordion, item);
                    updateToggle(root, accordion);
                });
                button.addEventListener("keydown", function (event) {
                    if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault();
                        toggleItem(accordion, item);
                        updateToggle(root, accordion);
                        return;
                    }
                    onItemKeyDown(event, items, index);
                });
            });
            var toggle = root.querySelector(selectors.toggle);
            if (toggle) {
                toggle.addEventListener("click", function () {
                    if (isSingleExpansion(accordion)) {
                        return;
                    }
                    var expand = !allExpanded(accordion);
                    itemsOf(accordion).forEach(function (item) {
                        setExpanded(item, expand);
                    });
                    updateToggle(root, accordion);
                });
            }
            updateToggle(root, accordion);
            addLinkIcons(root);
        }

        function addLinkIcons(root) {
            var src = root.getAttribute("data-link-icon");
            if (!src) {
                return;
            }
            var links = root.querySelectorAll(".infantbooking__description a, .infantbooking__answer a");
            Array.prototype.forEach.call(links, function (link) {
                if (link.querySelector(".infantbooking__link-icon")) {
                    return;
                }
                var icon = document.createElement("img");
                icon.className = "infantbooking__link-icon";
                icon.src = src;
                icon.alt = "";
                icon.setAttribute("aria-hidden", "true");
                link.appendChild(icon);
            });
        }

        if (config && config.element) {
            init(config.element);
        }
    }

    function initElements(elements) {
        for (var i = 0; i < elements.length; i++) {
            new InfantBooking({ element: elements[i] });
        }
    }

    function onDocumentReady() {
        initElements(document.querySelectorAll(selectors.self));

        var Observer = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
        var body = document.querySelector("body");
        if (!Observer || !body) {
            return;
        }
        var observer = new Observer(function (mutations) {
            mutations.forEach(function (mutation) {
                [].slice.call(mutation.addedNodes).forEach(function (addedNode) {
                    if (addedNode.querySelectorAll) {
                        initElements(addedNode.querySelectorAll(selectors.self));
                    }
                });
            });
        });
        observer.observe(body, {
            subtree: true,
            childList: true
        });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }
}());
