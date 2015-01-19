package shinro;

import java.util.ArrayList;

/**
 * Finds the number and types of moves required to solve a given shinro puzzle
 * <p>
 * This is an implementation of the shinro puzzle solver as described in a paper by
 * David Oranchak. See http://oranchak.com/evostar-paper.pdf for more details.
 * <p>
 * The solve method returns an array of integers whose indices represent the
 * number of moves per difficulty levels found in the puzzle. These difficulty levels
 * are as follows:
 * <ul>
 * <li> 0: Total number of moves
 * <li> 1: Zero points to place in row or column
 * <li> 2: Number of unfilled spaces in a row or column equals the number of points
 * remaining to be found
 * <li> 3: Only one empty space in the path of an unsatisfied arrow
 * <li> 4: Only one empty space in row or column with a horizontal or vertical arrow
 * <li> 5: Number of nonintersecting arrows equal to number of points to find in 
 * subset of rows or columns
 * <li> 6: Move based on the pigeonhole principle
 * <li> 7: Placing a point at a location would cause an arrow to become unsatisfiable
 * <li> The last one: A ridiculous hack where it is indicated by either a zero or one
 * whether or not the solver has actually solved the puzzle
 * </ul> 
 * @author Joseph Eib
 * @since December 2014
 */
public class ShinroSolver {
	private ShinroPuzzle puzzle;
	private int[] numMovesByDifficulty;
	
	private static final int DIFFICULTYLEVELS = 7;
	public static final int ARRAYSIZE = DIFFICULTYLEVELS + 2;
	
	/**
	 * Create a new default ShinroSolver instance
	 * <p>
	 * This solver's puzzle will be blank and must be set
	 */
	public ShinroSolver() {
		this.puzzle = new ShinroPuzzle();
		this.numMovesByDifficulty = new int[ARRAYSIZE];
		for (int i = 0; i < this.numMovesByDifficulty.length; i++) {
			this.numMovesByDifficulty[i] = 0;
		}
	}
	
	/**
	 * Create a ShinroSolver instance with a specified ShinroPuzzle
	 * @param puzzle  the puzzle for this puzzle to solve
	 */
	public ShinroSolver(ShinroPuzzle puzzle) {
		this.puzzle = puzzle;
		this.puzzle.reset();
		this.numMovesByDifficulty = new int[ARRAYSIZE];
		for (int i = 0; i < this.numMovesByDifficulty.length; i++) {
			this.numMovesByDifficulty[i] = 0;
		}
	}
	
	/**
	 * Gets the puzzle field of this ShinroSolver
	 * @return the ShinroPuzzle referenced by this ShinroSolver's puzzle field
	 */
	public ShinroPuzzle getPuzzle() {
		return this.puzzle;
	}
	
	/**
	 * Sets the puzzle field of this ShinroSolver
	 * @param puzzle  the ShinroPuzzle object to set the puzzle field to
	 */
	public void setPuzzle(ShinroPuzzle puzzle) {
		this.puzzle = puzzle;
	}
	
