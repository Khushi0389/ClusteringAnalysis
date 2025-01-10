import java.io.*;
import java.awt.*;
import java.util.*;
import java.lang.*;
import java.text.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;

public class DF13 extends JFrame implements ActionListener {
    String str = "";
    FileReader fr = null;
    BufferedReader br = null;
    BufferedImage image = null;
    LineNumberReader count = null;
    StringTokenizer stk1 = null;
    DecimalFormat df = null;
    /////////////////////////////////////////////////////
    int width = 720, height = 720;
    int index = 1, img_index = 0;
    int cl_id = 0, s_cl_id = 0, min_pts = 6, last = -1, cnt = 0, pre_cnt = 0;
    int iter = 1;
    double eps_corr = 0.96, eps = 18, center_lat = 0.0, center_lon = 0.0, center_dep = 0.0, center_mag = 0.0;
    double initial_eps = eps;

    /////////////////////////////////////////////////////
    data_map data_m[] = null;
    display_map dm1 = null;
    cluster_data[] clus = null, s_clus = null;

    /////////////////////////////////////////////////////
    JPanel jp1;
    JButton b1, b2, b3, b4, b6, b7, b8;
    /////////////////////////////////////////////////////

    public DF13() {
        setTitle("Map");
        getContentPane().setLayout(new BorderLayout());
        dm1 = new display_map(width, height);
        dm1.setBackground(Color.BLACK);
        getContentPane().add(dm1, BorderLayout.CENTER);

        b1 = new JButton("Read");
        b2 = new JButton("Plot");
        b3 = new JButton("DBSCAN");
        b4 = new JButton("K-MEANS");
        b6 = new JButton("NEW-ALGO");
        b7 = new JButton("Save");
        b8 = new JButton("Exit");
        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        b4.addActionListener(this);
        b6.addActionListener(this);
        b7.addActionListener(this);
        b8.addActionListener(this);

        jp1 = new JPanel();

        jp1.add(b1);
        jp1.add(b2);
        jp1.add(b3);
        jp1.add(b4);
        jp1.add(b6);
        jp1.add(b7);
        jp1.add(b8);

        getContentPane().add(jp1, BorderLayout.SOUTH);

        setSize(width, height);
        setLocation(600, 10);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == b1) {
            System.out.println("Button 1");
            read_file();
        }
        if (ae.getSource() == b2) {
            System.out.println("Button 2");
            dm1.show_grids(1);
        }
        if (ae.getSource() == b3) {
            System.out.println("Button 3");
            performDBSCAN();
            dm1.show_result(2, cl_id);

        }
        if (ae.getSource() == b4) {
            System.out.println("Button 3");
            performKMeans();
            dm1.show_result(2, cl_id);

        }
        if (ae.getSource() == b6) {
            System.out.println("Button 5");
            Lat_lon_plot_4 llp = new Lat_lon_plot_4();
            llp.setVisible(true);
            llp.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        if (ae.getSource() == b7) {
            String res = "Result_";
            System.out.println("Button 7");

            image = new BufferedImage(dm1.getWidth(), dm1.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            dm1.paint(g);
            try {
                ImageIO.write(image, "png", new File(res + img_index + ".png"));
                img_index++;
            } catch (IOException ex) {

            }
        }
        if (ae.getSource() == b8) {
            System.out.println("Button 8");
            System.exit(0);
        }
    }

    public void normalize() {
        double lowest_lat = 99999.9, lowest_lon = 99999.9, lowest_dep = 99999.9, lowest_mag = 99999.9;
        double highest_lat = -99999.9, highest_lon = -99999.9, highest_dep = -99999.9, highest_mag = -99999.9;

        for (int i = 0; i < data_m.length; i++) {
            if (lowest_lat > data_m[i].lat) {
                lowest_lat = data_m[i].lat;
            }
            if (lowest_lon > data_m[i].lon) {
                lowest_lon = data_m[i].lon;
            }
            if (lowest_dep > data_m[i].depth) {
                lowest_dep = data_m[i].depth;
            }
            if (lowest_mag > data_m[i].mag) {
                lowest_mag = data_m[i].mag;
            }
            if (highest_lat < data_m[i].lat) {
                highest_lat = data_m[i].lat;
            }
            if (highest_lon < data_m[i].lon) {
                highest_lon = data_m[i].lon;
            }
            if (highest_dep < data_m[i].depth) {
                highest_dep = data_m[i].depth;
            }
            if (highest_mag < data_m[i].mag) {
                highest_mag = data_m[i].mag;
            }
        }

        System.out.println("Lowest latitude =  " + lowest_lat);
        System.out.println("Highest latitude =  " + highest_lat);
        System.out.println("Lowest longitude = " + lowest_lon);
        System.out.println("Highest longitude =  " + highest_lon);
        System.out.println("Lowest depth = " + lowest_dep);
        System.out.println("Highest depth =  " + highest_dep);
        System.out.println("Lowest magnitude = " + lowest_mag);
        System.out.println("Highest magnitude =  " + highest_mag);

        for (int i = 0; i < data_m.length; i++) {
            data_m[i].nor_lat = 60 + (((data_m[i].lat - lowest_lat) * (1 / (highest_lat - lowest_lat))) * 550);
            data_m[i].nor_lon = 60 + (((data_m[i].lon - lowest_lon) * (1 / (highest_lon - lowest_lon))) * 550);
            data_m[i].nor_dep = 60 + (((data_m[i].depth - lowest_dep) * (1 / (highest_dep - lowest_dep))) * 550);
            data_m[i].nor_mag = 60 + (((data_m[i].depth - lowest_mag) * (1 / (highest_dep - lowest_mag))) * 550);
        }

        for (int i = 0; i < data_m.length; i++) {
            DecimalFormat df = new DecimalFormat("#.#####");
            String lat_str = df.format(data_m[i].nor_lat);
            String lon_str = df.format(data_m[i].nor_lon);
            String dep_str = df.format(data_m[i].nor_dep);
            String mag_str = df.format(data_m[i].nor_mag);

            // System.out.println("Place " + i + " = " + lat_str + " " + lon_str + " " +
            // dep_str);
        }
    }

    public static void main(String args[]) {
        DF12 dg = new DF12();
        dg.setVisible(true);
        dg.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void read_file() {
        String filename = "earthquakes1970-2014.txt";
        try {
            fr = new FileReader(filename);

            count = new LineNumberReader(fr);

            while (count.skip(Long.MAX_VALUE) > 0) {
            }

            index = count.getLineNumber();

            System.out.println("total no. of lines  = " + index);
        } catch (IOException ioe) {
            System.out.println("File not found..." + ioe);
        }

        data_m = new data_map[index];

        try {
            fr = null;
            fr = new FileReader(filename);
            br = null;
            br = new BufferedReader(fr);

            for (int j = 0; j < index; j++) {
                data_m[j] = new data_map();
                data_m[j].id = j;

                str = br.readLine();

                stk1 = new StringTokenizer(str, "\t");
                int counter = stk1.countTokens();
                // System.out.println("No.of tokens = " + counter);

                while (stk1.hasMoreTokens()) {
                    data_m[j].date = stk1.nextToken();
                    data_m[j].lat = Double.parseDouble(stk1.nextToken());
                    data_m[j].lon = Double.parseDouble(stk1.nextToken());
                    data_m[j].depth = Double.parseDouble(stk1.nextToken());
                    data_m[j].mag = Double.parseDouble(stk1.nextToken());
                    data_m[j].last = -1;
                    data_m[j].cl_id = -1;
                    data_m[j].s_cl_id = -1;

                    // System.out.println(data_m[j].lat + " " + data_m[j].lon);
                }
            }
        } catch (IOException ioe) {
            System.out.println("File not found..." + ioe);
        }

        normalize();
    }

    class data_map {
        int id;
        String date = "";
        double lat, lon, depth, mag;
        double nor_lat, nor_lon, nor_dep, nor_mag;
        int last;
        int core;
        int core_count;
        double core_dist;
        int cl_id, s_cl_id;

        data_map() {
            core = -1;
            core_count = -1;
            cl_id = -1;
            core_dist = -1;
            s_cl_id = -1;
        }
    }

    class cluster_data {
        int num_mems; // Represents the number of members in the cluster
        int id = -1, no_mem, last_lat, last_lon, last_dep, last_mag;
        double center_lon, center_lat, center_dep, center_mag, radius_x, radius_y;
        int[] mems = new int[data_m.length];

        Color color; // Add a Color attribute
    }

    class display_map extends Canvas {
        int w1 = 0, h1 = 0, cent_x = 0, cent_y = 0;

        int flag = 0, c_id = -1, s_c_id = -1;

        Color colr;

        public display_map(int w, int h) {
            this.w1 = w;
            this.h1 = h;

            cent_x = w1 / 2;
            cent_y = h1 / 2;
            System.out.println("w = " + w1 + " h = " + h1 + " center x = " + cent_x + " cent_y = " + cent_y);
        }

        public void show_grids(int flg) {
            this.flag = flg;
            repaint();
        }

        public void show_result(int flg, int cid) {
            this.flag = flg;
            this.c_id = cid;
            repaint();
        }

        public void paint(Graphics g) {
            if (flag == 1) {
                g.setColor(new Color(255, 255, 255));

                for (int i = 0; i < data_m.length; i++) {
                    int x = (int) data_m[i].nor_lat;
                    int y = (int) data_m[i].nor_lon;

                    // g.fillOval(x, y, 3, 3);
                    g.fillOval(x, y, (int) (data_m[i].depth / 60) + 3, (int) (data_m[i].depth / 60) + 3);
                }
            }
            if (flag == 2) {
                for (int j = 0; j < c_id; j++) {
                    Random rand = new Random();
                    float r = rand.nextFloat();
                    float gr = rand.nextFloat();
                    float b = rand.nextFloat();
                    float alpha = 1.0f;

                    colr = new Color(r, gr, b, alpha);

                    for (int i = 0; i < data_m.length; i++) {
                        int x = (int) (data_m[i].nor_lat + 0.5);
                        int y = (int) (data_m[i].nor_lon + 0.5);

                        if (j == data_m[i].cl_id) {
                            g.setColor(colr);
                            g.fillOval(x, y, 3, 3);
                        }
                    }

                    int count = 0;

                    for (int i = 0; i < data_m.length; i++) {
                        if (j == data_m[i].cl_id) {
                            clus[j].mems[count] = i;
                            count++;
                        }
                    }

                    alpha = (rand.nextFloat() * (0.5f - 0.1f)) + 0.1f;
                    colr = new Color(r, gr, b, alpha);
                    g.setColor(colr);

                    g.fillOval((int) ((clus[j].center_lat - clus[j].radius_y / 2) + 0.5),
                            (int) ((clus[j].center_lon - clus[j].radius_x / 2) + 0.5), (int) (clus[j].radius_y + 0.5),
                            (int) (clus[j].radius_x + 0.5));
                    // g.setColor(Color.RED);
                    // g.drawRect((int)clus[j].center_lat, (int)clus[j].center_lon, 5, 5);

                    /*
                     * for(int i = 0; i < clus[j].mems.length-1; i++)
                     * {
                     * if(clus[j].mems[i] != -1)
                     * g.drawLine((int)(data_m[clus[j].mems[i]].nor_lat+0.5),
                     * (int)(data_m[clus[j].mems[i]].nor_lon+0.5),
                     * (int)(data_m[clus[j].mems[i+1]].nor_lat+0.5),
                     * (int)(data_m[clus[j].mems[i+1]].nor_lon+0.5)) ;
                     * }
                     */
                    g.setColor(new Color(255, 255, 255));

                    for (int i = 0; i < data_m.length; i++) {
                        int x = (int) (data_m[i].nor_lat + 0.5);
                        int y = (int) (data_m[i].nor_lon + 0.5);

                        if (data_m[i].cl_id == -1) {
                            g.fillOval(x, y, 5, 5);
                        }
                    }
                }
            }
        }
    }

    public void performDBSCAN() {
        eps = initial_eps;

        clus = new cluster_data[index];

        for (int i = 0; i < index; i++) {
            clus[i] = new cluster_data();
        }

        cl_id = 0;

        for (int i = 0; i < index; i++) {
            if (data_m[i].cl_id == -1) {
                if (expandCluster(data_m[i], cl_id)) {
                    cl_id++;
                }
            }
        }

        System.out.println("Total clusters formed: " + cl_id);
    }

    public boolean expandCluster(data_map d, int cl_id) {
        ArrayList<data_map> neighbors = regionQuery(d);

        if (neighbors.size() < min_pts) {
            d.cl_id = -1; // Noise Points
            return false;
        } else {
            clus[cl_id].id = cl_id;
            clus[cl_id].no_mem = 0;
            clus[cl_id].mems = new int[index];
            clus[cl_id].color = generateRandomColor(); // Assign random color to the cluster
            expandClusterCore(d, neighbors, cl_id);
            return true;
        }
    }

    public void expandClusterCore(data_map d, ArrayList<data_map> neighbors, int cl_id) {
        clus[cl_id].mems[clus[cl_id].no_mem] = d.id;
        clus[cl_id].no_mem++;
        d.cl_id = cl_id;

        for (int i = 0; i < neighbors.size(); i++) {
            data_map n = neighbors.get(i);
            if (n.cl_id == -1) {
                clus[cl_id].mems[clus[cl_id].no_mem] = n.id;
                clus[cl_id].no_mem++;
                n.cl_id = cl_id;
                ArrayList<data_map> nNeighbors = regionQuery(n);

                if (nNeighbors.size() >= min_pts) {
                    expandClusterCore(n, nNeighbors, cl_id);
                }
            }
        }
    }

    public ArrayList<data_map> regionQuery(data_map d) {
        ArrayList<data_map> neighbors = new ArrayList<>();

        for (int i = 0; i < index; i++) {
            double dist = Math.sqrt(Math.pow((d.nor_lat - data_m[i].nor_lat), 2)
                    + Math.pow((d.nor_lon - data_m[i].nor_lon), 2)
                    + Math.pow((d.nor_dep - data_m[i].nor_dep), 2)
                    + Math.pow((d.nor_mag - data_m[i].nor_mag), 2));

            if (dist <= eps) {
                neighbors.add(data_m[i]);
            }
        }

        return neighbors;
    }

    // Generate a random color
    public static Color generateRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b);
    }

