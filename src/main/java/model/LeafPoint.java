package model;

public class LeafPoint {

    // Reference to the cluster this point represents
    public Cluster cluster;

    // X coordinate of the cluster's center (used for drawing and pathfinding)
    public double centerX;

    // Y coordinate of the cluster's center
    public double centerY;

    public LeafPoint(Cluster cluster, double centerX, double centerY) {

        // Store the cluster this point belongs to
        this.cluster = cluster;

        // Store the center position of the cluster
        this.centerX = centerX;
        this.centerY = centerY;
    }
}