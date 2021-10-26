# msusel-pique-bin
## Introduction
This project is an operationalized PIQUE model for the assessment of security quality in binary files. 

PIQUE is not yet added to the Maven central repository, so this project will need to be [built](#building) and installed (via Maven) before it can be used as a library. 
___
## Tools
These will be automatically packaged with the PIQUE-Bin repository and .jar file, or installed through python/docker.

- [YARA](http://virustotal.github.io/yara/) and the [Yara-Rules repository](https://github.com/Yara-Rules/rules)
- [CVE-Bin-Tool](https://github.com/intel/cve-bin-tool)
- [CWE_Checker](https://github.com/fkie-cad/cwe_checker)
___

## Build Environment
- Java 8
- Maven
- Python 3.7+
- Docker
- [PIQUE](https://github.com/MSUSEL/msusel-pique)
___
## Building
1. Ensure the [Build Environment](#build-environment) requirements are met, including having already built [PIQUE](https://github.com/MSUSEL/msusel-pique).
2. Clone repository into `<project_root>` folder.
3. Derive the model as defined in the [Model Derivation](#model-derivation) section.
4. Run `mvn package` from `<project_root>`.
PIQUE-Bin is now available as a .jar file in the `target` folder. 
___
## Run Environment
- Java 8
- Python 3.7+
- Docker
___
## Running
Running PIQUE-Bin involves two main steps. 

### Model Derivation
First the model must be configured in the `src/main/resources/pique-bin.properties` file. Then, the model must be derived using a benchmark repository. This is done by running the `src/main/java/piquebinaries/runnable/QualityModelDeriver.java` file.

### Binary Analysis
Finally, the `src/main/java/piquebinaries/runnable/SingleProjectEvaluator.java` file may be run to analyze a binary. This will produce output in the `/out` folder. This can also be done through running the .jar file produced when the project is built. 


## Notes
Currently, we have not done testing to ensure that this will run on Linux or Mac. Additionally, a space in the path at any point appears to break the system, so if you are running into trouble that could be the cause.
