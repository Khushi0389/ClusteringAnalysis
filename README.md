# Clustering Analysis on Earthquake Data


This project analyzes the performance of clustering algorithms on numerical data, specifically earthquake data spanning from 1970 to 2014. The project implements and compares multiple clustering algorithms using Java and visualizes the results using a graphical user interface (GUI).

## **Features**
- Implementation of **K-Means**, **K-Medoids**, **DBSCAN**, and a custom algorithm.
- Visualization of clustering results using **Java Swing**.
- Handling of noise and outliers in earthquake data.
- Sub-clustering for finer granularity of clusters.

## **Technologies Used**
- **Programming Language**: Java
- **Visualization**: Java Swing
- **Development Environment**: Any Java-supported IDE (e.g., IntelliJ IDEA, Eclipse)

## **Dataset**
The dataset contains earthquake records from 1970 to 2014 with the following attributes:
- **Latitude**: Geospatial coordinate
- **Longitude**: Geospatial coordinate
- **Depth**: Depth of the earthquake in kilometers
- **Magnitude**: Intensity of the earthquake

### Data Preprocessing
- **Normalization**: Attributes like latitude, longitude, depth, and magnitude are normalized to fit within a uniform range.
- **Feature Selection**: Relevant attributes are selected for clustering.

## **Implemented Clustering Algorithms**
### **1. K-Means**
- A partition-based clustering algorithm that minimizes intra-cluster variance by iteratively updating cluster centroids.

### **2. K-Medoids**
- Similar to K-Means but uses actual data points (medoids) as cluster centers, making it more robust to outliers.

### **3. DBSCAN (Density-Based Spatial Clustering of Applications with Noise)**
- A density-based algorithm that identifies clusters of arbitrary shapes and effectively handles noise.

### **4. Custom Algorithm**
- A custom density-based clustering algorithm tailored for earthquake data, offering flexibility in handling noise and outliers.

## **How to Run the Project**
### **1. Prerequisites**
- Install **Java JDK 8** or higher.
- Use any Java IDE (e.g., IntelliJ IDEA, Eclipse).
- Download the dataset (`earthquakes1970-2014.txt` or `eq_1.csv`).

### **2. Steps**
1. Clone this repository:
   ```bash
   git clone https://github.com/Khushi0389/ClusteringAnalysis.git
   ```
2. Open the project in your Java IDE.
3. Compile and run `DF12.java` or `Lat_lon_plot_4.java`.
4. Use the GUI buttons to:
   - **Read**: Load the dataset.
   - **Plot**: Visualize raw data.
   - **Cluster**: Perform clustering using selected algorithms.
   - **Save**: Save visualization results as an image.

## **Visualization Features**
- **Cluster Representation**: Clusters are represented in distinct colors for easy interpretation.
- **Sub-Clustering**: Provides detailed clustering in dense earthquake zones.
- **Noise Handling**: Noise points are marked separately for clarity.

## **Key Insights**
- **DBSCAN** performed best for identifying irregular cluster shapes and handling noise.
- **K-Means** and **K-Medoids** worked well for spherical and evenly distributed clusters but were sensitive to parameter tuning.
- The custom algorithm effectively adapted to the earthquake dataset.

## **Strengths and Limitations**
### **Strengths**
- Handles noise and outliers robustly.
- Supports visualization for better interpretability.
- Implements multiple clustering methods for comparison.

### **Limitations**
- Sensitive to parameter selection (e.g., ε and MinPts for DBSCAN).
- Scalability issues for very large datasets.

## **Future Work**
- Optimize algorithms for larger datasets.
- Automate parameter tuning for DBSCAN.
- Extend the project to include real-time earthquake data streaming and clustering.

## **Acknowledgments**
- **Guide**: Dr. Sauravjyoti Sarmah, Assistant Professor, Jorhat Engineering College.

## **Contact**
For any queries, please contact Khushi Gupta at [khushigupta10857@example.com].

