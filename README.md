# msusel-pique-csharp-sec
## Introduction
This project is an operationalized PIQUE model for the assessment of security quality in C# source code projects.

PIQUE is not yet added to the Maven central repository, so this project will need to be [built](#building) and installed (via Maven) before it can be used as a library.
___
## Tools
These will be automatically packaged with the PIQUE-C#-Sec repository and .jar file, or installed through python/docker.

- [Security Code Scan](https://security-code-scan.github.io/)
- [Insider](https://github.com/insidersec/insider)
___

## Build Environment
- Java 8
- Maven
- MSBuild
- R 3.6.1+
- [PIQUE](https://github.com/MSUSEL/msusel-pique)
___
## Building
1. Ensure the [Build Environment](#build-environment) requirements are met, including having already built [PIQUE](https://github.com/MSUSEL/msusel-pique).
2. Clone repository into `<project_root>` folder.
3. Derive the model as defined in the [Model Derivation](#model-derivation) section.
4. Run `mvn package` from `<project_root>`.
   PIQUE-C#-Sec is now available as a .jar file in the `target` folder.
___
## Running
Running PIQUE-C#-Sec involves two main steps.

### Model Derivation
First the model must be configured in the `src/main/resources/pique-csharp-sec.properties` file. Then, the model must be derived using a benchmark repository. This is done by running the `src/main/java/piquecsharpsec/runnable/QualityModelDeriver.java` file.

### Project Evaluator
Finally, the `src/main/java/piquecsharpsec/runnable/SingleProjectEvaluator.java` file may be run to analyze a C# source code project. This will produce output in the `/out` folder. This can also be done through running the .jar file produced when the project is built.

## Deployment - Run quality assessment via a JAR file

### Downloads
- Step 1: Download [msusel-pique-csharp-sec-0.0.1-jar-with-dependencies.jar](https://github.com/MSUSEL/msusel-pique-csharp-sec/blob/main/target/msusel-pique-csharp-sec-0.0.1-jar-with-dependencies.jar) into a directory that contains the project needed to be analyzed.
- Step 2: Download [csharp-opensource](https://github.com/MSUSEL/benchmarks/tree/main/csharp-opensource) benchmark repository into a directory that contains the project needed to be analyzed (we have provided the ``bench-csharp-combo`` benchmark repository for quick testing as it only contains three projects).
- Step 3: Include the project needed to be analyzed into the directory (we have provided the ``choco`` project in the directory that can be used if you do not have a project to be analyzed and just want to test the jar file).

### Included in v1.0 Zip File
- The [pique-csharp-sec.properties](https://github.com/MSUSEL/msusel-pique-csharp-sec/blob/main/src/main/resources/pique-csharp-sec.properties) file is already included in the directory.
- The [full-pique-csharp-sec_description.json](https://github.com/MSUSEL/msusel-pique-csharp-sec/blob/main/src/main/resources/full-pique-csharp-sec_description.json) file is already included in the directory.

### Pointers
- Step 4: Point project.root= to your desired project in pique-csharp-sec.properties file.
- Step 5: Point benchmark.repo= to your desired benchmark repository (csharp-opensource) in pique-csharp-sec.properties file.

### Run
- Step 6 (Model Derivaton): run `java -jar msusel-pique-csharp-sec-0.0.1-jar-with-dependencies.jar -d` to derive a derived model.
- Step 7 (Project Evaluator): run `java -jar msusel-pique-csharp-sec-0.0.1-jar-with-dependencies.jar -e` to evaluate.
- Step 8: Find the evaluated result file in /out directory.

## Notes
Currently, we have not done testing to ensure that this will run on Linux or Mac. Additionally, a space in the path at any point appears to break the system, so if you are running into trouble that could be the cause.
