/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.agnitas.util.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * Utility class dealing with java.lang.Process.
 */
public class ProcessUtils {
	
	/** log4j logger. */
	private static final transient Logger logger = Logger.getLogger( ProcessUtils.class);
	
	/**
	 * Writes the output of the given process to log4j.
	 * 
	 * @param process			process to log
	 * @param logger			logger to use
	 * @param priority			priority of log message
	 * @param outputMessage		message inserted before stdout output
	 * @param errorMessage		message inserted before stderr output
	 */
	public static void logProcessOutput( Process process, Logger logger, Priority priority, String outputMessage, String errorMessage) {
		logStream( process.getInputStream(), logger, priority, outputMessage);
		logStream( process.getErrorStream(), logger, priority, errorMessage);
	}
	
	/**
	 * Write content of stream to log4j.
	 * 
	 * @param stream 		stream to log
	 * @param logger		logger to use
	 * @param priority		priority of log message
	 * @param message		message before stream output
	 */
	public static void logStream( InputStream stream, Logger logger, Priority priority, String message) {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
			FileUtils.streamToStream(stream, stream.available(), byteStream);
			
			logger.log( priority, message + ":\n" + byteStream.toString());
		} catch( IOException e) {
			ProcessUtils.logger.warn( "Unable to log stream", e);
		}
	}
}
