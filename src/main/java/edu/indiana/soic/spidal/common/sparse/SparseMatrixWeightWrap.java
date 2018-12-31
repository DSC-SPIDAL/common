package edu.indiana.soic.spidal.common.sparse;

public class SparseMatrixWeightWrap{

    private SparseMatrix weight;

    public SparseMatrix getWeight() {
        return weight;
    }

    public void setWeight(SparseMatrix weight) {
        this.weight = weight;
    }

    public SparseMatrix getDistance() {
        return distance;
    }

    public void setDistance(SparseMatrix distance) {
        this.distance = distance;
    }

    private SparseMatrix distance;
    private double avgDist = 1.0;
    private boolean isSammon;

    public SparseMatrixWeightWrap(SparseMatrix weight, SparseMatrix distance, boolean isSammon) {
        this.weight = weight;
        this.distance = distance;
        this.isSammon = isSammon;
    }

    public double getWeight(int columArrayIndex){
        if(weight == null) return 1.0;

        double w = weight.getValues()[columArrayIndex];

        if(!isSammon) return w;

        double d = distance.getValues()[columArrayIndex];
        return w / Math.max(d, 0.001 * avgDist);
    }

    public void setAvgDistForSammon(double avgDist) {
        this.avgDist = avgDist;
    }

}
