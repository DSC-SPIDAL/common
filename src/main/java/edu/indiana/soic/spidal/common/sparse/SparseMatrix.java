package edu.indiana.soic.spidal.common.sparse;

/**
 * The sparse matrix is stored in CSR format
 * [Values], [Column indices] and [Row pointers]
 */
public class SparseMatrix {
    private final short[] values;
    private double[] valuesDouble;
    private final int[] columns;
    private final int[] rowPointers;
    boolean isDouble = false;
    /**
     * keeps the diagonal values of the matrix if applicable;
     */
    private final double[] diagonal;

    private boolean hasDiagonal;

    public SparseMatrix(short[] values, int[] columns, int[] rowPointers) {
        this.rowPointers = rowPointers;
        this.values = values;
        this.columns = columns;
        this.diagonal = null;
        this.hasDiagonal = false;

    }

    public SparseMatrix(short[] values, int[] columns, int[] rowPointers,
                        double[] diagonal) {
        this.rowPointers = rowPointers;
        this.values = values;
        this.columns = columns;
        this.diagonal = diagonal;
        this.hasDiagonal = true;

    }


    public SparseMatrix(int values, int columns, int rowPointers, boolean isDouble) {
        this.values = new short[0];
        this.valuesDouble = new double[values];
        this.columns = new int[columns];
        this.rowPointers = new int[rowPointers];
        this.diagonal = null;
        this.hasDiagonal = false;
        this.isDouble = isDouble;
    }
    public SparseMatrix(int values, int columns, int rowPointers) {
        this.values = new short[values];
        this.columns = new int[columns];
        this.rowPointers = new int[rowPointers];
        this.diagonal = null;
        this.hasDiagonal = false;
    }

    public double[] getDiagonal() {
        return diagonal;
    }

    public boolean isHasDiagonal() {
        return hasDiagonal;
    }

    public void setHasDiagonal(boolean hasDiagonal) {
        this.hasDiagonal = hasDiagonal;
    }

    public SparseMatrix(int values, int columns, int rowPointers,
                        int diagonals) {

        if(rowPointers != diagonals) throw new IllegalStateException("The " +
                "diagonal must be of size rows");

        this.values = new short[values];
        this.columns = new int[columns];
        this.rowPointers = new int[rowPointers];
        this.diagonal = new double[diagonals];
        this.hasDiagonal = true;
    }

    public SparseMatrix(int values, int columns, int rowPointers,
                        int diagonals, boolean isDouble) {

        if(rowPointers != diagonals) throw new IllegalStateException("The " +
                "diagonal must be of size rows");

        this.values = new short[0];
        this.valuesDouble = new double[values];
        this.columns = new int[columns];
        this.rowPointers = new int[rowPointers];
        this.diagonal = new double[diagonals];
        this.hasDiagonal = true;
        this.isDouble = isDouble;
    }

    public short[] getValues() {
        return values;
    }

    public double[] getValuesDouble(){
        return valuesDouble;
    }

    public int[] getColumns() {
        return columns;
    }

    public int[] getRowPointers() {
        return rowPointers;
    }

    public boolean isDouble(){
        return isDouble;
    }
}