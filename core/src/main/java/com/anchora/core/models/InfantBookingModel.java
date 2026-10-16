package com.anchora.core.models;

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

/**
 * Left-side heading and description, plus the accordion items authored in the dialog.
 */
@Model(
        adaptables = Resource.class,
        resourceType = InfantBookingModel.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InfantBookingModel {

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

    @ValueMapValue
    private String linkIcon;

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

    public String getSectionHeading() {
        return sectionHeading;
    }

    public String getDescription() {
        return description;
    }

    public String getExpandAllLabel() {
        return expandAllLabel;
    }

    public String getCollapseAllLabel() {
        return collapseAllLabel;
    }

    public String getLinkIcon() {
        return linkIcon;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(sectionHeading)
                && StringUtils.isBlank(description)
                && items.isEmpty();
    }

    public String getHeadingId() {
        return resource.getPath().replaceAll("[^A-Za-z0-9_-]", "-") + "-heading";
    }

    public boolean isShowExpandAll() {
        return !items.isEmpty();
    }

    public List<InfantBookingItem> getItems() {
        return items;
    }
}
