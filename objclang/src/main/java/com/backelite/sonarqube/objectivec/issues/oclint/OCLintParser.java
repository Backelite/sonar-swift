/**
 * Swift SonarQube Plugin - Objective-C module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.objectivec.issues.oclint;

import com.backelite.sonarqube.commons.StaxParser;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.component.ResourcePerspectives;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final class OCLintParser {

    private final SensorContext sensorContext;
    private final FileSystem fileSystem;

    public OCLintParser(final SensorContext sensorContext, final FileSystem fileSystem) {
        this.sensorContext = sensorContext;
        this.fileSystem = fileSystem;
    }

    public void parseReport(final File file) {

        try {
            final InputStream reportStream = new FileInputStream(file);
            parseReport(reportStream);
            reportStream.close();
        } catch (final IOException e) {
            LoggerFactory.getLogger(getClass()).error("Error processing file named {}", file, e);
        }

    }

    public void parseReport(final InputStream inputStream) {

        try {
            final StaxParser parser = new StaxParser(
                    new OCLintXMLStreamHandler(sensorContext, fileSystem));
            parser.parse(inputStream);
        } catch (final XMLStreamException e) {
            LoggerFactory.getLogger(getClass()).error(
                    "Error while parsing XML stream.", e);
        }
    }

}
