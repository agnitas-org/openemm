/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import com.agnitas.preview.Page;
import com.agnitas.preview.PageImpl;
import com.agnitas.util.Log;
import com.agnitas.util.ParameterParser;

/**
 * This class provides the method that can be called via XML-RPC
 * as a replacement for the no more supported RMI version
 */
public class Merger {
	/**
	 * Logging interface
	 */
	private Log log;

	/**
	 * Constructor
	 */
	public Merger() {
		log = new Log("merger", Log.DEBUG, 0);
	}

	/**
	 * Controls the behaviour of the merger process; see the
	 * Runner class for valid values of the parametere
	 *
	 * @param command the command to be executed
	 * @param option  the option for this command
	 * @return a status string
	 */
	public String remote_control(String command, String option) {
		Runner run = new Runner(log, command, option);

		run.start();
		return run.result();
	}
	
	public String preview (String parameter) {
		Map <String, Object>	opts = new HashMap <> ();
		String			result;

		try {
			for (Map.Entry <String, String> entry : (new ParameterParser (parameter)).parse ().entrySet ()) {
				opts.put (entry.getKey (), entry.getValue ());
			}
		} catch (Exception e) {
			return "Unparsable option \"" + parameter + "\": " + e.toString ();
		}
		MailgunImpl mailout = null;
		try {
			mailout = new MailgunImpl();
			Page output = new PageImpl ();
			opts.put ("preview-output", output);
			mailout.initialize ("preview:" + opts.get ("mailing-id"), opts);
			mailout.prepare (opts);
			mailout.execute (opts);
			
			ByteArrayOutputStream	out = new ByteArrayOutputStream (8192);
			try (JsonGenerator gen = (new JsonFactory ()).createGenerator (out)) {			
				gen.writeStartObject ();
				gen.writeFieldName ("subject");
				gen.writeString (output.getHeaderField ("subject"));
				gen.writeFieldName ("from");
				gen.writeString (output.getHeaderField ("from"));
				gen.writeFieldName ("header");
				gen.writeString (output.getHeader ());
				gen.writeFieldName ("text");
				gen.writeString (output.getText ());
				gen.writeFieldName ("html");
				gen.writeString (output.getHTML ());
				gen.writeEndObject ();
				gen.flush ();
			}
			result = out.toString ();
		} catch (Exception e) {
			result = "Failed to create preview using \"" + parameter + "\": " + e.toString ();
		} finally {
			try {
				mailout.done ();
			} catch (Exception e) {
				result = "Failed to deinitialize for \"" + parameter + "\": " + e.toString ();
			}
		}
		return result;
	}
}
