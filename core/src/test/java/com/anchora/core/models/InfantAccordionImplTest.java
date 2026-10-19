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

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.anchora.core.models.impl.InfantAccordionImpl;
import com.anchora.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class InfantAccordionImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(InfantAccordionImpl.class);
        context.create().resource("/apps/anchora/components/infantaccordion");
        context.create().page("/content/anchora");
        Resource component = context.create().resource("/content/anchora/jcr:content/root/infantaccordion",
                "sling:resourceType", InfantAccordionImpl.RESOURCE_TYPE,
                "sectionHeading", "Booking for infants (under 2 years)",
                "description", "Learn how to book babies on a flight.",
                "expandAllLabel", "Expand all items",
                "collapseAllLabel", "Collapse all items");
        Resource items = context.create().resource(component, "items");
        context.create().resource(items, "item0",
                "title", "Infants on domestic flights",
                "description", "<p>Lap or seat.</p>");
        context.create().resource(items, "item1",
                "title", "Infants on international flights",
                "description", "<p>Own ticket.</p>");
        context.currentResource(component);
    }

    @Test
    void readsHeadingAndAccordionItems() {
        InfantAccordion model = context.currentResource().adaptTo(InfantAccordion.class);

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
        InfantAccordion model = accordion("/content/anchora/jcr:content/root/empty-accordion");

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
        InfantAccordion model = accordion("/content/anchora/jcr:content/root/intro-only",
                "description", "Learn how to book babies on a flight.");

        assertFalse(model.isEmpty());
        assertFalse(model.isShowExpandAll());
    }

    @Test
    void dropsBlankRowsAndKeepsPartialRows() {
        Resource component = accordionResource("/content/anchora/jcr:content/root/partial-items");
        Resource items = context.create().resource(component, "items");
        context.create().resource(items, "blank", "title", "  ", "description", "");
        context.create().resource(items, "titleOnly", "title", "How soon can infants travel after birth?");
        context.create().resource(items, "bodyOnly", "description", "<p>Own ticket.</p>");

        InfantAccordion model = component.adaptTo(InfantAccordion.class);

        assertEquals(2, model.getItems().size());
        assertEquals("How soon can infants travel after birth?", model.getItems().get(0).getTitle());
        assertNull(model.getItems().get(0).getDescription());
        assertNull(model.getItems().get(1).getTitle());
        assertEquals("<p>Own ticket.</p>", model.getItems().get(1).getDescription());
        assertTrue(model.isShowExpandAll());
        assertFalse(model.isEmpty());
    }

    private InfantAccordion accordion(String path, Object... properties) {
        return accordionResource(path, properties).adaptTo(InfantAccordion.class);
    }

    private Resource accordionResource(String path, Object... properties) {
        Map<String, Object> values = new HashMap<>();
        values.put("sling:resourceType", InfantAccordionImpl.RESOURCE_TYPE);
        for (int i = 0; i < properties.length; i += 2) {
            values.put(String.valueOf(properties[i]), properties[i + 1]);
        }
        return context.create().resource(path, values);
    }
}
