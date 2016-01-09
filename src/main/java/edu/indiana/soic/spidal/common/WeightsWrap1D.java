package edu.indiana.soic.spidal.common;

public class WeightsWrap1D {
    private final short[] weights;
    private final short[] distances;
    private TransformationFunction function = null;
    private double avgDist = 1.0;
    private final boolean isSammon;
    private final double[] simpleWeights;
    private final Range rowRange;
    private int globalColCount = 0;

    private static final double INV_SHORT_MAX = 1.0/Short.MAX_VALUE;

    public WeightsWrap1D(
        short[] weights, short[] distances, boolean isSammon, int globalColCount) {
        this.weights = weights;
        this.distances = distances;
        this.isSammon = isSammon;
        this.simpleWeights = null;
        this.rowRange = null;
        this.globalColCount = globalColCount;
    }

    public WeightsWrap1D(double[] simpleWeights, Range rowRange, short[] distances, boolean isSammon, int globalColCount, TransformationFunction function) {
        this.simpleWeights = simpleWeights;
        this.distances = distances;
        this.isSammon = isSammon;
        this.weights = null;
        this.rowRange = rowRange;
        this.globalColCount = globalColCount;
        this.function = function;
    }

    public double getWeight(int i, int j){
        double w = 0;
        if (weights != null) {
            w = weights[i*globalColCount+j] * INV_SHORT_MAX;
        } else if (simpleWeights != null) {
            w = simpleWeights[i + rowRange.getStartIndex()] * simpleWeights[j];
            if (function != null){
                w = function.transform(w);
            }
        } else {
            w = 1.0;
        }
        if (!isSammon) return w;
        double d = distances[i*globalColCount+j] * INV_SHORT_MAX;
        return w / Math.max(d, 0.001 * avgDist);
    }

    public void setAvgDistForSammon(double avgDist) {
        this.avgDist = avgDist;
    }
}
