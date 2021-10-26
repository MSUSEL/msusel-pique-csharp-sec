package experiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import pique.analysis.ITool;
import pique.model.Diagnostic;
import pique.model.ModelNode;
import tool.CVEBinToolWrapper;
import tool.CWECheckerToolWrapper;
import tool.YaraRulesToolWrapper;
import utilities.PiqueProperties;

public class ToolRunner {

	public static void main(String[] args) {
		Properties prop = PiqueProperties.getProperties();

        // Initialize objects
        Path benchmarkRepo = Paths.get(prop.getProperty("benchmark.repo"));
        benchmarkRepo = Paths.get(benchmarkRepo.toFile().getParent()+"/benchmark/");

        Path resources = Paths.get(prop.getProperty("blankqm.filepath")).getParent();
        
        //init tools
        ITool cweCheckerTool = new CWECheckerToolWrapper();
        ITool yaraRulesWrapper = new YaraRulesToolWrapper(resources);
        ITool cveBinTool = new CVEBinToolWrapper();
        Set<ITool> tools = Stream.of(cveBinTool,cweCheckerTool, yaraRulesWrapper).collect(Collectors.toSet());

        
        File[] files = benchmarkRepo.toFile().listFiles();
        Path outputDest = Paths.get(prop.getProperty("results.directory"));
		File outputFile = Paths.get(outputDest.toString() + "/FullFindingData.txt").toFile();
		
        //init diagnostic names
		Set<String> yaraDiags = yaraRulesWrapper.parseAnalysis(yaraRulesWrapper.analyze(files[0].toPath())).keySet();
		Set<String> cweDiags = cweCheckerTool.parseAnalysis(cweCheckerTool.analyze(files[0].toPath())).keySet();
		Set<String> cveDiags = cveBinTool.parseAnalysis(cveBinTool.analyze(files[0].toPath())).keySet();
        List<String> allDiags = new ArrayList<String>();
        allDiags.addAll(yaraDiags);
        allDiags.addAll(cweDiags);
        allDiags.addAll(cveDiags);
        
        appendToFile(outputFile,",");
        
        Map<String,Integer> temp = getMapOfZeros(allDiags);
        for (String x : temp.keySet()) { //doing it this way to make sure we get the same order as we will later on
        	appendToFile(outputFile,x+",");
        }
        appendln(outputFile,"");
        
		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean check = false;
		for (File x : files) {
			if (!check) { //use this to start at a certain file in case execution is interrupted
				if (x.getName().equals("hashdeep")) check = true;
				else continue;
			}
			System.out.println(x.getName());
			appendToFile(outputFile,x.getName() + ",");
			Map<String,Integer> findingCounts = getMapOfZeros(allDiags);
			tools.forEach(tool -> {
				Map<String,Diagnostic> out = tool.parseAnalysis(tool.analyze(x.toPath()));
				assignValues(out,findingCounts);
			});
			for (String key : findingCounts.keySet()) {
				appendToFile(outputFile,findingCounts.get(key)+",");
			}
			appendln(outputFile,"");
        }
	       
	}
	
	private static Map<String,Integer> getMapOfZeros(List<String> keys) {
		Map<String,Integer> retMap = new HashMap<String,Integer>();
		for (String x: keys) {
			retMap.put(x, 0);
		}
		return retMap;
	}
	
	private static int findingCount(Map<String, Diagnostic> out) {
		int count = 0;
		for (Diagnostic d : out.values()) {
			count+=d.getNumChildren();
		}
		return count;
	}
	
	private static Map<String,Integer> assignValues(Map<String,Diagnostic> toolOutput, Map<String,Integer> diagnostics) {
		for (String key : toolOutput.keySet()) {
			Set<String> findings = toolOutput.get(key).getChildren().keySet();
			if (findings !=null) {
				diagnostics.put(key, diagnostics.get(key)+findings.size());
			}
		}
		return diagnostics;
	}

	private static List<String> setToList(Set<String> set) {
		List retList = new ArrayList<String>();
		for (Object x : set) {
			retList.add(x);
		}
		return retList;
	}
	
	public static void appendln(File f, String str) {
		appendToFile(f,str+"\n");
	}
	
	public static void appendToFile(File f, String str) {
		try (FileWriter fw = new FileWriter(f.getAbsolutePath(),true)){
			fw.write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