	/**
	 * Gets an ArrayList of the spaces "behind" an arrow
	 * <p>
	 * The list goes in the opposite direction that the arrow points.
	 * If the space in question is not an arrow, an empty ArrayList is returned.
	 * @param row  the row index of the arrow
	 * @param col  the column index of the arrow
	 * @return an ArrayList of GridPos representing the spaces "behind" the specified
	 * arrow
	 */
	private ArrayList<GridPos> getSpacesBehindArrow(int row, int col) {
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		if (!puzzle.isArrow(row, col)) {
			return myList;
		}
		else {
			int i;
			switch (puzzle.atPos(row, col)) {
			case ShinroPuzzle.n: //pass through
			case ShinroPuzzle.N: for (i = row; i < puzzle.size(); i++) {
						if (puzzle.isArrow(i, col) && 
								(puzzle.atPos(i, col) == ShinroPuzzle.S ||
										puzzle.atPos(i, col) == ShinroPuzzle.s)) {
							break;
						}
						myList.add(new GridPos(i, col));
					}
					break;
			case ShinroPuzzle.s: //pass through
			case ShinroPuzzle.S: for (i = row; i >= 0; i--) {
						if (puzzle.isArrow(i, col) && 
								(puzzle.atPos(i, col) == ShinroPuzzle.N ||
										puzzle.atPos(i, col) == ShinroPuzzle.n)) {
							break;
						}
						myList.add(new GridPos(i, col));
					}
					break;
			case ShinroPuzzle.e: //pass through
			case ShinroPuzzle.E: for (i = col; i >= 0; i--) {
						if (puzzle.isArrow(row, i) && 
								(puzzle.atPos(row, i) == ShinroPuzzle.W ||
										puzzle.atPos(row, i) == ShinroPuzzle.w)) {
							break;
						}
						myList.add(new GridPos(row, i));
					}
					break;
			case ShinroPuzzle.w: //pass through
			case ShinroPuzzle.W: for (i = col; i < puzzle.size(); i++) {
						if (puzzle.isArrow(row, i) && 
								(puzzle.atPos(row, i) == ShinroPuzzle.E ||
										puzzle.atPos(row, i) == ShinroPuzzle.e)) {
							break;
						}
						myList.add(new GridPos(row, i));
					}
					break;
			default: return myList;
			}
			return myList;
		}		
	}
	
	/**
	 * GridPos-based convenience method for {@link #getSpacesBehindArrow(int, int)}
	 * @param pos The GridPos of the specified arrow
	 * @return an ArrayList of GridPos representing the spaces "behind" the specified
	 * arrow
	 */
	private ArrayList<GridPos> getSpacesBehindArrow(GridPos pos) {
		return this.getSpacesBehindArrow(pos.getRow(), pos.getCol());
	}
	
	private ArrayList<GridPos> getUnsatisfiedArrows() {
		ArrayList <GridPos> myList = 
				puzzle.getListByType(ShinroPuzzle.N); //any arrow
		ArrayList <GridPos> result = new ArrayList<GridPos>();
		for (GridPos pos : myList) {
			if (!puzzle.isSatisfied(pos)) {
				result.add(pos);
			}
		}
		return result;
	}
	
