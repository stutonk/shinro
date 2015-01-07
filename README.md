shinro
======

##A Java project for a Shinro puzzle generator and game

###Generator
The generator is based on [ideas and a genetic algorithm originally proposed by David Oranchak](http://oranchak.com/evostar-paper.pdf) and has been designed to be fully automated. It takes no command line parameters but there are several tweakable parameters in the source file itself to create puzzles with a specific number of moves, symmetry, etc. Generated puzzles are outputted as a plain text of space-delimited integers with names that look like `shinro999g_YP_100f_2210552000`. See the source commentary for additional details.

####Instructions:
1. git clone https://github.com/stutonk/shinro.git
2. cd shinro
3. javac generator/ShinroGenerator.java
4. java generator/ShinroGenerator
