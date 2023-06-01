/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

public enum AgnTag {

    DB("agnDB"),
    IMAGE("agnIMAGE"),
    EMAIL("agnEMAIL"),
    SUBSCRIBER_COUNT("agnSUBSCRIBERCOUNT"),
    DATE("agnDATE"),
    SYS_INFO("agnSYSINFO"),
    DYN("agnDYN"),
    DVALUE("agnDVALUE"),
    TITLE("agnTITLE"),
    TITLE_FULL("agnTITLEFULL"),
    TITLE_FIRST("agnTITLEFIRST"),
    IMG_LINK("agnIMGLINK"),
    SWYN("agnSWYN"),
    SEND_DATE("agnSENDDATE"),
    GRID_PH("gridPH");

    private final String name;

    AgnTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
