/**
 * 
 */
package shinro;

import java.util.ArrayList;

/**
 * @author Joseph Eib
 * @since December 2014
 */

//TODO: Remove DEBUG statements, cleanup, comments
public class ShinroSolver {
	private ShinroPuzzle puzzle;
	private int[] numMovesByDifficulty;
	
	private static final int DIFFICULTYLEVELS = 8; //TODO: elaborate
	
	public ShinroSolver() {
		this.puzzle = new ShinroPuzzle();
		this.numMovesByDifficulty = new int[DIFFICULTYLEVELS];
		for (int i = 0; i < this.numMovesByDifficulty.length; i++) {
			this.numMovesByDifficulty[i] = 0;
		}
	}
	
	public ShinroSolver(ShinroPuzzle puzzle) {
		this.puzzle = puzzle;
		this.numMovesByDifficulty = new int[DIFFICULTYLEVELS];
		for (int i = 0; i < this.numMovesByDifficulty.length; i++) {
			this.numMovesByDifficulty[i] = 0;
		}
	}
	
	public ShinroPuzzle getPuzzle() {
		return this.puzzle;
	}
	
	public void setPuzzle(ShinroPuzzle puzzle) {
		this.puzzle = puzzle;
	}
	
	//returns blank list if not arrow
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
	
	private boolean listContainsList(ArrayList<GridPos> listA, 
			ArrayList<GridPos> listB) {
		for (GridPos pos : listB) {
			if (!listA.contains(pos)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean listIntersectsList(ArrayList<GridPos> listA, 
			ArrayList<GridPos> listB) {
		for (GridPos pos : listA) {
			if (listB.contains(pos)) {
				return true;
			}
		}
		return false;
	}	
	
	//may not be most efficient but is correct
	private void removeIntersecting(ArrayList<GridPos> list) {
		ArrayList<GridPos> remList = new ArrayList<GridPos>();
		ArrayList<GridPos> subA = new ArrayList<GridPos>();
		ArrayList<GridPos> subB = new ArrayList<GridPos>();
		for (int i = 0; i < list.size(); i++) {
			subA = puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
					puzzle.getArrowToEdge(list.get(i)));
			for (int j = 0; j < list.size(); j++) {
				subB = puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
						puzzle.getArrowToEdge(list.get(j)));
				if (i != j && this.listIntersectsList(subA, subB)) {
					remList.add(list.get(i));
				}
			}
		}
		for (GridPos pos : remList) {
			list.remove(pos);
		}
	}
	
	private int nextMove() {
		if (this.findZeroToPlace()) {
			//System.out.println("ZTP");
			return 1; //num Zero to Place
		}
		else if (this.findNumUnfilledEqRemaining()) {
			//System.out.println("UER");
			return 2; //num Unfilled Eq Remaining
		}
		else if (this.findOneFreeSquare()) {
			//System.out.println("OFS");
			return 3; //num One Free Square
		}
		else if (this.findOneToPlace()) {
			//System.out.println("OTP");
			return 4; //num One to Place
		}
		else if (this.findNonIntersecting()) {
			//System.out.println("NI");
			return 5; //num Non Intersecting
		}
		else if (this.findPigeonhole()) {
			//System.out.println("PH");
			return 6; //num Pigeonhole
		}
		else if (this.findUnsatisfiable()) {
			//System.out.println("U!");
			return 7; //num Unsatisfiable
		}
		else {
			//System.out.println("Err: Solver Not Smart Enough =("); //DEBUG
			return -1;
		}
	}
	
	private ArrayList<Integer> getListOfRows(ArrayList<GridPos> list) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (GridPos pos : list) {
			result.add(pos.getRow());
		}
		return result;
	}
	
