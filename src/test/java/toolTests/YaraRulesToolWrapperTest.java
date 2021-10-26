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

import org.junit.Test;

import pique.analysis.Tool;
import pique.model.Diagnostic;
import tool.CWECheckerToolWrapper;
import tool.YaraRulesToolWrapper;
import utilities.PiqueProperties;

public class YaraRulesToolWrapperTest {
	
	
	@Test
	public void ToolShouldHaveFindingsOnBinaryWithRuleViolations() {
		Properties prop = PiqueProperties.getProperties();
        Path resources = Paths.get(prop.getProperty("blankqm.filepath")).getParent();
		Tool yara = new YaraRulesToolWrapper(resources);

        Path testBin = Paths.get("src/test/resources/benchmark/systemd-hwdb");
        
        Path analysisOutput = yara.analyze(testBin);

        Map<String,Diagnostic> output = yara.parseAnalysis(analysisOutput);
        
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
	
	@Test
	public void ToolShouldReturnNullIfNoBinariesExist() {
		Properties prop = PiqueProperties.getProperties();
        Path resources = Paths.get(prop.getProperty("blankqm.filepath")).getParent();
		Tool yara = new YaraRulesToolWrapper(resources);
        
		Path testBin = Paths.get("src/test/resources/emptyDir/fake");
		
        Path analysisOutput = yara.analyze(testBin);
        Map<String,Diagnostic> output = yara.parseAnalysis(analysisOutput);

        assert(output==null);
	}
	
	@Test
	public void ToolShouldHaveNoFindingsOnSimpleCleanBinary() {
		Properties prop = PiqueProperties.getProperties();
        Path resources = Paths.get(prop.getProperty("blankqm.filepath")).getParent();
		Tool yara = new YaraRulesToolWrapper(resources);

        Path testBin = Paths.get("src/test/resources/HelloWorld");
        
        Path analysisOutput = yara.analyze(testBin);

        Map<String,Diagnostic> output = yara.parseAnalysis(analysisOutput);
        
        assertTrue(output!=null);
        assertTrue(output.size()>0);
        
        for (Diagnostic diag : output.values()) {
        	if (diag.getChildren().size()>0) {
        		//if we hit this, we've found at least one finding
        		fail();        	
    		}
        }
	}
	
	@Test
	public void ToolShouldHaveConsistentFindings() {
		Properties prop = PiqueProperties.getProperties();
        Path resources = Paths.get(prop.getProperty("blankqm.filepath")).getParent();
		Tool yara = new YaraRulesToolWrapper(resources);

        Path testBin = Paths.get("src/test/resources/benchmark/systemd-hwdb");
        
        //Analyze several times
        Path analysisOutput = yara.analyze(testBin);
        Map<String,Diagnostic> output1 = yara.parseAnalysis(analysisOutput);
        analysisOutput = yara.analyze(testBin);
        Map<String,Diagnostic> output2 = yara.parseAnalysis(analysisOutput);
        analysisOutput = yara.analyze(testBin);
        Map<String,Diagnostic> output3 = yara.parseAnalysis(analysisOutput);
        
        //for each diagnostic, check that the same number of findings appear in each run
        for (String x : output1.keySet()) {
        	Diagnostic d1 = output1.get(x);
        	Diagnostic d2 = output2.get(x);
        	Diagnostic d3 = output3.get(x);
        	
        	assert(d1.getChildren().size())==(d2.getChildren().size());         	
            assert(d2.getChildren().size())==(d3.getChildren().size());
        }
	}
}
