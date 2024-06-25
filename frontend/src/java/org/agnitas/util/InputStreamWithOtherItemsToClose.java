/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamWithOtherItemsToClose extends InputStream {
	private InputStream baseInputStream = null;
	private String name = null;
	private Closeable[] otherItemsToClose = null;

	public InputStreamWithOtherItemsToClose(final InputStream baseInputStream, final String name, final Closeable... otherItemsToClose) {
		this.baseInputStream = new BufferedInputStream(baseInputStream);
		this.name = name;
		this.otherItemsToClose = otherItemsToClose;
	}

	public InputStreamWithOtherItemsToClose(final InputStream baseInputStream, final Closeable... otherItemsToClose) {
		this.baseInputStream = new BufferedInputStream(baseInputStream);
		this.otherItemsToClose = otherItemsToClose;
	}

	public String getName() {
		return name;
	}

	@Override
	public int read() throws IOException {
		return baseInputStream.read();
	}

	@Override
	public void close() throws IOException {
		baseInputStream.close();
		if (otherItemsToClose != null) {
			for (final Closeable otherItemToClose : otherItemsToClose) {
				if (otherItemToClose != null) {
					otherItemToClose.close();
				}
			}
		}
	}
}
