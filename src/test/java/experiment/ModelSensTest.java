/**
 * MIT License
 * Copyright (c) 2019 Montana State University Software Engineering Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package experiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pique.analysis.ITool;
import pique.evaluation.Project;
import pique.model.Diagnostic;
import pique.model.ModelNode;
import pique.model.QualityModel;
import pique.model.QualityModelImport;
import tool.FakeFindingWrapper;
import tool.InsiderAnalyzer;
import tool.RoslynatorLoc;
import tool.SecurityCodeScanAnalyzer;
import utilities.PiqueProperties;

/**
 * Behavioral class responsible for running TQI evaluation of a single project
 * in a language agnostic way.
 */
// TODO (1.0): turn into static methods (maybe unless logger problems)
public class ModelSensTest {

    public static void main(String[] args){
        new ModelSensTest();
    }

    private Project project;


    public ModelSensTest(){
        Properties prop = PiqueProperties.getProperties();
        Path projectRoot = Paths.get(prop.getProperty("project.root"));
        Path resultsDir = Paths.get(prop.getProperty("results.directory"));

        String resultsDirString = (resultsDir.toString() + "/sensAnalysis/" + LocalDateTime.now()).replaceAll("[^a-zA-Z0-9\\/]", "");
        resultsDir = Paths.get(resultsDirString);
        Path qmLocation = Paths.get("out/CsharpSecurityQualityModel.json");
        Path resources = Paths.get(prop.getProperty("blankqm.filepath")).getParent();

        File dataFile = Paths.get(resultsDir.getParent().toAbsolutePath().toString() + "/DataCollection.csv").toFile();

        //init tools
        ITool securityCodeScan = new SecurityCodeScanAnalyzer();
        ITool insider = new InsiderAnalyzer();
        Set<ITool> tools = Stream.of(securityCodeScan, insider).collect(Collectors.toSet());

        Project outputProj = runEvaluator(projectRoot, resultsDir, qmLocation, tools);
        QualityModel evaluatedQM = outputProj.getQualityModel();
        BigDecimal originalTQI = evaluatedQM.getTqi().getValue();

        //added to also report security aspects in addition to TSI
        BigDecimal originalAuthenticity = evaluatedQM.getQualityAspect("Authenticity").getValue();
        BigDecimal originalAvailability = evaluatedQM.getQualityAspect("Availability").getValue();
        BigDecimal originalAuthorization = evaluatedQM.getQualityAspect("Authorization").getValue();
        BigDecimal originalConfidentiality = evaluatedQM.getQualityAspect("Confidentiality").getValue();
        BigDecimal originalAccountability = evaluatedQM.getQualityAspect("Accountability").getValue();
        BigDecimal originalNonrepudiation = evaluatedQM.getQualityAspect("Non-repudiation").getValue();
        BigDecimal originalIntegrity = evaluatedQM.getQualityAspect("Integrity").getValue();

        System.out.println("Original TQI: " + originalTQI);

        //get all diagnostics
        Collection<ModelNode> diagnostics = evaluatedQM.getDiagnostics().values();
        boolean check = false;
        for (ModelNode d : diagnostics) {
        	securityCodeScan = new SecurityCodeScanAnalyzer();
            insider = new InsiderAnalyzer();

        	String toolName = ((Diagnostic)d).getToolName();

        	if (toolName.equals("security-code-scan")) {
        		securityCodeScan = new FakeFindingWrapper(securityCodeScan,1,d.getName());
        	}
        	else if (toolName.equals("insider")) {
        		insider = new FakeFindingWrapper(insider,1, d.getName());
        	}

        	tools = Stream.of(securityCodeScan, insider).collect(Collectors.toSet());
        	outputProj = runEvaluator(projectRoot, resultsDir, qmLocation, tools);
            evaluatedQM = outputProj.getQualityModel();
            BigDecimal tqi = evaluatedQM.getTqi().getValue();
            BigDecimal tqiDiff = originalTQI.subtract(tqi);

            //added to report security aspects in addition to TSI
            BigDecimal authenticity = evaluatedQM.getQualityAspect("Authenticity").getValue();
            BigDecimal authenticityDiff = originalAuthenticity.subtract(authenticity);

            BigDecimal availability = evaluatedQM.getQualityAspect("Availability").getValue();
            BigDecimal availabilityDiff = originalAvailability.subtract(availability);

            BigDecimal authorization = evaluatedQM.getQualityAspect("Authorization").getValue();
            BigDecimal authorizationDiff = originalAuthorization.subtract(authorization);

            BigDecimal confidentiality = evaluatedQM.getQualityAspect("Confidentiality").getValue();
            BigDecimal confidentialityDiff = originalConfidentiality.subtract(confidentiality);

            BigDecimal accountability = evaluatedQM.getQualityAspect("Accountability").getValue();
            BigDecimal accountabilityDiff = originalAccountability.subtract(accountability);

            BigDecimal nonrepudation = evaluatedQM.getQualityAspect("Non-repudiation").getValue();
            BigDecimal nonrepudiationDiff = originalNonrepudiation.subtract(nonrepudation);

            BigDecimal integrity = evaluatedQM.getQualityAspect("Integrity").getValue();
            BigDecimal integrityDiff = originalIntegrity.subtract(integrity);

            //String strToAppend = d.getName()+", "+tqiDiff;
            String strToAppend = d.getName()+", "+tqiDiff+", "+authenticityDiff+", "+availabilityDiff+", "+authorizationDiff+", "+confidentialityDiff+", "+accountabilityDiff+", "+nonrepudiationDiff+", "+integrityDiff;


            System.out.println(strToAppend);
            System.out.println("New tqi: " + tqi);

            appendln(dataFile,strToAppend);
        }


    }

