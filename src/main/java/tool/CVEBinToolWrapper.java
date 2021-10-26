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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
 * This tool wrapper will install CVE-Bin-Tool through the command line and Python. It will then run and analyze the output of the tool.
 * When parsing the output of the tool, a command line call to run a Python script is made. This script is responsible for translating from 
 * CVE number to the CWE it is categorized as by the NVD.
 * @author Andrew Johnson
 *
 */
public class CVEBinToolWrapper extends Tool implements ITool  {
	
			
	public CVEBinToolWrapper() {
		super("cve-bin-tool", null);
	}

	// Methods
		/**
		 * @param path The path to a binary file for the desired solution of project to
		 *             analyze
		 * @return The path to the analysis results file
		 */
		@Override
		public Path analyze(Path projectLocation) {
			System.out.println(this.getName() + " Running...");
			File tempResults = new File(System.getProperty("user.dir") + "/out/cve-bin-tool.json");
			tempResults.delete(); // clear out the last output. May want to change this to rename rather than delete.
			tempResults.getParentFile().mkdirs();

			String[] cmd = {"python", 
					"-m", "cve_bin_tool.cli", 
					"-f", "json", 
					projectLocation.toAbsolutePath().toString(),
					"-o",tempResults.toPath().toAbsolutePath().toString(),};
			
			try {
				helperFunctions.getOutputFromProgram(cmd,true);

			} catch (IOException  e) {
				e.printStackTrace();
			}

			return tempResults.toPath();
		}

		/**
		 * parses output of tool from analyze().
		 * 
		 * @param toolResults location of the results, output by analyze() 
		 * @return A Map<String,Diagnostic> with findings from the tool attached. Returns null if tool failed to run.
		 */
		@Override
		public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
			System.out.println(this.getName() + " Parsing Analysis...");
			Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());

			String results = "";

			try {
				results = helperFunctions.readFileContent(toolResults);

			} catch (IOException e) {
				System.err.println("No results to read from cve-bin-tool.");
				return null;
			}
			
			ArrayList<String> cveList = new ArrayList<String>();
			ArrayList<Integer> severityList = new ArrayList<Integer>();
			
			try {
				JSONArray jsonResults = new JSONArray(results);
				
				for (int i = 0; i < jsonResults.length(); i++) {
					JSONObject jsonFinding = (JSONObject) jsonResults.get(i); 
					//Need to change this for this tool.
					String findingName = jsonFinding.get("cve_number").toString();
					String findingSeverity = jsonFinding.get("severity").toString();
					severityList.add(this.severityToInt(findingSeverity));
					cveList.add(findingName);
				}
				
				//make a string of all the CWE names to pass to getCWE function
				String findingsString = "";
				for (String x : cveList) {
					findingsString = findingsString +" " + x;
				}
				//get CWE names
				String[] findingNames = helperFunctions.getCWE(findingsString);
				
				for (int i = 0; i < findingNames.length; i++) {
					
					
					Diagnostic diag = diagnostics.get(("CVE-" +findingNames[i]+" Diagnostic"));
					if (diag == null) { 
						//this means that either it is unknown, mapped to a CWE outside of the expected results, or is not assigned a CWE
						//We may want to treat this in another way.
						diag = diagnostics.get("CVE-Unknown-Other Diagnostic");
					}
					Finding finding = new Finding("",0,0,severityList.get(i));
					finding.setName(cveList.get(i));
					diag.setChild(finding);
				}
				

			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return diagnostics;
		}

		/**
		 * Initializes the tool by installing it through python pip from the command line.
		 */
		@Override
		public Path initialize(Path toolRoot) {
			final String[] cmd = {"python", "-m", "pip", "install", "cve-bin-tool==2.1.post1"}; 
			
			try {
				helperFunctions.getOutputFromProgram(cmd, true);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return toolRoot;
		}

		//private ArrayList<String> identifyCWEs() {
			// identify all relevant diagnostics from the model structure
		//	ArrayList<String> cweList = new ArrayList<String>();	
		//}
		
		//maps low-critical to numeric values based on the highest value for each range.
		private Integer severityToInt(String severity) {
			Integer severityInt = 1;
			switch(severity.toLowerCase()) {
				case "low": {
					severityInt = 4;
					break;
				}
				case "medium": {
					severityInt = 7;
					break;
				}
				case "high": {
					severityInt = 9;
					break;
				}
				case "critical": {
					severityInt = 10;
					break;
				}
			}
			
			return severityInt;
		}
		
		 
}
