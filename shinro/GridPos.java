package shinro;

/**
 * Data structure to represent a row and column pair (that is, a grid position)
 * @author Joseph Eib
 */
public class GridPos {
	private int row, col;
	
	/**
	 * Creates a new GridPos object
	 * <p>
	 * The row and column are initialized to zero
	 */
	public GridPos() {
		this.row = this.col = 0;
	}

	/**
	 * Created a new GridPos object with specified row and column values
	 * @param row  the desired row
	 * @param col  the desired column
	 */
	public GridPos(int row, int col) {
		this.row = row;
		this.col = col;
	}

	/**
	 * Gets the value of this GridPos's row
	 * @return this GridPos's row value
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Sets the value of this GridPos's row
	 * @param row  the desired new row value
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Gets the value of this GridPos's column
	 * @return this GridPos's column value
	 */
	public int getCol() {
		return col;
	}

	/**
	 * Sets the value of this GridPos's row
	 * @param col  the desired new column value
	 */
	public void setCol(int col) {
		this.col = col;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + row + ", " + col + ")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GridPos))
			return false;
		GridPos other = (GridPos) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
	
	
}
