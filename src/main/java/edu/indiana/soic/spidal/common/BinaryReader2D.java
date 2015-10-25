package edu.indiana.soic.spidal.common;

import mpi.MPI;
import mpi.MPIException;

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
        boolean divideByShortMax, TransformationFunction function, int repetitions) {

        int trueGlobalRowCount, trueGlobalColCount;
        trueGlobalRowCount = trueGlobalColCount = globalColCount / repetitions;
        int shouldBeZero = globalColCount % repetitions;
        assert shouldBeZero == 0;

        int rowStartIdx = rows.getStartIndex();
        int rowEndIdx = rows.getEndIndex();

        int rowStartRepNumber = rowStartIdx / trueGlobalRowCount;
        int rowEndRepNumber = rowEndIdx / trueGlobalRowCount;

        short[][] rowBlock = new short[rows.getLength()][globalColCount];
        int trueRowStartIdx = rowStartIdx - (rowStartRepNumber * trueGlobalRowCount);
        int trueRowEndIdx = rowEndIdx - (rowEndRepNumber * trueGlobalRowCount);
        int rowStartOffset = 0;
        for (int i = rowStartRepNumber; i <= rowEndRepNumber; ++i){
            if (i != rowStartRepNumber) trueRowStartIdx = 0;
            if (i != rowEndRepNumber) trueRowEndIdx = trueGlobalRowCount-1;

            // TODO - remove after testing
            try {
                if (MPI.COMM_WORLD.getRank() == 1149){
                    System.out.println("++++++ rowStartIdx="+rows.getStartIndex() + " rowEndIdx=" + rows.getEndIndex() + " rowCount=" + rows.getLength() + " rowStartOffset=" + rowStartOffset + " trueRowStartIdx=" + trueRowStartIdx + " trueRowEndIdx=" + trueRowEndIdx + " repNumber=" + i);
                }
            }
            catch (MPIException e) {
                e.printStackTrace();
            }

            readRowRangeInternal(
                fname, new Range(trueRowStartIdx, trueRowEndIdx),
                trueGlobalColCount, endianness, divideByShortMax, function,
                rowBlock, rowStartOffset);
            rowStartOffset += ((trueRowEndIdx - trueRowStartIdx)+1);
        }

        for (int row = 0; row < rows.getLength(); ++row) {
            short[] tmp = rowBlock[row];
            for (int i = 1; i < repetitions; ++i) {
                System.arraycopy(tmp, 0, tmp, i*trueGlobalColCount,trueGlobalColCount);
            }
        }
        return rowBlock;
    }

    public static short[][] readRowRange(
        String fname, Range rows, int globalColCount, ByteOrder endianness,
        boolean divideByShortMax, TransformationFunction function) {

        short[][] rowBlock = new short[rows.getLength()][globalColCount];
        readRowRangeInternal(fname, rows, globalColCount, endianness, divideByShortMax, function, rowBlock, 0);
        return rowBlock;
    }



    public static void readRowRangeInternal(
        String fname, Range rows, int globalColCount, ByteOrder endianness,
        boolean divideByShortMax, TransformationFunction function, short[][] rowBlock, int rowStartOffset) {
        try (FileChannel fc = (FileChannel) Files.newByteChannel(Paths.get(
            fname), StandardOpenOption.READ)) {

            final long dataTypeSize = ((long)Short.BYTES);
            final int procRowStartIdx = rows.getStartIndex();
            final int procRowCount = rows.getLength();

            final long procLocalByteStartOffset = ((long)procRowStartIdx) * ((long)globalColCount) * dataTypeSize;
            final long procLocalByteExtent = ((long)procRowCount) * ((long)globalColCount) * dataTypeSize;

            MappedByteBuffer mappedByteBuffer;
            long remainingBytes = procLocalByteExtent;
            long bytesRead = 0L;
            double tmp;
            while (remainingBytes > 0){
                int chunkSizeInBytes = (int)(remainingBytes > Integer.MAX_VALUE ? Integer.MAX_VALUE : remainingBytes);
                mappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, procLocalByteStartOffset+bytesRead, chunkSizeInBytes);
                mappedByteBuffer.order(endianness);

                for (int i = 0; i <= chunkSizeInBytes-dataTypeSize;){
                    int procLocalRow = (int)(bytesRead / (dataTypeSize*globalColCount));
                    int globalCol = (int)((bytesRead % (dataTypeSize*globalColCount))/dataTypeSize);

                    tmp = mappedByteBuffer.getShort(i) * (divideByShortMax ? INV_SHORT_MAX : 1.0);
                    bytesRead+=((int)dataTypeSize);

                    // -1.0 indicates missing values
                    assert tmp == -1.0 || (tmp >= 0.0 && tmp <= 1.0);
                    if (function != null) {
                        tmp = function.transform(tmp);
                    }
                    try {
                        rowBlock[procLocalRow+rowStartOffset][globalCol] = (short)(tmp * Short.MAX_VALUE);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {

                        try {
                            System.out.println("***ERROR*** RANK=" + MPI.COMM_WORLD.getRank() + " totalRows=" + rowBlock.length + " rowStartOffset=" + rowStartOffset + " idx=" + (procLocalRow+rowStartOffset));
                        }
                        catch (MPIException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();


                    }

                    i += ((int)dataTypeSize);
                }
                remainingBytes -= chunkSizeInBytes;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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


