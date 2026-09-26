# LeafVision

LeafVision is a JavaFX application for detecting and analysing autumn leaves in images.

The application processes image pixels using colour thresholds, identifies connected groups of leaf pixels as clusters, ranks detected leaves by size and allows the user to visualise and interact with the results.

It also includes a pathfinding feature that creates and animates a route between detected leaves.

---

## Screenshot

![LeafVision Screenshot](screenshots/leafvision.png)

---

## Features

- Load images using a JavaFX interface
- Detect autumn leaf colours including yellow, orange, red and brown
- Convert images into a processed black-and-white view
- Group connected leaf pixels using Union-Find
- Detect and rank individual leaf clusters
- Draw bounding boxes around detected leaves
- Display the number of detected clusters
- Click individual clusters to view information
- Filter clusters by minimum and maximum size
- Display clusters using different colours
- Select a starting leaf for pathfinding
- Generate a route between detected leaves
- Animate the generated path through the image
- Adjust image-processing settings using sliders and checkboxes

---

## Tech Stack

- Java
- JavaFX
- FXML
- Image processing
- Union-Find / Disjoint Set data structure
- Travelling Salesman Problem approximation
- Object-Oriented Programming

---

## How It Works

### Image Processing

The application reads each pixel in the selected image and analyses its:

- Hue
- Saturation
- Brightness

The user can adjust threshold values and select which autumn colours should be detected.

Pixels that match the selected conditions are treated as leaf pixels, while the remaining pixels are treated as background.

### Cluster Detection

After processing the image, connected leaf pixels are grouped together using a Union-Find data structure.

Each detected cluster stores information including:

- Pixel count
- Bounding box coordinates
- Ranking by size

The detected clusters can then be displayed and interacted with through the JavaFX interface.

### Pathfinding

The centre point of each visible leaf cluster is used for pathfinding.

The application uses a nearest-neighbour Travelling Salesman Problem approximation to build a route between the detected leaves.

The route is then animated through the JavaFX interface.

---

## Project Structure

```text
src/
│
├── model/
│   ├── Cluster.java
│   └── LeafPoint.java
│
├── tsp/
│   └── TSPPathFinder.java
│
├── uf/
│   └── UnionFind.java
│
├── vision/
│   └── ImageProcessor.java
│
└── org/example/
    ├── LeafVisionApp.java
    └── MainController.java
