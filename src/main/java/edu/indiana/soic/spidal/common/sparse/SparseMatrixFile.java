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
                                              int numPoints, ByteOrder endianness, int rank) {
        //TODO: check if we can use arrays instead of lists we need to know the length of values before hand for this
        // maybe we can do a two pass method also need to update data read code
        // so that it can handle large files ref readRowRangeInternal method
        int startRow = globalThreadRowRange.getStartIndex();
        int endRow = globalThreadRowRange.getEndIndex();
        int length = globalThreadRowRange.getLength();
        long blockSize = 1024 * 1024 * 200; // 200Mb, the index file will take 200*4
        if (startRow < 0 || startRow > endRow || startRow > numPoints) {
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
            long totalLengthindex = fcIndex.size();
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

            long[] perRank_sizes = {242640710, 242649406, 242645493, 242640623, 242667570, 242644329, 242640516, 242642490, 242639799, 242708821, 242658652, 242649311, 242737227, 242645092, 242673549,
                    242675358, 242643137, 242642760, 242641885, 242645658, 242642497, 242765256, 242662894, 242642940, 242683072, 242656422, 242648278, 242781930, 242649160, 242682061, 242660954, 242651201,
                    242642483, 242647595, 242648893, 242672475, 242639770, 242641334, 242642090, 242642377, 242648390, 242730862, 242642371, 242691567, 242641863, 242644863, 242640724, 242641551, 242663978,
                    242641753, 242642423, 242649750, 242643928, 242642450, 242651540, 242649351, 242641401, 242642457, 242665124, 242650915, 242645351, 242678770, 242675208, 242641857, 242643353, 242640066,
                    242643387, 242642080, 242644071, 242640446, 242642454, 242662807, 242657101, 242676515, 242750285, 242655254, 242640389, 242641134, 242640690, 242641913, 242649499, 242640749, 242640046,
                    242655470, 242647992, 242641789, 242674707, 242640094, 242691971, 242645122, 242641133, 242644099, 242639759, 242640798, 242673139, 242641055, 242651647, 242663789, 242660391, 242640320,
                    242644580, 242640082, 242749703, 242720196, 242641065, 242655864, 242651618, 242646981, 242639939, 242646788, 242643079, 242640925, 242640726, 242642715, 242650926, 242640240, 242687150,
                    242642429, 242695227, 242650448, 242645095, 242654866, 242707091, 242640225, 242639937, 242645156, 242668376, 242640902, 242640218, 242642559, 242642624, 242676289, 242736988, 242721600,
                    242643256, 242649233, 242641497, 242660607, 242642135, 242640068, 242641492, 242640128, 242653966, 242642247, 242639834, 242764764, 242641720, 242644750, 242644134, 242719384, 242642279,
                    242651421, 242654357, 242642293, 242731581, 242710793, 242643875, 242643074, 242640034, 242645412, 242698593, 242642583, 242641214, 242661919, 242640784, 242642956, 242670960, 242667842,
                    242642938, 242642508, 242640468, 242691235, 242640769, 242650762, 242642717, 242642069, 242703293, 242640795, 242641947, 242643881, 242645486, 242640996, 242640467, 242641156, 242640516,
                    242656257, 242640367, 242640428, 242642592, 242641405, 242651836, 242657820, 242640490, 242648797, 242646342, 242657828, 242646719, 242664372, 242682352, 242685743, 242713539, 242642606,
                    242641557, 242662118, 242651532, 242688073, 242640534, 242640255, 242651769, 242674749, 242640124, 242662869, 242644432, 242640981, 242641930, 242643374, 242689143, 242670948, 242643350,
                    242684225, 242651257, 242639826, 242660979, 238865592};
            //Pass 1 figure out the cols and values sizes
//            long entryCount = 0;
//            int[] counts = new int[numPoints];
//
//            while (currentRead < totalLengthindex) {
//                outbyteBufferindex.clear();
//
//                rbSizeIn = (blockSize * 2 > (totalLengthindex - currentRead)) ?
//                        (totalLengthindex - currentRead) : blockSize * 2;
//
//                //if the size is smaller create two new smaller buffs
//                if (rbSizeIn != outbyteBufferindex.capacity()) {
//                    System.out.println("#### Using new ByteBuffer");
//                    outbyteBufferindex = ByteBuffer.allocate((int) rbSizeIn);
//                    outbyteBufferindex.order(endianness);
//                    outbyteBufferindex.clear();
//                }
//                fcIndex.read(outbyteBufferindex, currentRead);
//                outbyteBufferindex.flip();
//
//                while (outbyteBufferindex.hasRemaining()) {
//                    int row = outbyteBufferindex.getInt();
//                    int col = outbyteBufferindex.getInt();
//                    counts[row]++;
//                    entryCount++;
//                    if (row != col) {
//                        entryCount++;
//                        counts[col]++;
//                    }
//                }
//
//                currentRead += rbSizeIn;
//
//            }
//
//            int[] rows = new int[225];
//            long perProc = entryCount / 224;
//            System.out.println("entry Count " + entryCount + " : " + perProc);
//
//            int index = 0;
//            for (int i = 1; i < rows.length; i++) {
//                long temp = 0;
//                while (index < numPoints && temp < perProc) {
//                    temp += counts[index++];
//                }
//                rows[i] = index;
//            }
//            rows[224] = numPoints;

            long countsPerCur = perRank_sizes[rank];
//            for (int i = rows[rank]; i < rows[rank + 1]; i++) {
//                countsPerCur += counts[i];
//            }
            System.out.println(rank + " $$$$$$$$$ " + countsPerCur);
//            currentRead = 0;
//            rbSizeIn = rbSizeDa * 2; // Bacause we have two int |4*2| values
//            outbyteBufferindex =
//                    ByteBuffer.allocate((int) rbSizeIn);
//            outbyteBufferindex.order(endianness);

            short[] values = new short[(int) countsPerCur];
            int[] columns = new int[(int) countsPerCur];
            int entryIndex = 0;
            int[] rowPointer = new int[length];
            Arrays.fill(rowPointer, -1);
            int count = 0;
            long countflips = 0;
            //checks if the loop has already completed the row range
            boolean isDone = false;
            Map<Integer, List<int[]>> flipValues = new HashMap<>();
            int previousLocalRow = 0;
            int row, col, value;
            int[] temp;
            int gcCount = 1;

            //Used to check if the first local row is not skipped
            boolean firstRow = true;
            outer:
            while (currentRead < totalLength) {
                gcCount++;
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

                    row = outbyteBufferindex.getInt();
                    col = outbyteBufferindex.getInt();
                    if (row > endRow && col > endRow) break outer;
                    value = outbyteBufferdata.getInt();
                    //add it for future ref
                    if (col >= startRow && col <= endRow) {
                        if (flipValues.containsKey(col)) {
                            temp = new int[]{row, value};
                            flipValues.get(col).add(temp);
                        } else {
                            temp = new int[]{row, value};
                            flipValues.put(col, new ArrayList<>());
                            flipValues.get(col).add(temp);
                        }
                        countflips++;
                        if (countflips % 49999999 == 0) {
                            System.out.println("%%%%%%%%% : " + countflips);
                        }
                    }
                    if (row >= startRow && row <= endRow) {
                        int localRow = row - startRow;
                        if (firstRow) {
                            if (localRow != 0) {
                                //check the flipped values to fill this in
                                System.out.println("^^^^^^^^^ got missing first row" + row);
                                int templocalRow = 0;
                                while (templocalRow < localRow) {
                                    if (flipValues.containsKey(templocalRow + startRow)) {
                                        List<int[]> tempList = flipValues.remove(templocalRow + startRow);
                                        for (int[] vals : tempList) {
                                            values[entryIndex] = (short) ((vals[1] * INV_INT_MAX) * Short.MAX_VALUE);
                                            columns[entryIndex] = vals[0];
                                            entryIndex++;
                                            if (rowPointer[templocalRow] == -1) {
                                                rowPointer[templocalRow] = count;
                                            }
                                            count++;
                                        }
                                        templocalRow++;
                                    }
                                }
                            }

                            firstRow = false;
                        }
                        //If we are jumping couple of rows check that they are
                        //in the flipValues and add them before moving on to the
                        //current row
                        while (localRow - previousLocalRow > 1) {
                            previousLocalRow++;
                            if (flipValues.containsKey(previousLocalRow + startRow)) {
                                List<int[]> tempList = flipValues.remove(previousLocalRow + startRow);
                                for (int[] vals : tempList) {
                                    values[entryIndex] = (short) ((vals[1] * INV_INT_MAX) * Short.MAX_VALUE);
                                    columns[entryIndex] = vals[0];
                                    entryIndex++;
                                    if (rowPointer[previousLocalRow] == -1) {
                                        rowPointer[previousLocalRow] = count;
                                    }
                                    count++;
                                }
                            }
                        }

                        //If there were previous values for this row we need to
                        //add the first since they have lower column values
                        if (flipValues.containsKey(row)) {
                            List<int[]> tempList = flipValues.remove(row);
                            for (int[] vals : tempList) {
                                values[entryIndex] = (short) ((vals[1] * INV_INT_MAX) * Short.MAX_VALUE);
                                columns[entryIndex] = vals[0];
                                entryIndex++;
                                if (rowPointer[localRow] == -1) {
                                    rowPointer[localRow] = count;
                                    previousLocalRow = localRow;
                                }
                                count++;
                            }
                        }
                        values[entryIndex] = (short) ((value * INV_INT_MAX) * Short.MAX_VALUE);
                        columns[entryIndex] = col;
                        entryIndex++;
                        if (rowPointer[localRow] == -1) {
                            rowPointer[localRow] = count;
                            previousLocalRow = localRow;
                        }
                        count++;
                        if (entryIndex % 49999999 == 0) {
                            System.out.println(startRow + " Too Large #########################");
                        }

                    }
                }

                currentRead += rbSizeDa;
                if (gcCount > 200 && gcCount % 100 == 0) System.gc();
            }

            //Check if there are any trailing elements that have not been filled
            while (previousLocalRow < rowPointer.length - 1) {
                previousLocalRow++;
                if (flipValues.containsKey(previousLocalRow + startRow)) {
                    List<int[]> tempList = flipValues.remove(previousLocalRow + startRow);
                    for (int[] vals : tempList) {
                        values[entryIndex] = (short) ((vals[1] * INV_INT_MAX) * Short.MAX_VALUE);
                        columns[entryIndex] = vals[0];
                        entryIndex++;
                        if (rowPointer[previousLocalRow] == -1) {
                            rowPointer[previousLocalRow] = count;
                        }
                        count++;
                    }
                }
            }
            fcData.close();
            fcIndex.close();
            System.gc();
            System.out.printf("%d,,%d,%d,%d\n", startRow, endRow, (endRow - startRow), count);

            SparseMatrix sparseMatrix =
                    new SparseMatrix(values, columns, rowPointer);
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
