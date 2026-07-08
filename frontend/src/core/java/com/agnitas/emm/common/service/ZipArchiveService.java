/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common.service;

import com.agnitas.emm.common.exceptions.ZipArchiveException;
import com.agnitas.util.Tuple;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;

@Service
public interface ZipArchiveService<T> {

    /**
     * Creates a zip archive from a collection of data containers.
     *
     * @param items          the collection of data containers to be processed into zip entries
     * @param fileNameSuffix the suffix to be added to the generated zip file name
     * @param dataFunction   a function that takes an item of type {@code T} and returns a tuple containing the file name and its data as a byte array
     * @return the created zip file
     * @throws ZipArchiveException if the zip archive cannot be created or if an error occurs during the process
     */
    File createZipArchive(Collection<T> items, String fileNameSuffix, Function<T, Tuple<String, byte[]>> dataFunction);

}
