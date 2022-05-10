package utilitiesTests;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import pique.model.Diagnostic;
import utilities.PiqueTestProperties;
import utilities.helperFunctions;

public class helperFunctionTests {


	@Test
	public void testFileReadingDoesNotError() {
		Properties prop = PiqueTestProperties.getProperties();
		Path p = Paths.get(prop.getProperty("blankqm.filepath"));
		try {
			assert(helperFunctions.readFileContent(p).length()>0);
		} catch (IOException e) {
			fail();
		}
	}
	
	@Test
	public void testFileFormatting() {
		String exampleFile = "/usr/Andrew Johnson/example";
		exampleFile = helperFunctions.formatFileWithSpaces(exampleFile);
		assert(exampleFile.equals("/usr/'Andrew Johnson'/example"));
		
		exampleFile = "\\usr\\Andrew Johnson\\example";
		exampleFile = helperFunctions.formatFileWithSpaces(exampleFile);
		assert(exampleFile.equals("\\usr\\'Andrew Johnson'\\example"));
	}
	
	@Test
	public void testInitializeDiagnostics() {
		//this is hard-coded to look at the main/resources files, so we will use a real tool in this case.
		Map<String,Diagnostic> map = helperFunctions.initializeDiagnostics("not a tool");
		assert(map.values().size()==0);
		
		map = helperFunctions.initializeDiagnostics("security-code-scan");
		assert(map.values().size()==31);
		
		
	}
	
}
