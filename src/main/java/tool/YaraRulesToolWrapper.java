package tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
import utilities.helperFunctions;

/**
 * This class is responsible for that automatic analysis using YARA and an associated set of rules which are packaged into the PIQUE-Bin repository. 
 * This solution is not desirable - perhaps the YARA executable could be automatically downloaded along with the git repository of rules. 
 * One common issue with this tool is the rules themselves being quarantined by anti-virus due to containing the patterns of some common malware.
 * 
 * @author Andrew Johnson
 *
 */
public class YaraRulesToolWrapper extends Tool implements ITool {
	
	private ArrayList<String> ruleCategories;
	private final String ruleCatPrefix = "RULE CATEGORY ";
	
	public YaraRulesToolWrapper(Path toolRoot) {
		super("yara-rules", toolRoot);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param path The path to a binary file for the desired solution of project to
	 *             analyze
	 * @return The path to the analysis results file
	 */
	@Override
	public Path analyze(Path projectLocation) {
		System.out.println(this.getName() + " Running...");
		
		ruleCategories = new ArrayList<String>();
		Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());
		for (String diagnosticName :diagnostics.keySet()) {
			String ruleFileName = (diagnosticName.split(" "))[1]; // Diagnostics are "Yara rulename Diagnostic"
			ruleCategories.add(ruleFileName);
		}
		
		File tempResults = new File(System.getProperty("user.dir") + "\\out\\yaraRulesOutput.txt");
		tempResults.delete(); // clear out the last output file. May want to change this to rename rather than delete.

		//check if file to be analyzed exists, only run analysis if it does.
		if (projectLocation.toFile().exists()) {
			tempResults.getParentFile().mkdirs();
			try (BufferedWriter writer = Files.newBufferedWriter(tempResults.toPath())) 
			{
			    for (String rule : ruleCategories) {
					String ruleResults = runYaraRules(rule,projectLocation);
					if (ruleResults.contains("error scanning")) {
						throw new IOException();
					}
					writer.write(ruleCatPrefix+rule+"\n");
					writer.write(ruleResults+"\n");
				}
			} catch (IOException e) {
				System.err.println("error when analyzing with Yara");
				tempResults.delete();
			}

		} 
		else System.err.println("Error running YARA. " +projectLocation.toString() + " does not exist.");
		
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

		//get contents output file
		String results = "";
		
		try {
			results = helperFunctions.readFileContent(toolResults);

		} catch (IOException e) {
			System.err.println("Error reading results of YaraRulesToolWrapper");
			return null;
		}
		
		String findingCategory = "";
		int ensureUniqueFinding = 0; //this is used to ensure findings aren't counted as duplicates
		for (String line :  results.split("\\r?\\n")) {
			if (line.contains(ruleCatPrefix)) {
				findingCategory = line.substring(ruleCatPrefix.length());
			}
			else if (line.length()>0) {
				String[] splitLine = line.split(" ");
				String ruleName = splitLine[0];
				Finding finding = new Finding(splitLine[1],ensureUniqueFinding++,0,1); //might need to change. setting arbitrary number for line #
				finding.setName(ruleName);
				Diagnostic relevantDiag = diagnostics.get("Yara "+findingCategory+ " Diagnostic");
				relevantDiag.setChild(finding);
			}
		}
		return diagnostics;
	}

	/**
	 * In the current state of things, no initialization is needed for this tool, as the executable and rules are 
	 * included in the repository.
	 */
	@Override
	public Path initialize(Path toolRoot) {
		
		//check if docker image has been built already
		final String[] imageCheck = {"docker", "image", "inspect", this.getName()};
		String check = "";
		try {
			check = helperFunctions.getOutputFromProgram(imageCheck, false);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if (check.contains("Error: No such image")) { //we haven't built the image yet, need to build
			final String cmd[] = {"docker", 
					"build",
					"-t", this.getName(),
					toolRoot.toAbsolutePath().toString()};
			try {
				helperFunctions.getOutputFromProgram(cmd, true);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return toolRoot;
	}
	
	private String runYaraRules(String ruleName, Path projectLocation) {
		
		String[] cmd = {"docker", "run", "--rm", 
				"-v", projectLocation.toAbsolutePath().toString()+":/input",
				this.getName(), "-w",
				"rules/"+ruleName + "_index.yar",
				"/input"};
		String output = null;
		try {
			output = helperFunctions.getOutputFromProgram(cmd,true);
		} catch (IOException  e) {
			e.printStackTrace();
		}
		return output;
	}
	
}
