package generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import shinro.GridPos;
import shinro.ShinroPuzzle;
import shinro.ShinroSolver;

/**
 * Generates shinro puzzles and outputs them to a plain text file.
 * <p>
 * The genetic algorithm is based on ideas originally proposed by David Oranchak.
 * See http://oranchak.com/evostar-paper.pdf for more details.
 * <p>
 * There are a number of parameters that control the type of puzzle generated --
 * For a fully automated experience, leave 'RANDOMIZEALL' set to true. This allows
 * you to just set the least and greatest number of moves desired ('LEASTMOVES' and
 * 'MOSTMOVES') and the least difficulty factor the generator should select for
 * ('LEASTDIFFUCULY'). Otherwise, you can set 'RANDOMIZEALL' to false and 'minMoves'
 * as well as 'difficultyFactor' can be explicitly set to the desired number of moves
 * and difficulty factor to select for, respectively. See {@link shinro.ShinroSolver}
 * for information about difficulty factor values. 
 * <p> 
 * The generator will randomly enforce symmetry and clustering based on the
 * 'SYMMETRYRATE' the 'CLUSTERRATE' unless one of the associated parameters is 
 * explicitly set. When explicitly setting 'symmetry,' don't forget to also set 
 * one of either 'xAxis', 'yAxis', or 'rotational'. When explicitly setting 
 * 'cluster', you may optionally set 'clusterArrows' to enforce clustering of arrows
 * rather than clustering of points.  
 * 
 * @author Joseph Eib
 * @since January 2015
 */
public class ShinroGenerator {
	// general generator parameters
	private static int minMoves = 25;	
	private static int maxNoImprovement = 300; //should make this some function
	
	private static boolean symmetry = false;
	private static boolean xAxis = false;
	private static boolean yAxis = false;
	private static boolean rotational = false;
	
	private static boolean cluster = false;
	private static boolean clusterArrows = false;
	
	//fitness function parameters
	private static int difficultyFactor = 5;
	
	//automation constants
	private static final boolean RANDOMIZEALL = true; //minMoves and difficultyFactor
	private static final int LEASTMOVES = 15;
	private static final int MOSTMOVES = 40;
	private static final int LEASTDIFFICULTY = 4; //oneToPlace moves
	
	//constraint constants
	private static final int POPULATIONSIZE = 10;
	private static final int TOURNAMENTSIZE = 3;
	private static final int NUMPOINTS = 12;
	private static final double SYMMETRYRATE = 0.0007;
	private static final double CLUSTERRATE = 0.0007;
	
	//other constants
	private static final int PUZZSIZE = ShinroPuzzle.SIZE;
	
	/**
	 * Method which calculates the clustering function for a given puzzle
	 * <p>
	 * The clustering function is the summation of the per-item clustering
	 * @see #calcItemClustering(GridPos, ShinroPuzzle)
	 * @param puzzle  the puzzle whose clustering value is to be calculated
	 * @return a double representing the clustering function value for the puzzle
	 */
	private static double calcClustering(ShinroPuzzle puzzle) {
		double clustering = 0.0;
		ArrayList<GridPos> items = new ArrayList<GridPos>();
		if (clusterArrows) {
			items = puzzle.getListByType(ShinroPuzzle.N); //all arrows
		}
		else {
			items = puzzle.getListByType(ShinroPuzzle.POINT);
		}
		for (GridPos item : items) {
			clustering += calcItemClustering(item, puzzle);
		}		
		return clustering;
	}
	
	/**
	 * Calculates the fitness of a ShinroPuzzle based on the selection parameters
	 * <p>
	 * Fitness scores are based on the results of the solver. See
	 * {@link shinro.ShinroSolver} for more details.
	 * @see #epsilon(ShinroPuzzle, int)
	 * @param puzzle  the puzzle whose fitness is to be calculated
	 * @return a double representing the fitness of the puzzle
	 */
	private static double calcFitness(ShinroPuzzle puzzle) {
		double fitness = 0;
		ShinroPuzzle toSolve = puzzle.clone();
		ShinroSolver solver = new ShinroSolver(toSolve);
		int numMoves[] = solver.solve();
		
		fitness = epsilon(puzzle, numMoves[0]); //total num moves 
		fitness *= 1 - (1 / (1 + numMoves[difficultyFactor]));
		
		if (cluster) {
			fitness *= (1 - (1 / (1 + calcClustering(puzzle))));
		}
		
		return fitness;
	}
	
