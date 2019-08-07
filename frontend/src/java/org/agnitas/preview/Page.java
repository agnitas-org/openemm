/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.Map;

public interface Page {
	public void addContent(String key, String value);

	public void setError(String msg);

	public String getError();

	public Map<String, Object> compatibilityRepresentation();

	public byte[] getPartByID(String id, String charset, boolean escape);

	public byte[] getHeaderPart(String charset, boolean escape);

	public byte[] getHeaderPart(String charset);

	public byte[] getTextPart(String charset, boolean escape);

	public byte[] getTextPart(String charset);

	public byte[] getHTMLPart(String charset, boolean escape);

	public byte[] getHTMLPart(String charset);

	public String getByID(String id, boolean escape);

	public String getStrippedByID(String id, boolean escape);

	public String getHeader(boolean escape);

	public String getHeader();

	public String getText(boolean escape);

	public String getText();

	public String getHTML(boolean escape);

	public String getHTML();

	public String getStrippedHTML(boolean escape);

	public String getStrippedHTML();

	public String[] getIDs();

	public String[] getAttachmentNames();

	public byte[] getAttachment(String name);

	public String[] getHeaderFields(String field);

	public String getHeaderField(String field, boolean escape);

	public String getHeaderField(String field);
}
