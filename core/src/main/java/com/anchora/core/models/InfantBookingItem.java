package com.anchora.core.models;

/**
 * One accordion question and answer.
 */
public class InfantBookingItem {

    private final String title;
    private final String description;

    InfantBookingItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
