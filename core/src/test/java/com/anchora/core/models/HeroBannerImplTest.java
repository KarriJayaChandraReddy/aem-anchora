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

    private final Map<String, Asset> assetsByPath = new HashMap<>();

    private Page page;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(HeroBannerImpl.class);
        registerAsset(ASSET_PATH, jpeg("hero.jpg", 1_700_000_000_000L, "Crew member with a young passenger"));
        registerAsset(MULTI_ASSET_PATH, jpeg("hero.jpg", 1_700_000_000_000L, new Object[] {"Crew member with a young passenger"}));
        context.registerAdapter(Resource.class, Asset.class, (Function<Resource, Asset>) resource -> assetsByPath.get(resource.getPath()));
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

    @Test
    void blankTitleAndIntroductionAreTreatedAsMissing() {
        HeroBanner hero = banner(props("title", "  ", "introduction", " \n "));

        assertNull(hero.getTitle());
        assertNull(hero.getIntroduction());
        assertTrue(hero.isEmpty());
    }

    @Test
    void linksHeadingIdUsesTheComponentName() {
        Resource banner = context.create().resource(page, "hero-links",
                "sling:resourceType", HeroBannerImpl.RESOURCE_TYPE,
                "title", "Flying with infants and children");

        assertEquals("on-this-page-hero-links", banner.adaptTo(HeroBanner.class).getLinksHeadingId());
    }

    @Test
    void imageWithoutAFileReferenceIsNotRendered() {
        Resource banner = context.create().resource(page, "hero-no-file",
                "sling:resourceType", HeroBannerImpl.RESOURCE_TYPE,
                "title", "Flying with infants and children");
        context.create().resource(banner, "image", "fileReference", "  ");

        HeroBanner hero = banner.adaptTo(HeroBanner.class);

        assertFalse(hero.hasImage());
        assertNull(hero.getImageSrc());
    }

    @Test
    void resourceThatDoesNotAdaptToAnAssetIsNotRendered() {
        String path = "/content/anchora/plain.jpg";
        context.create().resource(path, "jcr:primaryType", "nt:unstructured");

        HeroBanner hero = bannerWithAsset(path);

        assertFalse(hero.hasImage());
        assertNull(hero.getImageSrc());
    }

    @Test
    void svgUsesTheOriginalRendition() {
        String path = "/content/dam/anchora/mark.svg";
        Asset asset = mock(Asset.class);
        when(asset.getMimeType()).thenReturn("image/svg+xml");
        when(asset.getMetadata(HeroBannerImpl.DAM_DESCRIPTION)).thenReturn("Mark");
        registerAsset(path, asset);

        HeroBanner hero = bannerWithAsset(path);

        assertEquals(path + "/_jcr_content/renditions/original", hero.getImageSrc());
        assertNull(hero.getImageSrcSet());
        assertEquals("Mark", hero.getImageAlt());
    }

    @Test
    void missingLastModifiedOmitsTheCacheKey() {
        String path = "/content/dam/anchora/untimed.jpg";
        registerAsset(path, jpeg("untimed.jpg", 0L, "Still"));

        String src = bannerWithAsset(path).getImageSrc();

        assertTrue(src.endsWith(".coreimg.85.1200.jpeg"));
        assertFalse(src.contains(".jpeg/"));
    }

    @Test
    void sanitisedAssetNameIsAddedToTheRenditionUrl() {
        String path = "/content/dam/anchora/named.png";
        registerAsset(path, jpeg("My Hero Photo.png", 1_700_000_000_000L, "Named"));

        assertTrue(bannerWithAsset(path).getImageSrc().contains("/1700000000000/my-hero-photo.jpeg"));
    }

    @Test
    void blankAssetNameIsLeftOutOfTheRenditionUrl() {
        String path = "/content/dam/anchora/symbols.jpg";
        registerAsset(path, jpeg("!!!.jpg", 1_700_000_000_000L, "Symbols"));

        String src = bannerWithAsset(path).getImageSrc();

        assertTrue(src.endsWith("/1700000000000.jpeg"));
        assertFalse(src.contains("/1700000000000/"));
    }

    @Test
    void nullAssetNameStillBuildsARenditionUrl() {
        String path = "/content/dam/anchora/unnamed.jpg";
        registerAsset(path, jpeg(null, 1_700_000_000_000L, "Unnamed"));

        assertTrue(bannerWithAsset(path).getImageSrc().endsWith("/1700000000000.jpeg"));
    }

    @Test
    void blankOrEmptyDamDescriptionLeavesAltEmpty() {
        registerAsset("/content/dam/anchora/whitespace.jpg", jpeg("whitespace.jpg", 1L, "   "));
        registerAsset("/content/dam/anchora/empty-array.jpg", jpeg("empty-array.jpg", 1L, new Object[0]));
        registerAsset("/content/dam/anchora/null-meta.jpg", jpeg("null-meta.jpg", 1L, null));

        assertEquals("", bannerWithAsset("/content/dam/anchora/whitespace.jpg").getImageAlt());
        assertEquals("", bannerWithAsset("/content/dam/anchora/empty-array.jpg").getImageAlt());
        assertEquals("", bannerWithAsset("/content/dam/anchora/null-meta.jpg").getImageAlt());
    }

    @Test
    void metadataArraySkipsBlanksAndUsesTheFirstText() {
        String path = "/content/dam/anchora/useful-array.jpg";
        registerAsset(path, jpeg("useful-array.jpg", 1L, new Object[] {"", "  ", null, "Useful"}));

        assertEquals("Useful", bannerWithAsset(path).getImageAlt());
    }

    @Test
    void javascriptAnchorsAreDropped() {
        Resource banner = context.create().resource(page, "hero-scripts",
                "sling:resourceType", HeroBannerImpl.RESOURCE_TYPE,
                "title", "Flying with infants and children");
        Resource links = context.create().resource(banner, "links");
        context.create().resource(links, "script", "label", "Bad", "anchor", "javascript:alert(1)");
        context.create().resource(links, "scriptUpper", "label", "Bad", "anchor", "  JAVASCRIPT:alert(1)");
        context.create().resource(links, "hashOnly", "label", "Hash", "anchor", "#");
        context.create().resource(links, "ok", "label", "Joey Club", "anchor", "joey-club");

        HeroBanner hero = banner.adaptTo(HeroBanner.class);

        assertEquals(1, hero.getJumpLinks().size());
        assertEquals("#joey-club", hero.getJumpLinks().get(0).getHref());
    }

    private void registerAsset(String path, Asset asset) {
        context.create().resource(path, "jcr:primaryType", "dam:Asset");
        assetsByPath.put(path, asset);
    }

    private static Asset jpeg(String name, long lastModified, Object description) {
        Asset asset = mock(Asset.class);
        when(asset.getMetadata(HeroBannerImpl.DAM_DESCRIPTION)).thenReturn(description);
        when(asset.getMimeType()).thenReturn("image/jpeg");
        when(asset.getName()).thenReturn(name);
        when(asset.getLastModified()).thenReturn(lastModified);
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
