package shinro;

import java.util.ArrayList;

/**
 * Data structure and methods to represent a Shinro puzzle
 * @author Joseph Eib
 * @since December 2014
 */
public class ShinroPuzzle implements Cloneable{
	private int[][] puzzleGrid;
	private int[] rowHeaderNum, colHeaderNum;
	private int size, numPoints;
	
	public static final int SIZE = 8, POINTS = 12;
	public static final int EMPTY = 0, POINT = 9, X = -9,
			N = 1, NE = 2, E = 3, SE = 4, S = 5, SW = 6, W = 7, NW = 8,
			n = -1, ne = -2, e = -3, se = -4, s = -5, sw = -6, w = -7, nw = -8;
	
	/**
	 * Creates a new empty ShinroPuzzle
	 * <p>
	 * All spaces are initialized to be EMPTY.
	 * All header numbers and numPoints are initialized to be zero.
	 */
	public ShinroPuzzle() {
		int i, j;
		this.size = SIZE;
		this.numPoints = 0;
		this.puzzleGrid = new int[this.size][this.size];
		this.rowHeaderNum = new int[this.size];
		this.colHeaderNum = new int[this.size];
		
		for (i = 0; i < this.size; i++)
			for (j = 0; j < this.size; j++)
				this.puzzleGrid[i][j] = 0;
		
		for (i = 0; i < this.size; i++) {
			this.rowHeaderNum[i] = this.colHeaderNum[i] = 0;
		}
	}
	
	/**
	 * Creates a new ShinroPuzzle with the specified layout.
	 * <p>
	 * Once the puzzle is initialized, any POINTs are removed by initPuzzle().
	 * @param intMatrix  the two-dimensional array containing the layout of the
	 * puzzle.
	 */
	public ShinroPuzzle(int[][] intMatrix) {
		this();
		this.initPuzzle(intMatrix);
	}
	
