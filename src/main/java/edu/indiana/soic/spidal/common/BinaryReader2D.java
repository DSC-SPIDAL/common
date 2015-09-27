package edu.indiana.soic.spidal.common;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BinaryReader2D {
    private static final double INV_SHORT_MAX = 1.0 / Short.MAX_VALUE;

    public static short[][] readRowRange(
        String fname, Range rows, int globalColCount, ByteOrder endianness,
        boolean divideByShortMax, TransformationFunction function) {
        try (FileChannel fc = (FileChannel) Files.newByteChannel(Paths.get(
            fname), StandardOpenOption.READ)) {

            final long dataTypeSize = Short.BYTES;
            final int procRowStartIdx = rows.getStartIndex();
            final int procRowCount = rows.getLength();

            final long procLocalByteStartOffset = procRowStartIdx * globalColCount * dataTypeSize;
            final long procLocalByteExtent = procRowCount * globalColCount * dataTypeSize;

            MappedByteBuffer mappedByteBuffer;
            short[][] rowBlock = new short[procRowCount][globalColCount];
            long remainingBytes = procLocalByteExtent;
            long bytesRead = 0L;
            double tmp;
            while (remainingBytes > 0){
                int chunkSizeInBytes = (int)(remainingBytes > Integer.MAX_VALUE ? Integer.MAX_VALUE : remainingBytes);
                mappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, procLocalByteStartOffset+bytesRead, chunkSizeInBytes);
                mappedByteBuffer.order(endianness);

                for (int i = 0; i < chunkSizeInBytes;){
                    tmp = mappedByteBuffer.getShort(i) * (divideByShortMax ? INV_SHORT_MAX : 1.0);
                    bytesRead+=((int)dataTypeSize);
                    int procLocalRow = (int)(bytesRead / (dataTypeSize*globalColCount));
                    int globalCol = (int)(bytesRead % (dataTypeSize*globalColCount));

                    // -1.0 indicates missing values
                    assert tmp == -1.0 || (tmp >= 0.0 && tmp <= 1.0);
                    if (function != null) {
                        tmp = function.transform(tmp);
                    }
                    rowBlock[procLocalRow][globalCol] = (short)(tmp * Short.MAX_VALUE);

                    i += ((int)dataTypeSize);
                }
                remainingBytes -= chunkSizeInBytes;
            }
            return rowBlock;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static short[][] readRowRangeOld(String fname, Range rows, int globalColCount, ByteOrder endianness, boolean divideByShortMax, TransformationFunction function){
        try (FileChannel fc = (FileChannel) Files
                .newByteChannel(Paths.get(fname), StandardOpenOption.READ)) {
            int dataTypeSize = Short.BYTES;
            long pos = ((long) rows.getStartIndex()) * globalColCount *
                    dataTypeSize;
            final long extent = ((long)rows.getLength()) * globalColCount * dataTypeSize;
            int maps = (int)(extent / ((long)Integer.MAX_VALUE));
            if ((extent%((long)Integer.MAX_VALUE)) > 0) ++maps;
//            System.out.println(extent + " " + Integer.MAX_VALUE + " r=" + (extent%((long)Integer.MAX_VALUE)) + " q=" + (int)(extent / Integer.MAX_VALUE) + " " + maps);

            MappedByteBuffer[] mappedByteBuffers = new MappedByteBuffer[maps];
            for (int i = 0; i < maps-1; ++i){
                mappedByteBuffers[i] = fc.map(FileChannel.MapMode.READ_ONLY, pos+(((long)i)*Integer.MAX_VALUE), Integer.MAX_VALUE);
                mappedByteBuffers[i].order(endianness);
            }
            mappedByteBuffers[maps-1] = fc.map(FileChannel.MapMode.READ_ONLY, pos+(((long)(maps-1))*Integer.MAX_VALUE), extent-((maps-1)*((long)Integer.MAX_VALUE)));
            mappedByteBuffers[maps-1].order(endianness);

            int rowCount = rows.getLength();
            short[][] rowBlock = new short[rowCount][];
            double tmp;
            for (int i = 0; i < rowCount; ++i){
                short [] rowBlockRow = rowBlock[i] = new short[globalColCount];
                for (int j = 0; j < globalColCount; ++j){
                    long procLocalPnum =  ((long)i) * globalColCount + j;
                    long procLocalBytePosition = procLocalPnum * dataTypeSize;
                    int bufferIdx = (int)(procLocalBytePosition / Integer.MAX_VALUE);
                    int bufferLocalBytePosition = (int)(procLocalBytePosition - (((long)bufferIdx)*Integer.MAX_VALUE));
                    System.out.println("****" + bufferLocalBytePosition);
                    tmp = mappedByteBuffers[bufferIdx].getShort(bufferLocalBytePosition) * (divideByShortMax ? 1.0/Short.MAX_VALUE : 1.0);
                    // -1.0 indicates missing values
                    assert tmp == -1.0 || (tmp >= 0.0 && tmp <= 1.0);
                    if (function != null) {
                        tmp = function.transform(tmp);
                    }
                    rowBlockRow[j] = (short)(tmp * Short.MAX_VALUE);
                }
            }
            return rowBlock;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static short[][] readConstant(Range rows, int globalColCount, double w){
        assert w >= 0.0 && w <= 1.0;
        int rowCount = rows.getLength();
        short[][] weights = new short[rowCount][];
        for (int i = 0; i < rowCount; ++i){
            weights[i] = new short[globalColCount];
            for (int j = 0; j < globalColCount; ++j){
                weights[i][j] = (short)(w * Short.MAX_VALUE);
            }
        }
        return weights;
    }
}


