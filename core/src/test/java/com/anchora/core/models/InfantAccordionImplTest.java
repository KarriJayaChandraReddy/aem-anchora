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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.anchora.core.models.impl.InfantAccordionImpl;
import com.anchora.core.testcontext.AppAemContext;
import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class InfantAccordionImplTest {

    private static final String ROOT = "/content/anchora/jcr:content/root";

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() throws Exception {
        context.addModelsForClasses(InfantAccordionImpl.class);

        JsonNode content = loadJson(getClass(), "infant-accordion-content.json");
        Iterator<String> names = content.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            createTree(context, ROOT + "/" + name, content.get(name));
        }
    }

    @Test
    void readsHeadingAndAccordionItems() {
        InfantAccordion model = accordion("withContent");

        assertEquals("Booking for infants (under 2 years)", model.getSectionHeading());
        assertEquals("Learn how to book babies on a flight.", model.getDescription());
        assertEquals("Expand all items", model.getExpandAllLabel());
        assertEquals("Collapse all items", model.getCollapseAllLabel());
        assertTrue(model.isShowExpandAll());
        assertEquals(2, model.getItems().size());
        assertEquals("Infants on domestic flights", model.getItems().get(0).getTitle());
        assertEquals("<p>Lap or seat.</p>", model.getItems().get(0).getDescription());
        assertFalse(model.isEmpty());
    }

    @Test
    void emptyWhenNothingIsAuthored() {
        InfantAccordion model = accordion("empty");

        assertNull(model.getSectionHeading());
        assertNull(model.getDescription());
        assertNull(model.getExpandAllLabel());
        assertNull(model.getCollapseAllLabel());
        assertTrue(model.getItems().isEmpty());
        assertFalse(model.isShowExpandAll());
        assertTrue(model.isEmpty());
    }

    @Test
    void descriptionAloneIsNotEmpty() {
        InfantAccordion model = accordion("introOnly");

        assertFalse(model.isEmpty());
        assertFalse(model.isShowExpandAll());
    }

    @Test
    void dropsBlankRowsAndKeepsPartialRows() {
        InfantAccordion model = accordion("partialItems");

        assertEquals(2, model.getItems().size());
        assertEquals("How soon can infants travel after birth?", model.getItems().get(0).getTitle());
        assertNull(model.getItems().get(0).getDescription());
        assertNull(model.getItems().get(1).getTitle());
        assertEquals("<p>Own ticket.</p>", model.getItems().get(1).getDescription());
        assertTrue(model.isShowExpandAll());
        assertFalse(model.isEmpty());
    }

    private InfantAccordion accordion(String name) {
        Resource resource = context.resourceResolver().getResource(ROOT + "/" + name);
        return adaptResource(resource, InfantAccordion.class);
    }
}