	/**
	 * GridPos-based convenience method for {@link #atPos(int, int)}
	 * @param pos  the GridPos of the desired location 
	 * @return the int value at the desired coordinates
	 */
	public int atPos(GridPos pos) {
		return this.atPos(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Gets a value from the puzzleGrid by the row and column indices
	 * @param row  the row index of the space to get
	 * @param col  the column index of the space to get
	 * @return the int value at the desired coordinates
	 */
	public int atPos(int row, int col) {
		return this.puzzleGrid[row][col];
	}
	
	/**
	 * GridPos-based convenience method for {@link #clearSpace(int, int)}
	 * @param pos  the GridPos of the desired location
	 */
	public void clearSpace(GridPos pos) {
		this.clearSpace(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Deletes the contents of a specified space unless it is an arrow
	 * If the space contains a POINT, then any arrows satisfied by the point will
	 * be unsatisfied.
	 * <p>
	 * This method does nothing if the space is already EMPTY
	 * @param row  the row index of the space to clear
	 * @param col  the column index of the space to clear
	 */
	public void clearSpace(int row, int col) {
		if (!this.isArrow(row, col)) {
			if (this.atPos(row, col) == POINT) {
				for (GridPos arrow : this.getPointingArrows(row, col)) {
					if (this.getTypeInList(POINT, 
							this.getArrowToEdge(arrow)).size() == 1) {
						this.unsatsifyArrow(arrow);
					}
				}
			}
			this.puzzleGrid[row][col] = EMPTY;
		}
	}
	
	/**
	 * Returns a clone of this ShinroPuzzle instance
	 * @return the copied ShinroPuzzle
	 */
	@Override
	public ShinroPuzzle clone() {
		ShinroPuzzle copy = new ShinroPuzzle();
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				copy.setPos(i, j, this.atPos(i, j));
			}
			copy.setRowHeaderNum(i, this.getRowHeaderNum(i));
			copy.setColHeaderNum(i, this.getColHeaderNum(i));
		}
		return copy;
	}
	
	/**
	 * Tests for logical equality between two ShinroPuzzles
	 * <p>
	 * This means an equal puzzle must have the same row and column header numbers
	 * as well as the same numbers of arrows and POINTS in the same places
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		else if (obj == this) return true;
		
		if (obj instanceof ShinroPuzzle) {
			ShinroPuzzle p = (ShinroPuzzle)obj;
			//same size?
			if (this.size() != p.size()) return false;
			else {
				for (int i = 0; i < this.size; i++) {
					//same headers?
					if (this.getRowHeaderNum(i) != p.getRowHeaderNum(i)) {
						return false;
					}
					if (this.getColHeaderNum(i) != p.getColHeaderNum(i)) {
						return false;
					}
					for (int j = 0; j < this.size; j++) {
						//same object at (i, j)?
						if (this.atPos(i, j) != p.atPos(i, j)) {
							//has X where space is?
							if ((this.atPos(i, j) == X && p.atPos(i, j) == EMPTY) 
									|| (this.atPos(i, j) == EMPTY 
											&& p.atPos(i, j) == X)) {
								continue;
							}
							return false;
						}
					}
				}
				return true;
			}
		}
		else return false;
	}
	
	/**
	 * Fills a ArrayList of spaces with an X
	 * <p>
	 * This method does nothing to ShinroSquares that are not empty.
	 * @see ShinroPuzzle#putX(int, int)
	 * @param spaces  an ArrayList of GridPos to fill with Xs
	 */
	public void fillSpacesWithX(ArrayList<GridPos> spaces) {
		for (int i = 0; i < spaces.size(); i++) {
			this.putX(spaces.get(i));
		}
	}
	
	/**
	 * GridPos-based convenience method for {@link #getArrowToEdge(int, int)}
	 * @param pos the GridPos of the desired arrow
	 * @return an ArrayList of the GridPos of all elements from a specified arrow
	 * to the edge it points to
	 */
	public ArrayList<GridPos> getArrowToEdge(GridPos pos) {
		return this.getArrowToEdge(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Gets an ArrayList of GridPos from an arrow to the edge the edge of the
	 * puzzleGrid
	 * <p>
	 * The list includes the arrow.
	 * If the item at the specified location is not an arrow, an empty ArrayList
	 * is returned.
	 * @param row  the row index of the specified arrow
	 * @param col  the column index of the specified arrow
	 * @return an ArrayList of the GridPos of all elements from a specified arrow
	 * to the edge it points to
	 */
	public ArrayList<GridPos> getArrowToEdge(int row, int col) {
		ArrayList<GridPos> result = new ArrayList<GridPos>();
		int myRow = row;
		int myCol = col;
		
		while ((myRow >= 0 && myRow < this.size) 
				&& (myCol >= 0 && myCol < this.size)) {
			result.add(new GridPos(myRow, myCol));
			
			switch(this.puzzleGrid[row][col]) {
			case n: //pass through
			case N: myRow--; 
					break;
			case s: //pass through
			case S: myRow++; 
					break;
			case e: //pass through
			case E: myCol++; 
					break;
			case w: //pass through
			case W: myCol--; 
					break;
			case ne: //pass through
			case NE: myRow--; myCol++; 
					break;
			case nw: //pass through
			case NW: myRow--; myCol--; 
					break;
			case se: //pass through
			case SE: myRow++; myCol++; 
					break;
			case sw: //pass through
			case SW: myRow++; myCol--; 
					break;
			default: break;
			}
		}
		return result;
	}
	
	/**
	 * Gets an ArrayList of all coordinate pairs in a specified column
	 * @param col  the column whose list of spaces is to be returned
	 * @return an ArrayList of GridPos representing the contents of the column
	 */
	public ArrayList<GridPos> getCol(int col) {
		ArrayList<GridPos> result = new ArrayList<GridPos>();
		for (int i = 0; i < this.size; i++) {
			result.add(new GridPos(i, col));
		}
		return result;
	}
	
	/**
	 * Gets the number of POINTs in the specified column
	 * @param col  the column whose header number is to be returned
	 * @return an integer representing the number of points in the column
	 */
	public int getColHeaderNum(int col) {
		return this.colHeaderNum[col];
	}
	
	/**
	 * GridPos-based convenience method for {@link #getDiagsFromPoint(int, int)}
	 * @param pos  the GridPos of the desired location
	 * @return an ArrayList of GridPos of spaces to the NW, NE, SW, and SE of the
	 * desired location
	 */
	public ArrayList<GridPos> getDiagsFromPoint(GridPos pos) {
		return this.getDiagsFromPoint(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Gets an ArrayList of the GridPos of any point diagonally intersecting a
	 * desired location
	 * @param row  the row index of the desired location
	 * @param col  the column index of the desired location
	 * @return an ArrayList of GridPos of spaces to the NW, NE, SW, and SE of the
	 * desired location
	 */
	public ArrayList<GridPos> getDiagsFromPoint(int row, int col) {
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		myList.add(new GridPos(row, col));
		int myRow, myCol, mySize = this.size();
		//NW
		myRow = row - 1; 
		myCol = col - 1;
		while (myRow >= 0 && myCol >= 0) {
			myList.add(new GridPos(myRow, myCol));
			myRow--;
			myCol--;
		}
		//NE
		myRow = row - 1;
		myCol = col + 1;
		while (myRow >= 0 && myCol < mySize) {
			myList.add(new GridPos(myRow, myCol));
			myRow--;
			myCol++;
		}
		//SW
		myRow = row + 1;
		myCol = col - 1;
		while (myRow < mySize && myCol >= 0) {
			myList.add(new GridPos(myRow, myCol));
			myRow++;
			myCol--;
		}
		//SE
		myRow = row + 1;
		myCol = col + 1;
		while (myRow < mySize && myCol < mySize) {
			myList.add(new GridPos(myRow, myCol));
			myRow++;
			myCol++;
		}
		return myList;
	}
	
	/**
	 * Gets a list of all GridPos that contain a specified ItemType
	 * @param type  the type of the spaces to get a list of
	 * @return an ArrayList of GridPos containing every instance of the
	 * desired type in the puzzleGrid.
	 */
	public ArrayList<GridPos> getListByType(int type) {
		ArrayList<GridPos> result = new ArrayList<GridPos>();
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				if ((this.isArrow(type) && this.isArrow(i, j)) 
						|| this.atPos(i, j) == type) {
					result.add(new GridPos(i, j));
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets the number of POINTs the puzzle was initialized with.
	 * @return the number of POINTs needed to solve this puzzle
	 */
	public int getNumPoints() {
		return this.numPoints;
	}
	
	/**
	 * GridPos-based convenience method for {@link #getPointingArrows(int, int)}
	 * @param pos the GridPos of the desired location
	 * @return an ArrayList of the GridPos of any arrow pointing to the location
	 */
	public ArrayList<GridPos> getPointingArrows(GridPos pos) {
		return this.getPointingArrows(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Gets a list of all arrows which point to a desired location
	 * @param row  the row index of the desired location
	 * @param col  the column index of the desired location
	 * @return an ArrayList of the GridPos of any arrow pointing to the location
	 */
	public ArrayList<GridPos> getPointingArrows(int row, int col) {
		ArrayList<GridPos> result = new ArrayList<GridPos>(), 
				myList = new ArrayList<GridPos>();

		if (this.atPos(row, col) == POINT) {
			//row
			myList = this.getTypeInList(N, this.getRow(row)); // any arrow
			if (myList.size() > 0) {
				for (GridPos pos : myList) { 
					if ((this.atPos(pos) == E || this.atPos(pos) == e) &&
							col > pos.getCol()) {
						result.add(pos);
					}
					else if ((this.atPos(pos) == W || this.atPos(pos) == w) &&
							col < pos.getCol()) {
						result.add(pos);
					}
				}
			}
			//col
			myList = this.getTypeInList(N, this.getCol(col)); // any arrow
			if (myList.size() > 0) {
				for (GridPos pos : myList) { 
					if ((this.atPos(pos) == S || this.atPos(pos) == s) &&
							row > pos.getRow()) {
						result.add(pos);
					}
					else if ((this.atPos(pos) == N || this.atPos(pos) == n) &&
							row < pos.getRow()) {
						result.add(pos);
					}
				}
			}
			//diags
			myList = this.getTypeInList(N, //any arrow 
					this.getDiagsFromPoint(row, col));
			if (myList.size() > 0) {
				for (GridPos pos : myList) { 
					if ((this.atPos(pos) == SE || this.atPos(pos) == se) &&
							row > pos.getRow() &&
							col > pos.getCol()) {
						result.add(pos);
					}
					else if ((this.atPos(pos) == SW || this.atPos(pos) == sw) &&
							row > pos.getRow() &&
							col < pos.getCol()) {
						result.add(pos);
					}
					else if ((this.atPos(pos) == NW || this.atPos(pos) == nw) &&
							row < pos.getRow() &&
							col < pos.getCol()) {
						result.add(pos);
					}
					else if ((this.atPos(pos) == NE || this.atPos(pos) == ne) &&
							row < pos.getRow() &&
							col > pos.getCol()) {
						result.add(pos);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Gets an ArrayList of all coordinate pairs in a desired row
	 * @param row  the row whose list of spaces is to be returned
	 * @return an ArrayList of GridPos representing the contents of the row
	 */
	public ArrayList<GridPos> getRow(int row) {
		ArrayList<GridPos> result = new ArrayList<GridPos>();
		for (int i = 0; i < this.size; i++) {
			result.add(new GridPos(row, i));
		}
		return result;
	}
	
	/**
	 * Gets the number of POINTs in the specified row
	 * @param row  the row whose header number is to be returned
	 * @return an integer representing the number of points in the row
	 */
	public int getRowHeaderNum(int row) {
		return this.rowHeaderNum[row];
	}
	
	/**
	 * Gets all elements of a specified type in a list of GridPos
	 * @param type  the type of element to get
	 * @param list  the ArrayList to get elements from
	 * @return an ArrayList of GridPos containing all the elements of the desired
	 * type from the original list.
	 */
	public ArrayList<GridPos> getTypeInList(int type, ArrayList<GridPos> list) {
		ArrayList<GridPos> myList = new ArrayList<GridPos>();
		for (GridPos pos : list) {
			if ((this.isArrow(type) && this.isArrow(pos)) 
					|| this.atPos(pos) == type) {
				myList.add(pos);
			}
		}
		return myList;
	}
	
	/**
	 * Populates a ShinroPuzzle's spaces based on the content of an integer matrix.
	 * <p>
	 * A POINT will increment the relevant row and column header numbers as well as
	 * numPoints.
	 * @see ShinroPuzzle#reset()
	 * @param intMatrix  a two-dimensional array if ints containing values to
	 * assign to corresponding spaces in the puzzleGrid
	 */
	public void initPuzzle(int[][] intMatrix) {
		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				int atPos = intMatrix[i][j];
				this.puzzleGrid[i][j] = atPos;
				if (intMatrix[i][j] == 9) {
					this.numPoints++;
					this.rowHeaderNum[i]++;
					this.colHeaderNum[j]++;
				}
			}
		}
		this.reset();
	}
	
	/**
	 * GridPos-based convenience method for {@link #isArrow(int, int)}
	 * @param pos  the GridPos of the desired location
	 * @return true if the space contains an arrow, false otherwise
	 */
	public boolean isArrow(GridPos pos) {
		return this.isArrow(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Determined whether or not the specified int value is an arrow
	 * @param arrow  the value of the quantity to check for arrowness
	 * @return true if the value is an arrow, false otherwise
	 */
	public boolean isArrow(int arrow) {
		if ((arrow > 0 && arrow < 9) || (arrow < 0 && arrow > -9)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Determines whether or not a specified space contains an arrow
	 * @param row  the row index of the desired location
	 * @param col  the column index of the desired location
	 * @return true if the space contains an arrow, false otherwise
	 */
	public boolean isArrow(int row, int col) {
		int arrow = this.puzzleGrid[row][col];
		if ((arrow > 0 && arrow < 9) || (arrow < 0 && arrow > -9)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * GridPos-based convenience method for {@link #isSatisfied(int, int)}
	 * @param pos  the position of the arrow to check for satisfaction
	 * @return true if the arrow is satisfied, false otherwise
	 */
	public boolean isSatisfied(GridPos pos) {
		return this.isSatisfied(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Checks if an int value represents a satisfied arrow
	 * @param arrow  the value to check for satisfied arrowness
	 * @return true if the value represents a satsified arrow, false otherwise
	 */
	public boolean isSatisfied(int arrow) {
		if (arrow < 0 && arrow > -9) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks whether or not an arrow at a given location is satisfied
	 * @param row  the row index of the location to check
	 * @param col  the column index of the location to check
	 * @return true if the arrow is satisfied, false otherwise
	 */
	public boolean isSatisfied(int row, int col) {
		if (this.atPos(row, col) < 0 && this.atPos(row, col) > -9) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * GridPos-based convenience method for {@link #putArrow(int, int, int)}
	 * @param pos the GridPos of the desired location
	 * @param dir the direction value of the new arrow
	 * @throws IllegalArgumentException if dir is not a valid arrow value
	 * @see ShinroPuzzle#isArrow(int)
	 */
	public void putArrow(GridPos pos, int dir) 
			throws IllegalArgumentException {
		this.putArrow(pos.getRow(), pos.getCol(), dir);
	}
	
	/**
	 * Places the desired arrow value into the space at the specified location
	 * <p>
	 * This method will overwrite whatever contents the space may already have.
	 * @param row  the row index of the space in which to place the arrow
	 * @param col  the column index of the space in which to place the arrow
	 * @param dir  the direction value of the new arrow
	 * @throws IllegalArgumentException if dir is not a valid arrow value
	 * @see ShinroPuzzle#isArrow(int)
	 */
	public void putArrow(int row, int col, int dir) 
			throws IllegalArgumentException {
		if (!this.isArrow(dir)) {
			throw new IllegalArgumentException("putArrow: dir is not valid "
					+ "arrow value.");
		}			
		this.puzzleGrid[row][col] = dir;
	}
	
	/**
	 * GridPos-based convenience method for {@link #putPoint(int, int)}
	 * @param pos  the GridPos of the desired location
	 */
	public void putPoint(GridPos pos) {
		this.putPoint(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Places a new POINT at the specified location
	 * <p> 
	 * Placing a POINT will satisfy any pointing arrows.
	 * This method does nothing if the specified space contains an arrow.
	 * @param row  the row index of the space in which to place the point
	 * @param col  the column index of the space in which to place the point
	 */
	public void putPoint(int row, int col) {
		if (!this.isArrow(row, col)) {
			this.puzzleGrid[row][col] = POINT;
			ArrayList<GridPos> myArrows = this.getPointingArrows(row, col);
			for (GridPos pos: myArrows) {
				this.satsifyArrow(pos);
			}
		}
	}
	
	/**
	 * GridPos-based convenience method for {@link #putX(int, int)}
	 * @param pos  the GridPos of the desired location
	 */
	public void putX(GridPos pos) {
		this.putX(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Places a new X at the specified location
	 * <p> 
	 * This method does nothing if the specified space contains a arrow or POINT
	 * @param row  the row index of the space in which to place the X
	 * @param col  the column index of the space in which to place the X
	 */
	public void putX(int row, int col) {
		if (!this.isArrow(row, col) && this.atPos(row, col) != POINT) {
			this.puzzleGrid[row][col] = X;
		}
	}
	
	/**
	 * Clears all spaces in the puzzleGrid that do not contain an arrow
	 * @see ShinroPuzzle#clearSpace(int, int)
	 */
	public void reset() {
		for (int i = 0; i < this.size; i ++) {
			for (int j = 0; j < this.size; j++) {
				this.clearSpace(i, j);
			}
		}
	}
	
	/**
	 * GridPos-based convenience method for {@link #satsifyArrow(int, int)}
	 * @param pos  the position of the the arrow to satisfy
	 * @throws IllegalArgumentException if the location is not an arrow
	 */
	public void satsifyArrow(GridPos pos) throws IllegalArgumentException {
		this.satsifyArrow(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Marks an arrow as satisfied at a desired location
	 * <p>
	 * The value at the location is negated.
	 * @param row  the row index of the desired location
	 * @param col  the column index of the desired location
	 * @throws IllegalArgumentException if the location is not an arrow
	 */
	public void satsifyArrow(int row, int col) throws IllegalArgumentException {
		if (!this.isArrow(row, col)) {
			throw new IllegalArgumentException("satisfyArrow: pos specified not "
					+ "an arrow");
		}
		if (!this.isSatisfied(row, col)) {
			this.puzzleGrid[row][col] *= -1;
		}
	}
	
	/**
	 * Sets the number of POINTs in the specified column
	 * @param col  the column whose header number is to be set
	 * @param value  the value to set the specified column header
	 */
	public void setColHeaderNum(int col, int value) {
		this.colHeaderNum[col] = value;
	}
	
	/**
	 * Sets the row and column header nums by analyzing the puzzleGrid
	 * <p>
	 * This method is provided for convenience in {@link shinro.ShinroSolver}
	 */
	public void setHeaders() {
		for (int i = 0; i < this.size; i++) {
			this.rowHeaderNum[i] = this.getTypeInList(POINT, this.getRow(i)).size();
			this.colHeaderNum[i] = this.getTypeInList(POINT, this.getCol(i)).size();
		}
	}
	/**
	 * GridPos-based convenience method for {@link #setPos(int, int, int)}
	 * @param pos  the GridPos of the value to set
	 * @param value the value to set at the desired position.
	 * @throws IllegalArgumentException if value is less than zero or greater
	 * than 9
	 */
	 
	public void setPos(GridPos pos, int value) throws IllegalArgumentException{
		this.setPos(pos.getRow(), pos.getCol(), value);
	}
	
	/**
	 * Directly sets a specified position to a specific value
	 * <p>
	 * This method is provided for convenience in {@link shinro.ShinroSolver}
	 * @param row  the row index of the position to set
	 * @param col  the column index of the position to set
	 * @param value  the desired integer value to set the position to
	 * @throws IllegalArgumentException if value is less than EMPTY(0) or greater
	 * than POINT(9)
	 */	 
	public void setPos(int row, int col, int value) throws IllegalArgumentException {
		if (value < EMPTY || value > POINT) {
			throw new IllegalArgumentException("setPos: Illegal value --> " + value);
		}
		else {
			this.puzzleGrid[row][col] = value;
		}
	}
	
	/**
	 * Sets the number of POINTs in the specified row
	 * @param row  the row whose header number is to be set
	 * @param value  the value to set the specified row header
	 */
	public void setRowHeaderNum(int row, int value) {
		this.rowHeaderNum[row] = value;
	}
	
	/**
	 * Gets the size (side length) of this ShinroPuzzle
	 * @return the size field of this ShinroPuzzle
	 */
	public int size() {
		return this.size;
	}
	
	/**
	 * Gets the String representation of a specified space's value
	 * @param row  the row index of the desired location
	 * @param col  the column index of the desired location
	 * @return  a String representing the value at the specified location
	 */
	public String spaceToString(int row, int col) {
		switch(this.puzzleGrid[row][col]) {
		case 0: return "[  ]";
		case 9: return "[++]";
		case -9: return "[XX]";
		case 1: return "[ N]";
		case 2: return "[NE]";
		case 3: return "[ E]";
		case 4: return "[SE]";
		case 5: return "[ S]";
		case 6: return "[SW]";
		case 7: return "[ W]";
		case 8: return "[NW]";
		case -8: return "[nw]";
		case -7: return "[ w]";
		case -6: return "[sw]";
		case -5: return "[ s]";
		case -4: return "[se]";
		case -3: return "[ e]";
		case -2: return "[ne]";
		case -1: return "[ n]";
		default: return "[  ]";
		}
	}
	
	/**
	 * Gets the puzzle in the form of a matrix of ints.
	 * @return an int matrix of size SIZE X SIZE with the current values of each
	 * puzzle space.
	 */
	public int[][] toIntMatrix() {				
		return this.puzzleGrid;
	}
	
	/**
	 * Prints a String representation of this ShinroPuzzle.
	 * <p>
	 * The printout includes indices and values of row and column headers.
	 * Spaces are enclosed in square brackets with a two-character representation 
	 * of their contents.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		int positionCount = 0;
		String result = "/  /";
		for (int i : this.colHeaderNum) {
			result += positionCount++ + ":" + i + "/";
		}
		positionCount = 0;
		result += "\n";
		for (int i = 0; i < this.size; i++) {
			result += positionCount++ + ":" + this.rowHeaderNum[i];
			for (int j = 0; j < this.size; j++) {
				result += this.spaceToString(i, j);
			}
			result += "\n";
		}
		return result;
	}
	
	/**
	 * Removes Xs, it present, from a list of spaces
	 * <p>
	 * This method does nothing to ShinroSquares that do not contain Xs.
	 * @see ShinroPuzzle#clearSpace(int, int)
	 * @param spaces  an ArrayList of GridPos to remove Xs from.
	 */
	public void unfillSpacesWithX(ArrayList<GridPos> spaces) {
		for (int i = 0; i < spaces.size(); i++) {
			if (this.atPos(spaces.get(i)) != POINT) {
				this.clearSpace(spaces.get(i));
			}
		}
	}
	
	/**
	 * GridPos-based convenience method for {@link #unsatsifyArrow(int, int)}
	 * @param pos  the GridPos of the desired location
	 * @throws IllegalArgumentException if the location does not contain an arrow
	 */
	public void unsatsifyArrow(GridPos pos) throws IllegalArgumentException {
		this.unsatsifyArrow(pos.getRow(), pos.getCol());
	}
	
	/**
	 * Marks as unsatisfied an arrow at a desired location
	 * @param row  the row index of the desired location
	 * @param col  the column index of the desired location
	 * @throws IllegalArgumentException if the location does not contain an arrow
	 */
	public void unsatsifyArrow(int row, int col) throws IllegalArgumentException {
		if (!this.isArrow(row, col)) {
			throw new IllegalArgumentException("unsatisfyArrow: pos specified not "
					+ "an arrow");
		}
		if (this.isSatisfied(row, col)) {
			this.puzzleGrid[row][col] *= -1;
		}
	}
	
	/**
	 * Determines whether or not a ShinroPuzzle is in a solved state
	 * <p>
	 * The puzzle is solved if the number of POINTS in each row and column match
	 * the number in the corresponding row or column header AND all arrows are
	 * satisfied. As a consequence, puzzles with multiple solutions will be counted
	 * as solved if the puzzle is in ANY valid solution state.
	 * @return true if the puzzle is in a solved state, false otherwise
	 */
	public boolean verifySolution() {
		if (this.getListByType(POINT).size() < this.getNumPoints()) {
			return false;
		}
		else {
			for (int i = 0; i < this.size(); i++) {
				if (this.getRowHeaderNum(i) 
						!= this.getTypeInList(POINT, this.getRow(i)).size()) {
					return false;
				}
				if (this.getColHeaderNum(i) 
						!= this.getTypeInList(POINT, this.getCol(i)).size()) {
					return false;
				}
			}
			for (GridPos arrow : this.getListByType(N)) { //all arrows 
				if (!this.isSatisfied(arrow)) {
					return false;
				}
			}
			return true;
		}
	}
}
