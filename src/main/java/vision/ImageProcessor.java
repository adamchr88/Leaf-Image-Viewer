package vision;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import model.Cluster;
import uf.UnionFind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ImageProcessor {

    // Stores whether each pixel is considered a leaf pixel or not
    private boolean[] whitePixels;

    // Stores which root/set each pixel belongs to after union-find
    private int[] pixelRoots;

    // Width and height of the current image
    private int width;
    private int height;

    // Converts the original image into a black and white image
    // White means leaf pixel, black means background
    public WritableImage convertToBlackWhite(Image image,
                                             double hueMin,
                                             double hueMax,
                                             double satMin,
                                             double brightMin,
                                             boolean detectYellow,
                                             boolean detectOrange,
                                             boolean detectRed,
                                             boolean detectBrown) {

        // Store image size
        width = (int) image.getWidth();
        height = (int) image.getHeight();

        // Create arrays with one entry per pixel
        whitePixels = new boolean[width * height];
        pixelRoots = new int[width * height];

        PixelReader reader = image.getPixelReader();
        WritableImage bwImage = new WritableImage(width, height);
        PixelWriter writer = bwImage.getPixelWriter();

        // Go through every pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // Read the pixel colour
                Color color = reader.getColor(x, y);

                // Extract hue, saturation and brightness
                double hue = color.getHue();
                double saturation = color.getSaturation();
                double brightness = color.getBrightness();

                // General threshold chosen by the user
                boolean matchesGeneralThreshold =
                        hue >= hueMin && hue <= hueMax &&
                                saturation >= satMin &&
                                brightness >= brightMin;

                // Check if the colour matches one of the chosen autumn leaf colours
                boolean matchesSelectedLeafColour =
                        matchesSelectedLeafColour(
                                hue, saturation, brightness,
                                detectYellow, detectOrange, detectRed, detectBrown
                        );

                // Pixel is a leaf only if it passes both tests
                boolean isLeaf = matchesGeneralThreshold && matchesSelectedLeafColour;

                // Convert 2D position into 1D index
                int index = y * width + x;

                // Store whether this is a leaf pixel
                whitePixels[index] = isLeaf;

                // Root not known yet, so set to -1 for now
                pixelRoots[index] = -1;

                // Write the output black/white pixel
                if (isLeaf) {
                    writer.setColor(x, y, Color.WHITE);
                } else {
                    writer.setColor(x, y, Color.BLACK);
                }
            }
        }

        return bwImage;
    }

    // Checks whether a pixel matches one of the autumn leaf colours selected by the user
    private boolean matchesSelectedLeafColour(double hue,
                                              double saturation,
                                              double brightness,
                                              boolean detectYellow,
                                              boolean detectOrange,
                                              boolean detectRed,
                                              boolean detectBrown) {

        boolean yellow =
                detectYellow &&
                        hue >= 35 && hue <= 70 &&
                        saturation >= 0.15 &&
                        brightness >= 0.20;

        boolean orange =
                detectOrange &&
                        hue >= 15 && hue < 35 &&
                        saturation >= 0.15 &&
                        brightness >= 0.20;

        boolean red =
                detectRed &&
                        ((hue >= 0 && hue <= 15) || (hue >= 345 && hue <= 360)) &&
                        saturation >= 0.15 &&
                        brightness >= 0.15;

        boolean brown =
                detectBrown &&
                        hue >= 10 && hue <= 35 &&
                        saturation >= 0.10 &&
                        brightness >= 0.10 &&
                        brightness <= 0.65;

        // Return true if any selected colour range matches
        return yellow || orange || red || brown;
    }

    // Uses union-find to group connected white pixels into clusters
    public List<Cluster> findClusters() {

        // One disjoint set element per pixel
        int totalPixels = width * height;
        UnionFind uf = new UnionFind(totalPixels);

        // First pass: join neighbouring white pixels into the same set
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int index = y * width + x;

                // Ignore black/background pixels
                if (!whitePixels[index]) {
                    continue;
                }

                // Check right neighbour
                if (x + 1 < width) {
                    int rightIndex = y * width + (x + 1);
                    if (whitePixels[rightIndex]) {
                        uf.union(index, rightIndex);
                    }
                }

                // Check down neighbour
                if (y + 1 < height) {
                    int downIndex = (y + 1) * width + x;
                    if (whitePixels[downIndex]) {
                        uf.union(index, downIndex);
                    }
                }
            }
        }

        // Map each root to a Cluster object
        Map<Integer, Cluster> clusterMap = new HashMap<>();

        // Second pass: build cluster objects from the disjoint sets
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int index = y * width + x;

                if (!whitePixels[index]) {
                    continue;
                }

                // Find the root of this pixel's set
                int root = uf.find(index);

                // Store root for later use
                pixelRoots[index] = root;

                // Create cluster if not already created
                clusterMap.putIfAbsent(root, new Cluster(root));
                Cluster cluster = clusterMap.get(root);

                // Increase cluster size
                cluster.size++;

                // Update cluster bounding box
                cluster.minX = Math.min(cluster.minX, x);
                cluster.maxX = Math.max(cluster.maxX, x);
                cluster.minY = Math.min(cluster.minY, y);
                cluster.maxY = Math.max(cluster.maxY, y);
            }
        }

        // Return all clusters as a list
        return new ArrayList<>(clusterMap.values());
    }

    // Creates a black and white image where one selected cluster is highlighted in orange
    public WritableImage createSingleClusterImage(int targetRoot) {

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int index = y * width + x;

                if (!whitePixels[index]) {
                    writer.setColor(x, y, Color.BLACK);
                } else if (pixelRoots[index] == targetRoot) {
                    writer.setColor(x, y, Color.ORANGE);
                } else {
                    writer.setColor(x, y, Color.WHITE);
                }
            }
        }

        return image;
    }

    // Creates a version of the image where different clusters are coloured randomly
    public WritableImage createRandomClusterImage(int minClusterSize, int maxClusterSize) {

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        // Count size of each cluster
        Map<Integer, Integer> clusterSizes = new HashMap<>();

        for (int i = 0; i < pixelRoots.length; i++) {
            if (pixelRoots[i] != -1) {
                clusterSizes.put(pixelRoots[i], clusterSizes.getOrDefault(pixelRoots[i], 0) + 1);
            }
        }

        // Assign a random colour to each valid cluster
        Map<Integer, Color> rootColours = new HashMap<>();
        Random random = new Random();

        for (Map.Entry<Integer, Integer> entry : clusterSizes.entrySet()) {
            int root = entry.getKey();
            int size = entry.getValue();

            if (size >= minClusterSize && size <= maxClusterSize) {
                rootColours.put(root, Color.color(
                        random.nextDouble(),
                        random.nextDouble(),
                        random.nextDouble()
                ));
            }
        }

        // Draw coloured clusters
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int index = y * width + x;

                if (!whitePixels[index]) {
                    writer.setColor(x, y, Color.BLACK);
                } else {
                    int root = pixelRoots[index];
                    Color c = rootColours.get(root);

                    if (c != null) {
                        writer.setColor(x, y, c);
                    } else {
                        writer.setColor(x, y, Color.WHITE);
                    }
                }
            }
        }

        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}