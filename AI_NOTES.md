# AI notes

## Tools

I used Cursor’s coding agent for this project.

- It wrote and updated the Hero Banner, Feature Teaser, and Infant Accordion: Java models, HTL, dialogs, and Sass.
- It drafted `README.md` and `AUTHORING.md`, then I asked for shorter English.
- It ran Maven to install the package on the local author, and checked the page in the browser.

`AGENTS.md` and `CLAUDE.md` were already in the repo. I did not create them. They only describe the project layout and the usual Maven commands.

## One mistake, and how I found it

The agent rounded all four corners of the Joey Club image by almost the same amount. On the reference page the top corners and the bottom-left corner are only slightly round. The bottom-right corner is much larger.

I found it by opening the preview and comparing it with the reference page. The picture looked like a normal rounded rectangle. I told the agent to keep three corners small and make only the bottom-right corner large. The title, description, and link sizes stayed as they were.

## One suggestion I rejected

The agent suggested building the Feature Teaser as a new component, the same way as the Hero Banner. It would have had its own Java model, its own HTML, and its own link rules.

I rejected that. Core Teaser already shows the title, the rich text, the DAM image, and links inside the site with `.html` on the end. Copying all of that would be extra code to maintain. We only added what Core Teaser does not do: the heading is locked to H2, an empty teaser is hidden on the published page, and a link that opens in a new tab gets `rel="noopener noreferrer"`.

## A team of five

I would not let one person accept a large AI change alone.

- Keep `AGENTS.md` as the shared rules: module layout, the install command, and which components we extend.
- Give the agent one small task at a time, such as one dialog or one test class.
- The person who accepts the change must be able to explain every line. That is the same bar as the walk-through.
- Run `mvn -pl core test` before the change is shared. A failed test stops the build.
- Another person reviews the diff, especially HTL and dialog XML, because those are easy for an agent to get slightly wrong.
- Page content stays out of Git until we agree to put it in `ui.content`. The agent must not invent a second copy of the page in the repository.
