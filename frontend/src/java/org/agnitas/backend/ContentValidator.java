/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package	org.agnitas.backend;

import	org.agnitas.util.Log;

import	bsh.EvalError;
import	bsh.Interpreter;

/**
 * This class provides a validation option for all textual content
 * blocks. The validator has to be written in Beanshell and these
 * parameter are pased for each validation run:
 * - data: A reference to the global configuration org.agnitas.backend.Data instance
 * - block: The content itself as a org.agnitas.backend.BlockData
 * - area: Either "component" if part of the template or "content" if a dynamic text block
 * - name: Either the name of the template (for "component") or the name of the text block (for "content")
 * - content: The content itself as a string (block.content)
 * 
 * The Beanshell Script should return null on success or a string on error.
 * This string should describe the error as detailed as possible.
 * 
 */
public class ContentValidator {
	private Data	data;
	private String	name;
	private String	code;

	/**
	 * Constructor
	 * 
	 * @param nData a reference to the global configuration
	 * @param nName the name of this validation code
	 * @param nCode the code of the Beanshell Script itself
	 */
	public ContentValidator (Data nData, String nName, String nCode) {
		data = nData;
		name = nName;
		code = nCode;
	}
	/**
	 * Validate a text block and throw a RuntimeException, if
	 * validation fails.
	 * 
	 * @param contentArea either "component" or "content"
	 * @param contentName the name of the block
	 * @param conentID    the unique ID of the content block (in combination with contentArea)
	 * @param content     the content itself
	 * @throws RuntimeException
	 */
	public void validate (String contentArea, String contentName, long contentID, BlockData content) {
		Interpreter	bsh = new Interpreter ();
		Object		rc;

		try {
			String	id = contentArea + ":" + contentName + " (" + contentID + ")";

			bsh.set ("data", data);
			bsh.set ("block", content);
			bsh.set ("area", contentArea);
			bsh.set ("name", contentName);
			bsh.set ("content", content != null && content.content != null ? content.content : "");
			rc = bsh.eval (code);
			if (rc != null) {
				String	msg = name + ": validation failed for " + id + ": " + rc.toString ();
				data.logging (Log.ERROR, "contentvalidator", msg);
				throw new RuntimeException ("[content validation]: " + msg);
			} else {
				data.logging (Log.DEBUG, "contentvalidator", name + ": validation passed for " + id);
			}
		} catch (EvalError e) {
			data.logging (Log.ERROR, "contentvalidator", name + ":failed to evaluate: " + e.toString (), e);
		}
	}
}
