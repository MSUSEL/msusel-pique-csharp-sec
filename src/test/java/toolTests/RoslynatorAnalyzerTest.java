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
package toolTests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import pique.analysis.Tool;
import pique.model.Diagnostic;
import tool.RoslynatorAnalyzer;
import utilities.PiqueProperties;

public class RoslynatorAnalyzerTest {

    @Ignore
    @Test
    public void ToolShouldHaveFindingsOnCodeWithDiagnostics() {
        Properties prop = PiqueProperties.getProperties();
        Tool roslynatorAnalyzer = new RoslynatorAnalyzer(Paths.get(prop.getProperty("roslynator.tool.root")), Paths.get(prop.getProperty("msbuild.roslynator")));

        Path testBin = Paths.get("src/test/resources/benchmark/systemd-hwdb");

        Path analysisOutput = roslynatorAnalyzer.analyze(testBin);

        Map<String,Diagnostic> output = roslynatorAnalyzer.parseAnalysis(analysisOutput);

        assertTrue(output!=null);
        assertTrue(output.size()>0);

        for (Diagnostic diag : output.values()) {
            if (diag.getChildren().size()>0) {
                //if we hit this, we've found at least one finding
                return;
            }
        }
        //if we didn't return from the above statement, force the test to fail
        fail();
    }
/*
    @Test
    public void ToolShouldReturnNullIfNoBinariesExist() {
        Properties prop = PiqueProperties.getProperties();
        Tool roslynatorAnalyzer = new RoslynatorAnalyzer(Paths.get(prop.getProperty("roslynator.tool.root")), Paths.get(prop.getProperty("msbuild.bin")));

        Path testBin = Paths.get("src/test/resources/benchmark/emptyDir");
        Path analysisOutput = roslynatorAnalyzer.analyze(testBin);
        Map<String,Diagnostic> output = roslynatorAnalyzer.parseAnalysis(analysisOutput);
        assert(output==null);
    }

    @Test
    public void ToolShouldHaveNoFindingsOnSimpleCleanBinary() {
        Properties prop = PiqueProperties.getProperties();
        Tool roslynatorAnalyzer = new RoslynatorAnalyzer(Paths.get(prop.getProperty("roslynator.tool.root")), Paths.get(prop.getProperty("msbuild.bin")));

        Path testBin = Paths.get("src/test/resources/HelloWorld");

        Path analysisOutput = roslynatorAnalyzer.analyze(testBin);

        Map<String,Diagnostic> output = roslynatorAnalyzer.parseAnalysis(analysisOutput);

        assertTrue(output!=null);
        assertTrue(output.size()>0);

        for (Diagnostic diag : output.values()) {
            if (diag.getChildren().size()>0) {
                //if we hit this, we've found at least one finding
                fail();        	}
        }
    }
*/

}
