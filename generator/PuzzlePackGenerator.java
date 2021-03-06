package generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import shinro.ShinroPuzzle;
import shinro.ShinroSolver;

/**
 * Creates a difficulty-ordered puzzle pack from files generated by 
 * generator.ShinroGenerator
 * <p>
 * The program takes a single parameter: the puzzle pack name, which can contain
 * spaces.
 * <p>
 * The pack has the format:
 * PACKNAME
 * numPuzzles: k
 * Puzzle 1
 * difficulty: d
 * 0 0 0 0 0 0 0 0... 
 * ...
 * Puzzle n
 * difficulty: d(sub n)
 * 0 0 0 0 0 0 0 0...
 * 
 * @author Joseph Eib
 * @since January 2015
 */
public class PuzzlePackGenerator {
	
	private static final String PACKPREFIX = "pack";
	private static String packName = "Default Pack";
	
	/* This is just a one-shot inner class for facilitating the ease of sorting
	 * the puzzles by difficulty. Nothing to see here!
	 */
	private static class PuzzleInfo implements Comparable<PuzzleInfo> {
		int difficulty;
		String puzzleString;
		
		public PuzzleInfo(int difficulty, String puzzleString) {
			this.difficulty = difficulty;
			this.puzzleString = puzzleString;
		}

		@Override
		public int compareTo(PuzzleInfo arg0) {
			if (this.difficulty == arg0.difficulty) {
				return 0;
			}
			else if (this.difficulty > arg0.difficulty) {
				return 1;
			}
			else {
				return -1;
			}
		}		
	}

	/**
	 * PuzzlePackGenerator entry point.
	 * <p>
	 * This program takes a single command-line parameter, the puzzle pack name.
	 * The pack name CAN have spaces. Please note: behavior with an escape-containing
	 * string has not been tested.
	 * @param args  the puzzle pack name
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			packName = "";
			for (String string : args) {
				packName += string + " ";
			}
			packName = packName.trim();
		}
		System.out.println("Creating puzzle pack '" + packName + "'");
		String filename = PACKPREFIX + packName.replaceAll("\\p{Z}","");
		filename = filename.toLowerCase(); //Android needs lowercase
		
		try {
			File dir = new File("."); //The directory the program is in
			File[] files = dir.listFiles();
			ArrayList<File> puzzles = new ArrayList<File>(); 
			
			//Create an ArrayList of puzzles
			for (File file : files) {
				if (file.getName().length() > 7 
						&& file.getName().substring(0, 6).equals("shinro")) {
					puzzles.add(file);
				}
			}
			
			int numPuzzles = puzzles.size();
			
			if (numPuzzles == 0) {
				System.out.println("No shinro puzzle files in this directory. "
						+ "Terminating.");
				System.exit(1);
			}
			
			//Read all the puzzles into an ArrayList
			PrintWriter writer = new PrintWriter(filename);
			writer.println(packName);
			
			writer.println("numPuzzles: " + numPuzzles);
			
			ArrayList<PuzzleInfo> packPuzzles = new ArrayList<PuzzleInfo>();
			
		    for (File puzzle : puzzles) {
		    	Scanner reader = new Scanner(puzzle);
		    	String puzzleString = reader.nextLine();
		    	
		    	//read puzzle into a ShinroPuzzle so we can get get solver info
		    	int [][] puzzleInts = new int[ShinroPuzzle.SIZE][ShinroPuzzle.SIZE];
		    	int row = 0, col = 0;		    	
		    	Scanner puzzleReader = new Scanner(puzzleString);
		    	
		    	while (puzzleReader.hasNextInt()) {
		    		puzzleInts[row][col] = puzzleReader.nextInt();
		    		col++;
		    		if (col > ShinroPuzzle.SIZE - 1) {
		    			col = 0;
		    			row++;
		    		}
		    	}
		    	
		    	puzzleReader.close();
		    	
		    	ShinroPuzzle shinroPuzzle = new ShinroPuzzle(puzzleInts);
		    	ShinroSolver shinroSolver = new ShinroSolver(shinroPuzzle);
		    	int[] solverInfo = shinroSolver.solve();
		    	
		    	/* I sort of winged this difficulty calculation because I'm not
		    	 * exactly sure how to go about this particular kind of statistical
		    	 * analysis. The goal was to have a difficulty rating between 1 and
		    	 * 100 where 1 was the easiest puzzle possible and 100 is the
		    	 * hardest. Moves below difficulty 5 are trivial to identify so
		    	 * puzzles consisting mostly of these moves should have a very low
		    	 * difficulty score. Anyway, after tweaking this function I finally 
		    	 * achieved a good enough distribution to be moving on with. If you 
		    	 * happen to be peeping through this source and actually know what 
		    	 * you're doing in this arena, please contact me through the 
		    	 * appropriate channels and let me know so I can have a more robust 
		    	 * difficulty rating.
		    	 */
		    	int weightedSum = 0, difficulty = 0;
		    	double difficultyQuotient = 0;		    	
		    	for (int i = 1; i < solverInfo.length - 1; i++) {
	    			weightedSum += solverInfo[i] * i * i * i * i;
		    	}
		    	
		    	difficultyQuotient = weightedSum 
		    			/ solverInfo[0]; // divided by totalNumMoves		    	
		    	difficulty = (int)(Math.round(difficultyQuotient / 500 * 100));
		    	
		    	packPuzzles.add(new PuzzleInfo(difficulty, puzzleString));
		    	
		    	reader.close();
		    }
		    //Sort the list of puzzles based on difficulty and insert into pack
		    Collections.sort(packPuzzles);
		    
		    int puzzleCount = 1;
		    for (PuzzleInfo puzzle : packPuzzles) {
		    	writer.println("Puzzle " + puzzleCount++); //increments puzzleCount
		    	writer.println("difficulty " + puzzle.difficulty);
		    	writer.println(puzzle.puzzleString);
		    }
			
		    System.out.println("Created pack with " + numPuzzles + " puzzles.");
			writer.close();
		}
		catch (IOException e) {
			System.out.println("IO Error: " + e.getMessage());
		}
	}
}
