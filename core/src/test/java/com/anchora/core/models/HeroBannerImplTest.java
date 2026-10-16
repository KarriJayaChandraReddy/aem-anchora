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
package com.anchora.core.models;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.anchora.core.models.impl.HeroBannerImpl;
import com.anchora.core.testcontext.AppAemContext;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AemContextExtension.class)
class HeroBannerImplTest {

    private static final String ASSET_PATH = "/content/dam/anchora/hero.jpg";
    private static final String MULTI_ASSET_PATH = "/content/dam/anchora/hero-multi.jpg";

    private final AemContext context = AppAemContext.newAemContext();

    private Page page;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(HeroBannerImpl.class);
        context.create().resource(ASSET_PATH, "jcr:primaryType", "dam:Asset");
        context.create().resource(MULTI_ASSET_PATH, "jcr:primaryType", "dam:Asset");
        context.registerAdapter(Resource.class, Asset.class, (Function<Resource, Asset>) this::adaptAsset);
        page = context.create().page("/content/anchora/us/en/flying-with-infants-children");
    }

    @Test
    void returnsAuthoredTitleAndIntroduction() {
        HeroBanner hero = banner(props("title", "Flying with infants and children", "introduction", "Young flyers are welcome."));

        assertEquals("Flying with infants and children", hero.getTitle());
        assertEquals("Young flyers are welcome.", hero.getIntroduction());
        assertFalse(hero.isEmpty());
    }

    @Test
    void authoredAltWinsOverDamDescription() {
        HeroBanner hero = bannerWithImage("alt", "Author description");

        assertTrue(hero.hasImage());
        assertEquals("Author description", hero.getImageAlt());
        assertNull(hero.getImageRole());
    }

    @Test
    void blankAltUsesDamDescription() {
        HeroBanner hero = bannerWithImage();

        assertEquals("Crew member with a young passenger", hero.getImageAlt());
    }

    @Test
    void blankAltUsesFirstDamDescriptionWhenMetadataIsMultiValued() {
        HeroBanner hero = bannerWithAsset(MULTI_ASSET_PATH);

        assertEquals("Crew member with a young passenger", hero.getImageAlt());
    }

    @Test
    void decorativeImageSuppressesAltText() {
        HeroBanner hero = bannerWithImage("alt", "Author description", "isDecorative", true);

        assertEquals("", hero.getImageAlt());
        assertEquals("presentation", hero.getImageRole());
    }

    @Test
    void imageServesTwoWidths() {
        HeroBanner hero = bannerWithImage();

        assertTrue(hero.getImageSrc().contains(".coreimg.85.1200.jpeg"));
        assertTrue(hero.getImageSrcSet().contains(".coreimg.85.600.jpeg"));
        assertTrue(hero.getImageSrcSet().contains(" 600w"));
        assertTrue(hero.getImageSrcSet().contains(".coreimg.85.1200.jpeg"));
        assertTrue(hero.getImageSrcSet().contains(" 1200w"));
    }

    @Test
    void missingAssetDoesNotRenderBrokenImage() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("sling:resourceType", HeroBannerImpl.RESOURCE_TYPE);
        properties.put("title", "Flying with infants and children");
        Resource banner = context.create().resource(page, "hero", properties);
        context.create().resource(banner, "image", "fileReference", "/content/dam/anchora/missing.jpg");

        HeroBanner hero = banner.adaptTo(HeroBanner.class);

        assertFalse(hero.hasImage());
        assertNull(hero.getImageSrc());
        assertFalse(hero.isEmpty());
    }

    @Test
    void linksHeadingDefaultsWhenBlankAndUsesAuthoredText() {
        assertEquals("On this page", banner(props("title", "Flying with infants and children")).getLinksHeading());
        assertEquals("In this article", banner(props(
                "title", "Flying with infants and children",
                "linksHeading", "  In this article  ")).getLinksHeading());
    }

    @Test
    void jumpLinksAreNormalisedAndIncompleteItemsAreDropped() {
        Resource banner = context.create().resource(page, "hero",
                "sling:resourceType", HeroBannerImpl.RESOURCE_TYPE,
                "title", "Flying with infants and children");
        Resource links = context.create().resource(banner, "links");
        context.create().resource(links, "item0", "label", "Joey Club", "anchor", "joey-club");
        context.create().resource(links, "item1", "label", "Booking", "anchor", "#booking-infants");
        context.create().resource(links, "item2", "label", "Missing anchor", "anchor", "  ");
        context.create().resource(links, "item3", "label", " ", "anchor", "nowhere");

        HeroBanner hero = banner.adaptTo(HeroBanner.class);

        assertEquals(2, hero.getJumpLinks().size());
        assertEquals("Joey Club", hero.getJumpLinks().get(0).getLabel());
        assertEquals("#joey-club", hero.getJumpLinks().get(0).getHref());
        assertEquals("#booking-infants", hero.getJumpLinks().get(1).getHref());
    }

    @Test
    void emptyWhenTitleAndImageAreMissing() {
        HeroBanner hero = banner(props("introduction", "Only an introduction"));

        assertTrue(hero.isEmpty());
        assertTrue(hero.getJumpLinks().isEmpty());
    }

    private Asset adaptAsset(Resource resource) {
        if (!ASSET_PATH.equals(resource.getPath()) && !MULTI_ASSET_PATH.equals(resource.getPath())) {
            return null;
        }
        Asset asset = mock(Asset.class);
        Object description = MULTI_ASSET_PATH.equals(resource.getPath())
                ? new Object[] {"Crew member with a young passenger"}
                : "Crew member with a young passenger";
        when(asset.getMetadata(HeroBannerImpl.DAM_DESCRIPTION)).thenReturn(description);
        when(asset.getMimeType()).thenReturn("image/jpeg");
        when(asset.getName()).thenReturn("hero.jpg");
        when(asset.getLastModified()).thenReturn(1_700_000_000_000L);
        return asset;
    }

    private HeroBanner bannerWithImage(Object... imageProperties) {
        return bannerWithAsset(ASSET_PATH, imageProperties);
    }

    private HeroBanner bannerWithAsset(String assetPath, Object... imageProperties) {
        Resource banner = context.create().resource(page, "hero-" + System.nanoTime(),
                "sling:resourceType", HeroBannerImpl.RESOURCE_TYPE,
                "title", "Flying with infants and children");
        Map<String, Object> image = new HashMap<>();
        image.put("fileReference", assetPath);
        for (int i = 0; i < imageProperties.length; i += 2) {
            image.put(String.valueOf(imageProperties[i]), imageProperties[i + 1]);
        }
        context.create().resource(banner, "image", image);
        return banner.adaptTo(HeroBanner.class);
    }

    private HeroBanner banner(Map<String, Object> properties) {
        properties.put("sling:resourceType", HeroBannerImpl.RESOURCE_TYPE);
        return context.create().resource(page, "hero-" + System.nanoTime(), properties).adaptTo(HeroBanner.class);
    }

    private static Map<String, Object> props(Object... pairs) {
        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            properties.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return properties;
    }
}
