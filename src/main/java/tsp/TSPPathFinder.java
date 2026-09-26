package tsp;

import model.LeafPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Uses nearest neighbour TSP approximation
public class TSPPathFinder {

    // Builds a path that visits all leaf points starting from a chosen point
    public List<LeafPoint> buildPath(List<LeafPoint> points, LeafPoint startPoint) {

        List<LeafPoint> path = new ArrayList<>();

        // If input is invalid, return empty path
        if (points == null || points.isEmpty() || startPoint == null) {
            return path;
        }

        // Store all points that have not been visited yet
        Set<LeafPoint> unvisited = new HashSet<>(points);

        // Start from the selected starting point
        LeafPoint current = startPoint;

        path.add(current);
        unvisited.remove(current);

        // Keep going until all points are visited
        while (!unvisited.isEmpty()) {

            LeafPoint nearest = null;
            double bestDistance = Double.MAX_VALUE;

            // Find the closest unvisited point
            for (LeafPoint point : unvisited) {

                double distance = distance(current, point);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearest = point;
                }
            }

            // Move to the nearest point
            current = nearest;

            path.add(current);
            unvisited.remove(current);
        }

        return path;
    }

    // Calculates distance between two points
    private double distance(LeafPoint a, LeafPoint b) {

        double dx = a.centerX - b.centerX;
        double dy = a.centerY - b.centerY;

        return Math.sqrt(dx * dx + dy * dy);
    }
}