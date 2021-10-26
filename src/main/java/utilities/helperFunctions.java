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
package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pique.model.Diagnostic;
import pique.model.ModelNode;
import pique.model.QualityModel;
import pique.model.QualityModelImport;

/**
 * Collection of common helper functions used across the project
 *
 */
public class helperFunctions {
	
	/**
	 * A method to check for equality up to some error bounds
	 * @param x The first number
	 * @param y	The second number
	 * @param eps The error bounds
	 * @return True if |x-y|<|eps|, or in other words, if x is within eps of y.
	 */
	public static boolean EpsilonEquality(BigDecimal x, BigDecimal y, BigDecimal eps) {
		BigDecimal val = x.subtract(y).abs();
		int comparisonResult = val.compareTo(eps.abs());
		if (comparisonResult==1) {
			return false;
		}
		else {
			return true;
		}
	}
	/**
	 * Adds a "-" to a CWE name. If a dash is already in the string, will return the string.
	 * @param cwe String of CWE in style "CWE125"
	 * @return String of CWE with dash, for example "CWE-125"
	 */
	public static String addDashtoCWEName(String cwe) {
		String dashed = cwe;
		if (!cwe.contains("-")) {
			String cweName = cwe.substring(0, 3);
			String cweNum = cwe.substring(3);
			dashed = cweName + "-" + cweNum;
		}
		return dashed;
	}
	
	/**
	 * Given a set of CVE names in a string separated by spaces, return the CWEs the CVEs are associated with
	 * @param cve A string of one or more CVE names, separated by spaces
	 * @return An array of CWEs associated with the given CVEs
	 */
	public static String[] getCWE(String cve) {
		String cwe = "";
		String pathToScript = System.getProperty("user.dir")+"/src/main/java/utilities/CVEtoCWE.py";
		String[] cmd = {"python", pathToScript, cve};
		
		
		try {
			cwe = getOutputFromProgram(cmd,false);
		} catch (IOException e) {
			System.err.println("Error running CVEtoCWE.py");
			e.printStackTrace();
		}
		String[] cwes = cwe.split("\n \n");
		return cwes;
	}
	
	 /**
	  * Taken directly from https://stackoverflow.com/questions/13008526/runtime-getruntime-execcmd-hanging
	  * 
	  * @param program - A string as would be passed to Runtime.getRuntime().exec(program)
	  * @return the text output of the command. Includes input and error.
	  * @throws IOException
	  */
	public static String getOutputFromProgram(String[] program, boolean print) throws IOException {
	    Process proc = Runtime.getRuntime().exec(program);
	    return Stream.of(proc.getErrorStream(), proc.getInputStream()).parallel().map((InputStream isForOutput) -> {
	        StringBuilder output = new StringBuilder();
	        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	            	if(print) {
	            		System.out.println(line);
	            	}
	                output.append(line);
	                output.append("\n");
	            }
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	        return output;
	    }).collect(Collectors.joining());
	}
	
	 /**
	  * 
	  * 
	  * @param filePath - Path of file to be read
	  * @return the text output of the file content.
	  * @throws IOException
	  */
	public static String readFileContent(Path filePath) throws IOException
    {
        StringBuilder contentBuilder = new StringBuilder();
 
        try (Stream<String> stream = Files.lines( filePath, StandardCharsets.UTF_8)) 
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) 
        {
            throw e;
        }
 
        return contentBuilder.toString();
    }
	
	/**
	 * This function finds all diagnostics associated with a certain toolName and returns them in a Map with the diagnostic name as the key.
	 * This is used common to initialize the diagnostics for tools.
	 * @param toolName The desired tool name
	 * @return All diagnostics in the model structure with tool equal to toolName
	 */
	public static Map<String, Diagnostic> initializeDiagnostics(String toolName) {
		// load the qm structure
		Properties prop = PiqueProperties.getProperties();
		Path blankqmFilePath = Paths.get(prop.getProperty("blankqm.filepath"));
		QualityModelImport qmImport = new QualityModelImport(blankqmFilePath);
        QualityModel qmDescription = qmImport.importQualityModel();

        Map<String, Diagnostic> diagnostics = new HashMap<>();
        
        // for each diagnostic in the model, if it is associated with this tool, 
        // add it to the list of diagnostics
        for (ModelNode x : qmDescription.getDiagnostics().values()) {
        	Diagnostic diag = (Diagnostic) x;
        	if (diag.getToolName().equals(toolName)) {
        		diagnostics.put(diag.getName(),diag);
        	}
        }
       
		return diagnostics;
	}	
	
	public static String formatFileWithSpaces(String pathWithSpace) {
		String retString = pathWithSpace.replaceAll("([a-zA-Z]*) ([a-zA-Z]*)", "'$1 $2'");
		return retString;
	}

}
