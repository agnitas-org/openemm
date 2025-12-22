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
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.dao.CssDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.sass.AgnitasSassCompiler;
import com.agnitas.util.Tuple;
import com.agnitas.web.perm.annotations.Anonymous;
import de.larsgrefer.sass.embedded.SassCompilationFailedException;
import de.larsgrefer.sass.embedded.SassCompiler;
import jakarta.servlet.ServletContext;
import org.apache.bval.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import sass.embedded_protocol.EmbeddedSass.OutboundMessage.CompileResponse.CompileSuccess;

@RestController
public class AppCssController {
    
	private static final Logger logger = LogManager.getLogger(AppCssController.class);
	private static final String ETAG = "W/\"" + new Date().getTime() + "\"";
    
    private final ServletContext servletContext;
    private final ConfigService configService;
    private final CssDao cssDao;
	private final Tuple<String, String> cssBundle;

    public AppCssController(CssDao cssDao, ConfigService configService, ServletContext servletContext) {
        this.cssDao = cssDao;
        this.configService = configService;
        this.servletContext = servletContext;
		this.cssBundle = tryCompile();
    }

	/**
	 * This endpoint loads SCSS variables from the css_tbl and generates a CSS File out of the application.scss.
	 * Deletes all but most recent "dart-sass" directory after generating final CSS
	 * When CSS cannot be compiled, backup 'application.min.css' is used
     */
    @Anonymous
    @GetMapping(value = "/application.min.css.action", produces = "text/css; charset=UTF-8")
    public ResponseEntity<String> css(WebRequest webRequest) {
        if (webRequest.checkNotModified(ETAG)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.ok()
                .eTag(ETAG)
                .body(cssBundle.getFirst());
    }

	@Anonymous
	@GetMapping(value = "/css/sourcemap.action", produces = "application/json; charset=UTF-8")
	public ResponseEntity<String> cssSourceMap() {
		return ResponseEntity.ok().body(cssBundle.getSecond());
	}

	private Tuple<String, String> tryCompile() {
		try {
			CompileSuccess compileResult = compileScss();

			return Tuple.of(
					addSourceMapUrl(compileResult.getCss()),
					adjustSourceMapPaths(compileResult.getSourceMap())
			);
		} catch (Exception e) {
			logger.error("Error while compiling CSS: {}", e.getMessage(), e);
			return loadFallback();
		}
	}

	private String adjustSourceMapPaths(String sourceMap) {
		return sourceMap.replace("file://" + servletContext.getRealPath("."), "");
	}

	private String addSourceMapUrl(String css) {
		return "%s%n/*# sourceMappingURL=css/sourcemap.action */".formatted(css);
	}

	private Tuple<String, String> loadFallback() {
		try {
			return Tuple.of(
					Files.readString(Paths.get(servletContext.getRealPath("/assets/application.css"))),
					Files.readString(Paths.get(servletContext.getRealPath("/assets/application.css.map")))
			);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

    private CompileSuccess compileScss() throws SassCompilationFailedException, IOException {
        File sassExecutableTempDirPath = new File(configService.getValue(ConfigValue.ExecutableTempDirPath));
        try (SassCompiler compiler = AgnitasSassCompiler.bundled(sassExecutableTempDirPath)) {
            replaceCssParametersInFile(servletContext.getRealPath("/assets/sass/boot/variables.scss"));
            return compiler.compileFile(new File(servletContext.getRealPath("/assets/sass/application.scss")));
        } finally {
            cleanDirectory(sassExecutableTempDirPath);
        }
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
						logger.error("Exception: {}", e.getMessage(), e);
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
						logger.error("Exception: {}", e.getMessage(), e);
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
	public void replaceCssParametersInFile(String filepath) {
		Map<String, String> parameterMap = cssDao.getCssParameterData(0);
		
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
			logger.error("Exception: {}", e.getMessage(), e);
		}
		// Once everything is complete, delete old file..
		File oldFile = new File(filepath);
		oldFile.delete();
		
		// And rename tmp file's name to old file name
		File newFile = new File(tmpFileName);
		newFile.renameTo(oldFile);
	}
	
}
