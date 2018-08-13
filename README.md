shinro
======

## A Shinro game library and puzzle (pack) generator in Java

### General Instructions:
1. git clone https://github.com/stutonk/shinro.git
2. cd shinro
3. javac \*/\*.java

### ShinroGenerator
The generator is based on [ideas and a genetic algorithm originally proposed by David Oranchak](http://oranchak.com/evostar-paper.pdf) and has been designed to be fully automated. It takes no command line parameters but there are several tweakable parameters in the source file itself to create puzzles with a specific number of moves, symmetry, etc. Generated puzzles are written as a plain text of space-delimited integers with names that look like `shinro_999g_YP_100f_2210552000`. See the source commentary for additional details.

Currently, clustering and symmetry have been disabled due to concerns about the generator's correctness. The generator will often converge on unsolvable puzzles if the parameters aren't exactly right (in which case, it just gives up without result). There doesn't seem to be a very clear way to alter the fitness function to select for a solvable puzzle. Additionally, what exactly constitute the correct parameters seems to vary from difficultyFactor to difficultyFactor. For that reason, RANDOMIZEALL automation is not currently reccommended. Most puzzles seem to be want to be in the 25-33 minMoves range. For difficultyFactors > 4, the numOfDifficulty should be <= 4.

A big TODO is to provide some command line functionality for tweaking the generator parameters.

#### Instructions:
Currently automation with RANDOMIZEALL is NOT recommended. You're going to have to tweak the various parameters (specifically minMoves, difficultyFactor and numOfDifficulty), recompile with "javac generator/ShinroGenerator.java" and then run with "java generator/ShinroGenerator" for every puzzle you want to generate.

### PuzzlePackGenerator
This takes puzzles generated by ShinroGenerator and collects them into a single file where the puzzles are ordered by
difficulty. Have a look at the source commentary for details about the layout of the generated pack file. The desired
pack name (which may have spaces) is the only command line argument.

#### Instructions:
After generating a few puzzles with ShinroGenerator, run with "java generator/PuzzlePackGenerator Pack Name Here"
