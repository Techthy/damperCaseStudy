# Modular Active Spring-Damper System Case Study (MAFDS)


This repository contains a case study for managing uncertainty in a Modular Active Spring-Damper System (MAFDS) using the Vitruv framework. The case study demonstrates how to annotate mechanical models with quantified uncertainty information and maintain consistency across different model representations.

# Running the project

In order to run the project as usual with maven you need to have the StoEx project installed in your local maven repository
You can do this by downloading the StoEx project from [here](https://github.com/Techthy/stoex) and running the following command:

```bash
  mvn clean  install
``` 

in the project directory of StoEx. Afterwards you can run the project as usual with maven.



# File Structure
- `consistency/`: Consistency management between models using Vitruv
  - `src/main/reactions/tools/vitruv/methodologisttemplate/consistency/`: Reaction rules for model synchronization
  - `src/main/java/tools/vitruv/methodologisttemplate/consistency/`: Java helper classes for reactions
- `models/`: Example models
  - `example.mafds`: MAFDS model with uncertainty annotations
- `vsum/`: Test files
    - `src/test/java/tools/vitruv/methodologisttemplate/vsum/mafds`: Total weight calculation tests
    - `src/test/java/tools/vitruv/methodologisttemplate/vsum/uncertainty`: Utils for testing uncertainty annotations

# Data taken from:
- "Mastering Uncertainty in Mechanical Engineering" by Pelz et al.
