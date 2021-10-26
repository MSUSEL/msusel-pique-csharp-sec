package calibrationTests;


import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import calibration.BinaryCWEWeighter;
import pique.calibration.IWeighter;
import pique.calibration.WeightResult;
import pique.model.QualityModel;
import pique.model.QualityModelImport;
import utilities.PiqueTestProperties;

public class BinaryCWEWeighterTests {

	@Test
	public void SimpleCWEWeighterTest() {
		String pathToCsv = "src/test/resources/comparisons.csv";
		
		Properties prop = PiqueTestProperties.getProperties();
		
		Path blankqmFilePath = Paths.get(prop.getProperty("blankqm.filepath"));
		
		QualityModelImport qmImport = new QualityModelImport(blankqmFilePath);
        QualityModel qmDescription = qmImport.importQualityModel();
        
        IWeighter weighter = new BinaryCWEWeighter();
        Set<WeightResult> results = weighter.elicitateWeights(qmDescription, Paths.get(pathToCsv));
        //Just check to make sure a few values are as expected
        
        //check to make sure every value is found in the weights
        boolean[] checks = new boolean[5];
        
        for (WeightResult wr : results) {
        	
        	Map<String, BigDecimal> wrWeights = wr.getWeights();
        	if (wr.getName().equals("Integrity")) {
        		checks[0]=true;
        		assert(wrWeights.get("Category CWE-234").compareTo(new BigDecimal(0.5))==0);
        		assert(wrWeights.get("Category CWE-123").compareTo(new BigDecimal(0.5))==0);
        	}
        	else if (wr.getName().equals("Confidentiality")) {
        		checks[1]=true;
        		assert(wrWeights.get("Category CWE-234").compareTo(new BigDecimal(1))==0);
        		assert(wrWeights.get("Category CWE-123").compareTo(new BigDecimal(0))==0);
        	}
        	else if (wr.getName().equals("Yara email Measure")) {
        		checks[2]=true;
        		assert(wrWeights.get("Yara email Diagnostic").compareTo(new BigDecimal(1))==0);
        	}
        	else if (wr.getName().equals("Category CWE-123")) {
        		checks[3]=true;
        		assert(wrWeights.get("CVE-CWE-123 Measure").compareTo(new BigDecimal(0.5))==0);
        		assert(wrWeights.get("CWE-123 Weakness Measure").compareTo(new BigDecimal(0.5))==0);
        	}
        	else if (wr.getName().equals("Binary Security Quality")) {
        		checks[4]=true;
        		assert(wrWeights.get("Confidentiality").compareTo(new BigDecimal(0.4))<0); //should be 0.33333...
        		assert(wrWeights.get("Integrity").compareTo(new BigDecimal(0.6))>0); //should be 0.6666...
        	}
        }
    	for (boolean x : checks) {
    		assert(x==true);
    	}
	}
}
