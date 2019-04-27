package edu.indiana.soic.spidal.common.sparse;

import edu.indiana.soic.spidal.common.Range;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class SparseMatrixFile {

    private static final double INV_SHORT_MAX = 1.0 / Short.MAX_VALUE;
    private static final double INV_INT_MAX = 1.0 / Integer.MAX_VALUE;


    private String pathname;

    private File indicesFile;
    private File dataFile;


    private SparseMatrixFile() {
    }

    public SparseMatrixFile(String pathname, long rowLength, long colLength) {
        this.pathname = pathname;
    }

    /**
     * Square matrix only
     *
     * @param indicesPath indices
     * @param dataPath    data
     * @return partial sparse matrix
     */
    public static SparseMatrix loadIntoMemory(String indicesPath,
                                              String dataPath,
                                              Range globalThreadRowRange,
                                              int dim, ByteOrder endianness) {
        //TODO: check if we can use arrays instead of lists we need to know the length of values before hand for this
        // maybe we can do a two pass method also need to update data read code
        // so that it can handle large files ref readRowRangeInternal method
        int startRow = globalThreadRowRange.getStartIndex();
        int endRow = globalThreadRowRange.getEndIndex();
        int length = globalThreadRowRange.getLength();
        long blockSize = 1024 * 1024 * 200; // 200Mb, the index file will take 200*4
        if (startRow < 0 || startRow > endRow || startRow > dim) {
            throw new RuntimeException("Illegal row range");
        }
        try {
            SparseMatrixFile smf = new SparseMatrixFile();
            smf.indicesFile = new File(indicesPath);
            smf.dataFile = new File(dataPath);
            FileChannel fcIndex = (FileChannel) Files
                    .newByteChannel(Paths.get(indicesPath),
                            StandardOpenOption.READ);
            FileChannel fcData = (FileChannel) Files
                    .newByteChannel(Paths.get(dataPath),
                            StandardOpenOption.READ);
            long totalLength = fcData.size();
            long rbSizeDa = (blockSize > totalLength) ?
                    totalLength : blockSize;
            long rbSizeIn = rbSizeDa * 2; // Bacause we have two int |4*2| values
            // for each data value which is a short |2| value

            long currentRead = 0;
            ByteBuffer outbyteBufferdata =
                    ByteBuffer.allocate((int) rbSizeDa);
            ByteBuffer outbyteBufferindex =
                    ByteBuffer.allocate((int) rbSizeIn);
            outbyteBufferdata.order(endianness);
            outbyteBufferindex.order(endianness);

            List<Double> values = new ArrayList<>();
            List<Integer> columns = new ArrayList<>();
            int[] rowPointer = new int[length];
            Arrays.fill(rowPointer, -1);
            int count = 0;
            int countflips = 0;
            //checks if the loop has already completed the row range
            boolean isDone = false;
            Map<Integer, List<double[]>> flipValues = new HashMap<>();
            int previousLocalRow = 0;
            outer:
            while (currentRead < totalLength) {
                outbyteBufferdata.clear();
                outbyteBufferindex.clear();

                rbSizeDa = (blockSize > (totalLength - currentRead)) ?
                        (totalLength - currentRead) : blockSize;
                rbSizeIn = rbSizeDa * 2;

                //if the size is smaller create two new smaller buffs
                if (rbSizeDa != outbyteBufferdata.capacity()) {
                    System.out.println("#### Using new ByteBuffer");
                    outbyteBufferdata = ByteBuffer.allocate((int) rbSizeDa);
                    outbyteBufferindex = ByteBuffer.allocate((int) rbSizeIn);
                    outbyteBufferdata.clear();
                    outbyteBufferindex.clear();
                }
                fcData.read(outbyteBufferdata, currentRead);
                fcIndex.read(outbyteBufferindex, currentRead * 2);
                outbyteBufferdata.flip();
                outbyteBufferindex.flip();


                while (outbyteBufferindex.hasRemaining() && outbyteBufferdata.hasRemaining()) {
                    //first check if the loaded values contains the range
                    //we check the last row index for that
//                    if (outbyteBufferindex.getInt((int) (rbSizeIn - 8)) < startRow)
//                        break;

                    int row = outbyteBufferindex.getInt();
                    int col = outbyteBufferindex.getInt();
                    if (row > endRow && col > endRow) break outer;
                    double value = outbyteBufferdata.getInt() * INV_INT_MAX;
                    //add it for future ref
                    if (col >= startRow && col <= endRow) {
//                        if (flipValues.containsKey(col)) {
//                            double[] temp = {row, value};
//                            flipValues.get(col).add(temp);
//                        } else {
//                            double[] temp = {row, value};
//                            flipValues.put(col, new ArrayList<>());
//                            flipValues.get(col).add(temp);
//                        }
                        countflips++;
//                        if (countflips % 49999999 == 0) {
//                            System.out.println("%%%%%%%%% : " + countflips);
//                        }
                    }
                    if (row >= startRow && row <= endRow) {
                        int localRow = row - startRow;
                        countflips++;
                        //If we are jumping couple of rows check that they are
                        //in the flipValues and add them before moving on to the
                        //current row
//                        while (localRow - previousLocalRow > 1) {
//                            previousLocalRow++;
//                            if (flipValues.containsKey(previousLocalRow + startRow)) {
//                                List<double[]> temp = flipValues.remove(previousLocalRow + startRow);
//                                for (double[] vals : temp) {
//                                    values.add(vals[1]);
//                                    columns.add((int) vals[0]);
//                                    if (rowPointer[previousLocalRow] == -1) {
//                                        rowPointer[previousLocalRow] = count;
//                                    }
//                                    count++;
//                                }
//                            }
//                        }
//
//                        //If there were previous values for this row we need to
//                        //add the first since they have lower column values
//                        if (flipValues.containsKey(row)) {
//                            List<double[]> temp = flipValues.remove(row);
//                            for (double[] vals : temp) {
//                                values.add(vals[1]);
//                                columns.add((int) vals[0]);
//                                if (rowPointer[localRow] == -1) {
//                                    rowPointer[localRow] = count;
//                                    previousLocalRow = localRow;
//                                }
//                                count++;
//                            }
//                        }
//                        values.add(value);
//                        columns.add(col);
//                        if (rowPointer[localRow] == -1) {
//                            rowPointer[localRow] = count;
//                            previousLocalRow = localRow;
//                        }
                        count++;
//                        if (values.size() % 49999999 == 0) {
//                            System.out.println(startRow + " Too Large #########################");
//                        }

                    }
                }

                currentRead += rbSizeDa;
            }

            //Check if there are any trailing elements that have not been filled
//            while (previousLocalRow < rowPointer.length - 1) {
//                previousLocalRow++;
//                if (flipValues.containsKey(previousLocalRow + startRow)) {
//                    List<double[]> temp = flipValues.remove(previousLocalRow + startRow);
//                    for (double[] vals : temp) {
//                        values.add(vals[1]);
//                        columns.add((int) vals[0]);
//                        if (rowPointer[previousLocalRow] == -1) {
//                            rowPointer[previousLocalRow] = count;
//                        }
//                        count++;
//                    }
//                }
//            }
            System.out.printf("%d,%d\n", startRow, countflips);

            SparseMatrix sparseMatrix =
                    new SparseMatrix(ArrayUtils.toPrimitive(values.toArray(new Double[values.size()])),
                            ArrayUtils.toPrimitive(columns.toArray(new Integer[columns.size()])),
                            rowPointer);
            return sparseMatrix;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int bytesToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    private static long bytesToLong(byte[] bytes) {
        return (bytes[0] & 0xFFL) << 56
                | (bytes[1] & 0xFFL) << 48
                | (bytes[2] & 0xFFL) << 40
                | (bytes[3] & 0xFFL) << 32
                | (bytes[4] & 0xFFL) << 24
                | (bytes[5] & 0xFFL) << 16
                | (bytes[6] & 0xFFL) << 8
                | (bytes[7] & 0xFFL);
    }

    private static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }
}