    // Kmeans
    public void performKMeans() {
        int k = 5; // Specify the number of clusters (k value)

        // Initialize the cluster centers randomly
        cluster_data[] centers = initializeClusterCenters(k);

        // Perform K-Means iterations
        int maxIterations = 100; // Specify the maximum number of iterations
        int iterations = 0;

        while (iterations < maxIterations) {
            // Assign data points to the nearest cluster center
            assignDataPointsToClusters(centers);

            // Update the cluster centers
            updateClusterCenters(centers);

            iterations++;
        }

        // Show the results on the display_map
        // Calculate SSE
        double sse = calculateSSE(centers);

        // Show the results on the display_map
        dm1.show_result(2, k);

        System.out.println("Sum of Squared Errors (SSE): " + sse);
    }

    private cluster_data[] initializeClusterCenters(int k) {
        cluster_data[] centers = new cluster_data[k];

        Random rand = new Random();

        for (int i = 0; i < k; i++) {
            centers[i] = new cluster_data();
            centers[i].id = i;
            centers[i].no_mem = 0;
            centers[i].last_lat = -1;
            centers[i].last_lon = -1;
            centers[i].last_dep = -1;
            centers[i].last_mag = -1;
            centers[i].color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        }

        return centers;
    }

    private double calculateSSE(cluster_data[] centers) {
        double sse = 0.0;

        for (data_map point : data_m) {
            cluster_data center = centers[point.cl_id];

            double distance = calculateDistance(point, center);
            sse += distance * distance;
        }

        return sse;
    }

