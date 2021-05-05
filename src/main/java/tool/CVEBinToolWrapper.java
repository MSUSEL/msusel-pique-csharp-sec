package tool;

import pique.analysis.ITool;
import pique.model.Diagnostic;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class CVEBinToolWrapper implements ITool {


    @Override
    public Path analyze(Path path) {
        path = path.toAbsolutePath();

        File outputFile = new File(System.getProperty("results.directory") + "output");

        return null;
    }

    @Override
    public Map<String, Diagnostic> parseAnalysis(Path path) {
        return null;
    }

    @Override
    public Path initialize(Path path) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
