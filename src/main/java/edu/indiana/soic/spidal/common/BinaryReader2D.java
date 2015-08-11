package edu.indiana.soic.spidal.common;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BinaryReader2D {
    public static short[][] readRowRange(String fname, Range rows, int globalColCount, ByteOrder endianness){
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
            for (int i = 0; i < rowCount; ++i){
                rowBlock[i] = new short[globalColCount];
                short [] rowBlockRow = rowBlock[i];
                for (int j = 0; j < globalColCount; ++j){
                    int procLocalPnum =  i * globalColCount + j;
                    int bytePosition = procLocalPnum * dataTypeSize;
                    rowBlockRow[j] = mappedBytes.getShort(bytePosition);
                }
            }
           return rowBlock;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static short[][] readConstant(Range rows, int globalColCount, short w){

        int rowCount = rows.getLength();
        short[][] weights = new short[rowCount][];
        for (int i = 0; i < rowCount; ++i){
            weights[i] = new short[globalColCount];
            for (int j = 0; j < globalColCount; ++j){
                weights[i][j] = ((short)(w * Short.MAX_VALUE));
            }
        }
        return weights;
    }
}


