/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.bval.util.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.CssDao;
import com.agnitas.sass.AgnitasSassCompiler;

import de.larsgrefer.sass.embedded.SassCompiler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sass.embedded_protocol.EmbeddedSass.OutboundMessage.CompileResponse.CompileSuccess;

public class CssServlet extends HttpServlet {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(CssServlet.class);
	
	/** Cache of generated CSS */
	private static String APPLICATION_MIN_CSS_CACHE = null;
	private static String REDESIGNED_APPLICATION_MIN_CSS_CACHE = null;

	/** Serial version UID. */
	private static final long serialVersionUID = -595094416663851734L;

	private static final String CHARSET = "UTF-8";

	private static String CSS_CACHE_TIMESTAMP = null;
	
	protected CssDao cssDao;
	
	protected ConfigService configService;
	private ApplicationContext applicationContext;
	
	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	/**
	 * This servlet loads SCSS variables from the css_tbl and generates a CSS File out of the application.scss.
	 * The CSS is cached. When the CSS cannot be found, a new one is generated.
	 * The caching is done for performance reasons.
	 * Deletes all but most recent "dart-sass" directory after generating final CSS
	 * When CSS cannot be compiled, backup 'application.min.css' is used
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean useRedesignedVersion = isRedesignedVersionAvailable(request);
 		String cachedCss = getCssCache(useRedesignedVersion);
		File sassExecutableTempDirPath = new File(getConfigService().getValue(ConfigValue.ExecutableTempDirPath));

        if (cachedCss == null) {
			try (SassCompiler sassCompiler = AgnitasSassCompiler.bundled(sassExecutableTempDirPath)) {
				replaceCssParametersInFile(request.getServletContext().getRealPath("/assets/sass/boot/variables.scss"), 0);
				
				//compile CSS from given SCSS File
				String scssPath = useRedesignedVersion ? "/assets/sass_redesign/application.scss" : "/assets/sass/application.scss";
				CompileSuccess compileSuccess = sassCompiler.compileFile(new File(request.getServletContext().getRealPath(scssPath)));
				
				//get compiled CSS
				String css = compileSuccess.getCss();

				cachedCss = cacheCss(minifyString(css), useRedesignedVersion);
			} catch (Exception e) {
				//use backup css if exception occurs
				cachedCss = cacheCss(Files.readString(Paths.get(request.getServletContext().getRealPath("/assets/application.min.css"))), useRedesignedVersion);
				logger.error("Exception: " + e.getMessage(), e);
			} finally {
				cleanDirectory(sassExecutableTempDirPath);
			}
		}
		// build filepath (outFileName + .css) and return it.
		response.setCharacterEncoding(CHARSET);
		if (StringUtils.isBlank(CSS_CACHE_TIMESTAMP)) {
			CSS_CACHE_TIMESTAMP = Long.toString(new Date().getTime());
		}
		response.setHeader("etag", "W/\"" + CSS_CACHE_TIMESTAMP + "\"");
		response.setContentType("text/css");
		PrintWriter writer = response.getWriter();
		writer.print(cachedCss);
	}

	private boolean isRedesignedVersionAvailable(HttpServletRequest request) {
		return BooleanUtils.toBoolean(request.getParameter("redesigned"));
	}

	private String getCssCache(boolean redesigned) {
		if (redesigned) {
			return REDESIGNED_APPLICATION_MIN_CSS_CACHE;
		}

		return APPLICATION_MIN_CSS_CACHE;
	}

	private String cacheCss(String css, boolean redesigned) {
		if (redesigned) {
			REDESIGNED_APPLICATION_MIN_CSS_CACHE = css;
		} else {
			APPLICATION_MIN_CSS_CACHE = css;
		}

		return css;
	}
	
	/**
	 * Deletes all generated "dart-sass" directory but last modified
	 */
	private void cleanDirectory(File dir) {
		long timestamp = 0;
		String lastFileName = "";
		List<String> toDelete = new ArrayList<>();
		
		for (File file: dir.listFiles()) {
			if (file.getName().startsWith("dart-sass")) {
				if (timestamp < file.lastModified()) {
					if (StringUtils.isNotBlank(lastFileName)) { 
						toDelete.add(lastFileName);
					}
					timestamp = file.lastModified();
					lastFileName = file.getName();
				} else {
					try {
						deleteDirectoryRecursion(file.toPath());
					} catch (IOException e) {
						logger.error("Exception: " + e.getMessage(), e);
					}
				}
			}
		}
		
		for (File file: dir.listFiles()) {
			for (String toDeleteFileName: toDelete) {
				if (file.getName().equals(toDeleteFileName)) {
					try {
						deleteDirectoryRecursion(file.toPath());
					} catch (IOException e) {
						logger.error("Exception: " + e.getMessage(), e);
					}
				}
			}
		}
		
	}
	
	/**
	 * Recursively deletes a file and all of its containing files
	 */
	private void deleteDirectoryRecursion(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectoryRecursion(entry);
				}
			}
		}
		Files.delete(path);
	}
	
	/**
	 * Replaces SCSS variables in given Filepath
	 */
	public void replaceCssParametersInFile(String filepath, int companyID) {
		Map<String, String> parameterMap = getCssDao().getCssParameterData(companyID);
		
		String tmpFileName = filepath + ".tmp";

		try (BufferedReader br = new BufferedReader(new FileReader(filepath));
				BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFileName))){
			String line;
			
			while ((line = br.readLine()) != null) {
				for (Map.Entry<String,String> entry : parameterMap.entrySet()) {
	 				if (line.contains("$" + entry.getKey() + ":")) {
	 					line = "$" + entry.getKey() + ": " + entry.getValue() + ";";
	 				}
				}
				bw.write(line + "\n");
			}
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage(), e);
		}
		// Once everything is complete, delete old file..
		File oldFile = new File(filepath);
		oldFile.delete();
		
		// And rename tmp file's name to old file name
		File newFile = new File(tmpFileName);
		newFile.renameTo(oldFile);
	}
	
	/**
	 * Minifies a given CSS-String and returns it.
	 * The RegEx removes excess whitespaces and comments
	 */
	public String minifyString(String str) {
		// Remove comments and whitespace
		str = str.replaceAll( "(?s)\\s|/\\*.*?\\*/" , " " );
		
		return str.trim();
	}
	
	public void setCssDao(CssDao cssDao) {
		this.cssDao = cssDao;
	}

	private CssDao getCssDao() {
		if (cssDao == null) {
			cssDao = getApplicationContext().getBean("CssDao", CssDao.class);
		}
		return cssDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	private ConfigService getConfigService() {
		if (configService == null) {
			configService = (ConfigService) getApplicationContext().getBean("ConfigService");
		}
		return configService;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		}
		return applicationContext;
	}
}
