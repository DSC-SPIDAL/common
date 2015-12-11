package edu.indiana.soic.spidal.common;

public class WeightsWrap {
    private final short[][] weights;
    private final short[][] distances;
    private double avgDist = 1.0;
    private final boolean isSammon;
    private final double[] simpleWeights;
    private final Range rowRange;

    public WeightsWrap(
        short[][] weights, short[][] distances, boolean isSammon) {
        this.weights = weights;
        this.distances = distances;
        this.isSammon = isSammon;
        this.simpleWeights = null;
        this.rowRange = null;
    }

    public WeightsWrap(double[] simpleWeights, Range rowRange, short[][] distances, boolean isSammon) {
        this.simpleWeights = simpleWeights;
        this.distances = distances;
        this.isSammon = isSammon;
        this.weights = null;
        this.rowRange = rowRange;
    }

    public double getWeight(int i, int j){
        double w = 0;
        if (weights != null) {
            w = weights[i][j] * 1.0 / Short.MAX_VALUE;
        } else if (simpleWeights != null) {
            w = simpleWeights[i + rowRange.getStartIndex()] * simpleWeights[j];
        } else {
            w = 1.0;
        }
        if (!isSammon) return w;
        double d = distances[i][j] * 1.0 / Short.MAX_VALUE;
        return w / Math.max(d, 0.001 * avgDist);
    }

    public void setAvgDistForSammon(double avgDist) {
        this.avgDist = avgDist;
    }
}
