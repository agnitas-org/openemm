/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_BIG_TEXT;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_COLOR;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_IMAGE_SRC;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_LINK_HREF;
import static com.agnitas.emm.grid.grid.util.PlaceholderUtils.PLACEHOLDER_DEFAULT_CONTENT_TEXT;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a type of custom (user-defined) placeholder (see {@link com.agnitas.emm.grid.grid.beans.GridDivMarkupSpanType#Placeholder}).
 * Attention: these types are persisted in the database as numbers so never change an assigned id values!
 */
public enum GridCustomPlaceholderType {

    Label(0, PLACEHOLDER_DEFAULT_CONTENT_TEXT),
    Text(1, PLACEHOLDER_DEFAULT_CONTENT_BIG_TEXT),
    Image(2, PLACEHOLDER_DEFAULT_CONTENT_IMAGE_SRC),
    Link(3, PLACEHOLDER_DEFAULT_CONTENT_LINK_HREF),
    ImageLink(4, PLACEHOLDER_DEFAULT_CONTENT_IMAGE_SRC),
    Color(5, PLACEHOLDER_DEFAULT_CONTENT_COLOR),
    Select(6, ""),
    Check(7, ""),
    Multi(8, ""),
    ImageAlt(9, "", Image),
    ImageSource(10, "", Image);

    private final int id;
    private final String stub;
    private final GridCustomPlaceholderType valueSource;

    GridCustomPlaceholderType(int id, String stub) {
        this(id, stub, null);
    }

    GridCustomPlaceholderType(int id, String stub, GridCustomPlaceholderType valueSource) {
        this.id = id;
        this.stub = stub;
        this.valueSource = valueSource;
    }

    public static GridCustomPlaceholderType getById(int id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid PlaceholderType id: " + id));
    }

    public static Optional<GridCustomPlaceholderType> findById(int id) {
        return Stream.of(GridCustomPlaceholderType.values())
                .filter(t -> t.getId() == id)
                .findAny();
    }

    public static Optional<GridCustomPlaceholderType> findByName(String name) {
        return Stream.of(GridCustomPlaceholderType.values())
                .filter(type -> type.name().equalsIgnoreCase(name))
                .findAny();
    }

    public static GridCustomPlaceholderType getByName(String name) {
        return findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid PlaceholderType name: " + name));
    }

    public static boolean isDependent(int id) {
        return findById(id)
                .map(GridCustomPlaceholderType::getValueSource)
                .isPresent();
    }

    public static Set<GridCustomPlaceholderType> getDependents() {
        return Stream.of(GridCustomPlaceholderType.values())
                .filter(type -> isDependent(type.getId()))
                .collect(Collectors.toSet());
    }

    public int getId() {
        return id;
    }

    public String getStub() {
        return stub;
    }

    public GridCustomPlaceholderType getValueSource() {
        return valueSource;
    }

}
