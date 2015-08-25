package edu.indiana.soic.spidal.common;

public class WeightsWrap {
    private final short[][] weights;
    private final short[][] distances;
    private double avgDist = 1.0;
    private final boolean isSammon;

    public WeightsWrap(
        short[][] weights, short[][] distances, boolean isSammon) {
        this.weights = weights;
        this.distances = distances;
        this.avgDist = avgDist;
        this.isSammon = isSammon;
    }

    public double getWeight(int i, int j){
        double w = weights == null ? 1.0 : weights[i][j] * 1.0 / Short.MAX_VALUE;
        if (!isSammon) return w;
        double d = distances[i][j] * 1.0 / Short.MAX_VALUE;
        return w / Math.max(d, 0.001 * avgDist);
    }

    public void setAvgDistForSammon(double avgDist) {
        this.avgDist = avgDist;
    }
}
