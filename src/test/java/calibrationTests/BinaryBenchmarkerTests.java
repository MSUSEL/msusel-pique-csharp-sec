package calibrationTests;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import calibration.BinaryBenchmarker;
import utilities.PiqueTestProperties;

import pique.analysis.ITool;
import pique.calibration.IBenchmarker;
import pique.model.QualityModel;
import pique.model.QualityModelImport;
import tool.YaraRulesToolWrapper;

import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tool.TestTool;

public class BinaryBenchmarkerTests {

	@Test
	public void basicSDTest() {
		Properties prop = PiqueTestProperties.getProperties();
		
		Path benchmark = Paths.get(prop.getProperty("benchmark.repo"));
		Path blankqmFilePath = Paths.get(prop.getProperty("blankqm.filepath"));
		String project = Paths.get(prop.getProperty("project.root")).toString();
		
		QualityModelImport qmImport = new QualityModelImport(blankqmFilePath);
        QualityModel qmDescription = qmImport.importQualityModel();
		
        Set<ITool> tools = Stream.of(new TestTool()).collect(Collectors.toSet());
        
        
        IBenchmarker b = qmDescription.getBenchmarker();
		Map<String, BigDecimal[]> thresholds = b.deriveThresholds(benchmark,qmDescription, tools, project);
		
		for (BigDecimal[] x : thresholds.values()) {
			assert(x[1].compareTo(new BigDecimal("0"))>0); //assert upper threshold isn't zero
			assert(x[0].compareTo(new BigDecimal("0"))>=0); //assert lower threshold isn't negative
		}
	}
	
	@Test
	public void specificValueTest() {
		Properties prop = PiqueTestProperties.getProperties();
		
		Path benchmark = Paths.get(prop.getProperty("benchmark.repo"));
		Path blankqmFilePath = Paths.get(prop.getProperty("blankqm.filepath"));
		String project = Paths.get(prop.getProperty("project.root")).toString();
		
		QualityModelImport qmImport = new QualityModelImport(blankqmFilePath);
        QualityModel qmDescription = qmImport.importQualityModel();
		
        int severity = 5;
        Set<ITool> tools = Stream.of(new TestTool(severity)).collect(Collectors.toSet());
        
        
        IBenchmarker b = qmDescription.getBenchmarker();
		Map<String, BigDecimal[]> thresholds = b.deriveThresholds(benchmark,qmDescription, tools, project);
		
		for (BigDecimal[] x : thresholds.values()) {
			assert(x[1].compareTo(new BigDecimal(severity))==0); //mean is severity and sd is 0, thresholds should be [severity,severity]
		}
	}
}
