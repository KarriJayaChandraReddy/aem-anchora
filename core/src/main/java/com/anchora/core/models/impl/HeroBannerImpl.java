/*
 *  Copyright 2026 Anchora
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.anchora.core.models.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.anchora.core.models.HeroBanner;
import com.anchora.core.models.JumpLink;
import com.day.cq.dam.api.Asset;

/**
 * Resolves hero content, alt text, and responsive image URLs.
 */
@Model(
        adaptables = Resource.class,
        adapters = HeroBanner.class,
        resourceType = HeroBannerImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class HeroBannerImpl implements HeroBanner {

    public static final String RESOURCE_TYPE = "anchora/components/herobanner";

    static final String IMAGE_NODE = "image";
    static final String LINKS_NODE = "links";
    static final String PN_FILE_REFERENCE = "fileReference";
    static final String PN_ALT = "alt";
    static final String PN_DECORATIVE = "isDecorative";
    static final String PN_LABEL = "label";
    static final String PN_ANCHOR = "anchor";
    public static final String DAM_DESCRIPTION = "dc:description";
    static final String ROLE_PRESENTATION = "presentation";
    static final String DEFAULT_LINKS_HEADING = "On this page";
    static final int JPEG_QUALITY = 85;
    static final int[] IMAGE_WIDTHS = {600, 1200};

    private static final String MIME_SVG = "image/svg+xml";

    @Self
    private Resource resource;

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String introduction;

    @ValueMapValue
    private String linksHeading;

    private boolean hasImage;
    private String imageSrc;
    private String imageSrcSet;
    private String imageAlt = "";
    private String imageRole;
    private List<JumpLink> jumpLinks = Collections.emptyList();

    @PostConstruct
    protected void init() {
        title = StringUtils.trimToNull(title);
        introduction = StringUtils.trimToNull(introduction);
        linksHeading = StringUtils.defaultIfBlank(StringUtils.trimToNull(linksHeading), DEFAULT_LINKS_HEADING);
        resolveImage();
        jumpLinks = readJumpLinks();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getIntroduction() {
        return introduction;
    }

    @Override
    public boolean isEmpty() {
        return title == null && !hasImage;
    }

    @Override
    public boolean hasImage() {
        return hasImage;
    }

    @Override
    public String getImageSrc() {
        return imageSrc;
    }

    @Override
    public String getImageSrcSet() {
        return imageSrcSet;
    }

    @Override
    public String getImageAlt() {
        return imageAlt;
    }

    @Override
    public String getImageRole() {
        return imageRole;
    }

    @Override
    public List<JumpLink> getJumpLinks() {
        return jumpLinks;
    }

    @Override
    public String getLinksHeading() {
        return linksHeading;
    }

    @Override
    public String getLinksHeadingId() {
        return "on-this-page-" + resource.getName();
    }

    private void resolveImage() {
        Resource imageResource = resource.getChild(IMAGE_NODE);
        if (imageResource == null) {
            return;
        }
        ValueMap imageProps = imageResource.getValueMap();
        String fileReference = StringUtils.trimToNull(imageProps.get(PN_FILE_REFERENCE, String.class));
        if (fileReference == null) {
            return;
        }
        Resource assetResource = resource.getResourceResolver().getResource(fileReference);
        Asset asset = assetResource == null ? null : assetResource.adaptTo(Asset.class);
        if (asset == null) {
            return;
        }

        boolean decorative = imageProps.get(PN_DECORATIVE, false);
        String authoredAlt = StringUtils.trimToNull(imageProps.get(PN_ALT, String.class));
        hasImage = true;
        imageAlt = resolveAlt(decorative, authoredAlt, asset);
        imageRole = decorative ? ROLE_PRESENTATION : null;
        buildSources(imageResource, asset, fileReference);
    }

    private static String resolveAlt(boolean decorative, String authoredAlt, Asset asset) {
        if (decorative) {
            return "";
        }
        if (authoredAlt != null) {
            return authoredAlt;
        }
        return metadataText(asset.getMetadata(DAM_DESCRIPTION));
    }

    /**
     * DAM metadata values are often stored as a single string or as an array.
     */
    static String metadataText(Object value) {
        if (value instanceof Object[]) {
            for (Object item : (Object[]) value) {
                String text = metadataText(item);
                if (!text.isEmpty()) {
                    return text;
                }
            }
            return "";
        }
        if (value == null) {
            return "";
        }
        return StringUtils.defaultString(StringUtils.trimToNull(value.toString()));
    }

    private void buildSources(Resource imageResource, Asset asset, String fileReference) {
        if (MIME_SVG.equals(asset.getMimeType())) {
            imageSrc = fileReference + "/_jcr_content/renditions/original";
            imageSrcSet = null;
            return;
        }

        String name = seoName(asset.getName());
        long lastModified = asset.getLastModified();
        String base = imageResource.getPath();
        String[] urls = new String[IMAGE_WIDTHS.length];
        StringBuilder srcSet = new StringBuilder();
        for (int i = 0; i < IMAGE_WIDTHS.length; i++) {
            urls[i] = buildRenditionUrl(base, IMAGE_WIDTHS[i], lastModified, name);
            if (i > 0) {
                srcSet.append(", ");
            }
            srcSet.append(urls[i]).append(' ').append(IMAGE_WIDTHS[i]).append('w');
        }
        imageSrc = urls[urls.length - 1];
        imageSrcSet = srcSet.toString();
    }

    static String buildRenditionUrl(String imagePath, int width, long lastModified, String name) {
        StringBuilder url = new StringBuilder(imagePath)
                .append(".coreimg.")
                .append(JPEG_QUALITY)
                .append('.')
                .append(width)
                .append(".jpeg");
        if (lastModified > 0) {
            url.append('/').append(lastModified);
            if (StringUtils.isNotBlank(name)) {
                url.append('/').append(name);
            }
            url.append(".jpeg");
        }
        return url.toString();
    }

    static String seoName(String assetName) {
        String base = StringUtils.substringBeforeLast(StringUtils.defaultString(assetName), ".");
        if (StringUtils.isBlank(base)) {
            base = StringUtils.defaultString(assetName);
        }
        return base.replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-+|-+$)", "").toLowerCase(Locale.ROOT);
    }

    private List<JumpLink> readJumpLinks() {
        Resource links = resource.getChild(LINKS_NODE);
        if (links == null) {
            return Collections.emptyList();
        }
        List<JumpLink> items = new ArrayList<>();
        for (Resource child : links.getChildren()) {
            ValueMap props = child.getValueMap();
            String label = StringUtils.trimToNull(props.get(PN_LABEL, String.class));
            String href = toHref(props.get(PN_ANCHOR, String.class));
            if (label != null && href != null) {
                items.add(new JumpLinkImpl(label, href));
            }
        }
        return Collections.unmodifiableList(items);
    }

    static String toHref(String anchor) {
        String value = StringUtils.trimToEmpty(anchor);
        if (value.startsWith("#")) {
            value = value.substring(1).trim();
        }
        if (value.isEmpty() || value.regionMatches(true, 0, "javascript:", 0, "javascript:".length())) {
            return null;
        }
        return "#" + value;
    }
}
