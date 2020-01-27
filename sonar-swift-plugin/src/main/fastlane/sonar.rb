#
# backelite-sonar-swift-plugin - Enables analysis of Swift and Objective-C projects into SonarQube.
# Copyright © 2019 David Yang (david.tcha.yang@gmail.com)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

require "java-properties"
require 'shellwords'

desc "Launch code analysis for SonarQube"
lane :metrics do
	properties = JavaProperties.load(File.join(ENV["PWD"], "sonar-project.properties"))
	default_output_directory = "./sonar-reports"
	derived_data_path = "./DerivedData"

	sonar_run_build(properties: properties, output_directory: default_output_directory)
	sonar_run_tests(properties: properties, output_directory: default_output_directory, derived_data_path: derived_data_path)
	sonar_run_slather(properties: properties, output_directory: default_output_directory, derived_data_path: derived_data_path)
	sonar_run_oclint(properties: properties, output_directory: default_output_directory)
	sonar_run_swiftlint(properties: properties, output_directory: default_output_directory)
	sonar_run_lizard(properties: properties, output_directory: default_output_directory)
	sonar_run_scanner()
end

desc "Run and build project to gather compile commands database"
private_lane :sonar_run_build do |options|
	# Extract sonar property values
	workspace	= options[:properties][:"sonar.swift.workspace"]
	app_scheme	= options[:properties][:"sonar.swift.appScheme"]

	gym(
    	workspace: workspace,
    	scheme: app_scheme, 
    	clean: true,
    	skip_package_ipa: true,
    	xcpretty_report_json: options[:output_directory] + "/compile_commands.json",
    	xcargs: "COMPILER_INDEX_STORE_ENABLE=NO"
	)
end

desc "Run unit tests"
private_lane :sonar_run_tests do |options|
	# Extract sonar property values
	workspace	= options[:properties][:"sonar.swift.workspace"]
	app_scheme	= options[:properties][:"sonar.swift.appScheme"]

	run_tests(
    	workspace: workspace,
    	scheme: app_scheme, 
    	clean: false,
    	code_coverage: true, 
    	derived_data_path: options[:derived_data_path], 
    	output_directory: options[:output_directory],
    	output_types: "junit",
    	output_files: "TEST-report.xml"
    )
end

desc "Run Slather coverage"
private_lane :sonar_run_slather do |options|
	# Extract sonar property values
	binary_basename 				= options[:properties][:"sonar.coverage.binaryNames"]
	app_scheme 						= options[:properties][:"sonar.swift.appScheme"]
	excluded_paths_from_coverage 	= options[:properties][:"sonar.swift.excludedPathsFromCoverage"]
	project 						= options[:properties][:"sonar.swift.project"]

	if binary_basename.nil? || binary_basename.empty?
		slather(
		   	cobertura_xml: true, 
		   	scheme: app_scheme,
		   	input_format: "profdata", 
		   	ignore: excluded_paths_from_coverage.split(","), 
		   	build_directory: options[:derived_data_path], 
		   	output_directory: options[:output_directory],
		   	proj: project
		)
	else
		slather(
		   	cobertura_xml: true, 
		   	scheme: app_scheme,
		   	input_format: "profdata", 
		   	ignore: excluded_paths_from_coverage.split(","), 
		   	binary_basename: binary_basename.split(","),
		   	build_directory: options[:derived_data_path], 
		   	output_directory: options[:output_directory],
		   	proj: project
		)
	end
    # Rename the file
    sh "cd ../#{options[:output_directory]} && mv cobertura.xml coverage-swift.xml"
end

desc "Run OCLint analysis"
private_lane :sonar_run_oclint do |options|
	# Extract sonar property values
	sources = options[:properties][:"sonar.sources"]

	sources.split(",").each do |source|
		# if source path contains objective-c file (*.m)
		unless Dir.glob("../#{source}/**/*.m").empty?
			output_filename = source.gsub("/", "_") + "-oclint.xml"
			sh "cd ../#{options[:output_directory]} && oclint-json-compilation-database -v --include .*/#{source} -- -rc LONG_LINE=250 -max-priority-1 10000 -max-priority-2 10000 -max-priority-3 10000 -report-type pmd -o #{output_filename}"
		end
	end
end

desc "Run Lizard"
private_lane :sonar_run_lizard do |options|
	# Extract sonar property values
	sources = options[:properties][:"sonar.sources"]

	source_folders = sources.split(",").map { |source| source.shellescape }.join(" ")
	lizard(
		source_folder: source_folders,
		language: 'swift,objectivec',
		export_type: 'xml',
		report_file: options[:output_directory] + '/lizard-report.xml'
	)
end

desc "Run SwiftLint analysis"
private_lane :sonar_run_swiftlint do |options|
	# Extract sonar property values
	sources = options[:properties][:"sonar.sources"]

	# iterating through each sources path
	sources.split(",").each do |source|
		output_filename = options[:output_directory] + "/" + source.gsub("/", "_") + "-swiftlint.txt"
		swiftlint(mode: :lint, path: source, output_file: output_filename, ignore_exit_status: true)
	end
end

desc "Run sonar-scanner"
private_lane :sonar_run_scanner do |options|
	sonar(project_configuration_path: "sonar-project.properties")
end