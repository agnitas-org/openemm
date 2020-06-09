/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class TarGzUtilities {
	public static void decompress(File zipFileToDecompress, File decompressToPath) throws Exception {
		if (!zipFileToDecompress.exists()) {
			throw new Exception("Source path does not exist: " + decompressToPath.getAbsolutePath());
		} else if (!zipFileToDecompress.isFile()) {
			throw new Exception("Source path is not a file: " + decompressToPath.getAbsolutePath());
		} else if (decompressToPath.exists()) {
			throw new Exception("Destination path already exists: " + decompressToPath.getAbsolutePath());
		} else {
	        try (TarArchiveInputStream fin = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(zipFileToDecompress))))) {
				decompressToPath.mkdirs();
				
	            TarArchiveEntry entry;
	            while ((entry = fin.getNextTarEntry()) != null) {
	                if (entry.isDirectory()) {
	                    continue;
	                }
	                String entryFilePath = entry.getName();
	                entryFilePath = entryFilePath.replace("\\", "/");
	                if (entryFilePath.startsWith("/") || entryFilePath.startsWith("../") || entryFilePath.endsWith("/..") || entryFilePath.contains("/../")) {
	                    throw new Exception("Traversal error in tar gz file: " + zipFileToDecompress.getAbsolutePath());
	                }
	                File currentfile = new File(decompressToPath, entryFilePath);
	                if (!currentfile.getCanonicalPath().startsWith(decompressToPath.getCanonicalPath())) {
	                    throw new Exception("Traversal error in tar gz file: " + zipFileToDecompress.getAbsolutePath() + "/");
	                }
	                File parent = currentfile.getParentFile();
	                if (!parent.exists()) {
	                    parent.mkdirs();
	                }
	                IOUtils.copy(fin, new FileOutputStream(currentfile));
	            }
	        } catch (Exception e) {
	        	try {
	        		if (decompressToPath.exists()) {
	        			FileUtils.deleteDirectory(decompressToPath);
	        		}
				} catch (Exception e2) {
					// do nothing
					e2.printStackTrace();
				}
	        
				throw new Exception("Cannot decompress '" + zipFileToDecompress + "'", e);
			}
		}
	}
}
