# aem-anchora

## Hero Banner (HB-01 to HB-06)

The Hero Banner is a custom component, `anchora/components/herobanner`. It is not a Core Components proxy. The page needs one H1, a DAM image with two widths, alt text that falls back to the asset description, and an “On this page” list. Core Teaser and Core Image do not provide that combination without hiding most of their dialog, so the component has its own Sling Model.

`HeroBanner` is the interface and `HeroBannerImpl` is the implementation. The component sets `cq:isContainer` so the out-of-the-box image is a child of the banner. `herobanner.html` prints the model values and includes that child with `anchora/components/image`, which extends Core Image v3. The banner dialog has only a title, a rich-text introduction, an “On this page” heading, and a label-and-anchor multifield. The picture, alt text, and decorative option are authored on the nested image component. That image component serves the responsive widths from the image policy, uses the DAM description when alt text is blank, and marks a decorative image so assistive technology skips it. With no title and no image, edit mode shows a placeholder and the published page renders nothing. The nested image appears once a title or an image has been authored.

## Teaser (TS-04)

The Feature Teaser is not a from-scratch component. `anchora/components/featureteaser` sets `sling:resourceSuperType` to the Anchora teaser proxy, and that proxy delegates to Core Teaser v2 (`core/wcm/components/teaser/v2/teaser`) with `imageDelegate` set to `anchora/components/image`.

Core Teaser already renders the title, rich text, image, internal `.html` links, and the new-tab target. The Feature Teaser only adds what Core does not do for this page: the heading level is fixed to H2 by the template policy, an empty instance shows an editor placeholder and renders nothing, and a link that opens in a new tab gets `rel="noopener noreferrer"`. The generic Teaser proxy is left unchanged so other pages can still use it.

## Infant Booking

The Infant Booking is a custom component, `anchora/components/infantbooking`. It is not a Core Components proxy. The page needs an H2 and a rich-text introduction beside an accordion of title-and-answer pairs, plus expand-all and collapse-all labels and a DAM icon on links. Core Accordion is a container of nested components and does not provide that combination, so this component has its own Sling Model. The existing Accordion proxy is left unchanged.

`InfantBookingModel` reads the dialog and `InfantBookingItem` is one accordion entry. `infantbooking.html` only prints the model values. The dialog has a heading, a rich-text description, a title-and-description multifield, expand-all and collapse-all labels, and a DAM link icon. The heading is an H2 and each item title is an H3. Panels start collapsed, and several can be open at once. A blank item is skipped. The link icon is appended to links in the introduction and the answers, with an empty alt and `aria-hidden="true"`. With no heading, no description, and no items, edit mode shows a placeholder and the published page renders nothing.

