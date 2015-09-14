package edu.indiana.soic.spidal.common;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BinaryReader2D {
    public static short[][] readRowRange(String fname, Range rows, int globalColCount, ByteOrder endianness, boolean divideByShortMax, TransformationFunction function){
        try (FileChannel fc = (FileChannel) Files
                .newByteChannel(Paths.get(fname), StandardOpenOption.READ)) {
            int dataTypeSize = Short.BYTES;
            long pos = ((long) rows.getStartIndex()) * globalColCount *
                    dataTypeSize;
            MappedByteBuffer mappedBytes = fc.map(
                    FileChannel.MapMode.READ_ONLY, pos,
                    rows.getLength() * globalColCount * dataTypeSize);
            mappedBytes.order(endianness);

            int rowCount = rows.getLength();
            short[][] rowBlock = new short[rowCount][];
            double tmp;
            for (int i = 0; i < rowCount; ++i){
                short [] rowBlockRow = rowBlock[i] = new short[globalColCount];
                for (int j = 0; j < globalColCount; ++j){
                    int procLocalPnum =  i * globalColCount + j;
                    int bytePosition = procLocalPnum * dataTypeSize;
                    tmp = mappedBytes.getShort(bytePosition) * (divideByShortMax ? 1.0/Short.MAX_VALUE : 1.0);
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

    public static short[][] readRowRange(String fname, Range rows, int globalColCount, ByteOrder endianness, boolean divideByShortMax, double transform){
        try (FileChannel fc = (FileChannel) Files
            .newByteChannel(Paths.get(fname), StandardOpenOption.READ)) {
            int dataTypeSize = Short.BYTES;
            long pos = ((long) rows.getStartIndex()) * globalColCount *
                       dataTypeSize;
            MappedByteBuffer mappedBytes = fc.map(
                FileChannel.MapMode.READ_ONLY, pos,
                rows.getLength() * globalColCount * dataTypeSize);
            mappedBytes.order(endianness);

            int rowCount = rows.getLength();
            short[][] rowBlock = new short[rowCount][];
            double tmp;
            for (int i = 0; i < rowCount; ++i){
                short [] rowBlockRow = rowBlock[i] = new short[globalColCount];;
                for (int j = 0; j < globalColCount; ++j){
                    int procLocalPnum =  i * globalColCount + j;
                    int bytePosition = procLocalPnum * dataTypeSize;
                    tmp = mappedBytes.getShort(bytePosition) * (divideByShortMax ? 1.0/Short.MAX_VALUE : 1.0);
                    // -1.0 indicates missing values
                    assert tmp == -1.0 || (tmp >= 0.0 && tmp <= 1.0);
                    if (transform != 1.0 && tmp >= 0.0) {
                        tmp = Math.pow(tmp, transform);
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


