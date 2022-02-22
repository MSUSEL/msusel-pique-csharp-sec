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
- Python 3.7+
- Docker
- [PIQUE](https://github.com/MSUSEL/msusel-pique)
___
## Building
1. Ensure the [Build Environment](#build-environment) requirements are met, including having already built [PIQUE](https://github.com/MSUSEL/msusel-pique).
2. Clone repository into `<project_root>` folder.
3. Derive the model as defined in the [Model Derivation](#model-derivation) section.
4. Run `mvn package` from `<project_root>`.
PIQUE-C#-Sec is now available as a .jar file in the `target` folder. 
___
## Run Environment
- Java 8
- Python 3.7+
- Docker
___
## Running
Running PIQUE-C#-Sec involves two main steps. 

### Model Derivation
First the model must be configured in the `src/main/resources/pique-csharp-sec.properties` file. Then, the model must be derived using a benchmark repository. This is done by running the `src/main/java/piquecsharpsec/runnable/QualityModelDeriver.java` file.

### Binary Analysis
Finally, the `src/main/java/piquecsharpsec/runnable/SingleProjectEvaluator.java` file may be run to analyze a binary. This will produce output in the `/out` folder. This can also be done through running the .jar file produced when the project is built. 


## Notes
Currently, we have not done testing to ensure that this will run on Linux or Mac. Additionally, a space in the path at any point appears to break the system, so if you are running into trouble that could be the cause.
