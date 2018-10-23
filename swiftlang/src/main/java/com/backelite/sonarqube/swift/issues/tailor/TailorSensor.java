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
package com.backelite.sonarqube.swift.issues.tailor;

import com.backelite.sonarqube.commons.Constants;
import com.backelite.sonarqube.swift.lang.core.Swift;
import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;

import java.io.File;

/**
 * Created by tzwickl on 22/11/2016.
 */

public class TailorSensor implements Sensor {

    public static final String REPORT_PATH_KEY = Constants.PROPERTY_PREFIX + ".tailor.report";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/*tailor.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(TailorSensor.class);

    private final Settings conf;
    private final FileSystem fileSystem;

    public TailorSensor(final FileSystem fileSystem, final Settings config) {
        this.conf = config;
        this.fileSystem = fileSystem;
    }

    private void parseReportIn(final String baseDir, final TailorReportParser parser) {

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{reportPath()});
        scanner.setBasedir(baseDir);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        for (String filename : files) {
            LOGGER.info("Processing Tailor report {}", filename);
            parser.parseReport(new File(filename));
        }

    }

    private String reportPath() {
        String reportPath = this.conf.getString(REPORT_PATH_KEY);
        if (reportPath == null) {
            reportPath = DEFAULT_REPORT_PATH;
        }
        return reportPath;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .onlyOnLanguage(Swift.KEY)
                .name("Tailor")
                .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext context) {

        final String projectBaseDir = this.fileSystem.baseDir().getAbsolutePath();

        TailorReportParser parser = new TailorReportParser(context, this.fileSystem);
        parseReportIn(projectBaseDir, parser);

    }
}
