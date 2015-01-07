package generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import shinro.*;

/**
 * @author Joseph Eib
 * @since January 2015
 */
//TODO: comments
public class ShinroGenerator {
	// general generator parameters
	private static int minMoves = 28;	
	private static int maxNoImprovement = 300; //I should make this some function
	private static boolean symmetry = false;
	private static boolean xAxis = false;
	private static boolean yAxis = false;
	private static boolean rotational = false;
	
	//fitness function parameters
	private static int difficultyFactor = 5;
	private static boolean cluster = false;
	private static boolean clusterArrows = false;
	
	//constraint constants
	private static final boolean RANDOMIZEALL = true; //minMoves and difficultyFactor
	private static final int LEASTMOVES = 15;
	private static final int MOSTMOVES = 40;
	private static final int LEASTDIFFICULTY = 4; //oneToPlace moves
	private static final int POPULATIONSIZE = 10;
	private static final int TOURNAMENTSIZE = 3;
	private static final int NUMPOINTS = 12;
	private static final double SYMMETRYRATE = 0.0005;
	private static final double CLUSTERRATE = 0.0005;
	
	//other constants
	private static final int PUZZSIZE = ShinroPuzzle.SIZE;
	
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
	
	private static ShinroPuzzle generatePuzzle() {
		ShinroPuzzle[] population = initPopulation();
		ShinroPuzzle elite = new ShinroPuzzle();
		double prevFitness = 0.0, newFitness = 0.0;
		int noImprovementCount = 0, numGenerations = 0;
		
		System.out.println("Generating puzzle...");
		
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
					//Adjust the count -- consider updating this to be by some function
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
	
	private static void printStatsWritePuzzle(ShinroPuzzle puzzle, 
			int numGens, double fitness) {
		String fileString = "shinro";
		
		System.out.println("\n" + puzzle);
		System.out.println("Min moves: " + minMoves + ", difficulty factor: "
				+ difficultyFactor);
		System.out.println("Total generations: " + numGens);
		fileString += String.format("%dg", numGens);
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
		System.out.println("Final fitness: " + fitness);
		fileString += "_" + String.format("%3df", (int)(fitness * 100)) + "_";
		
		System.out.print("Solver info: ");
		ShinroPuzzle puzzleCopy = puzzle.clone();
		puzzleCopy.reset();
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
			System.out.println("File created successfully.");
			out.close();
		}
		catch (IOException e) {
			System.out.println("Crirical error -- could not create outputfile: "
					+ e.getMessage());
		}
	}
	
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
	
	private static ShinroPuzzle mutate(ShinroPuzzle puzzle) {
		Random rand = new Random();
		ShinroPuzzle mutated = puzzle.clone();
		int mutation = rand.nextInt(6); //six possibilities (0 to 5)

		if (mutation == 0) {
			//step through the puzzle and probabilistically mutate spaces
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
	
	private static double calcFitness(ShinroPuzzle puzzle) {
		double fitness = 0;
		ShinroPuzzle toSolve = puzzle.clone();
		toSolve.reset();
		ShinroSolver solver = new ShinroSolver(toSolve);
		int numMoves[] = solver.solve();
		
		fitness = epsilon(puzzle, numMoves[0]); //total num moves 
		fitness *= 1 - (1 / (1 + numMoves[difficultyFactor]));
		
		if (cluster) {
			fitness *= (1 - (1 / (1 + calcClustering(puzzle))));
		}
		
		return fitness;
	}
	
	private static double epsilon(ShinroPuzzle puzzle, int totalMoves) {
		double denominator = 
				1.0
				+ Math.abs(NUMPOINTS 
						- puzzle.getListByType(ShinroPuzzle.POINT).size())
				+ countPointlessArrows(puzzle)
				+ Math.abs((minMoves) - totalMoves);
				// + countViolatedConstraints(puzzle);
		if (symmetry) {
			denominator += countNonsymmetrical(puzzle);
		}
				
		return 1.0 / denominator;				
	}
	
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
	
	private static void removePointlessArrows(ShinroPuzzle puzzle) {
		for (GridPos arrow : puzzle.getListByType(ShinroPuzzle.N)) { //any arrow
			if (puzzle.getTypeInList(ShinroPuzzle.POINT,
					puzzle.getArrowToEdge(arrow)).size() == 0) {
				puzzle.setPos(arrow.getRow(), arrow.getCol(), ShinroPuzzle.EMPTY);
			}
		}
	}
	
	private static int countNonsymmetrical(ShinroPuzzle puzzle) {
		int violations = 0;
		
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
	
	private static int mirror(int loc) {
		return (PUZZSIZE - 1 - loc);
	}
	
	public static void main(String[] args) {
		if (RANDOMIZEALL) {
			Random rand = new Random();
			minMoves = rand.nextInt(MOSTMOVES - LEASTMOVES) + LEASTMOVES;
			difficultyFactor = rand.nextInt(7 - LEASTDIFFICULTY) + LEASTDIFFICULTY;
		}
		
		generatePuzzle();
		
	}	
}