	/**
	 * Calculates the per-item clustering of a specific item in a certain puzzle
	 * <p>
	 * If an item of the same type has is found with diagonal adjacency, the weak 
	 * clustering value of .25 is added to the total sum. If an item of the same type
	 * is found with horizontal or vertical adjacency, the strong clustering value of
	 * 1.0 is added to the total sum.
	 * @param item  the item whose per-item clustering value is to be calculated
	 * @param puzzle  the puzzle in which the item is an element
	 * @return a double representing the per-item clustering for the specified item
	 */
	private static double calcItemClustering(GridPos item, ShinroPuzzle puzzle) {
		double itemClustering = 0.0;
		int itemRow = item.getRow(), itemCol = item.getCol();
		int row = 0, col = 0;
		
		if (itemRow == 0) {
			row = itemRow;
		}
		else {
			row = itemRow - 1;
		}
		
		if (itemCol == 0) {
			col = itemCol;
		}
		else {
			col = itemCol - 1;
		}
		
		for (/*nothing*/; row <= (itemRow + 1); row++) {
			//quit if out of bounds -- rows
			if (row >= PUZZSIZE) {
				break;
			}						
			for (/*nothing*/; col <= (itemCol + 1); col++) {
				//quit if out of bounds -- columns
				if (col >= PUZZSIZE) {
					break;
				}
				//calculate the clustering per item
				if ((puzzle.atPos(row, col) == puzzle.atPos(item))
						|| (clusterArrows && puzzle.isArrow(item))) {
					//skip if this is the item
					if (row == itemRow && col == itemCol) {
						continue;
					}
					//diagonal adjacency
					if ((row == (itemRow - 1) || row == (itemRow + 1))
							&& (col == (itemCol - 1) 
									|| col == (itemCol + 1))) {
						itemClustering += 0.25; //weak clustering
					}
					//vertical or horizontal adjacency
					else {
						itemClustering += 1.0; //strong clustering
					}
				}
			}
			if (itemCol == 0) {
				col = itemCol;
			}
			else {
				col = itemCol - 1;
			}
		}
		
		return itemClustering;
	}
	
