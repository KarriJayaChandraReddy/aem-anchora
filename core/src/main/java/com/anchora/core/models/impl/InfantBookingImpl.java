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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.anchora.core.models.InfantBooking;
import com.anchora.core.models.InfantBookingItem;

/**
 * Reads the infant booking dialog: introduction fields and accordion items.
 */
@Model(
        adaptables = Resource.class,
        adapters = InfantBooking.class,
        resourceType = InfantBookingImpl.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InfantBookingImpl implements InfantBooking {

    public static final String RESOURCE_TYPE = "anchora/components/infantbooking";

    private static final String ITEMS = "items";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";

    @SlingObject
    private Resource resource;

    @ValueMapValue
    private String sectionHeading;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private String expandAllLabel;

    @ValueMapValue
    private String collapseAllLabel;

    private List<InfantBookingItem> items = Collections.emptyList();

    @PostConstruct
    protected void init() {
        Resource itemsResource = resource.getChild(ITEMS);
        if (itemsResource == null) {
            return;
        }
        List<InfantBookingItem> built = new ArrayList<>();
        for (Resource child : itemsResource.getChildren()) {
            ValueMap properties = child.getValueMap();
            String title = properties.get(TITLE, String.class);
            String itemDescription = properties.get(DESCRIPTION, String.class);
            if (StringUtils.isBlank(title) && StringUtils.isBlank(itemDescription)) {
                continue;
            }
            built.add(new InfantBookingItem(title, itemDescription));
        }
        items = Collections.unmodifiableList(built);
    }

    @Override
    public String getSectionHeading() {
        return sectionHeading;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getExpandAllLabel() {
        return expandAllLabel;
    }

    @Override
    public String getCollapseAllLabel() {
        return collapseAllLabel;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(sectionHeading)
                && StringUtils.isBlank(description)
                && items.isEmpty();
    }

    @Override
    public boolean isShowExpandAll() {
        return !items.isEmpty();
    }

    @Override
    public List<InfantBookingItem> getItems() {
        return items;
    }
}
