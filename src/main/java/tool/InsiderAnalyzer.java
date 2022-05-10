package tool;

import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.evaluation.DefaultDiagnosticEvaluator;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.FileUtility;
import utilities.helperFunctions;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ITool implementation static analysis tool class.
 *
 * Insider Repo and Documentation: https://github.com/insidersec/insider
 * Insider download: https://github.com/insidersec/insider/releases
 * Insider is focused on covering the OWASP Top 10, by finding vulnerabilities right in the source code.
 *
 */

public class InsiderAnalyzer extends Tool implements ITool {

    public InsiderAnalyzer() {
        super("insider", null);
    }

    // Methods
    /**
     * @param projectLocation The path to the .sln file for the desired solution of project to
     *             analyze
     * @return The path to the analysis results file
     */

    @Override
    public Path analyze(Path projectLocation) {

        projectLocation = projectLocation.toAbsolutePath();
        String sep = File.separator;
        File tempResults = new File("report.json");

        /* this code not required for Insider tool

        // Append .sln or .csproj file to path
        // TODO: refactor to method and find better way that doesn't use stacked if statements.
        Set<String> targetFiles = FileUtility.findFileNamesFromExtension(projectLocation, ".sln", 1);
        if (targetFiles.size() == 1) {
            projectLocation = Paths.get(projectLocation.toString(), targetFiles.iterator().next() + ".sln");
        }
        else if (targetFiles.size() > 1) {
            throw new RuntimeException("More than one .sln file exists in the give path root directory. " +
                    "Ensure the directory has only one .sln file to target.");
        }
        else {
            targetFiles = FileUtility.findFileNamesFromExtension(projectLocation, ".csproj", 1);
            if (targetFiles.size() == 1) {
                projectLocation = Paths.get(projectLocation.toString(), targetFiles.iterator().next() + ".csproj");
            }
            else if (targetFiles.size() > 1) {
                throw new RuntimeException("A .sln file not found and more than one .csproj file exists in the give path root directory. " +
                        "Ensure the directory has only one .csproj file to target.");
            }
        }*/

        //this method of giving the command line pieces modeled from Andrew Johnson's PIQUE-Bin, the CVEBinToolWrapper class

        //command for CLI call
        String[] cmd = {"./insider",
                "-tech", "csharp",
                "-target",
                projectLocation.toAbsolutePath().toString()};

        try {
            helperFunctions.getOutputFromProgram(cmd,true);

        } catch (IOException  e) {
            e.printStackTrace();
        }

        return tempResults.toPath();

    }

    @Override
    public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
        System.out.println(this.getName() + " Parsing Analysis...");
        Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());

        String results = "";

        try {
            if (toolResults.toFile().isFile()) {
                results = helperFunctions.readFileContent(toolResults);
            }
            else {
                System.err.println("Insider failed to run");
                return null;
            }

        } catch (IOException e) {
            System.err.println("Error when reading Insider tool results.");
            e.printStackTrace();
            return null;
        }

        try {
            if (results.length() > 0) {

                JSONObject jsonObj = new JSONObject(results);

                //null check
                if (jsonObj.has("vulnerabilities")) {
                    JSONArray jsonResults = jsonObj.getJSONArray("vulnerabilities");

                    if (jsonResults.length() == 0) {
                        System.err.println("No findings from Insider");
                    }

                    for (int i = 0; i < jsonResults.length(); i++) {

                        JSONObject ruleId1 = jsonResults.getJSONObject(i);
                        String ruleId = ruleId1.getString("cwe");

                        JSONObject jsonFinding = (JSONObject) jsonResults.get(i);
                        String findingName = ruleId;

                        Finding finding = new Finding("", i, 0, 1); //might need to change. Passing 'i' as line number to ensure findings have different names
                        Diagnostic relDiag = diagnostics.get(findingName);

                        if (relDiag == null) {
                            System.err.println("Error finding diagnostic for Insider finding. Check to ensure all diagnostics are in the model. Ignoring this finding.");
                        } else {
                            relDiag.setChild(finding);
                        }
                    }
                }
            }
            else {
                System.err.println("No findings from Insider");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return diagnostics;
    }

    // Helper methods

    private Diagnostic findMapMemberByDiagnosticId(Map<String, Diagnostic> diagnostics, String id) {
        if (diagnostics.containsKey(id)) { return diagnostics.get(id); }
        else { return new Diagnostic(id, "", "insider", new DefaultDiagnosticEvaluator()); }
    }

    @Override
    public Path initialize(Path toolRoot) {
        return null;
    }

    @Override
    public String getName() {
        return "insider";
    }
}
