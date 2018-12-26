package edu.indiana.soic.spidal.common.sparse;

public class SparseMatrixUtils {

    public static void sparseMatrixMatrixMultiply(SparseMatrix sparseMatrix,
                                             double[] B, int N, double[] out) {
        double[] values = sparseMatrix.getValues();
        double[] mutiples = new double[values.length];
        int[] columns = sparseMatrix.getColumns();
        int[] rowPointers = sparseMatrix.getRowPointers();
        int bOffSet = 0;

        for(int dimension = 0; dimension < N; dimension++){
            bOffSet = N*dimension;
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
                    tempSum += values[trackIndex]*mutiples[trackIndex];
                    trackIndex++;
                }
                out[bOffSet + localRow] = tempSum;

            }


        }
    }
}
