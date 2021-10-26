package tool;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.model.ModelNode;
import pique.model.QualityModel;
import pique.model.QualityModelImport;
import utilities.PiqueTestProperties;

public class TestTool  extends Tool implements ITool {

	private int severity;
	
	public TestTool() {
		super("testTool",null);
		severity=0;
	}
	
	public TestTool(int severity) {
		super("testTool",null);
		this.severity=severity;
	}

	@Override
	public Path analyze(Path projectLocation) {
		return null;
	}

	@Override
	public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
		Map<String, Diagnostic> fakeDiags = new HashMap<String, Diagnostic>();
		
		Properties prop = PiqueTestProperties.getProperties();
		Path blankqmFilePath = Paths.get(prop.getProperty("blankqm.filepath"));
		
		QualityModelImport qmImport = new QualityModelImport(blankqmFilePath);
        QualityModel qmDescription = qmImport.importQualityModel();
		
        for (ModelNode x : qmDescription.getDiagnostics().values()) {
        	Diagnostic diag = (Diagnostic) x;
        	if (diag.getToolName().equals(this.getName())) {
        		Finding fakeFind = new Finding("",99999,99999,1+(int)(10*Math.random()));
        		if (severity!=0) fakeFind = new Finding("",99999,99999,severity);
        		fakeFind.setName("FakeFinding");
        		diag.setChild(fakeFind);
        		fakeDiags.put(diag.getName(),diag);
        	}
        	
        }
		
		return fakeDiags;
	}

	@Override
	public Path initialize(Path toolRoot) {
		return null;
	}
	

}
