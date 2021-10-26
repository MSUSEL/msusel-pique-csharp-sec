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
package tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.model.ModelNode;
import pique.model.QualityModel;
import pique.model.QualityModelImport;
import utilities.PiqueProperties;
import utilities.helperFunctions;


/**
 * This tool automates the installation and running of the cwe_checker tool. Installation is handled by running docker from the command line, and analysis is
 * performed by running the docker container. 
 * @author Andrew
 *
 */
public class CWECheckerToolWrapper extends Tool implements ITool {


	public CWECheckerToolWrapper() {
		super("cwe_checker", null);
	}

	// Methods
	/**
	 * @param path The path to a binary file for the desired solution of project to
	 *             analyze
	 * @return The path to the analysis results file
	 */
	public Path analyze(Path projectLocation) {
		System.out.println(this.getName() + " Running...");
		File tempResults = new File(System.getProperty("user.dir") + "/out/CWECheckerOutput.json");
		tempResults.delete(); // clear out the last output. May want to change this to rename rather than delete.
		tempResults.getParentFile().mkdirs();
		String out = "";
		String[] cmd = {"docker", "run", "--rm", "-v",
				projectLocation.toAbsolutePath().toString()+":/input", 
				"fkiecad/cwe_checker:latest", 
				"--json", "--quiet", 
				"/input"};
		try (BufferedWriter writer = Files.newBufferedWriter(tempResults.toPath())) {
			out = helperFunctions.getOutputFromProgram(cmd,true);
			writer.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (out.toLowerCase().contains("error") || out.toLowerCase().contains("report")) {
			tempResults.delete();
		}

		return tempResults.toPath();
	}

	/**
	 * parses output of tool from analyze().
	 * 
	 * @param toolResults location of the results, output by analyze() 
	 * @return A Map<String,Diagnostic> with findings from the tool attached. Returns null if tool failed to run.
	 */
	public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
		System.out.println(this.getName() + " Parsing Analysis...");
		Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());

		String results = "";

		try {
			if (toolResults.toFile().isFile()) {
				results = helperFunctions.readFileContent(toolResults);
			}
			else {
				System.err.println("CWE_Checker failed to run");
				return null;
			}

		} catch (IOException e) {
			System.err.println("Error when reading CWEChecker tool results.");
			e.printStackTrace();
			return null;
		}
			
		try {
		
			if (results.length() > 0) {
				JSONArray jsonResults = new JSONArray(results);
				for (int i = 0; i < jsonResults.length(); i++) {
					JSONObject jsonFinding = (JSONObject) jsonResults.get(i);
					String findingName = jsonFinding.get("name").toString();
					findingName = helperFunctions.addDashtoCWEName(findingName) + " Weakness Diagnostic";
					Finding finding = new Finding("",i,0,1); //might need to change. Passing 'i' as line number to ensure findings have different names
					Diagnostic relDiag = diagnostics.get(findingName);
					if (relDiag == null) {
						System.err.println("Error finding diagnostic for CWE_Checker finding. Check to ensure all CWEs are in the model. Ignoring this finding.");
					}
					else {
						relDiag.setChild(finding);
					}
				}
			}
			else {
				System.err.println("No findings from cwe_checker");
			}
			

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return diagnostics;
	}

	/**
	 * Initializes the tool by installing through docker from the command line.
	 */
	public Path initialize(Path toolRoot) {
		final String cmd[] = {"docker", 
				"pull",
				"fkiecad/cwe_checker:stable"};
		try {
			helperFunctions.getOutputFromProgram(cmd, true);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return toolRoot;
	}

	

}
