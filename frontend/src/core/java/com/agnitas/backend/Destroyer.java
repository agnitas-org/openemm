/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;

import com.agnitas.util.Log;

/**
 * this class is used to remove pending mailings
 */
public class Destroyer implements Closeable {
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
			String[]	parts = name.split ("=");
			
			if (parts.length == 6) {
				String[]	mailingIDParts = parts[3].split ("[^0-9]+");
				
				if (mailingIDParts.length > 0) {
					long	filenameMailingID = Long.parseLong (mailingIDParts[mailingIDParts.length - 1]);
					
					if (filenameMailingID == mailingID)
						return true;
				}
			}
			return false;
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
		data.setup (null, null);
	}

	/**
	 * Cleanup
	 */
	@Override
	public void close() {
		try {
			data.done();
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Start destruction
	 *
	 * @return message string
	 */
	public String destroy() {
		String msg;
		String path;
		String destination;

		msg = "Destroy:";
		path = data.targetPath();
		destination = data.mailing.outputDirectory("deleted");
		msg += " [" + path + " -> " + destination;
		try {
			msg += " " + doDestroy(path, destination);
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
	private int doDestroy(String path, String destination) {
		File file;
		File[] files;
		int n;

		file = new File(path);
		files = file.listFiles(new DestroyFilter(mailingID));
		for (n = 0; n < files.length; ++n) {
			File current = files[n];
			File target = new File(destination, current.getName ());
			
			if (! current.renameTo (target)) {
				data.logging(Log.ERROR, "destroy", "File " + current + " cannot be moved to " + target + ", try to remove it");
				if (!current.delete()) {
					data.logging(Log.ERROR, "destroy", "File " + current + " cannot be removed");
				}
			}
		}
		return files.length;
	}
}
