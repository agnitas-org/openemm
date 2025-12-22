/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.puppeteer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Optional;
import java.util.concurrent.Executors;

import jakarta.servlet.ServletContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class PuppeteerServiceManager implements SmartLifecycle {

    private static final Logger logger = LogManager.getLogger(PuppeteerServiceManager.class);
    private static final int PORT = 3000;
    public static final String PUPPETEER_SERVICE_URL = "http://localhost:" + PORT;
    private final ServletContext servletContext;
    private Process puppeteerProcess;

    public PuppeteerServiceManager(@Autowired(required = false) ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override // start Puppeteer service when the application starts
    public void start() {
        String path = getPath();
        if (path == null) {
            logger.error("Cannot start Puppeteer service due to invalid path.");
            return;
        }

        if (isPuppeteerProcessRunning()) {
            logger.info("Puppeteer service is already running. Skipping startup.");
            return;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("node", path);
            puppeteerProcess = processBuilder.start();

            serviceStreamToLogger(puppeteerProcess.getInputStream(), false);
            serviceStreamToLogger(puppeteerProcess.getErrorStream(), true);

            logger.info("Puppeteer service started successfully.");
        } catch (IOException e) {
            logger.error("Failed to start Puppeteer service - {}", e.getMessage(), e);
        }
    }

    @Override // stop Puppeteer service when the application shuts down
    public void stop() {
        if (puppeteerProcess != null && puppeteerProcess.isAlive()) {
            puppeteerProcess.destroy();
            logger.info("Puppeteer service stopped.");
        }
    }

    @Override
    public boolean isRunning() {
        return isPuppeteerProcessRunning();
    }

    public boolean isInstalled() {
        if (getPath() == null) {
            return false;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("node", "-e", "require.resolve('puppeteer')")
                    .directory(new File(servletContext.getRealPath("WEB-INF")))
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD);

            return processBuilder.start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String getPath() {
        String path = Optional.ofNullable(servletContext)
                .map(s -> s.getRealPath("WEB-INF/puppeteer/puppeteer-service.js"))
                .orElse(null);

        if (path == null || path.trim().isEmpty()) {
            logger.error("Missing path to Puppeteer service script.");
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            logger.error("Puppeteer service script does not exist at path: '{}'.", path);
            return null;
        }
        return path;
    }

    private boolean isPuppeteerProcessRunning() { // check in system processes
        if (puppeteerProcess != null && puppeteerProcess.isAlive()) {
            return true;
        }

        try (ServerSocket socket = new ServerSocket(PORT)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private void serviceStreamToLogger(InputStream inputStream, boolean isErrorStream) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.log(isErrorStream ? Level.ERROR : Level.INFO, "Puppeteer: {}", line);
                }
            } catch (IOException e) {
                if (!"Stream closed".equals(e.getMessage())) { // decrease logs pollution
                    logger.error("Error reading Puppeteer process stream.", e);
                }
            }
        });
    }

}
