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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

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

        if (puppeteerProcess != null && puppeteerProcess.isAlive()) {
            logger.info("Puppeteer service is already running. Skipping startup.");
            return;
        }

        killOrphanedPuppeteerProcesses();

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

    /**
     * Kills Puppeteer-related processes left over from a previous JVM instance that was
     * terminated without a graceful Spring shutdown (e.g. SIGKILL or OOM killer).
     * <p>
     * Two categories of orphans are handled:
     * <ul>
     *   <li>The Node.js process running {@code puppeteer-service.js} together with any
     *       Chrome processes that are still its live descendants.</li>
     *   <li>Detached Chrome processes from puppeteer's bundled Chrome whose Node.js parent
     *       was itself already killed by SIGKILL. Identified by the installation path
     *       {@code puppeteer/chrome} (e.g. {@code ~/.cache/puppeteer/chrome/...}).</li>
     * </ul>
     * Only processes owned by the current OS user are considered, preventing accidental
     * interference with other users' processes.
     */
    private void killOrphanedPuppeteerProcesses() {
        Optional<String> currentUserOpt = ProcessHandle.current().info().user();
        if (currentUserOpt.isEmpty()) {
            logger.warn("Cannot determine current OS user; skipping orphaned Puppeteer process cleanup.");
            return;
        }

        String currentUser = currentUserOpt.get();

        List<ProcessHandle> orphanedNodeProcesses = findOwnedProcesses(
                currentUser,
                info -> info.commandLine()
                        .map(cmd -> cmd.contains("puppeteer-service.js"))
                        .orElse(false)
        );

        List<ProcessHandle> orphanedChromeProcesses = findOwnedProcesses(
                currentUser,
                info -> info.command()
                        .map(cmd -> cmd.contains("puppeteer/chrome"))
                        .orElse(false)
        );

        orphanedNodeProcesses.forEach(ph -> killWithDescendants(ph, "Puppeteer node"));
        orphanedChromeProcesses.forEach(ph -> killWithDescendants(ph, "puppeteer Chrome"));

        if (!orphanedNodeProcesses.isEmpty() || !orphanedChromeProcesses.isEmpty()) {
            logger.info("Killed {} orphaned Puppeteer node process(es) and {} orphaned Chrome process(es).",
                    orphanedNodeProcesses.size(), orphanedChromeProcesses.size());
        }
    }

    private List<ProcessHandle> findOwnedProcesses(String user, Predicate<ProcessHandle.Info> predicate) {
        return ProcessHandle.allProcesses()
                .filter(ph -> ph.info().user().map(user::equals).orElse(false))
                .filter(ph -> predicate.test(ph.info()))
                .toList();
    }

    private void killWithDescendants(ProcessHandle ph, String label) {
        logger.warn("Killing orphaned {} process (PID: {}).", label, ph.pid());
        ph.descendants().forEach(child -> {
            logger.warn("Killing descendant process (PID: {}).", child.pid());
            child.destroyForcibly();
        });
        ph.destroyForcibly();
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
