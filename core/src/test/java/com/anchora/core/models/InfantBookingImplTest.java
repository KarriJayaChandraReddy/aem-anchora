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

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.anchora.core.models.impl.InfantBookingImpl;
import com.anchora.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class InfantBookingImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(InfantBookingImpl.class);
        context.create().resource("/apps/anchora/components/infantbooking");
        context.create().page("/content/anchora");
        Resource component = context.create().resource("/content/anchora/jcr:content/root/infantbooking",
                "sling:resourceType", InfantBookingImpl.RESOURCE_TYPE,
                "sectionHeading", "Booking for infants (under 2 years)",
                "description", "Learn how to book babies on a flight.");
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
        InfantBooking model = context.currentResource().adaptTo(InfantBooking.class);

        assertEquals("Booking for infants (under 2 years)", model.getSectionHeading());
        assertEquals("Learn how to book babies on a flight.", model.getDescription());
        assertTrue(model.isShowExpandAll());
        assertEquals(2, model.getItems().size());
        assertEquals("Infants on domestic flights", model.getItems().get(0).getTitle());
        assertEquals("<p>Lap or seat.</p>", model.getItems().get(0).getDescription());
        assertFalse(model.isEmpty());
    }
}
