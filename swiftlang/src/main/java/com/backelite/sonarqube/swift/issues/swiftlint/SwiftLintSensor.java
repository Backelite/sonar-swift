/**
 * Swift SonarQube Plugin - Swift module - Enables analysis of Swift and Objective-C projects into SonarQube.
 * Copyright © 2015 Backelite (${email})
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.backelite.sonarqube.swift.issues.swiftlint;

import com.backelite.sonarqube.commons.Constants;
import com.backelite.sonarqube.swift.lang.core.Swift;
import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;

import java.io.File;


public class SwiftLintSensor implements Sensor {

    public static final String REPORT_PATH_KEY = Constants.PROPERTY_PREFIX + ".swiftlint.report";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/*swiftlint.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftLintSensor.class);

    private final Settings conf;
    private final FileSystem fileSystem;

    public SwiftLintSensor(final FileSystem fileSystem, final Settings config) {
        this.conf = config;
        this.fileSystem = fileSystem;
    }

    private void parseReportIn(final String baseDir, final SwiftLintReportParser parser) {

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{reportPath()});
        scanner.setBasedir(baseDir);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        for (String filename : files) {
            LOGGER.info("Processing SwiftLint report {}", filename);
            parser.parseReport(new File(filename));
        }

    }

    private String reportPath() {
        String reportPath = conf.getString(REPORT_PATH_KEY);
        if (reportPath == null) {
            reportPath = DEFAULT_REPORT_PATH;
        }
        return reportPath;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .name("SwiftLint")
                .onlyOnLanguage(Swift.KEY)
                .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(org.sonar.api.batch.sensor.SensorContext context) {

        final String projectBaseDir = fileSystem.baseDir().getAbsolutePath();

        SwiftLintReportParser parser = new SwiftLintReportParser(context, fileSystem);
        parseReportIn(projectBaseDir, parser);
    }
}