	/**
	 * Determines whether listA contains listB
	 * @param listA  the containing list
	 * @param listB  the contained list 
	 * @return true if listA contains listB
	 */
	private boolean listContainsList(ArrayList<GridPos> listA, 
			ArrayList<GridPos> listB) {
		for (GridPos pos : listB) {
			if (!listA.contains(pos)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Determines whether or not listA and listB have any intersecting elements
	 * @param listA  one of the lists to check for intersection
	 * @param listB  the other list to check for intersection
	 * @return true of the lists have any intersecting elements
	 */
	private boolean listIntersectsList(ArrayList<GridPos> listA, 
			ArrayList<GridPos> listB) {
		for (GridPos pos : listA) {
			if (listB.contains(pos)) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * Removes arrows from a list that have intersecting empty spaces in their paths
	 * <p>
	 * ONLY empty spaces are considered
	 * @param list  the list to remove intersecting arrows from
	 */
	private void removeIntersectingArrows(ArrayList<GridPos> list) {
		ArrayList<GridPos> removalList = new ArrayList<GridPos>();
		ArrayList<GridPos> subA = new ArrayList<GridPos>();
		ArrayList<GridPos> subB = new ArrayList<GridPos>();
		for (int i = 0; i < list.size(); i++) {
			subA = puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
					puzzle.getArrowToEdge(list.get(i)));
			for (int j = 0; j < list.size(); j++) {
				subB = puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
						puzzle.getArrowToEdge(list.get(j)));
				if (i != j && this.listIntersectsList(subA, subB)) {
					removalList.add(list.get(i));
				}
			}
		}
		for (GridPos pos : removalList) {
			list.remove(pos);
		}
	}
	
	/**
	 * Determines the difficulty level of the next move and applies it to the puzzle
	 * state.
	 * <p>
	 * Thus method takes a greedy approach that attempts to make simpler moves first
	 * and calls methods that change the state of the puzzle.
	 * @see #findZeroToPlace()
	 * @see #findNumUnfilledEqRemaining()
	 * @see #findOneFreeSquare()
	 * @see #findOneAndHorizOrVert()
	 * @see #findNonIntersecting()
	 * @see #findPigeonhole()
	 * @see #findUnsatisfiable()
	 * @return the integer value of the next move to be made in the puzzle
	 */
	private int nextMove() {
		if (this.findZeroToPlace()) {
			return 1; //num Zero to Place
		}
		else if (this.findNumUnfilledEqRemaining()) {
			return 2; //num Unfilled Eq Remaining
		}
		else if (this.findOneFreeSpace()) {
			return 3; //num One Free Space
		}
		else if (this.findOneAndHorizOrVert()) {
			return 4; //num One to Place
		}
		else if (this.findNonIntersecting()) {
			return 5; //num Non Intersecting
		}
		else if (this.findPigeonhole()) {
			return 6; //num Pigeonhole
		}
		else if (this.findUnsatisfiable()) {
			return 7; //num Unsatisfiable
		}
		else {
			return -1;
		}
	}
	
	/**
	 * Gets a list of every row index for every GridPos in a list
	 * @param list  the list to extract the row indices from
	 * @return an ArrayList of Integers with every row index in the original list
	 */
	private ArrayList<Integer> getListOfRows(ArrayList<GridPos> list) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (GridPos pos : list) {
			result.add(pos.getRow());
		}
		return result;
	}
	
	/**
	 * Gets a list of every column index for every GridPos in a list
	 * @param list  the list to extract the row indices from
	 * @return an ArrayList of Integers with every column index in the original list
	 */
	private ArrayList<Integer> getListOfCols(ArrayList<GridPos> list) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (GridPos pos : list) {
			result.add(pos.getCol());
		}
		return result;
	}
	
	/**
	 * Calculates the difference between a row's header number and the current number
	 * of points in the row
	 * @param row  the row to calculate the difference in
	 * @return an integer representing the difference between the header and current
	 * points
	 */
	private int calcDiffInRow(int row) {
		return puzzle.getRowHeaderNum(row) - 
				puzzle.getTypeInList(ShinroPuzzle.POINT, puzzle.getRow(row)).size();
	}
	
	/**
	 * Calculates the difference between a column's header number and the current 
	 * number of points in the row
	 * @param col  the column to calculate the difference in
	 * @return an integer representing the difference between the header and current
	 * points
	 */
	private int calcDiffInCol(int col) {
		return puzzle.getColHeaderNum(col) -
				puzzle.getTypeInList(ShinroPuzzle.POINT, puzzle.getCol(col)).size();
	}
	
	/**
	 * Puts Xs in remaining empty spaces of a row of column if there is zero more
	 * points to place in that row or column
	 * @return true if a move of this type is located
	 */
	private boolean findZeroToPlace() {
		//first look for 0 row and column header nums
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		for (int i = 0; i < puzzle.size(); i++) {
			if (puzzle.getRowHeaderNum(i) == 0) {
				myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getRow(i));
				if (myList.size() > 0) {
					puzzle.fillSpacesWithX(myList);					
					return true;
				}
			}
			if (puzzle.getColHeaderNum(i) == 0) {
				myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getCol(i));
				if (myList.size() > 0) {
					puzzle.fillSpacesWithX(myList);					
					return true;
				}
			}
			//look for zero diff
			if (this.calcDiffInRow(i) == 0) {
				myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getRow(i));
				if (myList.size() > 0) {
					puzzle.fillSpacesWithX(myList);					
					return true;
				}
			}
			if (this.calcDiffInCol(i) == 0) {
				myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getCol(i));
				if (myList.size() > 0) {
					puzzle.fillSpacesWithX(myList);					
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Puts points in a row or column if the number of empty spaces equals the number
	 * of remaining points to be placed in that row or column
	 * @return true if a move of this type is found.
	 */
	private boolean findNumUnfilledEqRemaining() {
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		for (int i = 0; i < puzzle.size(); i++) {
			myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getRow(i));
			if (myList.size() != 0 && this.calcDiffInRow(i) == myList.size()) {
				for (GridPos pos : myList) {
					puzzle.putPoint(pos);
				}
				return true;
			}
			myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getCol(i));
			if (myList.size() != 0 && this.calcDiffInCol(i) == myList.size()) {
				for (GridPos pos : myList) {
					puzzle.putPoint(pos);
				}
				return true;
			}
		} //ELSE
		return false;
	}
	
	/**
	 * Puts a point where an unsatisfied arrow only has one empty space in its path
	 * @return true if a move of this type is found
	 */
	private boolean findOneFreeSpace() {
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		for (GridPos arrow : this.getUnsatisfiedArrows()) {
			myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
					puzzle.getArrowToEdge(arrow));
			if (myList.size() == 1) {
				puzzle.putPoint(myList.get(0));
				return true;
			}
		}
		return false;
	}
	
	//row column row column
	//unsatisfied arrows only
	/**
	 * Puts Xs in spaces in spaces behind an unsatisfied horizontal or vertical
	 * arrow when there is only one point remaining to be found in a row or column
	 * @return true if a move of this type is found
	 */
	private boolean findOneAndHorizOrVert() {
		int i;
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		ArrayList<GridPos> mySpaces = new ArrayList<GridPos>();
		for (i = 0; i < puzzle.size(); i++) {
			if (this.calcDiffInRow(i) == 1) {
				myList = puzzle.getTypeInList(ShinroPuzzle.N,
						puzzle.getRow(i)); //any arrow
				for (GridPos pos : myList) {
					switch (puzzle.atPos(pos)) {
					case ShinroPuzzle.E: //pass through
					case ShinroPuzzle.W:
						mySpaces = 
								puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
										this.getSpacesBehindArrow(pos));
						if (mySpaces.size() > 0) {
							//System.out.print(pos + "\n"); //DEBUG
							puzzle.fillSpacesWithX(mySpaces);
							return true;
						}
					default: break;
					}					
				}
			}
			if (this.calcDiffInCol(i) == 1) {
				myList = puzzle.getTypeInList(ShinroPuzzle.N,
						puzzle.getCol(i)); // any arrow
				for (GridPos pos : myList) {
					switch (puzzle.atPos(pos)) {
					case ShinroPuzzle.N: //pass through
					case ShinroPuzzle.S:
						mySpaces = 
								puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
									this.getSpacesBehindArrow(pos));
						if (mySpaces.size() > 0) {
							puzzle.fillSpacesWithX(mySpaces);
							return true;
						}
					default: break;
					}					
				}
			}
		}
		return false;
	}
	
	/**
	 * Places Xs based on the combinatorial ruling out of possible spaces due to
	 * nonintersecting arrows.
	 * <p>
	 * For some subset "theSubset" of rows or columns, if a set of arrows "arrowSet"
	 * whose empty spaces are entirely within theSubset can be found and arrowSet's
	 * cardinality is equal to the number of points left to be found in theSubset,
	 * then all of the points must be in the path of an arrow in arrowSet. Therefore,
	 * any empty spaces in theSubset not in arrowSet can be filled with an X.
	 * @return true if a move of this type is found
	 */
	private boolean findNonIntersecting() {
		ArrayList<GridPos> theSubset = new ArrayList<GridPos>();
		ArrayList<GridPos> arrowSet = new ArrayList<GridPos>();
		int i, j, remainingInSubset = 0;
		//every contiguous subset of rows
		for (j = 0; j < (puzzle.size() - 1); j++) {
			theSubset = new ArrayList<GridPos>();
			arrowSet = new ArrayList<GridPos>();
			remainingInSubset = 0;
			//build theSubset
			for (i = j; i < puzzle.size(); i++) {
				theSubset.addAll(
						puzzle.getTypeInList(
								ShinroPuzzle.EMPTY,
								puzzle.getRow(i)
						)
				);
				//find target cardinality
				remainingInSubset += this.calcDiffInRow(i);
				//the following condition should always be found by findOneFreeSpace
				if (theSubset.size() <= 1) { 
					continue;
				}
				//build arrowSet
				for (GridPos pos : this.getUnsatisfiedArrows()) {
					if (this.listContainsList(theSubset,
							puzzle.getTypeInList(ShinroPuzzle.EMPTY,
									puzzle.getArrowToEdge(pos)))) {
						arrowSet.add(pos);
					}
				}
				//if arrowSet cardinality not enough, move on to the next iteration
				if (arrowSet.size() < remainingInSubset) {
					continue;
				}
				//go to the common method
				if (this.nonIntersectingCommon(remainingInSubset, theSubset,
						arrowSet)) {
					return true;
				}
			}
		}
		//every contiguous subset of columns
		for (j = 0; j < (puzzle.size() - 1); j++) {
			theSubset = new ArrayList<GridPos>();
			arrowSet = new ArrayList<GridPos>();
			remainingInSubset = 0;
			//build theSubset
			for (i = j; i < puzzle.size(); i++) {
				theSubset.addAll(
						puzzle.getTypeInList(
								ShinroPuzzle.EMPTY,
								puzzle.getCol(i)
						)
				);
				//find target cardinality
				remainingInSubset += this.calcDiffInCol(i);
				//the following condition should always be found by findOneFreeSpace
				if (theSubset.size() <= 1) { 
					continue;
				}
				//build arrowSet
				for (GridPos pos : this.getUnsatisfiedArrows()) {
					if (this.listContainsList(theSubset,
							puzzle.getTypeInList(ShinroPuzzle.EMPTY,
									puzzle.getArrowToEdge(pos)))) {
						arrowSet.add(pos);
					}
				}
				//if arrowSet cardinality not enough, move on to the next iteration
				if (arrowSet.size() < remainingInSubset) {
					continue;
				}
				//go to the common method
				if (this.nonIntersectingCommon(remainingInSubset, theSubset,
						arrowSet)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Common (for rows or columns) method for {@link #findNonIntersecting()}
	 * @param remainingInSubset  the number of points remaining to be found in
	 * theSubset
	 * @param theSubset  the subset of rows or columns being considered
	 * @param arrowSet  the set of arrows being considered
	 * @return true if the conditions mentioned in {@link #findNonIntersecting()}
	 * have been satisfied
	 */
	private boolean nonIntersectingCommon(int remainingInSubset, 
			ArrayList<GridPos> theSubset, ArrayList<GridPos> arrowSet) {
		ArrayList<GridPos> spacesInArrowSet = new ArrayList<GridPos>();
		ArrayList<GridPos> setToX = new ArrayList<GridPos>();
		this.removeIntersectingArrows(arrowSet);
		if (remainingInSubset != 0 
				&& arrowSet.size() == remainingInSubset) { //the move has been found!
			//build setP
			for (GridPos arrow : arrowSet) {
				for (GridPos space : puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
						puzzle.getArrowToEdge(arrow))) {
					spacesInArrowSet.add(space);
				}
			}
			//build final set
			setToX.addAll(theSubset);
			for (GridPos pos : spacesInArrowSet) {
				setToX.remove(pos);
			}
			if (setToX.size() > 0) {
				puzzle.fillSpacesWithX(setToX);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Places points based on a combinatorial strategy based on the pigeonhole
	 * principle
	 * <p>
	 * For any row or column, if the number of free spaces is greater than the
	 * number of points remaining to be found, make a set of all the spaces whose
	 * perpendicular element (columns if this is a row, rows if this is a column)
	 * has only one point to be found. Next, make a set of any arrow whose path of
	 * empty spaces is entirely in those columns if this is a row or rows if this
	 * is a column of setPerpWithOne. Arrows in this set cannot intersect 
	 * setPerpWithOne, nor each other. If the number if arrows in the arrowSet is the
	 * same as the difference between the number of points remaining to be found and
	 * the number of free spaces in the row or column, then any space in the row or 
	 * column that doesn't share a column or row, respectively, with any space 
	 * covered by one of the arrows must contain a point.
	 * <p>
	 * I'm not 100% sure my implementation is accurate but it seems to work fine --
	 * It seems like there could be some scenario where the number of arrows in the
	 * arrowSet is greater than the difference between numUnfilledInLine and
	 * diffInLine where either two arrows cover the same columns (or rows) and don't
	 * intersect or some subset of the arrows satisfy the criteria. Therefore, I
	 * have left the condition as |A| > numUnfilledInLine - diffInLine.
	 * @return true if a move of this type is found
	 */
	private boolean findPigeonhole() {		
		ArrayList<GridPos> arrows = this.getUnsatisfiedArrows();
		
		ArrayList<GridPos> setPerpWithOne = new ArrayList<GridPos>();
		ArrayList<GridPos> arrowSet = new ArrayList<GridPos>();
		ArrayList<GridPos> spaces = new ArrayList<GridPos>();
		ArrayList<GridPos> removalList = new ArrayList<GridPos>();
		int i, diffInLine, numUnfilledInLine;
		
		//columns
		ArrayList<Integer> rows = new ArrayList<Integer>();
		
		for (int line = 0; line < puzzle.size(); line++) {
			diffInLine = this.calcDiffInCol(line);
			numUnfilledInLine = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
					puzzle.getCol(line)).size();
			if (numUnfilledInLine > diffInLine) {
				//build setPerpWithOne
				setPerpWithOne = puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
						puzzle.getCol(line));
				for (i = setPerpWithOne.size() - 1; i >= 0; i--) {
					if (this.calcDiffInRow(setPerpWithOne.get(i).getRow()) != 1) {
						setPerpWithOne.remove(i);
					}
				}				
				//build arrowSet
				arrowSet.clear();
				arrowSet.addAll(arrows);				
				//remove any A that intersect setPerpWithOne
				for (i = arrowSet.size() - 1; i >= 0; i--) {
					if (this.listIntersectsList(setPerpWithOne, 
							puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
									puzzle.getArrowToEdge(arrowSet.get(i))))) {
						arrowSet.remove(i);
					}
				}				
				//remove arrows whose spaces don't share a row with setPerpWithOne
				removalList.clear();
				for (GridPos arrow : arrowSet) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					rows.addAll(this.getListOfRows(spaces));
					rows.removeAll(this.getListOfRows(setPerpWithOne));
					if (rows.size() > 0) {
						removalList.add(arrow);
					}
					rows.clear();
				}
				arrowSet.removeAll(removalList);				
				//finally, remove any intersecting arrows from the arrowSet
				this.removeIntersectingArrows(arrowSet);							
				//perhaps |A| MUST = numUnfilledInLine - diffInLine
				if (arrowSet.size() < (numUnfilledInLine - diffInLine)) {
					continue; //move on if not enough arrows in the arrowSet
				}				
				//Find which spaces must contain a point
				rows.clear();
				rows.addAll(
						this.getListOfRows(
								puzzle.getTypeInList(
										ShinroPuzzle.EMPTY,
										puzzle.getCol(line)
								)
						)
				);
				for (GridPos arrow : arrowSet) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					rows.removeAll(this.getListOfRows(spaces));
				}
				//To avoid an infinite loop, cols.size must be > 0
				if (rows.size() == 0) {
					continue;
				}				
				/* If one of the empty spaces in the column shares a row with
				 * the remaining values in rows, place a point.
				 */
				for (Integer row : rows) {
					for (GridPos pos : puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getCol(line))) {
						if (pos.getRow() == row) {
							puzzle.putPoint(pos);
						}
					}
				}
				return true;
			}
		}
		//rows
		ArrayList<Integer> cols = new ArrayList<Integer>();
		setPerpWithOne.clear();
		arrowSet.clear();
		removalList.clear();
		spaces.clear();
		
		for (int line = 0; line < puzzle.size(); line++) {
			diffInLine = this.calcDiffInRow(line);
			numUnfilledInLine = puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
					puzzle.getRow(line)).size();
			if (numUnfilledInLine > diffInLine) {
				//build setPerpWithOne
				setPerpWithOne = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
						puzzle.getRow(line));
				for (i = setPerpWithOne.size() - 1; i >= 0; i--) {
					if (this.calcDiffInCol(setPerpWithOne.get(i).getCol()) != 1) {
						setPerpWithOne.remove(i);
					}
				}				
				//build arrowSet
				arrowSet.clear();
				arrowSet.addAll(arrows);
				
				//remove any A that intersect setPerpWithOne
				for (i = arrowSet.size() - 1; i >= 0; i--) {
					if (this.listIntersectsList(setPerpWithOne, 
							puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
									puzzle.getArrowToEdge(arrowSet.get(i))))) {
						arrowSet.remove(i);
					}
				}				
				//remove arrows whose spaces don't share a row with setPerpWithOne
				removalList.clear();
				for (GridPos arrow : arrowSet) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					cols.addAll(this.getListOfCols(spaces));
					cols.removeAll(this.getListOfCols(setPerpWithOne));
					if (cols.size() > 0) {
						removalList.add(arrow);
					}
					cols.clear();
				}
				arrowSet.removeAll(removalList);				
				//finally, remove any intersecting arrows from the arrowSet
				this.removeIntersectingArrows(arrowSet);				
				//perhaps |A| MUST = numUnfilledInLine - diffInLine
				if (arrowSet.size() < (numUnfilledInLine - diffInLine)) {
					continue; //move on if not enough arrows in the arrowSet
				}				
				//Find which spaces must contain a point
				cols.clear();
				cols.addAll(
						this.getListOfCols(
								puzzle.getTypeInList(
										ShinroPuzzle.EMPTY,
										puzzle.getRow(line)
								)
						)
				);
				/* potentially in here, there could be a scenario where, for example,
				 * there are three arrows in A and numUnfilledInLine - diffInLine = 2
				 * and some two of the three will satisfy the criteria for the 
				 * pigeonhole principle. But this method seems to work
				 */
				for (GridPos arrow : arrowSet) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					cols.removeAll(this.getListOfCols(spaces));
				}
				//To avoid an infinite loop, cols.size must be > 0
				if (cols.size() == 0) {
					continue;	
				}				
				/* If one of the empty spaces in the row shares a column with
				 * the values in cols, place a point.
				 */
				for (Integer col : cols) {
					for (GridPos pos : puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getRow(line))) {
						if (pos.getCol() == col) {
							puzzle.putPoint(pos);
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Finds positions that cause an arrow to be unsatisfiable and places an X
	 * <p>
	 * This strategy is employed as the "highest difficulty" because of its brute
	 * force nature. For every empty space in the puzzle not pointed at by some
	 * arrow, consider if a point was placed in that location. If placing a point 
	 * there blocks all the spaces in the path of any unsatisfied arrow from being 
	 * illed, then there must not be a point there and it can be filled with an X.
	 * <p>
	 * Note: For simplicity, my implementation just looks at every space regardless
	 * of being pointed to by some arrow. It's arguable as to whether it would be
	 * more efficient to first have to filter out
	 * @return true if a move of this type is found
	 */
	private boolean findUnsatisfiable() {
		ArrayList<GridPos> arrowSpaces = new ArrayList<GridPos>();
		ArrayList<GridPos> nonArrowSpaces = new ArrayList<GridPos>();
		for (GridPos arrow : this.getUnsatisfiedArrows()) {
			for (GridPos space : puzzle.getTypeInList(ShinroPuzzle.EMPTY,
					puzzle.getArrowToEdge(arrow))) {
				arrowSpaces.add(space);
			}
		}
		nonArrowSpaces.addAll(puzzle.getListByType(ShinroPuzzle.EMPTY));
		nonArrowSpaces.removeAll(arrowSpaces);
		//For every empty space in puzzle not pointed to by some arrow
		for (GridPos space : nonArrowSpaces) {				
			//put a point
			puzzle.putPoint(space);
			//then, for every unsatisfied arrow
			for (GridPos arrow : this.getUnsatisfiedArrows()) {
				//mark the arrow as unsatisfiable unless proven otherwise
				boolean foundUnsatisfiable = true;
				//go through every empty space pointed to by the arrow
				for (GridPos checkSpace : puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
						puzzle.getArrowToEdge(arrow))) {
					//if BOTH the row and column for ANY space have at least
					//one space to fill, then the arrow is satisfiable
					if (this.calcDiffInCol(checkSpace.getCol()) > 0 &&
						this.calcDiffInRow(checkSpace.getRow()) > 0) {
						foundUnsatisfiable = false;
						break;
					}
				}
				//if the arrow IS unsatisfiable, put an X and return true
				if (foundUnsatisfiable) {					
					puzzle.clearSpace(space);
					puzzle.putX(space);
					return true;
				}
			}
			//clear the space if no unsatisfiable arrow is found
			puzzle.clearSpace(space);
		}
		return false;
	}
	
	/** Solves the puzzle by employing the strategies described in David Oranchak's
	 * paper.
	 * <p>
	 * The method returns an array if integers whose indices represent the number
	 * of moves found. These are as follows:
	 * <ul>
	 * <li> 0: Total number of moves
	 * <li> 1: Zero points to place in row or column
	 * <li> 2: Number of unfilled spaces in a row or column equals the number of 
	 * points remaining to be found
	 * <li> 3: Only one empty space in the path of an unsatisfied arrow
	 * <li> 4: Only one empty space in row or column with a horizontal or vertical 
	 * arrow
	 * <li> 5: Number of nonintersecting arrows equal to number of points to find in 
	 * subset of rows or columns
	 * <li> 6: Move based on the pigeonhole principle
	 * <li> 7: Placing a point at a location would cause an arrow to become 
	 * unsatisfiable
	 * <li> The last one: A ridiculous hack where it is indicated by either a zero 
	 * or one
 *   * whether or not the solver has actually solved the puzzle
	 * </ul>
	 * @see #nextMove()
	 * @return an array of integers representing the total number of moves and
	 * number of moves of each difficulty.
	 */
	public int[] solve() {
		int moveDifficulty;
		boolean solved = false;
		while (!solved) {
			moveDifficulty = this.nextMove();
			//System.out.println(this);  //uncomment for debug
			if (moveDifficulty > 0) {
				this.numMovesByDifficulty[moveDifficulty]++;
				this.numMovesByDifficulty[0]++; //num Total moves
				
				if (puzzle.verifySolution()) {
					this.numMovesByDifficulty[this.numMovesByDifficulty.length - 1] 
							= 1;
				}
			}
			else { //the puzzle is unsolvable
				break;
			}
		}
		return this.numMovesByDifficulty;
	}
	
	/* Just print the puzzle
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return puzzle.toString();
	}
}
