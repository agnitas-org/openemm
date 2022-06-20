/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.Map;

public interface Page {
	void addContent(String key, String value);

	void setError(String msg);

	String getError();

	Map<String, Object> compatibilityRepresentation();

	byte[] getPartByID(String id, String charset, boolean escape);

	byte[] getHeaderPart(String charset, boolean escape);

	byte[] getHeaderPart(String charset);

	byte[] getTextPart(String charset, boolean escape);

	byte[] getTextPart(String charset);

	byte[] getHTMLPart(String charset, boolean escape);

	byte[] getHTMLPart(String charset);

	String getByID(String id, boolean escape);

	String getStrippedByID(String id, boolean escape);

	String getHeader(boolean escape);

	String getHeader();

	String getText(boolean escape);

	String getText();

	String getHTML(boolean escape);

	String getHTML();

	String getSMS(boolean escape);

	String getSMS();

	String getStrippedHTML(boolean escape);

	String getStrippedHTML();

	String[] getIDs();

	String[] getAttachmentNames();

	byte[] getAttachment(String name);

	String[] getHeaderFields(String field);

	String getHeaderField(String field, boolean escape);

	String getHeaderField(String field);
}
