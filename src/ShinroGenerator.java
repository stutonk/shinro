/**
 * 
 */
package shinro;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Joseph Eib
 *
 */
//TODO: Cleanup, comments, symmetry, clustering, constraint, file out 
public class ShinroGenerator {
	//generator parameters
	private static final int MAXNOIMPROVEMENT = 300;
	
	//constraints
	private static final int POPULATIONSIZE = 10;
	private static final int TOURNAMENTSIZE = 3;
	private static final int NUMPOINTS = 12;
	private static final int MINIMUMMOVES = 28;	
	private static final boolean SYMMETRY = false;
	
	//for the fitness function
	private static final int DIFFICULTYFACTOR = 5;
	private static final boolean CLUSTER = false;
	
	//check
	private static ShinroPuzzle[] initPopulation() {
		Random rand = new Random();
		ShinroPuzzle[] population = new ShinroPuzzle[POPULATIONSIZE];
		for (int i = 0; i < population.length; i++) {
			population[i] = new ShinroPuzzle();
			for (int row = 0; row < ShinroPuzzle.SIZE; row++) {
				for (int col = 0; col < ShinroPuzzle.SIZE; col++) {
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
			if (noImprovementCount > MAXNOIMPROVEMENT || newFitness == 1.0) {
				//if the puzzle is invalid
				if (elite.getListByType(ShinroPuzzle.POINT).size() < NUMPOINTS 
						|| newFitness == 0.0) {
					noImprovementCount /= 2;
					continue; //keep going
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
		
		//Print stats:
		System.out.println("\n" + elite);
		System.out.println("Total generations: " + numGenerations);
		System.out.println("Final fitness: " + newFitness);
		
		System.out.print("Solver info: ");
		ShinroPuzzle eliteCopy = elite.clone();
		eliteCopy.reset();
		ShinroSolver ss = new ShinroSolver(eliteCopy);
		for (Integer i : ss.solve()) {
			System.out.print(i + " ");
		}
		System.out.println();
		
		return elite;
	}
	
	private static ShinroPuzzle[] nextGeneration(ShinroPuzzle[] population) {	
		ShinroPuzzle[] nextGen = new ShinroPuzzle[population.length];

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
			for (int i = 0; i < ShinroPuzzle.SIZE; i++) {
				for (int j = 0; j < ShinroPuzzle.SIZE; j++) {
					double r = Math.random();
					if (r <= rate) {
						switch (rand.nextInt(3)) {
						case 0: mutated.setPos(i, j, ShinroPuzzle.EMPTY);
								break;
						case 1: mutated.setPos(i, j, ShinroPuzzle.POINT);
								break;
						case 2: mutated.putArrow(i, j, rand.nextInt(9 - 1) + 1);
								break;
						default:break;
						}
					}
				}
			}
		}
		else if (mutation == 1) {
			//randomly swap up to three pairs of spaces
			int numTimes = rand.nextInt(3); //up to three times
			for (int i = 0; i < numTimes; i++) {
				int randRow1 = rand.nextInt(ShinroPuzzle.SIZE);
				int randCol1 = rand.nextInt(ShinroPuzzle.SIZE);
				int randRow2 = rand.nextInt(ShinroPuzzle.SIZE);
				int randCol2 = rand.nextInt(ShinroPuzzle.SIZE);
				int temp = mutated.atPos(randRow1, randCol1);
				mutated.setPos(randRow1, randCol1,
						mutated.atPos(randRow2, randCol2));
				mutated.setPos(randRow2, randCol2, temp);				
			}			
		}
		else if (mutation == 2) {
			//add an arrow to a random space
			int randRow = rand.nextInt(ShinroPuzzle.SIZE);
			int randCol = rand.nextInt(ShinroPuzzle.SIZE);
			int randArrow = rand.nextInt(9 - 1) + 1; //1 to 8
			mutated.setPos(randRow, randCol, randArrow);
		}
		else if (mutation == 3) {
			//delete a random arrow
			ArrayList<GridPos> arrows = 
					mutated.getListByType(ShinroPuzzle.N); //all arrows
			if (arrows.size() == 0) {
				return mutate(puzzle);
			}
			mutated.setPos(arrows.get(rand.nextInt(arrows.size())),
					ShinroPuzzle.EMPTY);
		}
		else if (mutation == 4) {
			//add a POINT to a random space
			int randRow = rand.nextInt(ShinroPuzzle.SIZE);
			int randCol = rand.nextInt(ShinroPuzzle.SIZE);
			mutated.setPos(randRow, randCol, 9);
		}
		else if (mutation == 5) {
			//delete a random POINT
			ArrayList<GridPos> points = puzzle.getListByType(ShinroPuzzle.POINT);
			if (points.size() == 0) {
				return mutate(puzzle);
			}
			mutated.setPos(points.get(rand.nextInt(points.size())),
					ShinroPuzzle.EMPTY);
		}
		mutated.setHeaders();
		removePointlessArrows(mutated);
		return mutated;
	}
	
	//TODO: Almost finished
	private static double calcFitness(ShinroPuzzle puzzle) {
		double fitness = 0;
		ShinroPuzzle toSolve = puzzle.clone();
		toSolve.reset();
		ShinroSolver solver = new ShinroSolver(toSolve);
		int numMoves[] = solver.solve();
		
		fitness = epsilon(puzzle, numMoves[0]); //total num moves 
		fitness *= 1 - (1 / (1 + numMoves[DIFFICULTYFACTOR]));
		
		if (CLUSTER) {
			//do more
		}
		
		return fitness;
	}
	
	//TODO: finish
	private static double epsilon(ShinroPuzzle puzzle, int totalMoves) {
		double denominator = 
				1.0
				+ Math.abs(NUMPOINTS 
						- puzzle.getListByType(ShinroPuzzle.POINT).size())
				+ countPointlessArrows(puzzle)
				+ Math.abs((MINIMUMMOVES) - totalMoves);
				// + countViolatedConstraints(puzzle);
		if (SYMMETRY) {
			//denominator += countUnsymmetrical(puzzle);
		}
				
		return 1.0 / denominator;				
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
	
	//private static void initConstraintGrid(int[][] constraintGrid) {
	//	
	//}
	
	//TODO: finisf file io
	public static void main(String[] args) {
		
		
		//if args
			//int constraintGrid = new int[ShinroPuzzle.SIZE][ShinroPuzzle.SIZE];
			//initConstraintGrid(constraintGrid);
		ShinroPuzzle p = generatePuzzle();
		
		//do some file output
		
	}	
}
