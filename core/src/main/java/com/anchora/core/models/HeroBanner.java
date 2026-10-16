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
 * Hero banner for the flying-with-infants page. HTL renders these values and does not decide them.
 */
public interface HeroBanner {

    /**
     * @return authored title, or {@code null} when blank
     */
    String getTitle();

    /**
     * @return authored introduction, or {@code null} when blank
     */
    String getIntroduction();

    /**
     * Empty means the author has set neither a title nor an image.
     *
     * @return {@code true} when the component should show the editor placeholder and no content markup
     */
    boolean isEmpty();

    /**
     * @return {@code true} when a DAM asset is available to render
     */
    boolean hasImage();

    /**
     * @return image URL, or {@code null} when {@link #hasImage()} is false
     */
    String getImageSrc();

    /**
     * @return responsive candidate list, or {@code null} when a single URL is enough
     */
    String getImageSrcSet();

    /**
     * Resolved alt text. Blank when the author left it empty and the asset has no description,
     * or when the image is decorative.
     *
     * @return alt text, never {@code null}
     */
    String getImageAlt();

    /**
     * @return {@code presentation} for a decorative image, otherwise {@code null}
     */
    String getImageRole();

    /**
     * @return in-page links, never {@code null}
     */
    List<JumpLink> getJumpLinks();

    /**
     * @return authored links heading, or {@code On this page} when blank
     */
    String getLinksHeading();

    /**
     * @return id for the links heading
     */
    String getLinksHeadingId();
}