    //region Get / Set
    public Project getEvaluatedProject() {
        return project;
    }

    /**
     * Entry point for running single project evaluation. The library assumes the user has extended Qatch
     * by implementing ITool with language-specific functionality.
     *
     * This method then evaluates the measures, properties, characteristics, and TQI according to the provided
     * quality model.
     *
     * @param projectDir
     *      Path to root directory of project to be analyzed.
     * @param resultsDir
     *      Directory to place the analysis results in. Does not needy to exist initially.
     * @param qmLocation
     *      Path to a completely derived quality model (likely .xml format).
     * @return
     *      The path to the produced quality analysis file on the hard disk.
     */
    public Project runEvaluator(Path projectDir, Path resultsDir, Path qmLocation, Set<ITool> tools) {

        // Initialize data structures
        initialize(projectDir, resultsDir, qmLocation);
        QualityModelImport qmImport = new QualityModelImport(qmLocation);
        QualityModel qualityModel = qmImport.importQualityModel();
        project = new Project(projectDir.getFileName().toString(), projectDir, qualityModel);

        // Validate State
        // TODO: validate more objects such as if the quality model has thresholds and weights, are there expected diagnostics, etc
        validatePreEvaluationState(project);

        // Run the static analysis tools process
        Map<String, Diagnostic> allDiagnostics = new HashMap<>();
        tools.forEach(tool -> {
            allDiagnostics.putAll(runTool(projectDir, tool));
        });


        // Apply tool results to Project object
        project.updateDiagnosticsWithFindings(allDiagnostics);

        BigDecimal tqiValue = project.evaluateTqi();

        // Create a file of the results and return its path
        return project;
    }


    /**
     * Assert input parameters are valid and create the output folder
     *
     * @param projectDir
     *      Path to directory holding the project to be evaluated. Must exist.
     * @param resultsDir
     *      Directory to place the analysis results in. Does not need to exist initially.
     * @param qmLocation
     *      Path to the quality model file. Must exist.
     */
    private void initialize(Path projectDir, Path resultsDir, Path qmLocation) {
        if (!projectDir.toFile().exists()) {
            throw new IllegalArgumentException("Invalid projectDir path given.");
        }
        if (!qmLocation.toFile().exists() || !qmLocation.toFile().isFile()) {
            throw new IllegalArgumentException("Invalid qmLocation path given.");
        }

        resultsDir.toFile().mkdirs();
    }


    /**
     * Run static analysis tool evaluation process:
     *   (1) run static analysis tool
     *   (2) parse: get object representation of the diagnostics described by the QM
     *   (3) make collection of diagnostic objects
     *
     * @param projectDir
     *      Path to root directory of project to be analyzed.
     * @param tool
     *      Analyzer provided by language-specific instance necessary to find findings of the project.
     * @return
     *      A mapping of (Key: property name, Value: measure object) where the measure objects contain the
     *      static analysis findings for that measure.
     */
    private Map<String, Diagnostic> runTool(Path projectDir, ITool tool) {

        // (1) run static analysis tool
        // TODO: turn this into a temp file that always deletes on/before program exit
        Path analysisOutput = tool.analyze(projectDir);

        // (2) prase output: make collection of {Key: diagnostic name, Value: diagnostic objects}
        return tool.parseAnalysis(analysisOutput);
    }


    /**
     * Sequence of state checks of the project's quality model before running evaluation.
     * Throws runtime error if any expected state is not achieved.
     *
     * @param project
     *      The project under evaluation. This project should have a contained qualityModel with
     *      weight and threshold instances.
     */
    // TODO (1.0) Update once basic tests passing
    private void validatePreEvaluationState(Project project) {
        QualityModel projectQM = project.getQualityModel();

        if (projectQM.getTqi().getWeights() == null) {
            throw new RuntimeException("The project's quality model does not have any weights instantiated to its TQI node");
        }

        projectQM.getQualityAspects().values().forEach(characteristic -> {

            if (characteristic.getWeights() == null) {
                throw new RuntimeException("The project's quality model does not have any weights instantiated to its characteristic node");
            }

//            characteristic.getChildren().values().forEach(productFactor -> {
//                if (productFactor.getMeasure().getThresholds() == null) {
//                    throw new RuntimeException("The project's quality model does not have any thresholds instantiated to its measure node.");
//                }
//            });
        });
    }

	private static void appendln(File f, String str) {
		try (FileWriter fw = new FileWriter(f.getAbsolutePath(),true)){
			fw.write(str + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
