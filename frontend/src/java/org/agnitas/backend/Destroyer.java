/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.io.File;
import java.io.FilenameFilter;
import java.util.StringTokenizer;

import org.agnitas.util.Log;

/**
 * this class is used to remove pending mailings
 */
public class Destroyer {
	/**
	 * Class to filter filenames for deletion
	 */
	private static class DestroyFilter implements FilenameFilter {
		/**
		 * the mailing ID
		 */
		private long mailingID;

		/**
		 * Constructor
		 *
		 * @param mailing_id the mailing ID to filter files for
		 */
		public DestroyFilter(long mailing_id) {
			super();
			mailingID = mailing_id;
		}

		/**
		 * If a file matches the filter
		 *
		 * @param dir  home directory of the file
		 * @param name name of the file
		 * @return true, if it should be deleted
		 */
		@Override
		public boolean accept(File dir, String name) {
			boolean st;
			StringTokenizer tok;

			st = false;
			tok = new StringTokenizer(name, "=");
			if (tok.countTokens() == 6) {
				int n;
				long mid;

				for (n = 0; n < 3; ++n) {
					tok.nextToken();
				}
				mid = Long.decode(tok.nextToken()).longValue();
				if (mid == mailingID) {
					st = true;
				}
			}
			return st;
		}
	}

	/**
	 * The mailing ID
	 */
	private long mailingID;
	/**
	 * Reference to configuration
	 */
	private Data data;

	/**
	 * Constructor
	 *
	 * @param mailing_id the mailing ID for the mailing to destroy
	 */
	public Destroyer(long mailing_id) throws Exception {
		if (mailing_id <= 0) {
			throw new Exception("Mailing_id is less or equal 0");
		}
		mailingID = mailing_id;
		data = new Data("destroyer");
	}

	/**
	 * Cleanup
	 */
	public void done() throws Exception {
		data.done();
	}

	/**
	 * Start destruction
	 *
	 * @return message string
	 */
	public String destroy() throws Exception {
		String msg;
		String path;

		msg = "Destroy:";
		path = data.targetPath();
		msg += " [" + path;
		try {
			msg += " " + doDestroy(path);
			msg += " done";
		} catch (Exception e) {
			msg += " failed: " + e.toString();
		}
		msg += "]";
		return msg;
	}

	/**
	 * Remove file(s) found in directory
	 *
	 * @param path the directory to search for
	 * @return number of files deleted
	 */
	private int doDestroy(String path) throws Exception {
		File file;
		File[] files;
		int n;

		file = new File(path);
		files = file.listFiles(new DestroyFilter(mailingID));
		for (n = 0; n < files.length; ++n) {
			if (!files[n].delete()) {
				data.logging(Log.ERROR, "destroy", "File " + files[n] + " cannot be removed");
			}
		}
		return files.length;
	}
}