	private ArrayList<Integer> getListOfCols(ArrayList<GridPos> list) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (GridPos pos : list) {
			result.add(pos.getCol());
		}
		return result;
	}
	
	private int calcDiffInRow(int row) {
		return puzzle.getRowHeaderNum(row) - 
				puzzle.getTypeInList(ShinroPuzzle.POINT, puzzle.getRow(row)).size();
	}
	
	private int calcDiffInCol(int col) {
		return puzzle.getColHeaderNum(col) -
				puzzle.getTypeInList(ShinroPuzzle.POINT, puzzle.getCol(col)).size();
	}
	
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
	
	//either do one or do them all
	//going with do them all for now
	//in order to do one, just do .get(0) and return true
	//TODO: I THINK each one of these is "one move"
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
	
	private boolean findOneFreeSquare() {
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		ArrayList<GridPos> myArrows = 
				puzzle.getListByType(ShinroPuzzle.N); //all arrows
		for (GridPos pos : myArrows) {
			if (puzzle.isSatisfied(pos)) {
				continue; //skip satisfied arrows
			}
			myList = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
					puzzle.getArrowToEdge(pos));
			if (myList.size() == 1) {
				puzzle.putPoint(myList.get(0));
				return true;
			}
		}
		return false;
	}
	
	//row column row column
	//unsatisfied arrows only
	private boolean findOneToPlace() {
		int i;
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		ArrayList<GridPos> mySpaces = new ArrayList<GridPos>();
		for (i = 0; i < puzzle.size(); i++) {
			if (this.calcDiffInRow(i) == 1) {
				myList = puzzle.getTypeInList(ShinroPuzzle.N,
						puzzle.getRow(i)); //any arrow
				for (GridPos pos : myList) {
					switch (puzzle.atPos(pos)) {
					//case e: //pass through
					case ShinroPuzzle.E: //pass through
					//case w: //pass through
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
					//case n: //pass through
					case ShinroPuzzle.N: //pass through
					//case s: //pass through
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
	
	/*
	Let X denote all free positions within some subset of rows (columns) of the
	puzzle, where |X| > 1. Let n denote the total count of stones remaining to be
	placed within the rows (columns) of X. Let A denote some subset of unsatisfied
	arrows of the puzzle. Let us require that each arrow in A has free positions 
	along its path that are contained entirely within X, and that no arrow in A 
	has a path whose free positions intersect the free positions in the path of 
	another arrow in A. Let P denote the set of positions within X that are 
	covered by the free positions along the paths of every arrow in A. If |A| = n,
	then we know that all remaining stones must be located somewhere in positions
	in P. Therefore, no stones will be found in X \ P, and these remaining positions
	can be marked as filled.
	*/
	private boolean findNonIntersecting() {
		ArrayList<GridPos> setX = new ArrayList<GridPos>();
		ArrayList<GridPos> setA = new ArrayList<GridPos>();
		ArrayList<GridPos> uArrows = this.getUnsatisfiedArrows();
		int i, j, n = 0;
		//every contiguous subset of rows
		for (j = 0; j < (puzzle.size() - 1); j++) {
			setX = new ArrayList<GridPos>();
			setA = new ArrayList<GridPos>();
			n = 0;
			for (i = j; i < puzzle.size(); i++) {
				setX.addAll(
						puzzle.getTypeInList(
								ShinroPuzzle.EMPTY,
								puzzle.getRow(i)
						)
				);
				n += this.calcDiffInRow(i);
				if (setX.size() <= 1) { //because of the solving algorithm, 
					continue; 			//this should never happen.
				}
				//build setA
				for (GridPos pos : uArrows) {
					if (this.listContainsList(setX,
							puzzle.getTypeInList(ShinroPuzzle.EMPTY,
									puzzle.getArrowToEdge(pos)))) {
						setA.add(pos);
					}
				}
				if (setA.size() < n) { //not enough arrows for spaces to fill
					continue;
				}
				if (this.nonIntersectingCommon(n, setX, setA)) {
					return true;
				}
			}
		}
		//every contiguous subset of columns
		for (j = 0; j < (puzzle.size() - 1); j++) {
			setX = new ArrayList<GridPos>();
			setA = new ArrayList<GridPos>();
			n = 0;
			for (i = j; i < puzzle.size(); i++) {
				setX.addAll(
						puzzle.getTypeInList(
								ShinroPuzzle.EMPTY,
								puzzle.getCol(i)
						)
				);
				n += this.calcDiffInCol(i);
				if (setX.size() <= 1) { //because of the solving algorithm, 
					continue; 			//this should never happen.
				}
				//build setA
				for (GridPos pos : uArrows) {
					if (this.listContainsList(setX,
							puzzle.getTypeInList(ShinroPuzzle.EMPTY,
									puzzle.getArrowToEdge(pos)))) {
						setA.add(pos);
					}
				}
				if (setA.size() < n) { //not enough arrows for spaces to fill
					continue;
				}
				if (this.nonIntersectingCommon(n, setX, setA)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean nonIntersectingCommon(int n, ArrayList<GridPos> setX,
			ArrayList<GridPos> setA) {
		ArrayList<GridPos> setP = new ArrayList<GridPos>();
		ArrayList<GridPos> setToX = new ArrayList<GridPos>();
		this.removeIntersecting(setA);
		if (n!= 0 && setA.size() == n) { //the move has been found!
			//build setP
			for (GridPos pos : setA) {
				for (GridPos p : puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
						puzzle.getArrowToEdge(pos))) {
					setP.add(p);
				}
			}
			//build final set
			setToX.addAll(setX);
			for (GridPos pos : setP) {
				setToX.remove(pos);
			}
			if (setToX.size() > 0) {
				//for (GridPos p : setA) {     //DEBUG
				//	System.out.print(p + " "); //
				//}							   //
				puzzle.fillSpacesWithX(setToX);
				return true;
			}
		}
		return false;
	}
	
	/*
	Let X denote a row (column). Let n be the number of stones remaining to be
	placed in X. Let m > n be the number of unfilled positions in X. Let P be the
	set of unfilled positions in X, whose column (row) count of remaining stones,
	less the count of placed stones, is equal to one. A total of m−n positions 
	along X will be marked as filled. We seek m − n unsatisfied arrows, A, whose
	paths contain unfilled positions. Let us require that there is no arrow in A
	whose unfilled positions intersect the unfilled positions of another arrow in
	A, or whose unfilled positions intersect P. Let us also impose that every 
	unfilled position represented by A must share the column (row) of a position 
	in P. Thus, satisfaction of an arrow in A identifies a subset of P in which a 
	filled position must be located. Let S be the set of all such subsets. Each 
	time a subset is added to S, the possible stone positions in X is reduced by
	one. Once this count reaches m − n, then we know that stones must be located 
	in any position in X that is not in P.
	*/
	
	//TODO: Some serious cleanup and this method may not be totally correct
	private boolean findPigeonhole() {		
		ArrayList<GridPos> arrows = this.getUnsatisfiedArrows();
		
		ArrayList<GridPos> setP = new ArrayList<GridPos>();
		ArrayList<GridPos> setA = new ArrayList<GridPos>();
		ArrayList<GridPos> spaces = new ArrayList<GridPos>();
		ArrayList<GridPos> remList = new ArrayList<GridPos>();
		int i, n, m;
		
		//columns
		ArrayList<Integer> rows = new ArrayList<Integer>();
		
		for (int x = 0; x < puzzle.size(); x++) {
			n = this.calcDiffInCol(x);
			m = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getCol(x)).size();
			if (m > n) {
				//build setP
				setP = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getCol(x));
				for (i = setP.size() - 1; i >= 0; i--) {
					if (this.calcDiffInRow(setP.get(i).getRow()) != 1) {
						setP.remove(i);
					}
				}
				
				//System.out.print("Col " + x + " setP: "); //DEBUG
				//for (GridPos p : setP) {				    //
				//	System.out.print(p + " ");			    //
				//}										    //
				//System.out.println();					    //
				
				//build setA
				setA.clear();
				setA.addAll(arrows);
				
				//remove any A that intersect setP
				for (i = setA.size() - 1; i >= 0; i--) {
					if (this.listIntersectsList(setP, 
							puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
									puzzle.getArrowToEdge(setA.get(i))))) {
						setA.remove(i);
					}
				}
				
				//remove from A arrows whose spaces don't share a row with setP
				remList.clear();
				for (GridPos arrow : setA) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					rows.addAll(this.getListOfRows(spaces));
					rows.removeAll(this.getListOfRows(setP));
					if (rows.size() > 0) {
						remList.add(arrow);
					}
					rows.clear();
				}
				setA.removeAll(remList);
				
				//finally, remove any intersecting arrows from A
				this.removeIntersecting(setA);
				
				//System.out.print("Col " + x + " setA: "); //DEBUG
				//for (GridPos a : setA) {				    //
				//	System.out.print(a + " ");			    //
				//}										    //
				//System.out.println("\nm-n = " + (m - n));	//
				
				//move on if not enough arrows in A
				//I think |A| MUST = m - n
				if (setA.size() < (m - n)) {
					continue;
				}
				
				//find which spaces need to be marked
				//this should be any empty space in row C not covered by one of the arrows
				rows.clear();
				rows.addAll(
						this.getListOfRows(
								puzzle.getTypeInList(
										ShinroPuzzle.EMPTY,
										puzzle.getCol(x)
								)
						)
				);
				for (GridPos arrow : setA) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					rows.removeAll(this.getListOfRows(spaces));
				}
				
				/* 
				 * To avoid an infinite loop, rows.size must be > 0
				 */	
				if (rows.size() == 0) {
					continue;
				}
				
				/*
				 * If one of the empty spaces in column X shares a row with
				 * the values in rows, mark it as filled.
				 */
				for (Integer row : rows) {
					for (GridPos pos : puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getCol(x))) {
						if (pos.getRow() == row) {
							puzzle.putPoint(pos);
						}
					}
				}
				
				//System.out.println("Col PH"); //DEBUG
				
				return true;
			}
		}
		//rows
		ArrayList<Integer> cols = new ArrayList<Integer>();
		setP.clear();
		setA.clear();
		remList.clear();
		spaces.clear();
		
		for (int x = 0; x < puzzle.size(); x++) {
			n = this.calcDiffInRow(x);
			m = puzzle.getTypeInList(ShinroPuzzle.EMPTY, puzzle.getRow(x)).size();
			if (m > n) {
				//build setP
				setP = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
						puzzle.getRow(x));
				for (i = setP.size() - 1; i >= 0; i--) {
					if (this.calcDiffInCol(setP.get(i).getCol()) != 1) {
						setP.remove(i);
					}
				}
				
				//System.out.print("Row " + x + " setP: "); //DEBUG
				//for (GridPos p : setP) {				  //
				//	System.out.print(p + " ");			  //
				//}										  //
				//System.out.println();					  //
				
				//build setA
				setA.clear();
				setA.addAll(arrows);
				
				//remove any A that intersect setP
				for (i = setA.size() - 1; i >= 0; i--) {
					if (this.listIntersectsList(setP, 
							puzzle.getTypeInList(ShinroPuzzle.EMPTY, 
									puzzle.getArrowToEdge(setA.get(i))))) {
						setA.remove(i);
					}
				}
				
				//remove from A arrows whose spaces don't share a row with setP
				remList.clear();
				for (GridPos arrow : setA) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					cols.addAll(this.getListOfCols(spaces));
					cols.removeAll(this.getListOfCols(setP));
					if (cols.size() > 0) {
						remList.add(arrow);
					}
					cols.clear();
				}
				setA.removeAll(remList);
				
				//finally, remove any intersecting arrows from A
				this.removeIntersecting(setA);
				
				//System.out.print("Row " + x + " setA: "); //DEBUG
				//for (GridPos a : setA) {				  //
				//	System.out.print(a + " ");			  //
				//}										  //
				//System.out.println("\nm-n = " + (m - n)); //
				
				//move on if not enough arrows in A
				//perhaps |A| MUST = m - n
				if (setA.size() < (m - n)) {
					continue;
				}
				
				//find which spaces need to be marked
				//this should be any space not covered by one of the arrows
				cols.clear();
				cols.addAll(
						this.getListOfCols(
								puzzle.getTypeInList(
										ShinroPuzzle.EMPTY,
										puzzle.getRow(x)
								)
						)
				);
				for (GridPos arrow : setA) {
					spaces = puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getArrowToEdge(arrow));
					cols.removeAll(this.getListOfCols(spaces));
				}
				
				/* potentially in here, there could be a scenario where, for example,
				 * there are three arrows in A and m-n = 2 and some two of the three
				 * will satisfy the criteria for the pigeonhole principle
				 */
				
				/* 
				 * To avoid an infinite loop, cols.size must be > 0
				 */				
				if (cols.size() == 0) {
					continue;
				}
				
				
				/*
				 * If one of the empty spaces in column X shares a row with
				 * the values in rows, mark it as filled.
				 */
				for (Integer col : cols) {
					for (GridPos pos : puzzle.getTypeInList(ShinroPuzzle.EMPTY,
							puzzle.getRow(x))) {
						if (pos.getCol() == col) {
							puzzle.putPoint(pos);
						}
					}
				}
				
				//System.out.println("Row PH"); //DEBUG
				
				return true;
			}
		}
		return false;
	}
	
	/* Finding Impossible Scenarios:
	 * Here's my guess with this: Iterate through unsatisfied arrows and their
	 * spaces. Place the point at each space and check for an unsatisfiable arrow.
	 * That is, if set S is the set of spaces for arrow A: if for every space in
	 * S there is a zero diffInRow or diffInCol, A is unsatisfiable. Then you can
	 * put an X at that point. If that doesn't work, then you have to start looking
	 * for combination scenarios (screw that)
	 */	
	private boolean findUnsatisfiable() {
		//For every empty space in puzzle
		for (GridPos space : puzzle.getListByType(ShinroPuzzle.EMPTY)) {				
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
					
					//System.out.println("@: " + space); //DEBUG
					
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
	
	public int[] solve() {
		int moveDifficulty;
		boolean solved = false;
		while (!solved) {
			moveDifficulty = this.nextMove();
			if (moveDifficulty > 0) {
				this.numMovesByDifficulty[moveDifficulty]++;
				this.numMovesByDifficulty[0]++; //num Total moves
				
				//System.out.print(this);									//DEBUG
				//System.out.println("Mv " + this.numMovesByDifficulty[0] +	//
				//		": " + moveDifficulty + "\n");						//
				
				if (puzzle.verifySolution()) {
					solved = true;
				}
			}
			else { //there was some kind of error/the puzzle is unsolvable
				break;
			}
		}
		if (solved) {
			//System.out.println("Puzzle solved!"); //DEBUG
		}
		return this.numMovesByDifficulty;
	}
	
	@Override
	public String toString() {
		return puzzle.toString();
		//and do some more stuff later
	}
}
