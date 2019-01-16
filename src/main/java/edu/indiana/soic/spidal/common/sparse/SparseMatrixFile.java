package edu.indiana.soic.spidal.common.sparse;

import edu.indiana.soic.spidal.common.Range;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SparseMatrixFile {

    private static final double INV_SHORT_MAX = 1.0 / Short.MAX_VALUE;


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
        long blockSize = 1024*1024*200; // 200Mb, the index file will take 200*4
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
            long rbSizeIn = rbSizeDa*4; // Bacause we have two int |4*2| values
            // for each data value which is a short |2| value

            long currentRead = 0;
            ByteBuffer byteBufferIndex = ByteBuffer.allocate((int)rbSizeIn);
            ByteBuffer byteBufferData = ByteBuffer.allocate((int)rbSizeDa);
            if(endianness.equals(ByteOrder.BIG_ENDIAN)){
                byteBufferIndex.order(ByteOrder.BIG_ENDIAN);
                byteBufferData.order(ByteOrder.BIG_ENDIAN);
            }else{
                byteBufferIndex.order(ByteOrder.LITTLE_ENDIAN);
                byteBufferData.order(ByteOrder.LITTLE_ENDIAN);
            }

            List<Double> values = new ArrayList<>();
            List<Integer> columns = new ArrayList<>();
            int[] rowPointer = new int[length];
            Arrays.fill(rowPointer, -1);
            int count = 0;

            while(currentRead < totalLength){
                rbSizeDa = (blockSize > (totalLength - currentRead)) ?
                        (totalLength - currentRead) : blockSize;
                rbSizeIn = rbSizeDa*4;

                if(byteBufferData.capacity() != rbSizeDa){
                    byteBufferData = ByteBuffer.allocate((int)rbSizeDa);
                    byteBufferIndex = ByteBuffer.allocate((int)rbSizeIn);
                    if(endianness.equals(ByteOrder.BIG_ENDIAN)){
                        byteBufferIndex.order(ByteOrder.BIG_ENDIAN);
                        byteBufferData.order(ByteOrder.BIG_ENDIAN);
                    }else{
                        byteBufferIndex.order(ByteOrder.LITTLE_ENDIAN);
                        byteBufferData.order(ByteOrder.LITTLE_ENDIAN);
                    }
                }
                byteBufferData.clear();
                byteBufferIndex.clear();
                fcIndex.read(byteBufferIndex);
                fcData.read(byteBufferData);
                byteBufferIndex.flip();
                byteBufferData.flip();

                while (byteBufferIndex.hasRemaining() && byteBufferData.hasRemaining()) {
                    int i = byteBufferIndex.getInt();
                    int j = byteBufferIndex.getInt();
                    double value = byteBufferData.getShort() * INV_SHORT_MAX;
                    if (i >= startRow && i <= endRow) {
                        int localRow = i - startRow;
                        values.add(value);
                        columns.add(j);
                        if(rowPointer[localRow] == -1){
                            rowPointer[localRow] = count;
                        }
                        count++;
                    }
                }

                currentRead += rbSizeDa;
            }

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

    private static int bytesToInt(byte[] bytes){
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
