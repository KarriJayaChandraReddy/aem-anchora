# Authoring guide

This guide is for a content author who has used AEM before, but has not used this project.

The page is **Flying with infants and children**. It uses the **Page Content** template. You add three components, in this order:

1. Hero Banner
2. Feature Teaser
3. Infant Accordion

All three are in the component group **Anchora - Content**.

---

## 1. Create the page

1. Open **Sites** and go to `/content/anchora/us/en`.
2. Create a page.
3. Choose the **Page Content** template.
4. Set the name to `flying-with-infants-children`.
5. Set the title to `Flying with infants and children`.
6. Open the page in the editor.

On this author the page already exists:

`http://localhost:4502/editor.html/content/anchora/us/en/flying-with-infants-children.html`

### The three areas of the template

| Area | Where | Can you edit it on the page? |
|---|---|---|
| Header | Top, on a grey bar | No. It is locked. |
| Content area | Middle, white | Yes. Drop components here. |
| Footer | Bottom, on a grey bar | No. It is locked. |

![The editor. The header is locked on the grey bar. The hero title is in the white content area.](docs/authoring/editor.png)

### Which components to use

In the white area, drag components from **Anchora - Content**. For this page, use only:

| Component | Use it for |
|---|---|
| Hero Banner | The title, introduction, photo, and "On this page" links |
| Feature Teaser | The Joey Club block |
| Infant Accordion | The booking questions |

### Which components NOT to use

- **Teaser.** This is a different component. Its heading is H3.
- **Accordion.** This is a different component. It has no introduction and no Expand all control.
- **Title**, if you want it to be the page heading. The Hero Banner title is the only H1. A Title component on this template can only be H2 to H6.

The template also allows other components in **Anchora - Content**, such as Text and Image. You do not need them for this page.

---

## 2. Fill in each component

Select the component and click the wrench icon to open its dialog.

### 2.1 Hero Banner

The dialog has two tabs: **Text** and **Image**. Set the photo on the Image tab. Do not add a separate Image component.

#### Text tab

| Field | What it does |
|---|---|
| Title | The page heading. It is the only H1, so use it once. Example: `Flying with infants and children`. |
| Introduction | The paragraph under the photo. You can use bold, italic, underline, lists, and links. Do not add a heading here. The only format allowed is Paragraph. |
| On this page | The label above the jump links. It is already filled with `On this page`. Leave it blank to keep that label. Change it only if you want different words. |
| Links | One row for each section further down the page. |

Each link row has two required fields:

- **Label** is the link text. Example: `Joey Club`.
- **Anchor** is the section id. Example: `joey-club`. Do not type `#`. The page adds it for you.

The anchors used on this page are:

- `joey-club`
- `booking-infants`
- `inflight-entertainment-for-kids`

The Feature Teaser ID must be `joey-club`. If it is different, the Joey Club link will not land on the teaser.

#### Image tab

| Field | What it does |
|---|---|
| Image | Pick a picture from the DAM. You cannot upload a file from your computer in this dialog. |
| Alt text | A short description of the picture for someone who cannot see it. |
| Image is decorative | Turn this on only when the picture adds no information. Screen readers skip it, and the alt text is ignored. |

**Photo size**

- Use a wide photo, at least 1200 pixels wide.
- On a tablet or desktop the photo is full width and about 450 pixels tall. The edges are cropped.
- Keep the important part of the photo in the upper half, because the crop sits high in the picture.
- On a phone the photo is hidden. Do not put essential text inside the picture.

**Alt text**

- Describe what is in the photo, for example who is shown and where.
- Do not start with "image of" or "picture of".
- If you leave Alt text blank, the page uses the description stored on the DAM asset. If the asset has no description, write one.

**Empty banner:** If there is no title and no photo, the editor shows a placeholder. The published page shows nothing until you add a title or a photo.

![Published hero: the H1 and the wide photo.](docs/authoring/hero.png)

### 2.2 Feature Teaser

Drag **Feature Teaser**, not **Teaser**.

The dialog has three tabs: **Text**, **Asset**, and **Links**.

#### Text tab

| Field | What it does |
|---|---|
| Pretitle | Optional text above the title. Leave it blank on this page. |
| Title | The heading. It is always H2. There is no heading picker. For this block, type `Joey Club`. |
| Get title from linked page | On by default. Turn it off when you type the title yourself. If you leave it on, the title comes from the linked page. |
| Description | The text under the title. Rich text: bold, italic, lists, and links. |
| Get description from linked page | On by default. Turn it off when you type the description yourself. |
| ID | The id that other links use to find this block. Set it to `joey-club` so the hero link lands here. |

#### Asset tab

| Field | What it does |
|---|---|
| Image | Pick a picture from the DAM. You cannot upload a file from your computer. |
| Alt text | Same rule as the hero. Leave it blank to use the DAM description. |
| Image is decorative | Turn this on only when the picture adds no information. |

**Photo size**

- Use a landscape picture, about 1200 pixels wide. The page does not crop it.
- On a desktop the picture sits on the left (up to about 590 pixels wide) and the text sits on the right.
- On a phone the picture is full width and the text sits on top of the bottom of the picture.

