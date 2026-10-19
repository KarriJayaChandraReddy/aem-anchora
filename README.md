# Anchora

This project builds one page in AEM as a Cloud Service: **Flying with infants and children**.

The page has three parts:

- **Hero Banner** — written for this project
- **Feature Teaser** — Core Teaser, with a few changes
- **Infant Accordion** — written for this project

The header and footer come from the project template. They are locked, so authors do not edit them on the page.

How to fill in the dialogs is in `AUTHORING.md`.

If the page is already on your local author, open it here:

- Preview: `http://localhost:4502/content/anchora/us/en/flying-with-infants-children.html?wcmmode=disabled`
- Editor: `http://localhost:4502/editor.html/content/anchora/us/en/flying-with-infants-children.html`

A new install does not create this page. The page content is only on the local author. See [Assumptions and next steps](#assumptions-and-next-steps).

## Prerequisites

Install these before you build.

| Tool | Version |
|---|---|
| Java | 21. This is the version in `.cloudmanager/java-version`. Use it for local builds. The project still compiles to Java 11. |
| Maven | 3.3.9 or newer |
| Node.js | 16.17.0. Maven installs this for you during the build. |
| npm | 8.15.0. Maven installs this for you during the build. |
| AEM SDK | `2026.10.28871.20261009T211133Z-260900`. Start the author on port 4502. |
| Core Components | 2.28.0 |

Sign in to the local author as `admin` / `admin`.

You only need Node.js on your computer if you run `npm start` or `npm run prod` yourself. The full Maven build installs Node for you.

The project was created with the AEM project archetype. The settings that were saved are in `archetype.properties`: `aemVersion=cloud`, `appId=anchora`, `frontendModule=general`, `includeDispatcherConfig=y`, `singleCountry=y`, `datalayer=y`, `includeExamples=n`. The file does not record the archetype version number. `languageCountry` is still the placeholder `${languageCountry}`.

## Set-up and deploy

1. Install Java 21 and Maven 3.3.9 or newer. Make sure both are on your PATH.
2. Start the AEM author. Wait until `http://localhost:4502` opens. Sign in as `admin` / `admin`.
3. From the project folder, run:

```bash
git clone <repository-url> aem-anchora
cd aem-anchora
mvn clean install -PautoInstallPackage
```

That one command:

- Builds the Java bundle and runs the unit tests. If a test fails, the build stops.
- Installs Node 16.17.0 and npm 8.15.0, then builds the CSS and JavaScript.
- Packages the app and installs it on the author at port 4502.

4. In the author, check that the bundle `com.anchora.core` is active. Use the OSGi console or CRXDE.
5. If the page is not there yet, create it under `/content/anchora/us/en` with the **Page Content** template. The page name is `flying-with-infants-children`. Add a Hero Banner, then a Feature Teaser, then an Infant Accordion. Field help is in `AUTHORING.md`.
6. Open the preview link above. You should see one H1, the Joey Club teaser, and the booking accordion. Hard-refresh the browser if the styles look old.

To rebuild only the CSS and JavaScript, then install again:

```bash
cd ui.frontend
npm ci
npm run prod
cd ..
mvn clean install -PautoInstallPackage
```

`npm start` inside `ui.frontend` opens a local webpack server. It does not update AEM.

You do not need Dispatcher to see the page on the author. To check the Dispatcher config:

```bash
cd dispatcher
./bin/validate.sh src
```

## Project structure

Only four modules were changed for this page.

| Module | What changed | Why |
|---|---|---|
| `core` | Hero Banner model, Infant Accordion model, and their tests | The rules for alt text, empty components, jump links, and accordion rows live in Java. The HTML template only prints the result. |
| `ui.apps` | Hero Banner, Feature Teaser, Infant Accordion, and `page/customheaderlibs.html` | These files are the component dialogs and the HTML. The page file also adds Open Graph tags. |
| `ui.frontend` | Styles for the hero, teaser, and accordion, plus the accordion script | This is the look and the open/close behaviour. The build puts them in the `anchora.site` client library. |
| `ui.content` | Page template and policies | The old locked page title was removed, so the hero can be the only H1. The teaser heading is fixed to H2. The hero image uses the existing image policy. |

These modules were not changed for this page: `ui.apps.structure`, `ui.config`, `dispatcher`, `it.tests`, and `ui.tests`.

`anchora/components/teaser` and `anchora/components/accordion` are the original Core Components. They were left as they are. The Feature Teaser and the Infant Accordion are separate components.

## Component reference

All three components share one client library: **`anchora.site`**.

The page policy `anchora/components/page/policy` loads `anchora.dependencies` and `anchora.site`. None of the three components has its own client library. None of the HTML files contains a `<style>` or `<script>` block.

`anchora.base` is loaded by `page/customheaderlibs.html`. `anchora.grid` is the layout grid that came with the project.

### Hero Banner

| | |
|---|---|
| Resource type | `anchora/components/herobanner` |
| Sling Model | `com.anchora.core.models.HeroBanner`, implemented by `com.anchora.core.models.impl.HeroBannerImpl` |
| Dialog | `ui.apps/.../herobanner/_cq_dialog/.content.xml` |
| Policy | No policy on the banner itself. The picture inside it uses `anchora/components/image/policy_651483963895698`. |
| Client library | `anchora.site` |

The banner is a container. The picture is a child Image component (`anchora/components/image`, which is Core Image v3).

Dialog fields:

| Tab | Field | Saved as |
|---|---|---|
| Text | Title | `./title` |
| Text | Introduction. Rich text. Paragraphs only. | `./introduction` |
| Text | On this page. If you leave it blank, the text is “On this page”. | `./linksHeading` |
| Text | Links. Label and Anchor are required. Do not type `#`. | `./links/*/label`, `./links/*/anchor` |
| Image | Image from the DAM. You cannot upload a file from your computer. | `./image/fileReference` |
| Image | Alt text | `./image/alt` |
| Image | Image is decorative | `./image/isDecorative` |

The image policy allows these widths: 320, 480, 600, 800, 1024, 1200, and 1600.

Alt text works like this:

1. If the image is decorative, there is no alt text.
2. If you type alt text, that text is used.
3. If you leave alt text blank, the DAM asset description (`dc:description`) is used.

If there is no title and no image, the editor shows “Hero Banner”. The published page shows nothing.

### Feature Teaser

| | |
|---|---|
| Resource type | `anchora/components/featureteaser` |
| Sling Model | No project model. It uses the Core Teaser model, `com.adobe.cq.wcm.core.components.models.Teaser`. |
| Dialog | Core Teaser dialog, with the heading-level fields hidden and the image tab replaced |
| Policy | `anchora/components/featureteaser/policy`. The heading is always H2. Styles: Image left (default), Image right, Image top, Image bottom. |
| Client library | `anchora.site` |

It extends `anchora/components/teaser`, which extends Core Teaser v2. Images go through `anchora/components/image`.

Dialog fields:

| Tab | Field | Saved as |
|---|---|---|
| Text | Pretitle. Optional. If it repeats the title, CSS hides it. | `./pretitle` |
| Text | Title. Always H2. “Get title from linked page” is on by default. | `./jcr:title` |
| Text | Description. Rich text. | `./jcr:description` |
| Text | ID. Use `joey-club` so the hero link can find this block. | `./id` |
| Asset | Image from the DAM. You cannot upload a file from your computer. | `./fileReference` |
| Asset | Alt text | `./alt` |
| Asset | Image is decorative | `./isDecorative` |
| Links | Page link | `./linkURL` |
| Links | Call to action. Link and text are required. You can open the link in a new tab. | `./actions` |

A link to a page inside the site ends with `.html`. A link that opens in a new tab gets `rel="noopener noreferrer"`. A “Back to top” link is shown under the image.

If the teaser has no title, pretitle, description, image, or link, the editor shows “Feature Teaser”. The published page shows nothing.

The old Teaser component still uses H3. Do not use that component for Joey Club.

### Infant Accordion

| | |
|---|---|
| Resource type | `anchora/components/infantaccordion` |
| Sling Model | `com.anchora.core.models.InfantAccordion`, implemented by `com.anchora.core.models.impl.InfantAccordionImpl`. One row is `InfantAccordionItem`. |
| Dialog | `ui.apps/.../infantaccordion/_cq_dialog/.content.xml` |
| Policy | None. Authors can add it because it is in the group **Anchora - Content**. |
| Client library | `anchora.site` |

Dialog fields:

| Tab | Field | Saved as |
|---|---|---|
| Content | Heading. Shown as H2. | `./sectionHeading` |
| Content | Description. Rich text: bold, italic, lists, and links. | `./description` |
| Content | Accordion items. Title is required and is shown as H3. Description is rich text. | `./items/*/title`, `./items/*/description` |
| Labels | Expand all items | `./expandAllLabel` |
| Labels | Collapse all items | `./collapseAllLabel` |

A row is skipped when both the title and the description are blank. Panels start closed. A reader can open more than one panel. The code can support one-open-at-a-time, but the dialog does not offer that choice, so the page always allows many open panels.

Each item is a button with `aria-expanded` and `aria-controls`. Enter, Space, arrow keys, Home, and End work on that button. The focused button has a visible outline.

If there is no heading, no description, and no items, the editor shows a placeholder. The published page shows nothing.

There is no field for a link icon. A link that sits alone on its own line gets an arrow from CSS.

### Page

`anchora/components/page` extends Core Page v3. `customheaderlibs.html` adds these tags when the values exist:

- `pagetitle`
- `og:title`
- `og:description`
- `og:url`
- `og:image`, `og:image:secure_url`, and `og:image:alt` from the page featured image

`og:title` is the page title. If the page has a brand name and that brand is allowed to override, the brand is added after the title. The description, canonical URL, and robots tags come from the normal page properties.

## Design decisions

**Hero Banner is a new component.** The page needs one H1, a DAM image, alt text that can fall back to the DAM description, a decorative option, and an “On this page” list. Core Teaser and Core Image each do only part of that. A small Java model holds the rules. The HTML only prints what the model returns. The picture is still the normal Image component, so the image widths stay on the image policy.

**Feature Teaser extends Core Teaser.** Core Teaser already does the title, rich text, image, `.html` links, and new-tab links. This project only adds what was missing: the heading is locked to H2, an empty teaser does not show on the published page, and a new-tab link gets `rel="noopener noreferrer"`. No extra Java model was added, because the Core model already has the link data.

**Infant Accordion is a new component.** Core Accordion is a stack of nested components. This page needs a heading and an introduction next to a list of questions and answers, plus Expand all and Collapse all. A repeatable dialog field fits that. Authors edit a closed panel in the dialog, so the answer is still available when the panel is closed on the page. The old Accordion component was not changed.

**Heading levels are fixed.** The hero title is the only H1. The template used to add another H1, and that title was removed. A normal Title component inside the page can only be H2 to H6. The teaser title is H2 because of its policy. Each accordion item title is H3 in the HTML.

## Testing

From the project folder:

```bash
mvn -pl core test
```

`mvn clean install` runs the same tests. If a test fails, the build fails. Do not use `-DskipTests` when you want to check the tests.

`HeroBannerImplTest` uses JUnit 5 and AEM Mocks. It checks:

- the title and introduction
- authored alt text, a blank alt that uses the DAM description, and a decorative image
- image addresses at 600 and 1200 pixels, including SVG and missing assets
- jump links, including a missing label, a missing anchor, and a `javascript:` value
- the empty banner

`InfantAccordionImplTest` checks the heading, the introduction, the expand and collapse labels, two items, a row that has only a title or only a description, a blank row that is dropped, and the empty component.

The tests do not check:

- the HTML output, the dialogs, or the policies
- the Feature Teaser, because it has no project Java model
- keyboard use, focus, or Expand all in a browser
- the real page, the phone and desktop layout, or `error.log`
- `it.tests` and `ui.tests`

The old sample tests in `core` still run as well. Those cover Hello World, the sample servlet, filter, listener, and scheduler.

## Requirement trace

| ID | What it asks for | Status | Where it is done |
|---|---|---|---|
| HB-01 | Dialog fields: title, introduction, DAM image, alt text | Done | Hero Banner dialog |
| HB-02 | Blank alt uses the DAM description. Decorative is a separate checkbox. | Done | `HeroBannerImpl.java` and its test |
| HB-03 | A list of label and anchor, shown as “On this page” | Done | `HeroBannerImpl.java`, `JumpLinkImpl.java`, `herobanner.html` |
| HB-04 | Only one H1. The image has at least two widths. | Done | `herobanner.html`, template structure, image policy |
| HB-05 | A Java model behind an interface. HTML does not decide the rules. | Done | `HeroBanner.java`, `HeroBannerImpl.java`, `herobanner.html` |
| HB-06 | No title and no image: placeholder in the editor, nothing broken when published | Done | `HeroBannerImpl.isEmpty`, `herobanner.html` |
| TS-01 | Title, rich text, image, link label, link target, open in a new tab | Done | Feature Teaser dialog and the Core Teaser Links tab |
| TS-02 | Heading comes from the policy and is H2. Authors cannot pick it on each teaser. | Done | `featureteaser/policy`. The dialog hides the heading picker. |
| TS-03 | Links inside the site end with `.html`. A new tab adds `rel="noopener noreferrer"`. | Done | Core Teaser, plus `featureteaser.html`, `title.html`, and `action.html` |
| TS-04 | Extend Core Teaser, and explain the choice | Done | `featureteaser/.content.xml` and [Design decisions](#design-decisions) |
| TS-05 | Title “Joey Club” and link “Find out about the Joey Club” to a page on this site | Done on this author only | The page content. The link target is `/content/anchora/us/en/joey-club`. It is not in Git. |
| AC-01 | A heading and introduction beside the accordion | Done | `infantaccordion.html`, `InfantAccordionImpl.java` |
| AC-02 | Authors can add, remove, and reorder items. Each item has a title and rich text. | Done | Dialog field `./items` |
| AC-03 | The six booking titles | Done on this author only | The page content. It is not in Git. |
| AC-04 | A setting for one open panel or many. All panels start closed. | Partly done | Panels start closed. There is no setting, so many panels can be open. |
| AC-05 | Buttons with `aria-expanded` and `aria-controls`, keyboard use, and a visible focus outline | Done | `infantaccordion.html`, `infantaccordion.js`, `_infantaccordion.scss` |
| AC-06 | A closed item can still be edited | Done | Items are edited in the dialog, not inside the closed panel |
| AC-07 | Explain why this is a new component | Done | [Design decisions](#design-decisions) |
| PT-01 | The template locks the header and footer. The page area allows only the components this page needs, plus Title and Text. | Partly done | Header and footer are locked. The page area still allows every component in **Anchora - Content**, plus forms. |
| PT-02 | Header and footer are defined once and cannot be edited on the page | Partly done | They are locked experience fragments. The header is still the sample navigation. The footer is a copyright line and an address, with no links. |
| PT-03 | The page is created from the template and filled in on the author | Done on this author | `/content/anchora/us/en/flying-with-infants-children`. A fresh install does not create it. |
| PT-04 | Page landmarks: `header`, a named `nav`, `main`, `footer`, and a skip link | Not done | There is no skip link, and the header and footer are not in those landmarks. |
| PT-05 | The page is readable at 375px and at 1280px | Partly done | The styles have breakpoints. There is no saved check at those two widths. |
| PT-06 | CSS and JavaScript come from client libraries. No style or script blocks in the HTML. | Done | `anchora.site` |
| EN-01 | Record the AEM archetype version and its settings | Partly done | Settings are in `archetype.properties`. The version number is missing. `languageCountry` is still a placeholder. |
| EN-02 | `mvn clean install -PautoInstallPackage` installs to the author on port 4502 | Done for the code | A clean author does not get the sample page. See EN-04. |
| EN-03 | Every Sling Model has JUnit 5 tests with AEM Mocks. A failed test stops the build. | Done | `HeroBannerImplTest`, `InfantAccordionImplTest`, and the sample model test |
| EN-04 | The finished page is stored in `ui.content` | Not done | The page exists only on this author. |
| EN-05 | Work is committed on a feature branch and merged with a pull request | Not done | |
| EN-06 | No browser console errors, and no new lines in `error.log` from this code | Not done | This was not signed off. |

## Assumptions and next steps

Assumptions:

- Local builds use Java 21, the same as Cloud Manager. The compiled code still targets Java 11.
- An author types `joey-club` in the anchor field. The model adds `#`. A value that starts with `javascript:` is ignored.
- The hero is empty only when both the title and the image are missing.
- Accordion answers are edited in the dialog. That is why a closed panel can still be edited.
- The teaser pretitle is hidden with CSS when it would repeat the title.
- The Open Graph tags are extra. The task did not ask for them. The image tag uses the page featured image.
- The text on the sample page lives on this author. The component code lives in Git.

Limits:

- A new AEM author does not get the page, the Joey Club page, or the images those components use.
- The page area allows more components than the task asked for.
- There is no skip link.
- Authors cannot choose one-open-panel or many-open-panels.
- The header and footer are still the sample ones from the project.
- The accordion tests do not open a browser.
- The hero Java code can build image addresses, but the page shows the nested Image component instead.

With one more day, I would save the page and the Joey Club page in `ui.content`, so one Maven install shows the finished page. Then I would limit which components can be dropped on the page, add the skip link and landmarks, replace the header and footer, and either add the one-open-panel choice or remove the unused code for it.
