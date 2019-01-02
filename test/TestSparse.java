import edu.indiana.soic.spidal.common.sparse.SparseMatrix;
import edu.indiana.soic.spidal.common.sparse.SparseMatrixUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSparse {

    @Test
    public void testSparseMatrixMulti() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        int[] columns = {0, 2, 0, 1, 2, 2};
        int[] rowpointer = {0, 2, 5};
        double[] vector = {2.0, 2.0, 2.0};
        double[] results = new double[3];
        double[] correct = {6.0, 24.0, 12.0};
        SparseMatrix sparseMatrix = new SparseMatrix(values, columns, rowpointer);
        SparseMatrixUtils.sparseMatrixMatrixMultiply(sparseMatrix, vector, 3, 1, results);
        assertEquals(results[0], correct[0]);
        assertEquals(results[1], correct[1]);
        assertEquals(results[2], correct[2]);
    }

    @Test
    public void testSparseMatrixMulti2() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        int[] columns = {0, 2, 0, 1, 2, 2};
        int[] rowpointer = {0, 2, 5};
        double[] vector = {2.0, 2.0, 2.0, 1.0, 1.0, 1.0};
        double[] results = new double[6];
        double[] correct = {6.0, 24.0, 12.0,3.0, 12.0, 6.0};
        SparseMatrix sparseMatrix = new SparseMatrix(values, columns, rowpointer);
        SparseMatrixUtils.sparseMatrixMatrixMultiply(sparseMatrix, vector, 3, 2, results);
        assertEquals(results[3], correct[3]);
        assertEquals(results[4], correct[4]);
        assertEquals(results[5], correct[5]);
    }

    @Test
    public void testSparseMatrixMultiWithDiagonal(){
        double[] values = {1.0,3.0,5.0,7.0,2.0,4.0,6.0,8.0,1.0,3.0,5.0,7.0,2.0,4.0,6.0,8.0};
        double[] B = {1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,1.0,1.0,1.0};
        int[] columns = {0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3};
        int[] rowPointer = {0,4,8,12};
        double[] diagonal = {1.0,4.0,5.0,8.0};
        double[] results = new double[4*3];
        SparseMatrix sparseMatrix = new SparseMatrix(values,columns,rowPointer, diagonal);
        SparseMatrixUtils.sparseMatrixMatrixMultiplyWithDiagonal(sparseMatrix, B, 4, 3, results, 0);
        assertEquals(55.0, results[0]);
    }
}
