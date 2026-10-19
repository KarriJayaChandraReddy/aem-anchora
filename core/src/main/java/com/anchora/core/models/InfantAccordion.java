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

import java.util.List;

/**
 * Infant Accordion section: a heading and introduction beside an accordion.
 * HTL renders these values and does not decide them.
 */
public interface InfantAccordion {

    /**
     * @return authored section heading, or {@code null} when blank
     */
    String getSectionHeading();

    /**
     * @return authored introduction HTML, or {@code null} when blank
     */
    String getDescription();

    /**
     * @return label for the control that opens every accordion item, or {@code null} when blank
     */
    String getExpandAllLabel();

    /**
     * @return label for the control that closes every accordion item, or {@code null} when blank
     */
    String getCollapseAllLabel();

    /**
     * Empty means the author has set neither a heading, an introduction, nor an accordion item.
     *
     * @return {@code true} when the component should show the editor placeholder and no content markup
     */
    boolean isEmpty();

    /**
     * @return {@code true} when at least one accordion item is present
     */
    boolean isShowExpandAll();

    /**
     * @return accordion items, never {@code null}
     */
    List<InfantAccordionItem> getItems();
}
