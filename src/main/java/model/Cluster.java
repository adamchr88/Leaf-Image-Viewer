package model;

public class Cluster {

    // The root id of this cluster - from Union-Find
    public int root;

    // Number of pixels in this cluster
    public int size;

    // Bounding box coordinates for drawing the rectangle around the cluster
    public int minX;
    public int maxX;
    public int minY;
    public int maxY;

    // Rank after sorting clusters by size
    // Largest cluster = 1, next largest = 2, etc..
    public int rank;

    public Cluster(int root) {

        // Store the Union-Find root that identifies this cluster
        this.root = root;

        // Cluster starts with size 0 and grows as pixels are added
        this.size = 0;

        // Start with extreme values so that the first pixel updates them correctly
        this.minX = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxY = Integer.MIN_VALUE;

        // Rank is assigned later after sorting
        this.rank = 0;
    }
}