package org.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Cluster;
import model.LeafPoint;
import tsp.TSPPathFinder;
import vision.ImageProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    // Pane used to display the original image plus rectangles and path lines
    @FXML
    private Pane imagePane;

    // Shows the original image
    @FXML
    private ImageView originalImageView;

    // Shows the black and white or coloured processed image
    @FXML
    private ImageView bwImageView;

    // Menu option to show or hide the cluster numbers
    @FXML
    private CheckMenuItem showNumbersMenuItem;

    // Label showing how many visible clusters were found
    @FXML
    private Label clusterCountLabel;

    // Label showing information about a clicked cluster
    @FXML
    private Label clusterInfoLabel;

    // Labels for showing the current slider values
    @FXML
    private Label hueMinValueLabel;

    @FXML
    private Label hueMaxValueLabel;

    @FXML
    private Label satMinValueLabel;

    @FXML
    private Label brightMinValueLabel;

    @FXML
    private Label minClusterValueLabel;

    @FXML
    private Label maxClusterValueLabel;

    // Sliders for threshold and cluster size settings
    @FXML
    private Slider hueMinSlider;

    @FXML
    private Slider hueMaxSlider;

    @FXML
    private Slider satMinSlider;

    @FXML
    private Slider brightMinSlider;

    @FXML
    private Slider minClusterSlider;

    @FXML
    private Slider maxClusterSlider;

    // Checkboxes for autumn leaf colour categories
    @FXML
    private CheckBox yellowCheckBox;

    @FXML
    private CheckBox orangeCheckBox;

    @FXML
    private CheckBox redCheckBox;

    @FXML
    private CheckBox brownCheckBox;

    // Stores the image selected by the user
    private Image loadedImage;

    // Object that handles the image processing work
    private final ImageProcessor processor = new ImageProcessor();

    // Stores the current list of detected clusters
    private List<Cluster> currentClusters;

    // Stores rectangles by cluster root so they can be updated during animation
    private final Map<Integer, Rectangle> rectangleMap = new HashMap<>();

    // Stores visible clusters as center points for pathfinding
    private final List<LeafPoint> visibleLeafPoints = new ArrayList<>();

    // Stores the cluster the user selected as the path starting point
    private LeafPoint selectedStartPoint;

    @FXML
    public void initialize() {
        updateSettingLabels();
    }

    // Opens an image file and displays it
    @FXML
    public void handleOpenImage() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.jpg", "*.jpeg", "*.gif"
                )
        );

        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            loadedImage = new Image(file.toURI().toString());
            originalImageView.setImage(loadedImage);

            // Clear old drawings and show the new original image
            imagePane.getChildren().clear();
            imagePane.getChildren().add(originalImageView);

            // Reset processed image and state
            bwImageView.setImage(null);
            currentClusters = null;
            selectedStartPoint = null;

            clusterCountLabel.setText("Visible clusters found: 0");
            clusterInfoLabel.setText("Click a cluster to see its size");
        }
    }

    // Converts image to black/white, finds clusters, ranks them, and draws boxes
    @FXML
    public void handleConvert() {

        if (loadedImage == null) {
            return;
        }

        updateSettingLabels();

        // Convert image using current GUI settings
        WritableImage bw = processor.convertToBlackWhite(
                loadedImage,
                hueMinSlider.getValue(),
                hueMaxSlider.getValue(),
                satMinSlider.getValue(),
                brightMinSlider.getValue(),
                yellowCheckBox.isSelected(),
                orangeCheckBox.isSelected(),
                redCheckBox.isSelected(),
                brownCheckBox.isSelected()
        );

        bwImageView.setImage(bw);

        // Find clusters using union-find
        currentClusters = processor.findClusters();

        // Sort clusters largest first
        currentClusters.sort((a, b) -> b.size - a.size);

        // Assign rank numbers
        for (int i = 0; i < currentClusters.size(); i++) {
            currentClusters.get(i).rank = i + 1;
        }

        selectedStartPoint = null;
        drawClusterBoxes();
        clusterInfoLabel.setText("Click a cluster to see its size and choose a path start");
    }

    // Shows all valid clusters in random colours on the processed image
    @FXML
    public void handleShowRandomSets() {
        if (loadedImage == null || currentClusters == null) {
            return;
        }

        int minSize = getMinClusterSize();
        int maxSize = getMaxClusterSize();

        bwImageView.setImage(processor.createRandomClusterImage(minSize, maxSize));
    }

    // Re-runs the current conversion settings
    @FXML
    public void handleResetBlackWhite() {
        handleConvert();
    }

    // Show or hide cluster numbers
    @FXML
    public void handleToggleNumbers() {
        if (currentClusters != null) {
            drawClusterBoxes();
        }
    }

    // Animates a path between visible clusters using the TSP pathfinder
    @FXML
    public void handleAnimatePath() {

        if (visibleLeafPoints.isEmpty()) {
            clusterInfoLabel.setText("No visible clusters available for path animation");
            return;
        }

        if (selectedStartPoint == null) {
            clusterInfoLabel.setText("Select a start cluster first");
            return;
        }

        // Redraw the boxes first so animation starts from a clean state
        drawClusterBoxes();

        TSPPathFinder pathFinder = new TSPPathFinder();
        List<LeafPoint> path = pathFinder.buildPath(visibleLeafPoints, selectedStartPoint);

        if (path.size() < 2) {
            return;
        }

        Timeline timeline = new Timeline();

        // Divide the total animation time across the clusters
        double stepMillis = 5000.0 / path.size();

        for (int i = 0; i < path.size(); i++) {

            final int index = i;

            KeyFrame frame = new KeyFrame(Duration.millis(stepMillis * i), e -> {

                LeafPoint currentPoint = path.get(index);
                Rectangle currentRect = rectangleMap.get(currentPoint.cluster.root);

                // Highlight current cluster in yellow
                if (currentRect != null) {
                    currentRect.setStroke(Color.YELLOW);
                }

                // Draw line from previous cluster to current cluster
                if (index > 0) {
                    LeafPoint previousPoint = path.get(index - 1);

                    Line line = new Line(
                            previousPoint.centerX,
                            previousPoint.centerY,
                            currentPoint.centerX,
                            currentPoint.centerY
                    );
                    line.setStroke(Color.RED);
                    line.setStrokeWidth(2);

                    imagePane.getChildren().add(1, line);
                }

                // Return previous cluster back to blue
                if (index > 0) {
                    LeafPoint previousPoint = path.get(index - 1);
                    Rectangle previousRect = rectangleMap.get(previousPoint.cluster.root);

                    if (previousRect != null) {
                        previousRect.setStroke(Color.BLUE);
                    }
                }

                clusterInfoLabel.setText(
                        "Animating path - current cluster: " + currentPoint.cluster.rank
                );
            });

            timeline.getKeyFrames().add(frame);
        }

        // Final cleanup after animation finishes
        KeyFrame finalFrame = new KeyFrame(Duration.millis(5000), e -> {
            for (Rectangle rect : rectangleMap.values()) {
                rect.setStroke(Color.BLUE);
            }
            clusterInfoLabel.setText("Path animation complete");
        });

        timeline.getKeyFrames().add(finalFrame);
        timeline.play();
    }

    // Exits the application
    @FXML
    public void handleExit() {
        System.exit(0);
    }

    // Updates the numbers shown beside the sliders
    @FXML
    public void handleSettingsChanged() {
        updateSettingLabels();
    }

    // Draws rectangles and numbers around visible clusters
    private void drawClusterBoxes() {

        imagePane.getChildren().clear();
        imagePane.getChildren().add(originalImageView);

        rectangleMap.clear();
        visibleLeafPoints.clear();

        // Get displayed image size so cluster coordinates can be scaled to screen size
        double displayWidth = originalImageView.getBoundsInParent().getWidth();
        double displayHeight = originalImageView.getBoundsInParent().getHeight();

        double scaleX = displayWidth / processor.getWidth();
        double scaleY = displayHeight / processor.getHeight();

        boolean showNumbers = showNumbersMenuItem.isSelected();
        int minSize = getMinClusterSize();
        int maxSize = getMaxClusterSize();

        int visibleClusterCount = 0;

        for (Cluster cluster : currentClusters) {

            // Skip clusters outside the chosen size range
            if (cluster.size < minSize || cluster.size > maxSize) {
                continue;
            }

            visibleClusterCount++;

            // Scale bounding box from original image coordinates to displayed coordinates
            double x = cluster.minX * scaleX;
            double y = cluster.minY * scaleY;
            double width = (cluster.maxX - cluster.minX) * scaleX;
            double height = (cluster.maxY - cluster.minY) * scaleY;

            Rectangle rect = new Rectangle(x, y, width, height);
            rect.setStroke(Color.BLUE);
            rect.setFill(Color.TRANSPARENT);
            rect.setStrokeWidth(2);

            rectangleMap.put(cluster.root, rect);

            // Create center point for pathfinding
            double centerX = x + (width / 2.0);
            double centerY = y + (height / 2.0);

            LeafPoint point = new LeafPoint(cluster, centerX, centerY);
            visibleLeafPoints.add(point);

            // Clicking a rectangle highlights that cluster and selects it as the TSP start
            rect.setOnMouseClicked(e -> {
                WritableImage highlighted = processor.createSingleClusterImage(cluster.root);
                bwImageView.setImage(highlighted);

                selectedStartPoint = point;

                clusterInfoLabel.setText(
                        "Leaf/Cluster Number: " + cluster.rank +
                                "   |   Estimated Size (pixel units): " + cluster.size +
                                "   |   Selected as path start"
                );

                e.consume();
            });

            imagePane.getChildren().add(rect);

            // Optionally draw rank number
            if (showNumbers) {
                Text label = new Text(x + 4, y + 14, String.valueOf(cluster.rank));
                label.setFill(Color.BLUE);
                imagePane.getChildren().add(label);
            }
        }

        clusterCountLabel.setText("Visible clusters found: " + visibleClusterCount);
    }

    // Returns the minimum cluster size from the slider
    private int getMinClusterSize() {
        return (int) minClusterSlider.getValue();
    }

    // Returns the maximum cluster size from the slider
    private int getMaxClusterSize() {
        return (int) maxClusterSlider.getValue();
    }

    // Updates all setting labels to reflect current slider values
    private void updateSettingLabels() {
        hueMinValueLabel.setText(String.format("%.0f", hueMinSlider.getValue()));
        hueMaxValueLabel.setText(String.format("%.0f", hueMaxSlider.getValue()));
        satMinValueLabel.setText(String.format("%.2f", satMinSlider.getValue()));
        brightMinValueLabel.setText(String.format("%.2f", brightMinSlider.getValue()));
        minClusterValueLabel.setText(String.format("%d", (int) minClusterSlider.getValue()));
        maxClusterValueLabel.setText(String.format("%d", (int) maxClusterSlider.getValue()));
    }
}