/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

public class JavaMailAttachment {
    private final String name;
    private final byte[] data;
    private final String mimeType;

    public JavaMailAttachment(String name, byte[] data, String mimeType) {
        this.name = name;
        this.data = data;
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public String getMimeType() {
        return mimeType;
    }
}
