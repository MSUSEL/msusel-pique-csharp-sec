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
 * Security Code Scan Documentation: https://security-code-scan.github.io/
 * Security Code Scan download: https://www.nuget.org/packages/SecurityCodeScan.VS2019/
 * Security Code Scan repo: https://github.com/security-code-scan/security-code-scan
 * Security Code Scan detects various security vulnerability patterns: SQL Injection,
 * Cross-Site Scripting (XSS), Cross-Site Request Forgery (CSRF), XML eXternal Entity Injection (XXE), etc.
 *
 */

public class SecurityCodeScanAnalyzer extends Tool implements ITool {

    public SecurityCodeScanAnalyzer() {
        super("security-code-scan", null);
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
        File tempResults = new File(System.getProperty("user.dir") +"/out/security_code_scan_output.json");
        //tempResults.delete(); // clear out the last output. May want to change this to rename rather than delete.
        tempResults.getParentFile().mkdirs();

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
        }

        //this method of giving the command line pieces modeled from David Rice's PIQUE-Csharp, the RoslynatorAnalyzer class

        //strings for CLI call
        String command = "security-scan";
        String ignore = "--ignore-msbuild-errors";
        String output = "--export=" + tempResults.toString();
        String target = projectLocation.toAbsolutePath().toString();

        if (!System.getProperty("os.name").contains("Windows")) {
            throw new RuntimeException("Security Code Scan C# analysis not supported on non-Windows machines.");
        }

        // Run the tool
        System.out.println("security-code-scan: beginning static analysis.\n\tTarget: " + projectLocation.toString());

        Process p;
        try {
            p = new ProcessBuilder(command, ignore, output, target).start();
            System.out.println(p);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = stdInput.readLine()) != null) {
                System.out.println("security-code-scan: " + line);
            }
            p.waitFor();
        }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }

        // Assert result file was created
        if (!tempResults.isFile()) {
            throw new RuntimeException("SecurityCodeScanAnalyzer.analyze() did not generate a results file in the expected location");
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
                System.err.println("Security Code Scan failed to run");
                return null;
            }

        } catch (IOException e) {
            System.err.println("Error when reading Security Code Scan tool results.");
            e.printStackTrace();
            return null;
        }

        try {
            if (results.length() > 0) {

            JSONObject jsonObj = new JSONObject(results);
            JSONArray jsonResults = jsonObj.getJSONArray("runs");

            JSONObject jsonResult1 = jsonResults.getJSONObject(0);
            JSONArray result = jsonResult1.getJSONArray("results");

                if (result.length() == 0) {
                    System.err.println("No findings from Security Code Scan");
                }

                for (int i = 0; i < result.length(); i++) {

                    JSONObject ruleId1 = result.getJSONObject(i);
                    String ruleId = ruleId1.getString("ruleId");

                    JSONObject jsonFinding = (JSONObject) result.get(i);
                    String findingName = ruleId;

                    Finding finding = new Finding("", i, 0, 1); //might need to change. Passing 'i' as line number to ensure findings have different names
                    Diagnostic relDiag = diagnostics.get(findingName);

                    if (relDiag == null) {
                        System.err.println("Error finding diagnostic for Security Code Scan finding. Check to ensure all diagnostics are in the model. Ignoring this finding.");
                    } else {
                        relDiag.setChild(finding);
                    }
                }
            }
            else {
                System.err.println("No findings from Security Code Scan");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return diagnostics;
    }

    // Helper methods

    private Diagnostic findMapMemberByDiagnosticId(Map<String, Diagnostic> diagnostics, String id) {
        if (diagnostics.containsKey(id)) { return diagnostics.get(id); }
        else { return new Diagnostic(id, "", "security-code-scan", new DefaultDiagnosticEvaluator()); }
    }

    @Override
    public Path initialize(Path toolRoot) {
       // return securityCodeScanInitializeToTempFolder();
        return null;
    }

    @Override
    public String getName() {
        return "security-code-scan";
    }
}
