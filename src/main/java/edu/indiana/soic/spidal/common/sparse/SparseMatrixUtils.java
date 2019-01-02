package edu.indiana.soic.spidal.common.sparse;

public class SparseMatrixUtils {

    /**
     * Simple sparse to matrix multiply
     *
     * @param sparseMatrix
     * @param B
     * @param N
     * @param dims
     * @param out
     */
    public static void sparseMatrixMatrixMultiply(SparseMatrix sparseMatrix,
                                                  double[] B, int N, int dims, double[] out) {
        double[] values = sparseMatrix.getValues();
        double[] mutiples = new double[values.length];
        int[] columns = sparseMatrix.getColumns();
        int[] rowPointers = sparseMatrix.getRowPointers();
        int bOffSet = 0;

        for (int dimension = 0; dimension < dims; dimension++) {
            bOffSet = N * dimension;
            // fill the multiples
            for (int i = 0; i < mutiples.length; i++) {
                int colIndex = columns[i];
                mutiples[i] = B[bOffSet + colIndex];
            }

            int trackIndex = 0;
            for (int localRow = 0; localRow < rowPointers.length; localRow++) {
                int rowPointer = rowPointers[localRow];
                int colCount = (localRow == rowPointers.length - 1) ?
                        values.length - rowPointer
                        : rowPointers[localRow + 1] - rowPointer;

                double tempSum = 0;
                for (int colC = 0; colC < colCount; colC++) {
                    tempSum += values[trackIndex] * mutiples[trackIndex];
                    trackIndex++;
                }
                out[bOffSet + localRow] = tempSum;

            }

        }
    }

    public static void sparseMatrixMatrixMultiplyWithDiagonal(SparseMatrix sparseMatrix,
                                                              double[] B, int N, int dims, double[] out, int globalRowOffset) {
        if (!sparseMatrix.isHasDiagonal()) {
            throw new IllegalStateException("Diagonal array needs to be intialized");
        }

        double[] values = sparseMatrix.getValues();
        double[] mutiples = new double[values.length];
        int[] columns = sparseMatrix.getColumns();
        int[] rowPointers = sparseMatrix.getRowPointers();
        double[] diagonal = sparseMatrix.getDiagonal();

        for (int dimension = 0; dimension < dims; dimension++) {
            // fill the multiples
            for (int i = 0; i < mutiples.length; i++) {
                int colIndex = columns[i];
                mutiples[i] = B[colIndex*dims + dimension];
            }

            int trackIndex = 0;
            for (int localRow = 0; localRow < rowPointers.length; localRow++) {
                int rowPointer = rowPointers[localRow];
                int colCount = (localRow == rowPointers.length - 1) ?
                        values.length - rowPointer
                        : rowPointers[localRow + 1] - rowPointer;
                int globalRow = localRow + globalRowOffset;
                double tempSum = 0;
                for (int colC = 0; colC < colCount; colC++) {

                    //The diagonal values are taken from the diagonal entry
                    if (globalRow == columns[trackIndex]) {
                        tempSum += diagonal[localRow] * mutiples[trackIndex];
                    } else {
                        tempSum += values[trackIndex] * mutiples[trackIndex];
                    }
                    trackIndex++;
                }
                out[localRow*dims + dimension] = tempSum;

            }

        }
    }

    public static void sparseMatrixMatrixMultiplyWithDiagonal(SparseMatrixWeightWrap sparseMatrixWeightWrap,
                                                              double[] B, double[] diagonal, int N, int dims, double[] out, int globalRowOffset) {
        double[] values = sparseMatrixWeightWrap.getDistance().getValues();
        double[] mutiples = new double[values.length];
        int[] columns = sparseMatrixWeightWrap.getDistance().getColumns();
        int[] rowPointers = sparseMatrixWeightWrap.getDistance().getRowPointers();

        for (int dimension = 0; dimension < dims; dimension++) {
            // fill the multiples
            for (int i = 0; i < mutiples.length; i++) {
                int colIndex = columns[i];
                mutiples[i] = B[colIndex*dims + dimension];
            }

            int trackIndex = 0;
            for (int localRow = 0; localRow < rowPointers.length; localRow++) {
                int rowPointer = rowPointers[localRow];
                int colCount = (localRow == rowPointers.length - 1) ?
                        values.length - rowPointer
                        : rowPointers[localRow + 1] - rowPointer;
                int globalRow = localRow + globalRowOffset;
                double tempSum = 0;
                for (int colC = 0; colC < colCount; colC++) {

                    //The diagonal values are taken from the diagonal entry
                    if (globalRow == columns[trackIndex]) {
                        tempSum += diagonal[localRow] * mutiples[trackIndex];
                    } else {
                        tempSum += -(1.0) * mutiples[trackIndex];
                    }
                    trackIndex++;
                }
                out[localRow*dims + dimension] = tempSum;
            }

        }
    }
}