    private void assignDataPointsToClusters(cluster_data[] centers) {
        for (data_map point : data_m) {
            double minDistance = Double.MAX_VALUE;
            int nearestCenter = -1;

            for (int i = 0; i < centers.length; i++) {
                double distance = calculateDistance(point, centers[i]);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCenter = i;
                }
            }

            point.cl_id = nearestCenter;
        }
    }

    private double calculateDistance(data_map point, cluster_data center) {
        double latDiff = point.nor_lat - center.center_lat;
        double lonDiff = point.nor_lon - center.center_lon;
        double depDiff = point.nor_dep - center.center_dep;
        double magDiff = point.nor_mag - center.center_mag;

        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff + depDiff * depDiff + magDiff * magDiff);
    }

    private void updateClusterCenters(cluster_data[] centers) {
        for (cluster_data center : centers) {
            double sumLat = 0.0;
            double sumLon = 0.0;
            double sumDep = 0.0;
            double sumMag = 0.0;
            int count = 0;

            for (data_map point : data_m) {
                if (point.cl_id == center.id) {
                    sumLat += point.nor_lat;
                    sumLon += point.nor_lon;
                    sumDep += point.nor_dep;
                    sumMag += point.nor_mag;
                    count++;
                }
            }

            if (count > 0) {
                center.center_lat = sumLat / count;
                center.center_lon = sumLon / count;
                center.center_dep = sumDep / count;
                center.center_mag = sumMag / count;
            }
        }
    }

    // Helper method to generate random colors
    private Color getRandomColor() {
        Random random = new Random();
        float hue = random.nextFloat();
        float saturation = 0.9f;
        float brightness = 0.9f;
        return Color.getHSBColor(hue, saturation, brightness);
    }

    void NewAlgo() {
        int seed = -1;
        int cnt = 1;
        do {
            seed = find_seed();
            System.out.println("seed " + cnt + " = " + seed);
            if (seed != -1) {
                System.out.println("core dist " + seed + " = " + data_m[seed].core_dist);
                expand_cluster(seed, data_m[seed].core_dist);
                cnt++;
                cl_id++;
            }
        } while (seed != -1);
    }

    void expand_cluster(int seed, double dist_c) {
        if (data_m[seed].cl_id != -1) {
            return;
        }

        data_m[seed].cl_id = cl_id;

        for (int i = 0; i < data_m.length; i++) {
            if (data_m[i].cl_id == -1) {
                if (i != seed && find_dist(seed, i) <= dist_c) {
                    if (data_m[i].core == 1) {
                        // if(data_m[i].core_dist < eps)
                        expand_cluster(i, dist_c);
                    } else {
                        data_m[i].cl_id = cl_id;
                    }
                }
            }
        }
    }

    int find_seed() {
        int data = -1;
        double min_d = 999999.9;

        for (int i = 0; i < data_m.length; i++) {
            if (data_m[i].cl_id == -1 && data_m[i].core == 1 && data_m[i].core_dist > 0) {
                if (min_d > data_m[i].core_dist) {
                    min_d = data_m[i].core_dist;
                    data = i;
                }
            }
        }

        return data;
    }

    void get_cores(double i_eps) {
        double dist[];

        for (int i = 0; i < data_m.length; i++) {
            dist = new double[data_m.length];

            for (int j = 0; j < data_m.length; j++) {
                dist[j] = find_dist(i, j);
            }

            int count = 0;
            double d = -999;

            for (int j = 0; j < data_m.length; j++) {
                if (dist[j] <= i_eps) {
                    count++;
                    if (count >= min_pts) {
                        d = dist[j];
                        // System.out.println(dist[j]);
                        break;
                    }
                }
            }
            System.out.println(d);
            data_m[i].core_dist = d;
            data_m[i].core = 1;
        }
    }

    public double find_dist(int m, int n) {
        double dist;

        dist = Math.sqrt(Math.pow((data_m[m].nor_lon - data_m[n].nor_lon), 2)
                + Math.pow((data_m[m].nor_lat - data_m[n].nor_lat), 2));
        // System.out.println("Dist = " + dist);

        return dist;
    }

    private double calculateDistance1(data_map point1, data_map point2) {
        double latDiff = point1.nor_lat - point2.nor_lat;
        double lonDiff = point1.nor_lon - point2.nor_lon;
        double depDiff = point1.nor_dep - point2.nor_dep;
        double magDiff = point1.nor_mag - point2.nor_mag;

        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff + depDiff * depDiff + magDiff * magDiff);
    }

}