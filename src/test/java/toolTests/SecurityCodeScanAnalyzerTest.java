package toolTests;

import org.junit.Test;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import tool.RoslynatorAnalyzer;
import tool.SecurityCodeScanAnalyzer;
import utilities.PiqueProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class SecurityCodeScanAnalyzerTest {

    @Test
    public void ToolShouldHaveFindingsOnCodeWithDiagnostics() {
        Properties prop = PiqueProperties.getProperties();
        Tool securityCodeScanAnalyzer = new SecurityCodeScanAnalyzer();

        Path testBin = Paths.get("src/test/resources/benchmark/systemd-hwdb");

        Path analysisOutput = securityCodeScanAnalyzer.analyze(testBin);

        Map<String, Diagnostic> output = securityCodeScanAnalyzer.parseAnalysis(analysisOutput);

        assertTrue(output != null);
        assertTrue(output.size() > 0);

        for (Diagnostic diag : output.values()) {
            if (diag.getChildren().size() > 0) {
                //if we hit this, we've found at least one finding
                return;
            }
        }
    }
}
