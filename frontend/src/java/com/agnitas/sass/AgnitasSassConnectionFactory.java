/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.sass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.protobuf.ByteString;

import de.larsgrefer.sass.embedded.SassCompilerFactory;
import de.larsgrefer.sass.embedded.connection.ProcessConnection;
import de.larsgrefer.sass.embedded.util.IOUtils;

public class AgnitasSassConnectionFactory {
    /**
     * Path of the extracted compiler executable.
     */
    private static File bundledDartExec;

    public static ProcessConnection bundled(File sassExecutableTempDirPath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(getBundledDartExec(sassExecutableTempDirPath).getAbsolutePath());

        return new ProcessConnection(processBuilder);
    }

    static File getBundledDartExec(File sassExecutableTempDirPath) throws IOException {
        if (bundledDartExec == null) {
            extractBundled(sassExecutableTempDirPath);
        }
        return bundledDartExec;
    }

    synchronized static void extractBundled(File sassExecutableTempDirPath) throws IOException {
        String resourcePath = getBundledCompilerDistPath();

        URL dist = SassCompilerFactory.class.getResource(resourcePath);

        if (dist == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        Path tempDirectory;
        if (sassExecutableTempDirPath != null) {
        	tempDirectory = Files.createTempDirectory(sassExecutableTempDirPath.toPath(), "dart-sass");
        } else {
        	tempDirectory = Files.createTempDirectory("dart-sass");
        }

        try {
            IOUtils.extract(dist, tempDirectory);
        } catch (IOException e) {
            throw new IOException(String.format("Failed to extract %s into %s", dist, tempDirectory), e);
        }

        File execDir = tempDirectory.resolve("sass_embedded").toFile();

        File[] execFile = execDir.listFiles(pathname -> pathname.isFile() && pathname.getName().startsWith("dart-sass-embedded"));

        if (execFile == null || execFile.length != 1) {
            throw new IllegalStateException("No (unique) executable file found in " + execDir);
        } else {
            bundledDartExec = execFile[0];
        }

        bundledDartExec.setWritable(false);
        bundledDartExec.setExecutable(true, true);
    }

    private static String getBundledCompilerDistPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        String classifier;
        String archiveExtension = "tar.gz";

        if (osName.contains("mac")) {
            if (osArch.equals("aarch64") || osArch.contains("arm") || isRunningOnRosetta2()) {
                classifier = "macos-arm64";
            } else {
                classifier = "macos-x64";
            }
        } else if (osName.contains("win")) {
            archiveExtension = "zip";
            classifier = osArch.contains("64") ? "windows-x64" : "windows-ia32";
        } else {
            if (osArch.equals("aarch64") || osArch.equals("arm64")) {
                classifier = "linux-arm64";
            } else if (osArch.contains("arm")) {
                classifier = "linux-arm";
            } else if (osArch.contains("64")) {
                classifier = "linux-x64";
            } else {
                classifier = "linux-ia32";
            }
        }

        return String.format("/de/larsgrefer/sass/embedded/sass_embedded-%s.%s", classifier, archiveExtension);
    }

    private static boolean isRunningOnRosetta2() {
        try {
            Process sysctl = Runtime.getRuntime().exec("sysctl -in sysctl.proc_translated");
            ByteString stdOut;
            try (InputStream in = sysctl.getInputStream()) {
                stdOut = ByteString.readFrom(in);
            }
            if (sysctl.exitValue() == 0 && stdOut.toStringUtf8().equals("1\n")) {
                return true;
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            // do nothing
        }
        return false;
    }
}
