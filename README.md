# Transducer-Simulator

A simulator for different transducer models, written in Java

## Transducer models

* Deterministic Two-way Transducers (2DFT)
* Deterministic MSO transducer (MSOT)
* (copyless) Streaming String Transducer (SST)

## Translation algorithms between models

* 2DFT <> SST

# Instructions

## Testing examples
All testing examples are stored in ./src/test/  
Example 2DFT that performs trasduction function one: ./src/test/tdftf1.txt  
Description of each transduction function and MSO encoding rules are in file example_model.txt

## Compile instructions
* For windows:  
    javac -d bin .\src\simulator\transducer\*.java .\src\simulator\util\*.java .\src\simulator\*.java  
    java -cp bin simulator.Simulator
* For linux:  
    javac -d bin ./src/simulator/transducer/*.java ./src/simulator/util/*.java ./src/simulator/*.java  
    java -cp bin simulator.Simulator

Compile tex file using an editor such as TeXstudio or command lines to get the pdf file of the state diagram.
## Extra information
Enter q to return to previous menu