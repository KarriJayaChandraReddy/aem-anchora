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

import static com.anchora.core.testcontext.ModelTestJsonSupport.adaptResource;
import static com.anchora.core.testcontext.ModelTestJsonSupport.createTree;
import static com.anchora.core.testcontext.ModelTestJsonSupport.loadJson;
import static com.anchora.core.testcontext.ModelTestJsonSupport.textOrNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.anchora.core.models.impl.HeroBannerImpl;
import com.anchora.core.testcontext.AppAemContext;
import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class HeroBannerImplTest {

    private static final String ROOT = "/content/anchora/us/en/flying-with-infants-children/jcr:content/root";

    private final AemContext context = AppAemContext.newAemContext();

    private final Map<String, Asset> assetsByPath = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        context.addModelsForClasses(HeroBannerImpl.class);
        context.registerAdapter(Resource.class, Asset.class, (Function<Resource, Asset>) resource -> assetsByPath.get(resource.getPath()));

        JsonNode content = loadJson(getClass(), "hero-banner-content.json");
        registerAssets(content.get("assets"));
        context.create().resource(textOrNull(content, "notAnAsset"), "jcr:primaryType", "nt:unstructured");

        JsonNode scenarios = content.get("scenarios");
        Iterator<String> names = scenarios.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            createTree(context, ROOT + "/" + name, scenarios.get(name));
        }
    }

    @Test
    void returnsAuthoredTitleAndIntroduction() {
        HeroBanner hero = hero("authored");

        assertEquals("Flying with infants and children", hero.getTitle());
        assertEquals("Young flyers are welcome.", hero.getIntroduction());
        assertFalse(hero.isEmpty());
    }

    @Test
    void authoredAltWinsOverDamDescription() {
        HeroBanner hero = hero("authoredAlt");

        assertTrue(hero.hasImage());
        assertEquals("Author description", hero.getImageAlt());
        assertNull(hero.getImageRole());
    }

    @Test
    void blankAltUsesDamDescription() {
        assertEquals("Crew member with a young passenger", hero("damAlt").getImageAlt());
    }

    @Test
    void blankAltUsesFirstDamDescriptionWhenMetadataIsMultiValued() {
        assertEquals("Crew member with a young passenger", hero("multiAlt").getImageAlt());
    }

    @Test
    void decorativeImageSuppressesAltText() {
        HeroBanner hero = hero("decorative");

        assertEquals("", hero.getImageAlt());
        assertEquals("presentation", hero.getImageRole());
    }

    @Test
    void imageServesTwoWidths() {
        HeroBanner hero = hero("twoWidths");

        assertTrue(hero.getImageSrc().contains(".coreimg.85.1200.jpeg"));
        assertTrue(hero.getImageSrcSet().contains(".coreimg.85.600.jpeg"));
        assertTrue(hero.getImageSrcSet().contains(" 600w"));
        assertTrue(hero.getImageSrcSet().contains(".coreimg.85.1200.jpeg"));
        assertTrue(hero.getImageSrcSet().contains(" 1200w"));
    }

    @Test
    void missingAssetDoesNotRenderBrokenImage() {
        HeroBanner hero = hero("missingAsset");

        assertFalse(hero.hasImage());
        assertNull(hero.getImageSrc());
        assertFalse(hero.isEmpty());
    }

    @Test
    void linksHeadingDefaultsWhenBlankAndUsesAuthoredText() {
        assertEquals("On this page", hero("defaultHeading").getLinksHeading());
        assertEquals("In this article", hero("authoredHeading").getLinksHeading());
    }

    @Test
    void jumpLinksAreNormalisedAndIncompleteItemsAreDropped() {
        HeroBanner hero = hero("links");

        assertEquals(2, hero.getJumpLinks().size());
        assertEquals("Joey Club", hero.getJumpLinks().get(0).getLabel());
        assertEquals("#joey-club", hero.getJumpLinks().get(0).getHref());
        assertEquals("#booking-infants", hero.getJumpLinks().get(1).getHref());
    }

    @Test
    void emptyWhenTitleAndImageAreMissing() {
        HeroBanner hero = hero("empty");

        assertTrue(hero.isEmpty());
        assertTrue(hero.getJumpLinks().isEmpty());
    }

    @Test
    void blankTitleAndIntroductionAreTreatedAsMissing() {
        HeroBanner hero = hero("blankText");

        assertNull(hero.getTitle());
        assertNull(hero.getIntroduction());
        assertTrue(hero.isEmpty());
    }

    @Test
    void linksHeadingIdUsesTheComponentName() {
        assertEquals("on-this-page-hero-links", hero("hero-links").getLinksHeadingId());
    }

    @Test
    void imageWithoutAFileReferenceIsNotRendered() {
        HeroBanner hero = hero("blankFile");

        assertFalse(hero.hasImage());
        assertNull(hero.getImageSrc());
    }

    @Test
    void resourceThatDoesNotAdaptToAnAssetIsNotRendered() {
        HeroBanner hero = hero("notAnAsset");

        assertFalse(hero.hasImage());
        assertNull(hero.getImageSrc());
    }

    @Test
    void svgUsesTheOriginalRendition() {
        HeroBanner hero = hero("svg");

        assertEquals("/content/dam/anchora/mark.svg/_jcr_content/renditions/original", hero.getImageSrc());
        assertNull(hero.getImageSrcSet());
        assertEquals("Mark", hero.getImageAlt());
    }

    @Test
    void missingLastModifiedOmitsTheCacheKey() {
        String src = hero("untimed").getImageSrc();

        assertTrue(src.endsWith(".coreimg.85.1200.jpeg"));
        assertFalse(src.contains(".jpeg/"));
    }

    @Test
    void sanitisedAssetNameIsAddedToTheRenditionUrl() {
        assertTrue(hero("named").getImageSrc().contains("/1700000000000/my-hero-photo.jpeg"));
    }

    @Test
    void blankAssetNameIsLeftOutOfTheRenditionUrl() {
        String src = hero("symbols").getImageSrc();

        assertTrue(src.endsWith("/1700000000000.jpeg"));
        assertFalse(src.contains("/1700000000000/"));
    }

    @Test
    void nullAssetNameStillBuildsARenditionUrl() {
        assertTrue(hero("unnamed").getImageSrc().endsWith("/1700000000000.jpeg"));
    }

    @Test
    void blankOrEmptyDamDescriptionLeavesAltEmpty() {
        assertEquals("", hero("whitespaceMeta").getImageAlt());
        assertEquals("", hero("emptyArray").getImageAlt());
        assertEquals("", hero("nullMeta").getImageAlt());
    }

    @Test
    void metadataArraySkipsBlanksAndUsesTheFirstText() {
        assertEquals("Useful", hero("usefulArray").getImageAlt());
    }

    @Test
    void javascriptAnchorsAreDropped() {
        HeroBanner hero = hero("links");

        assertEquals(1, hero.getJumpLinks().stream().filter(link -> "Joey Club".equals(link.getLabel())).count());
        assertEquals("#joey-club", hero.getJumpLinks().get(0).getHref());
    }

    private HeroBanner hero(String name) {
        Resource resource = context.resourceResolver().getResource(ROOT + "/" + name);
        return adaptResource(resource, HeroBanner.class);
    }

    private void registerAssets(JsonNode assets) {
        Iterator<String> paths = assets.fieldNames();
        while (paths.hasNext()) {
            String path = paths.next();
            context.create().resource(path, "jcr:primaryType", "dam:Asset");
            assetsByPath.put(path, mockAsset(assets.get(path)));
        }
    }

    private static Asset mockAsset(JsonNode assetJson) {
        Asset asset = mock(Asset.class);
        when(asset.getMimeType()).thenReturn(textOrNull(assetJson, "mimeType"));
        when(asset.getName()).thenReturn(textOrNull(assetJson, "name"));
        long lastModified = assetJson.has("lastModified") && !assetJson.get("lastModified").isNull()
                ? assetJson.get("lastModified").asLong()
                : 0L;
        when(asset.getLastModified()).thenReturn(lastModified);
        when(asset.getMetadata(HeroBannerImpl.DAM_DESCRIPTION)).thenReturn(metadata(assetJson.get("description")));
        return asset;
    }

    private static Object metadata(JsonNode description) {
        if (description == null || description.isNull()) {
            return null;
        }
        if (description.isArray()) {
            Object[] values = new Object[description.size()];
            for (int index = 0; index < description.size(); index++) {
                JsonNode item = description.get(index);
                values[index] = item.isNull() ? null : item.asText();
            }
            return values;
        }
        return description.asText();
    }
}