#### Links tab

| Field | What it does |
|---|---|
| Link | The page this block points to. A page inside the site is published with `.html` on the end. |
| Call-to-actions | The text link under the description. Add one row. |

On the call-to-action row:

- **Link** is required. For this block, choose `/content/anchora/us/en/joey-club`. Create that page first if it does not exist. A simple placeholder page is enough.
- **Open in a new browser tab.** Leave this off for a page on this site. If you turn it on, the link is marked so the new tab cannot control this page.
- **Text** is required. For this block, type `Find out about the Joey Club`.

#### Image position

Select the teaser and open the paintbrush icon (**Styles**). Choose one:

- Image left (default)
- Image right
- Image top
- Image bottom

**Empty teaser:** If there is no title, text, picture, or link, the editor shows **Feature Teaser**. The published page shows nothing.

When the teaser has content, a **Back to top** link appears under the picture.

![The Joey Club picture on the published page.](docs/authoring/teaser.png)

### 2.3 Infant Accordion

Drag **Infant Accordion**, not **Accordion**.

The dialog has two tabs: **Content** and **Labels**.

#### Content tab

| Field | What it does |
|---|---|
| Heading | The section title. It is an H2. On this page: `Booking for infants (under 2 years)`. |
| Description | The introduction beside the questions. You can use bold, italic, lists, and links. |
| Accordion items | One row for each question. |

Each row has:

- **Title** (required). It is an H3. This is the question people click.
- **Description.** This is the answer. Rich text: bold, italic, lists, and links.

The six titles on this page are:

1. Infants on domestic flights
2. Infants on international flights
3. Booking a seat for your infant
4. Travelling with more than one infant per adult
5. How soon can infants travel after birth?
6. My infant isn't born yet, how do I book?

#### Labels tab

| Field | What it does |
|---|---|
| Expand all items | The words on the control that opens every panel. On this page: `Expand all items`. |
| Collapse all items | The words on the control that closes every panel. On this page: `Collapse all items`. |

If both labels are empty, that control is hidden. Readers can still open panels one at a time.

#### How the panels behave

- All panels start closed.
- A reader can open more than one panel at a time.
- There is no field to limit the page to one open panel.

**Empty accordion:** If there is no heading, no description, and no items, the editor shows a placeholder. The published page shows nothing.

![The booking section: the H2, the introduction, Expand all, and the first question.](docs/authoring/accordion.png)

---

## 3. Manage accordion items

Open the Infant Accordion dialog and go to the **Content** tab. The **Accordion items** field is the list of questions.

| Task | How |
|---|---|
| Add a row | Click the plus button. Type the Title before you save. Title is required. |
| Edit a row | Change its Title or Description in the dialog. You do not need to open the panel on the page. A closed panel on the site still has its answer in the dialog. |
| Reorder rows | Drag a row up or down. The order in the dialog is the order on the page. |
| Remove a row | Click its delete button. The other rows stay. |

Rules for empty rows:

- A row with an empty title and an empty description is left out of the page.
- A row with only a title, or only a description, is still shown.

Save the dialog when the list is in the order you want.

---

## 4. Header and footer

Do not edit the header or footer on the page. They are locked by the **Page Content** template. In the editor they sit on the grey bars above and below the white content area.

Edit them on the experience fragments instead:

| Part | Path |
|---|---|
| Header | `/content/experience-fragments/anchora/us/en/site/header/master` |
| Footer | `/content/experience-fragments/anchora/us/en/site/footer/master` |

To change one: open the fragment, make the change, and save.

- Every page that uses this template shows the same header and footer.
- A change on the fragment shows up on this page.
- A change on this page does not change the fragment.

**Current content:**

- The header is the sample navigation from the project. In the editor it reads **This is Header**.
- The footer is a copyright line and an address. It has no list of links.

The template is at `/conf/anchora/settings/wcm/templates/page-content`. Change the template only if the lock is wrong. Everyday text changes belong on the fragments.

---

## 5. Preview and publish

### Preview on the author

Use either option:

- In the editor, click **Preview** in the top bar. This hides the editing frame.
- Open this address: `http://localhost:4502/content/anchora/us/en/flying-with-infants-children.html?wcmmode=disabled`

### Check before you publish

- There is one H1, the hero title. The introduction is not a heading.
- **On this page** jumps to the teaser when the teaser ID is `joey-club`.
- The teaser title is an H2. The link says **Find out about the Joey Club**.
- The accordion heading is an H2. Each question is an H3.
- Panels start closed. **Expand all items** opens them.
- An empty component shows a placeholder in the editor and nothing in preview.

### Publish

A local publish instance runs on port 4503. This page is already available there:

`http://localhost:4503/content/anchora/us/en/flying-with-infants-children.html`

To publish again after you edit:

1. Open the page in the editor on the author (`localhost:4502`).
2. Open **Page Information** (the slider icon at the top left).
3. Choose **Publish Page**.

You can also select the page in **Sites** and use **Quick Publish**.

**Important:** Publishing copies the page to the publish instance. It does not publish a DAM image or the Joey Club page unless you publish those too. If a picture or a link is missing after publishing, publish the asset and the linked page as well.