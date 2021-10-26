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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
import utilities.helperFunctions;


/**
 * 
 * @author Andrew Johnson
 * This class will output a single finding from the tool whose name is passed as a parameter. It will add the finding under the given diagnostic
 * name.
 */
public class FakeFindingWrapper extends Tool implements ITool {

	private int severity;
	private ITool realTool;
	private String targetDiagnosticName;
	/**
	 * @param realTool tool that this class will run and add a finding to
	 * @param maxSeverity maximum severity of the tool. Will make the severity of finding between 1 and maxVal.
	 */
	public FakeFindingWrapper(ITool realTool, int severity, String diagnosticName) {
		super(realTool.getName(), null);
		this.realTool = realTool;
		this.severity = severity;
		this.targetDiagnosticName = diagnosticName;
	}
 
	@Override
	public Path analyze(Path projectLocation) {
		return realTool.analyze(projectLocation);
	}

	@Override
	public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
		Map<String, Diagnostic> diagnostics = realTool.parseAnalysis(toolResults);
		
		Finding fakeFind = new Finding("",99999,99999,severity);
		fakeFind.setName("FakeFinding");
		
		Diagnostic relDiag = (Diagnostic) diagnostics.get(targetDiagnosticName);
		relDiag.setChild(fakeFind);
		
		//write this to file
		System.out.print(relDiag.getName() +", " + severity);
		   
		return diagnostics;
	}

	@Override
	public Path initialize(Path toolRoot) {
		return null;
	}

}
