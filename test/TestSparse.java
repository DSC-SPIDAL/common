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
}
