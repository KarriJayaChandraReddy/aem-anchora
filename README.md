# Anchora

This project builds one page in AEM as a Cloud Service: **Flying with infants and children**.

The page has three parts:

| Part | What it is |
|---|---|
| **Hero Banner** | A new component built for this project |
| **Feature Teaser** | The Core Teaser with a few changes |
| **Infant Accordion** | A new component built for this project |

The header and footer come from the project template. They are locked, so authors cannot edit them on the page.

Help with filling in the dialogs is in `AUTHORING.md`.

**Open the page on your local author:**

- Preview: `http://localhost:4502/content/anchora/us/en/flying-with-infants-children.html?wcmmode=disabled`
- Editor: `http://localhost:4502/editor.html/content/anchora/us/en/flying-with-infants-children.html`

> **Note:** A fresh install does not create this page. The page content exists only on the local author. See [Assumptions and next steps](#8-assumptions-and-next-steps).

---

## 1. Prerequisites

Install these before you build.

| Tool | Version | Notes |
|---|---|---|
| Java | 21 | Matches `.cloudmanager/java-version`. Use it for local builds. The code still compiles to Java 11. |
| Maven | 3.3.9 or newer | |
| Node.js | 16.17.0 | Maven installs it during the build. |
| npm | 8.15.0 | Maven installs it during the build. |
| AEM SDK | `2026.10.28871.20261009T211133Z-260900` | Start the author on port 4502. |
| Core Components | 2.28.0 | |

- Sign in to the local author with `admin` / `admin`.
- You only need Node.js on your own computer if you run `npm start` or `npm run prod` yourself.

**Archetype settings:** The project was created with the AEM project archetype. The saved settings are in `archetype.properties`:

```
aemVersion=cloud
appId=anchora
frontendModule=general
includeDispatcherConfig=y
singleCountry=y
datalayer=y
includeExamples=n
```

The file does not record the archetype version. `languageCountry` is still the placeholder `${languageCountry}`.

---

## 2. Set-up and deploy

Follow these steps from clone to a working page.

**Step 1.** Install Java 21 and Maven 3.3.9 or newer. Make sure both are on your PATH.

**Step 2.** Start the AEM author. Wait until `http://localhost:4502` opens, then sign in with `admin` / `admin`.

**Step 3.** Clone the project and build it:

```bash
git clone <repository-url> aem-anchora
cd aem-anchora
mvn clean install -PautoInstallPackage
```

This one command does three things:

1. Builds the Java bundle and runs the unit tests. If a test fails, the build stops.
2. Installs Node 16.17.0 and npm 8.15.0, then builds the CSS and JavaScript.
3. Packages the app and installs it on the author at port 4502.

**Step 4.** In the author, check that the bundle `com.anchora.core` is **Active**. Use the OSGi console or CRXDE.

**Step 5.** If the page does not exist yet, create it:

- Location: `/content/anchora/us/en`
- Template: **Page Content**
- Page name: `flying-with-infants-children`
- Add these components in order: Hero Banner, Feature Teaser, Infant Accordion

Field help is in `AUTHORING.md`.

**Step 6.** Open the preview link above. You should see:

- one H1
- the Joey Club teaser
- the booking accordion

If the styles look old, hard-refresh the browser.

### Rebuild only the CSS and JavaScript

```bash
cd ui.frontend
npm ci
npm run prod
cd ..
mvn clean install -PautoInstallPackage
```

`npm start` inside `ui.frontend` opens a local webpack server. It does **not** update AEM.

### Check the Dispatcher config (optional)

You do not need Dispatcher to see the page on the author. To validate its config:

```bash
cd dispatcher
./bin/validate.sh src
```

---

## 3. Project structure

Only four modules were changed for this page.

| Module | What changed | Why |
|---|---|---|
| `core` | Hero Banner model, Infant Accordion model, and their tests | The rules (alt text, empty components, jump links, accordion rows) live in Java. The HTML only prints the result. |
| `ui.apps` | Hero Banner, Feature Teaser, Infant Accordion, and `page/customheaderlibs.html` | These hold the dialogs and the HTML. The page file also adds Open Graph tags. |
| `ui.frontend` | Styles for the hero, teaser, and accordion, plus the accordion script | This is the look and the open/close behaviour. The build puts it in the `anchora.site` client library. |
| `ui.content` | Page template and policies | The old locked page title was removed so the hero is the only H1. The teaser heading is fixed to H2. The hero image uses the existing image policy. |

**Not changed:** `ui.apps.structure`, `ui.config`, `dispatcher`, `it.tests`, `ui.tests`.

`anchora/components/teaser` and `anchora/components/accordion` are the original Core Components and were left alone. The Feature Teaser and Infant Accordion are separate components.

---

## 4. Component reference

All three components use one client library: **`anchora.site`**.

- The page policy `anchora/components/page/policy` loads `anchora.dependencies` and `anchora.site`.
- No component has its own client library.
- No HTML file contains a `<style>` or `<script>` block.
- `anchora.base` is loaded by `page/customheaderlibs.html`.
- `anchora.grid` is the layout grid that came with the project.

### 4.1 Hero Banner

| | |
|---|---|
| Resource type | `anchora/components/herobanner` |
| Sling Model | `com.anchora.core.models.HeroBanner` (implemented by `com.anchora.core.models.impl.HeroBannerImpl`) |
| Dialog | `ui.apps/.../herobanner/_cq_dialog/.content.xml` |
| Policy | None on the banner. The picture inside uses `anchora/components/image/policy_651483963895698`. |
| Client library | `anchora.site` |

The banner is a container. The picture is a child Image component (`anchora/components/image`, which is Core Image v3).

**Dialog fields**

| Tab | Field | Saved as |
|---|---|---|
| Text | Title | `./title` |
| Text | Introduction (rich text, paragraphs only) | `./introduction` |
| Text | On this page (if blank, shows "On this page") | `./linksHeading` |
| Text | Links. Label and Anchor are required. Do not type `#`. | `./links/*/label`, `./links/*/anchor` |
| Image | Image from the DAM (you cannot upload from your computer) | `./image/fileReference` |
| Image | Alt text | `./image/alt` |
| Image | Image is decorative | `./image/isDecorative` |

**Image widths allowed by the policy:** 320, 480, 600, 800, 1024, 1200, 1600.

**How alt text is chosen:**

1. If the image is decorative, there is no alt text.
2. Otherwise, if you typed alt text, that text is used.
3. Otherwise, the DAM asset description (`dc:description`) is used.

**Empty banner:** If there is no title and no image, the editor shows "Hero Banner". The published page shows nothing.

### 4.2 Feature Teaser

| | |
|---|---|
| Resource type | `anchora/components/featureteaser` |
| Sling Model | No project model. It uses the Core Teaser model `com.adobe.cq.wcm.core.components.models.Teaser`. |
| Dialog | Core Teaser dialog, with the heading-level fields hidden and the image tab replaced |
| Policy | `anchora/components/featureteaser/policy`. Heading is always H2. Styles: Image left (default), Image right, Image top, Image bottom. |
| Client library | `anchora.site` |

It extends `anchora/components/teaser`, which extends Core Teaser v2. Images go through `anchora/components/image`.

**Dialog fields**

| Tab | Field | Saved as |
|---|---|---|
| Text | Pretitle (optional). CSS hides it if it repeats the title. | `./pretitle` |
| Text | Title. Always H2. "Get title from linked page" is on by default. | `./jcr:title` |
| Text | Description (rich text) | `./jcr:description` |
| Text | ID. Use `joey-club` so the hero link can find this block. | `./id` |
| Asset | Image from the DAM (you cannot upload from your computer) | `./fileReference` |
| Asset | Alt text | `./alt` |
| Asset | Image is decorative | `./isDecorative` |
| Links | Page link | `./linkURL` |
| Links | Call to action. Link and text are required. Can open in a new tab. | `./actions` |

**Link behaviour:**

- A link to a page inside the site ends with `.html`.
- A link that opens in a new tab gets `rel="noopener noreferrer"`.
- A "Back to top" link shows under the image.

**Empty teaser:** If there is no title, pretitle, description, image, or link, the editor shows "Feature Teaser". The published page shows nothing.

> The old Teaser component still uses H3. Do not use it for Joey Club.

### 4.3 Infant Accordion

| | |
|---|---|
| Resource type | `anchora/components/infantaccordion` |
| Sling Model | `com.anchora.core.models.InfantAccordion` (implemented by `com.anchora.core.models.impl.InfantAccordionImpl`). One row is `InfantAccordionItem`. |
| Dialog | `ui.apps/.../infantaccordion/_cq_dialog/.content.xml` |
| Policy | None. Authors can add it because it is in the group **Anchora - Content**. |
| Client library | `anchora.site` |

**Dialog fields**

| Tab | Field | Saved as |
|---|---|---|
| Content | Heading (shown as H2) | `./sectionHeading` |
| Content | Description (rich text: bold, italic, lists, links) | `./description` |
| Content | Accordion items. Title is required and shown as H3. Description is rich text. | `./items/*/title`, `./items/*/description` |
| Labels | Expand all items | `./expandAllLabel` |
| Labels | Collapse all items | `./collapseAllLabel` |

**Behaviour:**

- A row is skipped when both its title and description are blank.
- All panels start closed.
- A reader can open more than one panel at a time. The code can support one-at-a-time, but the dialog does not offer that choice.
- Each item is a button with `aria-expanded` and `aria-controls`.
- Enter, Space, arrow keys, Home, and End work on the button.
- The focused button has a visible outline.
- If there is no heading, description, or items, the editor shows a placeholder. The published page shows nothing.
- There is no field for a link icon. A link alone on its own line gets an arrow from CSS.

### 4.4 Page

`anchora/components/page` extends Core Page v3. `customheaderlibs.html` adds these tags when the values exist:

- `pagetitle`
- `og:title`
- `og:description`
- `og:url`
- `og:image`, `og:image:secure_url`, and `og:image:alt` (from the page featured image)

`og:title` is the page title. If the page has a brand name and the brand is allowed to override, the brand is added after the title. The description, canonical URL, and robots tags come from the normal page properties.

---

## 5. Design decisions

| Component | Choice | Reason |
|---|---|---|
| Hero Banner | **Custom build** | The page needs one H1, a DAM image, alt text that falls back to the DAM description, a decorative option, and an "On this page" list. Core Teaser and Core Image each cover only part of that. A small Java model holds the rules and the HTML only prints the result. The picture is still the normal Image component, so image widths stay on the image policy. |
| Feature Teaser | **Extends Core Teaser** | Core Teaser already handles the title, rich text, image, `.html` links, and new-tab links. We only added what was missing: heading locked to H2, hidden when empty on the published page, and `rel="noopener noreferrer"` on new-tab links. No extra Java model was needed because the Core model already has the link data. |
| Infant Accordion | **Custom build** | Core Accordion is a stack of nested components. This page needs a heading and intro beside a list of questions and answers, plus Expand all and Collapse all. A repeatable dialog field fits this. Authors edit answers in the dialog, so a closed panel can still be edited. The original Accordion was not changed. |

**Heading levels are fixed:**

- The hero title is the only H1. The template used to add another H1, so that title was removed.
- A normal Title component on the page can only be H2 to H6.
- The teaser title is H2 because of its policy.
- Each accordion item title is H3 in the HTML.

---

## 6. Testing

Run the tests from the project folder:

```bash
mvn -pl core test
```

`mvn clean install` runs the same tests. If a test fails, the build fails. Do not use `-DskipTests` when you want to check the tests.

### What the tests cover

Component content for these tests is JSON under `core/src/test/resources`, loaded by `ModelTestJsonSupport`.

**`HeroBannerImplTest`** (JUnit 5 and AEM Mocks):

- title and introduction
- typed alt text, blank alt using the DAM description, and decorative images
- image URLs at 600 and 1200 pixels, including SVG and missing assets
- jump links: missing label, missing anchor, and a `javascript:` value
- the empty banner

**`InfantAccordionImplTest`:**

- heading and introduction
- expand and collapse labels
- two items
- a row with only a title, and a row with only a description
- a blank row that is dropped
- the empty component

The old sample tests in `core` still run too (Hello World, sample servlet, filter, listener, scheduler).

### What the tests do NOT cover

- the HTML output, the dialogs, or the policies
- the Feature Teaser (it has no project Java model)
- keyboard use, focus, or Expand all in a browser
- the real page, phone and desktop layout, or `error.log`
- `it.tests` and `ui.tests`

---

## 7. Requirement trace

Status values: **Done**, **Done on this author only**, **Partly done**, **Not done**.

### Hero Banner

| ID | Requirement | Status | Where |
|---|---|---|---|
| HB-01 | Dialog fields: title, introduction, DAM image, alt text | Done | Hero Banner dialog |
| HB-02 | Blank alt uses DAM description. Decorative is a separate checkbox. | Done | `HeroBannerImpl.java` and its test |
| HB-03 | List of label and anchor, shown as "On this page" | Done | `HeroBannerImpl.java`, `JumpLinkImpl.java`, `herobanner.html` |
| HB-04 | One H1. Image has at least two widths. | Done | `herobanner.html`, template structure, image policy |
| HB-05 | Java model behind an interface. HTML does not decide the rules. | Done | `HeroBanner.java`, `HeroBannerImpl.java`, `herobanner.html` |
| HB-06 | No title and no image: editor placeholder, nothing broken when published | Done | `HeroBannerImpl.isEmpty`, `herobanner.html` |

### Feature Teaser

| ID | Requirement | Status | Where |
|---|---|---|---|
| TS-01 | Title, rich text, image, link label, link target, new tab | Done | Feature Teaser dialog and Core Teaser Links tab |
| TS-02 | Heading comes from policy and is H2. Authors cannot change it per teaser. | Done | `featureteaser/policy`. Dialog hides the heading picker. |
| TS-03 | Internal links end with `.html`. New tab adds `rel="noopener noreferrer"`. | Done | Core Teaser, plus `featureteaser.html`, `title.html`, `action.html` |
| TS-04 | Extend Core Teaser and explain why | Done | `featureteaser/.content.xml` and [Design decisions](#5-design-decisions) |
| TS-05 | Title "Joey Club" and link "Find out about the Joey Club" to a page on this site | Done on this author only | Page content. Link target is `/content/anchora/us/en/joey-club`. Not in Git. |

### Infant Accordion

| ID | Requirement | Status | Where |
|---|---|---|---|
| AC-01 | Heading and introduction beside the accordion | Done | `infantaccordion.html`, `InfantAccordionImpl.java` |
| AC-02 | Authors can add, remove, and reorder items. Each has a title and rich text. | Done | Dialog field `./items` |
| AC-03 | The six booking titles | Done on this author only | Page content. Not in Git. |
| AC-04 | A setting for one open panel or many. All panels start closed. | Partly done | Panels start closed. No setting, so many panels can be open. |
| AC-05 | Buttons with `aria-expanded` and `aria-controls`, keyboard use, visible focus outline | Done | `infantaccordion.html`, `infantaccordion.js`, `_infantaccordion.scss` |
| AC-06 | A closed item can still be edited | Done | Items are edited in the dialog, not inside the panel |
| AC-07 | Explain why this is a new component | Done | [Design decisions](#5-design-decisions) |

### Page and template

| ID | Requirement | Status | Where |
|---|---|---|---|
| PT-01 | Template locks header and footer. Page area allows only needed components, plus Title and Text. | Partly done | Header and footer are locked. The page area still allows every component in **Anchora - Content**, plus forms. |
| PT-02 | Header and footer defined once and not editable on the page | Partly done | Locked experience fragments. Header is still the sample navigation. Footer is a copyright line and address, with no links. |
| PT-03 | Page created from the template and filled in on the author | Done on this author only | `/content/anchora/us/en/flying-with-infants-children`. A fresh install does not create it. |
| PT-04 | Landmarks: `header`, named `nav`, `main`, `footer`, and a skip link | Not done | No skip link. Header and footer are not in those landmarks. |
| PT-05 | Readable at 375px and 1280px | Partly done | Styles have breakpoints. No saved check at those widths. |
| PT-06 | CSS and JavaScript from client libraries. No style or script blocks in HTML. | Done | `anchora.site` |

### Engineering

| ID | Requirement | Status | Where |
|---|---|---|---|
| EN-01 | Record the AEM archetype version and settings | Partly done | Settings are in `archetype.properties`. Version number is missing. `languageCountry` is still a placeholder. |
| EN-02 | `mvn clean install -PautoInstallPackage` installs to author on port 4502 | Done for the code | A clean author does not get the sample page. See EN-04. |
| EN-03 | Every Sling Model has JUnit 5 tests with AEM Mocks. A failed test stops the build. | Done | `HeroBannerImplTest`, `InfantAccordionImplTest`, sample model test |
| EN-04 | Finished page stored in `ui.content` | Not done | The page exists only on this author. |
| EN-05 | Work committed on a feature branch and merged by pull request | Not done | |
| EN-06 | No browser console errors and no new `error.log` lines from this code | Not done | Not signed off. |

---

## 8. Assumptions and next steps

### Assumptions

- Local builds use Java 21, the same as Cloud Manager. The compiled code still targets Java 11.
- Authors type `joey-club` in the anchor field. The model adds the `#`. Values that start with `javascript:` are ignored.
- The hero counts as empty only when both the title and image are missing.
- Accordion answers are edited in the dialog, so a closed panel can still be edited.
- The teaser pretitle is hidden with CSS when it would repeat the title.
- The Open Graph tags are an extra. The task did not ask for them. The image tag uses the page featured image.
- The text on the sample page lives on this author. The component code lives in Git.

### Known limitations

- A new AEM author does not get the page, the Joey Club page, or the images they use.
- The page area allows more components than the task asked for.
- There is no skip link.
- Authors cannot choose one open panel or many open panels.
- The header and footer are still the project samples.
- The accordion tests do not run in a browser.
- The hero Java code can build image URLs, but the page uses the nested Image component instead.

### With one more day

1. Save the page and the Joey Club page in `ui.content`, so one Maven install shows the finished page.
2. Limit which components can be dropped on the page.
3. Add the skip link and page landmarks.
4. Replace the sample header and footer.
5. Either add the one-open-panel setting, or remove the unused code for it.