	/**
	 * Counts the number of pointless arrows in a given puzzle
	 * <p>
	 * Pointless arrows do not have any point along their path
	 * @param puzzle  the puzzle whose number of pointless arrows is to be calculated
	 * @return the number of pointless arrows in the puzzle
	 */
	private static int countPointlessArrows(ShinroPuzzle puzzle) {
		int count = 0;
		for (GridPos arrow : puzzle.getListByType(ShinroPuzzle.N)) { //any arrow
			if (puzzle.getTypeInList(ShinroPuzzle.POINT,
					puzzle.getArrowToEdge(arrow)).size() == 0) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Counts the number of nonsymmetrical spaces in a given puzzle
	 * <p>
	 * What exactly constitutes a symmetrical space is based on which symmetry
	 * parameter is currently set to true.
	 * @param puzzle  the puzzle whose nonsymmetrical spaces are to be counted
	 * @return the number of nonsymmetrical spaces in the puzzle
	 */
	private static int countNonsymmetrical(ShinroPuzzle puzzle) {
		int violations = 0;
		
		if (!symmetry) {
			return violations;
		}
		
		for (int row = 0; row < PUZZSIZE; row++) {
			for (int col = 0; col < PUZZSIZE; col++) {
				int posValue = puzzle.atPos(row, col);
				if (posValue > 0) {
					if (xAxis && puzzle.atPos(mirror(row), col) 
							!= posValue) {
						violations++;
					}
					if (yAxis && puzzle.atPos(row, mirror(col))	!= posValue) {
						violations++;
					}
					if (rotational && puzzle.atPos(mirror(row), mirror(col)) 
							!= posValue) {
						violations++;
					}
				}
			}
		}
		
		return violations;
	}
	
	/**
	 * Calculates the normalized error count of a specific puzzle
	 * @param puzzle  the puzzle whose epsilon value is to be calculated
	 * @param totalMoves  the total number of moves it took to solve the puzzle
	 * @return a double representing the normalized error count of the puzzle
	 */
	private static double epsilon(ShinroPuzzle puzzle, int totalMoves) {
		double denominator = 
				1.0
				+ Math.abs(NUMPOINTS 
						- puzzle.getListByType(ShinroPuzzle.POINT).size())
				+ countPointlessArrows(puzzle)
				+ Math.abs((minMoves) - totalMoves);
		if (symmetry) {
			denominator += countNonsymmetrical(puzzle);
		}
				
		return 1.0 / denominator;				
	}
	
	/**
	 * Generates a new ShinroPuzzle based on David Oranchak's genetic algorithm
	 * <p>
	 * This method prints occasional status messages to the console in order to
	 * communicate progress.
	 * If the algorithm continues until MAXNOIMPROVMENT generations pass without any
	 * change in the puzzle's fitness. If the best-generated puzzle is invalid, the
	 * algorithm will continue until a valid puzzle is generated.
	 * @see #printStatsWritePuzzle(ShinroPuzzle, int, double)
	 * @return the generated puzzle
	 */
	public static ShinroPuzzle generatePuzzle() {
		ShinroPuzzle[] population = initPopulation();
		ShinroPuzzle elite = new ShinroPuzzle();
		double prevFitness = 0.0, newFitness = 0.0;
		int noImprovementCount = 0, numGenerations = 0;
		
		System.out.println("Generating puzzle...");
		System.out.println("Target moves: " + minMoves + ", Target difficulty: "
				+ difficultyFactor);
		
		while (true) {
			prevFitness = newFitness;
			population = nextGeneration(population);
			numGenerations++;
			elite = getElite(population);
			newFitness = calcFitness(elite);
			
			//Terminating conditions
			if (noImprovementCount > maxNoImprovement || newFitness == 1.0) {
				//if the puzzle is invalid
				if (newFitness == 0.0 
						|| elite.getListByType(ShinroPuzzle.POINT).size() != NUMPOINTS 
						|| (symmetry && countNonsymmetrical(elite) > 0)) {
					System.out.print("Invalid puzzle: continuing because ");
					if (newFitness == 0.0) {
						System.out.print("of zero fitness.");
					}
					else if (elite.getListByType(ShinroPuzzle.POINT).size() 
							!= NUMPOINTS) {
						System.out.print("there aren't the right number of points.");
					}
					else if (symmetry && countNonsymmetrical(elite) > 0) {
						System.out.print("of lack of required symmetry.");
					}					
					System.out.print("\n");
					//Adjust the count; consider updating this to be by some function
					noImprovementCount /= 2;
					continue; //resume algorithm
				}
				break; //terminate algorithm
			}
			//Update to approach termination
			if (prevFitness == newFitness) {
				noImprovementCount++;
			}
			else {
				noImprovementCount = 0;
				System.out.println("Fitness: " + newFitness);
			}
		}
		
		printStatsWritePuzzle(elite, numGenerations, newFitness);
		
		return elite;
	}
	
	/**
	 * Gets the elite genome in a given population
	 * @see #calcFitness(ShinroPuzzle)
	 * @param population  the population in which to find the elite genome
	 * @return the genome in the pouplation with the highest fitness value.
	 */
	private static ShinroPuzzle getElite(ShinroPuzzle[] population) {
		ShinroPuzzle elite = population[0];
		double currentFitness = 0, bestFitness = 0;
		
		for (int i = 0; i < population.length; i++) {
			currentFitness = calcFitness(population[i]);
			if (currentFitness > bestFitness) {
				bestFitness = currentFitness;
				elite = population[i];
			}
		}		
		return elite;
	}
	
	/**
	 * Encodes an initial population of genomes
	 * <p>
	 * For every space each genome, either an empty space, random arrow, or point
	 * is assigned with equal probability. Pointless arrows are removed to facilitate
	 * the generation of valid puzzles.
	 * @return an array of ShinroPuzzles of size POPULATIONSIZE which contains the
	 * newly-encoded genomes.
	 */
	private static ShinroPuzzle[] initPopulation() {
		Random rand = new Random();
		ShinroPuzzle[] population = new ShinroPuzzle[POPULATIONSIZE];
		for (int i = 0; i < population.length; i++) {
			population[i] = new ShinroPuzzle();
			for (int row = 0; row < PUZZSIZE; row++) {
				for (int col = 0; col < PUZZSIZE; col++) {
					try {
						//randomly assign an blank space, POINT, or arrow					
						switch (rand.nextInt(3)) {
						case 0: //population[i].setPos(row, col, ShinroPuzzle.EMPTY);
								break;
						case 1: population[i].setPos(row, col, ShinroPuzzle.POINT);
								break;
						case 2: population[i].setPos(row, col, 
									rand.nextInt(9 - 1) + 1);
								break;
						default:break;
						}					
					}
					catch (IllegalArgumentException e) {
						System.out.println(e.getMessage());
						System.out.println("Setting population[" + i + "].["
								+ row + "][" + col +"] to 0");
						population[i].clearSpace(row, col);
					}
				}
			}
			population[i].setHeaders();
			removePointlessArrows(population[i]);
		}
		return population;
	}
	
	public static void main(String[] args) {
		if (RANDOMIZEALL) {
			Random rand = new Random();
			minMoves = rand.nextInt(MOSTMOVES - LEASTMOVES) + LEASTMOVES;
			difficultyFactor = rand.nextInt(7 - LEASTDIFFICULTY) + LEASTDIFFICULTY;
		}
		
		generatePuzzle();
		
	}
	
	/**
	 * Finds the symmetrical mirror value of a row or column location in a 
	 * ShinroPuzzle
	 * @param loc  the row or column index of the location to mirror
	 * @return  an int representing the location mirrored across the central axis
	 */
	private static int mirror(int loc) {
		return (PUZZSIZE - 1 - loc);
	}
	
	/**
	 * Probabilistically applies a mutation to a ShinroPuzzle
	 * <p>
	 * After mutation any pointless arrows or, if symmetry is enforced, any 
	 * nonsymmetrical arrows are removed.
	 * The mutations are as follows:
	 * <ul>
	 * <li> Iterate through the puzzle and probabilistically mutate spaces
	 * <li> Randomly swap up to three pairs of spaces
	 * <li> Add an random arrow to a random space
	 * <li> Delete an arrow from a random space
	 * <li> Add a point to a random space
	 * <li> Delete a point from a random space
	 * </ul>
	 * @param puzzle  the puzzle to mutate
	 * @return the mutated puzzle
	 */
	private static ShinroPuzzle mutate(ShinroPuzzle puzzle) {
		Random rand = new Random();
		ShinroPuzzle mutated = puzzle.clone();
		int mutation = rand.nextInt(6); //six possibilities (0 to 5)

		if (mutation == 0) {
			//iterate through the puzzle and probabilistically mutate spaces
			double rate = Math.random();
			for (int i = 0; i < PUZZSIZE; i++) {
				for (int j = 0; j < PUZZSIZE; j++) {
					double r = Math.random();
					if (r <= rate) {
						switch (rand.nextInt(3)) {
						case 0: mutated.setPos(i, j, ShinroPuzzle.EMPTY);
								if (symmetry) {
									if (xAxis) {
										mutated.setPos(mirror(i), j, 
												ShinroPuzzle.EMPTY);
									}
									else if (yAxis) {
										mutated.setPos(i, mirror(j),
												ShinroPuzzle.EMPTY);
									}
									else if (rotational) {
										mutated.setPos(mirror(i), mirror(j),
												ShinroPuzzle.EMPTY);
									}
								}
								break;
						case 1: mutated.setPos(i, j, ShinroPuzzle.POINT);
								if (symmetry) {
									if (xAxis) {
										mutated.setPos(mirror(i), j, 
												ShinroPuzzle.POINT);
									}
									else if (yAxis) {
										mutated.setPos(i, mirror(j),
												ShinroPuzzle.POINT);
									}
									else if (rotational) {
										mutated.setPos(mirror(i), mirror(j),
												ShinroPuzzle.POINT);
									}
								}
								break;
						case 2: int randArrow = rand.nextInt(9 - 1) + 1;
								mutated.putArrow(i, j, randArrow);
								if (symmetry) {
									if (xAxis) {
										mutated.setPos(mirror(i), j, randArrow);
									}
									else if (yAxis) {
										mutated.setPos(i, mirror(j), randArrow);
									}
									else if (rotational) {
										mutated.setPos(mirror(i), mirror(j),
												randArrow);
									}
								}
								break;
						default:break;
						}
					}
				}
			}
		}
		else if (mutation == 1) { //nonsymmetrical
			//randomly swap up to three pairs of spaces
			int numTimes = rand.nextInt(3); //up to three times
			for (int i = 0; i < numTimes; i++) {
				int randRow1 = rand.nextInt(PUZZSIZE);
				int randCol1 = rand.nextInt(PUZZSIZE);
				int randRow2 = rand.nextInt(PUZZSIZE);
				int randCol2 = rand.nextInt(PUZZSIZE);
				int temp = mutated.atPos(randRow1, randCol1);
				mutated.setPos(randRow1, randCol1,
						mutated.atPos(randRow2, randCol2));
				mutated.setPos(randRow2, randCol2, temp);
				if (symmetry) {
					if (xAxis) {
						temp = mutated.atPos(mirror(randRow1), randCol1);
						mutated.setPos(mirror(randRow1), randCol1,
								mutated.atPos(mirror(randRow2), randCol2));
						mutated.setPos(mirror(randRow2), randCol2, temp);
					}
					else if (yAxis) {
						temp = mutated.atPos(randRow1, mirror(randCol1));
						mutated.setPos(randRow1, mirror(randCol1),
								mutated.atPos(randRow2, mirror(randCol2)));
						mutated.setPos(randRow2, mirror(randCol2), temp);
					}
					else if (rotational) {
						temp = mutated.atPos(mirror(randRow1), mirror(randCol1));
						mutated.setPos(mirror(randRow1), mirror(randCol1),
								mutated.atPos(mirror(randRow2), mirror(randCol2)));
						mutated.setPos(mirror(randRow2), mirror(randCol2), temp);
					}
				}
			}			
		}
		else if (mutation == 2) {
			//add an arrow to a random space
			int randRow = rand.nextInt(PUZZSIZE);
			int randCol = rand.nextInt(PUZZSIZE);
			int randArrow = rand.nextInt(9 - 1) + 1; //1 to 8
			mutated.setPos(randRow, randCol, randArrow);
			if (symmetry) {
				if (xAxis) {
					mutated.setPos(mirror(randRow),	randCol, randArrow);
				}
				else if (yAxis) {
					mutated.setPos(randRow,	mirror(randCol), randArrow);
				}
				else if (rotational) {
					mutated.setPos(mirror(randRow),	mirror(randCol), randArrow);
				}
			}
		}
		else if (mutation == 3) {
			//delete a random arrow
			ArrayList<GridPos> arrows = 
					mutated.getListByType(ShinroPuzzle.N); //all arrows
			if (arrows.size() == 0) {
				return mutate(puzzle);
			}
			GridPos which = arrows.get(rand.nextInt(arrows.size()));
			mutated.setPos(which, ShinroPuzzle.EMPTY);
			if (symmetry) {
				if (xAxis) {
					mutated.setPos(mirror(which.getRow()),	which.getCol(),
							ShinroPuzzle.EMPTY);
				}
				else if (yAxis) {
					mutated.setPos(which.getRow(), mirror(which.getCol()),
							ShinroPuzzle.EMPTY);
				}
				else if (rotational) {
					mutated.setPos(mirror(which.getRow()), mirror(which.getCol()),
							ShinroPuzzle.EMPTY);
				}
			}
		}
		else if (mutation == 4) {
			//add a POINT to a random space
			int randRow = rand.nextInt(PUZZSIZE);
			int randCol = rand.nextInt(PUZZSIZE);
			mutated.setPos(randRow, randCol, ShinroPuzzle.POINT);
			if (symmetry) {
				if (xAxis) {
					mutated.setPos(mirror(randRow), randCol, ShinroPuzzle.POINT);
				}
				else if (yAxis) {
					mutated.setPos(randRow, mirror(randCol), ShinroPuzzle.POINT);
				}
				else if (rotational) {
					mutated.setPos(mirror(randRow),	mirror(randCol),
							ShinroPuzzle.POINT);
				}
			}
		}
		else if (mutation == 5) {
			//delete a random POINT
			ArrayList<GridPos> points = puzzle.getListByType(ShinroPuzzle.POINT);
			if (points.size() == 0) {
				return mutate(puzzle);
			}
			GridPos which = points.get(rand.nextInt(points.size()));
			mutated.setPos(which, ShinroPuzzle.EMPTY);
			if (symmetry) {
				if (xAxis) {
					mutated.setPos(mirror(which.getRow()),	which.getCol(),
							ShinroPuzzle.EMPTY);
				}
				else if (yAxis) {
					mutated.setPos(which.getRow(), mirror(which.getCol()),
							ShinroPuzzle.EMPTY);
				}
				else if (rotational) {
					mutated.setPos(mirror(which.getRow()), mirror(which.getCol()),
							ShinroPuzzle.EMPTY);
				}
			}
		}
		mutated.setHeaders();
		removePointlessArrows(mutated);
		if (symmetry) {
			removeNonsymmetrical(mutated);
		}
		return mutated;
	}
	
	/**
	 * Generates the next generation of genomes.
	 * <p>
	 * New populations are generated by finding the elite genome from the last generation
	 * and reproducing it, then running tournament selections on the previous
	 * generation and mutating the victors until the new generation is complete. This
	 * method may randomly decide to enforce symmetry or clustering based on their
	 * respective rate constants.
	 * @param population  an array of ShinroPuzzles representing the previous
	 * generation's population
	 * @return an array of ShinroPuzzles representing the new generation's population
	 */
	private static ShinroPuzzle[] nextGeneration(ShinroPuzzle[] population) {	
		ShinroPuzzle[] nextGen = new ShinroPuzzle[population.length];
		Random rand = new Random();
		double r = Math.random(); //instantaneous rate for comparison
		
		//randomly enforce symmetry
		if (!symmetry && r <= SYMMETRYRATE) {
			System.out.print("Enforcing symmetry: ");
			symmetry = true;
			switch (rand.nextInt(3)) {
			case 0: xAxis = true;
					System.out.print("X Axis\n");
					break;
			case 1: yAxis = true;
					System.out.print("Y Axis\n");
					break;
			case 2: rotational = true;
					System.out.print("Rotational\n");
					break;
			default:break;
			}
		}
		
		//randomly enforce clustering
		r = Math.random();
		if (!cluster && r <= CLUSTERRATE) {
			System.out.print("Enforcing clustering ");
			cluster = true;
			if (rand.nextInt(5) == 0) {
				clusterArrows = true;
				System.out.print("of arrows.\n");
			}
			else {
				System.out.print("of points.\n");
			}
		}

		nextGen[0] = getElite(population);		
		for (int i = 1; i < nextGen.length; i++) {		
			nextGen[i] = mutate(runTournament(population));
		}
		
		return nextGen;
	}
	
	/**
	 * Prints statistics about a puzzle and writes the puzzle to a file.
	 * <p>
	 * This method prints statistics about the puzzle including the number of 
	 * generations, target number of moves and difficulty factor, final fitness, any
	 * symmetry or clustering, and the info from the solver. See
	 * {@link shinro.ShinroSolver} for additional details. 
	 * <p>
	 * The generated puzzle is also written to a plain text file as a sequence of space-
	 * delimited integers whose filename is of the following format:
	 * "shinro_HHHg_SC_KKKf_NN11234567" where "HHH" is the number of generations,
	 * S is the type of symmetry, if any, C is the type of clustering, if any, KKK
	 * is the final fitness, NN is the total number of moves, and the rest of the
	 * numbers are the number of moves per difficulty factor.  
	 * @param puzzle the puzzle whose statistics to print
	 * @param numGens the number of generations it took to generate this puzzle
	 * @param fitness the final fitness of the puzzle when generated
	 */
	private static void printStatsWritePuzzle(ShinroPuzzle puzzle, 
			int numGens, double fitness) {
		String fileString = "shinro_";
		
		System.out.println("\n" + puzzle);
		System.out.println("Min moves: " + minMoves + ", difficulty factor: "
				+ difficultyFactor);
		System.out.println("Total generations: " + numGens);
		fileString += String.format("%dg_", numGens);
		if (symmetry) {
			System.out.print("Symmetry: ");
			if (xAxis) {
				System.out.print("X Axis");
				fileString += "X";
			}
			else if (yAxis) {
				System.out.print("Y Axis");
				fileString += "Y";
			}
			else if (rotational) {
				System.out.print("Rotational");
				fileString += "R";
			}
			System.out.print("\n");
		}
		if (cluster) {
			System.out.print("Clustering ");
			if (clusterArrows) {
				System.out.print("of arrows.");
				fileString += "A";
			}
			else {
				System.out.print("of points.");
				fileString += "P";
			}
			System.out.print("\n");
		}
		if (symmetry || cluster) {
			fileString += "_";
		}
		System.out.println("Final fitness: " + fitness);
		fileString += String.format("%03df_", (int)(fitness * 100));
		
		System.out.print("Solver info: ");
		ShinroPuzzle puzzleCopy = puzzle.clone();
		ShinroSolver ss = new ShinroSolver(puzzleCopy);
		for (Integer i : ss.solve()) {
			System.out.print(i + " ");
			fileString += i;
		}
		System.out.println();
		
		File outFile = new File(fileString);
		try {
			outFile.createNewFile();
			PrintWriter out = new PrintWriter(outFile);
			int[][] puzzleInts = puzzle.toIntMatrix();
			for (int i = 0; i < PUZZSIZE; i++) {
				for (int j = 0; j < PUZZSIZE; j++) {
					out.print(puzzleInts[i][j] + " ");
				}
				out.println();
			}
			System.out.println("File '" + fileString + "' created successfully.");
			out.close();
		}
		catch (IOException e) {
			System.out.println("Crirical error -- could not create output file: "
					+ e.getMessage());
		}
	}
	
	/**
	 * Removes any nonsymmetrical spaces from a given puzzle
	 * <p>
	 * What exactly constitutes a symmetrical space is based on which symmetry
	 * parameter is currently set to true.
	 * @param puzzle  the puzzle whose nonsymmetrical spaces are to be removed
	 */
	private static void removeNonsymmetrical(ShinroPuzzle puzzle) {		
		for (int row = 0; row < PUZZSIZE; row++) {
			for (int col = 0; col < PUZZSIZE; col++) {
				int posValue = puzzle.atPos(row, col);
				if (posValue > 0) {
					if ((xAxis && puzzle.atPos(mirror(row), col) != posValue)
							|| (yAxis && puzzle.atPos(row, mirror(col))	!= posValue)
							|| (rotational && puzzle.atPos(mirror(row), mirror(col)) 
									!= posValue)) {
						puzzle.setPos(row, col, ShinroPuzzle.EMPTY);
					}
				}
			}
		}
	}
	
	/**
	 * Removes the pointless arrows from a given puzzle
	 * @param puzzle  the puzzle whose pointless arrows are to be removed
	 */
	private static void removePointlessArrows(ShinroPuzzle puzzle) {
		for (GridPos arrow : puzzle.getListByType(ShinroPuzzle.N)) { //any arrow
			if (puzzle.getTypeInList(ShinroPuzzle.POINT,
					puzzle.getArrowToEdge(arrow)).size() == 0) {
				puzzle.setPos(arrow.getRow(), arrow.getCol(), ShinroPuzzle.EMPTY);
			}
		}
	}
	
	/**
	 * Performs tournament selection on a population of genomes
	 * <p>
	 * Tournaments are of size TOURNAMENTSIZE. The victor is the elite genome of the
	 * tournament.
	 * @param population  the population to run the tournament on
	 * @return the victor of the tournament
	 */
	private static ShinroPuzzle runTournament(ShinroPuzzle[] population) {
		Random rand = new Random();
		ShinroPuzzle[] tournament = new ShinroPuzzle[TOURNAMENTSIZE];
		int[] selections = new int[TOURNAMENTSIZE];
		
		for (int i = 0; i < selections.length; i++) {
			selections[i] = rand.nextInt(POPULATIONSIZE);
			
			//make sure the same puzzle isn't selected twice
			if (i == 0) {
				continue;
			}
			for (int j = (i - 1); j >= 0; j--) {
				if (selections[i] == selections[j]) {
					i--; //repeat this iteration of outer loop
					break;
				}
			}
		}
		
		for (int i = 0; i < tournament.length; i++) {
			tournament[i] = population[selections[i]];
		}
		
		return getElite(tournament);		
	}
}