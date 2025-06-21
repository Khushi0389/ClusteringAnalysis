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
import java.awt.Image;
import java.awt.Toolkit;

public class Lat_lon_plot_4 extends JFrame implements ActionListener {
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
    double eps_corr = 0.96, eps = 100, center_lat = 0.0, center_lon = 0.0, center_dep = 0.0, center_mag = 0.0;
    double initial_eps = eps;
    double core_dists[];
    /////////////////////////////////////////////////////
    data_map data_m[] = null;
    display_map dm1 = null;
    cluster_data[] clus = null, s_clus = null;
    /////////////////////////////////////////////////////
    JPanel jp1;
    JButton b1, b2, b3, b4, b5, b6, b7, b8;
    /////////////////////////////////////////////////////

    public Lat_lon_plot_4() {
        setTitle("Map");
        getContentPane().setLayout(new BorderLayout());
        dm1 = new display_map(width, height);
        dm1.setBackground(Color.BLACK);
        getContentPane().add(dm1, BorderLayout.CENTER);

        b1 = new JButton("Read");
        b2 = new JButton("Plot");
        b3 = new JButton("Cluster");
        b4 = new JButton(" Clusters");
        b5 = new JButton("Sub Cluster");
        b6 = new JButton("sub-Clusters");
        b7 = new JButton("Save");
        b8 = new JButton("Exit");

        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        b4.addActionListener(this);
        b5.addActionListener(this);
        b6.addActionListener(this);
        b7.addActionListener(this);
        b8.addActionListener(this);

        jp1 = new JPanel();

        jp1.add(b1);
        jp1.add(b2);
        jp1.add(b3);
        jp1.add(b4);
        jp1.add(b5);
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

            get_cores(eps);
            clustering();
            System.out.println("\nFinal cl_id = " + cl_id);
        }
        if (ae.getSource() == b4) {
            show_cluster_boundary();
            try {
                write_result();
            } catch (IOException ioe) {

            }
            System.out.println("Button 4");
            dm1.show_result(2, cl_id);
        }
        if (ae.getSource() == b5) {
            System.out.println("Button 5");

            // clustering_2(eps_corr);
            System.out.println("\nFinal s_cl_id = " + s_cl_id);
        }
        if (ae.getSource() == b6) {
            // show_sub_cluster_boundary();
            // System.out.println("Button 6");
            dm1.show_sub_result(3, s_cl_id);
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

    void show_cluster_boundary() {
        int count = 0;

        clus = new cluster_data[cl_id];

        for (int j = 0; j < cl_id; j++) {
            clus[j] = new cluster_data();
        }

        for (int j = 0; j < cl_id; j++) {
            count = 0;
            center_lat = 0.0;
            center_lon = 0.0;
            center_dep = 0.0;
            center_mag = 0.0;

            for (int i = 0; i < data_m.length; i++) {
                if (data_m[i].cl_id == j) {
                    count++;
                    center_lat = center_lat + data_m[i].nor_lat;
                    center_lon = center_lon + data_m[i].nor_lon;
                    center_dep = center_dep + data_m[i].nor_dep;
                    center_mag = center_mag + data_m[i].nor_mag;
                }
            }
            center_lat = center_lat / count;
            center_lon = center_lon / count;
            center_dep = center_dep / count;
            center_mag = center_mag / count;

            clus[j].id = j;
            clus[j].center_lat = center_lat;
            clus[j].center_lon = center_lon;
            clus[j].center_dep = center_dep;
            clus[j].center_mag = center_mag;
            clus[j].no_mem = count;

            clus[j].mems = new int[count];

            int x = 0;

            for (int i = 0; i < data_m.length; i++) {
                if (data_m[i].cl_id == j) {
                    clus[j].mems[x] = i;
                    x++;
                }
            }
        }

        for (int j = 0; j < cl_id; j++) {
            double farthest = -9999.99;
            last = -1;

            for (int i = 0; i < data_m.length; i++) {
                if (data_m[i].cl_id == j) {
                    double dis = Math.sqrt(Math.pow((data_m[i].nor_lon - clus[j].center_lon), 2)
                            + Math.pow((data_m[i].nor_lat - clus[j].center_lat), 2));

                    if (dis > farthest) {
                        farthest = dis;
                        last = i;
                    }
                }
            }
            clus[j].last_lat = (int) data_m[last].nor_lat;
            clus[j].last_lon = (int) data_m[last].nor_lon;
            clus[j].last_dep = (int) data_m[last].nor_dep;
            clus[j].radius_x = farthest * 2;
            clus[j].radius_y = farthest * 2;
        }
    }

    void clustering() {
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
        Lat_lon_plot_4 llp = new Lat_lon_plot_4();
        llp.setVisible(true);
        llp.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void read_file() {
        String filename = "eq_1.csv";
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

                stk1 = new StringTokenizer(str, ",");
                int counter = stk1.countTokens();
                // System.out.println("No.of tokens = " + counter);

                while (stk1.hasMoreTokens()) {
                    data_m[j].date = stk1.nextToken();
                    data_m[j].lat = Double.parseDouble(stk1.nextToken());
                    data_m[j].lon = Double.parseDouble(stk1.nextToken());
                    data_m[j].depth = Double.parseDouble(stk1.nextToken());
                    data_m[j].mag = Double.parseDouble(stk1.nextToken());
                    data_m[j].place = stk1.nextToken();
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

        core_dists = new double[data_m.length];
    }

    void write_result() throws IOException {
        String filename = "Result.txt";
        FileWriter fw = new FileWriter(filename, false);
        BufferedWriter bw = new BufferedWriter(fw);

        for (int j = 0; j < cl_id; j++) {
            bw.write("Cluster " + j);
            bw.newLine();

            for (int i = 0; i < data_m.length; i++) {
                if (data_m[i].cl_id == j) {
                    bw.write("" + i + " ");
                    bw.write(data_m[i].date + " " + data_m[i].place + " " + data_m[i].lat + " " + data_m[i].lon + " "
                            + data_m[i].depth + " " + data_m[i].mag);
                    bw.newLine();
                }
            }
        }
        bw.close();
    }

    class data_map {
        int id;
        String date = "", place = "";;
        double lat, lon, depth, mag;
        double nor_lat, nor_lon, nor_dep, nor_mag;
        int last;
        int core;
        double core_dist;
        int cl_id, s_cl_id;

        data_map() {
            date = "";
            place = "";
            core = -1;
            core_dist = -1;
            cl_id = -1;
            s_cl_id = -1;
        }
    }

    class cluster_data {
        int id = -1, no_mem, last_lat, last_lon, last_dep, last_mag;
        double center_lon, center_lat, center_dep, center_mag, radius_x, radius_y;
        int mems[];
    }

    class display_map extends Canvas {
        int w1 = 0, h1 = 0, cent_x = 0, cent_y = 0;

        int flag = 0, c_id = -1, s_c_id = -1;

        Color colr;

        protected Image bgImage;

        public display_map() {

        }

        public void bg(Image image) {
            bgImage = image;
        }

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

        public void show_sub_result(int f, int scid) {
            this.flag = f;
            this.s_c_id = scid;
            repaint();
        }

        public void paint(Graphics g) {
            g.drawImage(bgImage, 0, 0, this);

            if (flag == 1) {
                g.setColor(new Color(255, 0, 0));

                for (int i = 0; i < data_m.length; i++) {
                    int x = (int) data_m[i].nor_lon;
                    int y = (int) data_m[i].nor_lat;

                    g.fillOval(x, y, 3, 3);
                    // g.fillOval(x, y, (int)(data_m[i].depth / 60)+3, (int)(data_m[i].depth /
                    // 60)+3);
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
                        int x = (int) (data_m[i].nor_lon + 0.5);
                        int y = (int) (data_m[i].nor_lat + 0.5);

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
            if (flag == 3) {
                for (int j = 0; j < s_c_id; j++) {
                    Random rand = new Random();
                    float r = rand.nextFloat();
                    float gr = rand.nextFloat();
                    float b = rand.nextFloat();
                    float alpha = 1.0f;

                    colr = new Color(r, gr, b, alpha);

                    for (int i = 0; i < data_m.length; i++) {
                        int x = (int) (data_m[i].nor_lat + 0.5);
                        int y = (int) (data_m[i].nor_lon + 0.5);

                        if (j == data_m[i].s_cl_id) {
                            g.setColor(colr);
                            g.fillOval(x, y, 3, 3);
                        }
                    }

                    int count = 0;

                    for (int i = 0; i < data_m.length; i++) {
                        if (j == data_m[i].s_cl_id) {
                            count++;
                        }
                    }

                    s_clus[j].mems = new int[count];

                    int n = 0;

                    for (int i = 0; i < data_m.length; i++) {
                        if (data_m[i].s_cl_id == j) {
                            s_clus[j].mems[n] = i;
                            n++;
                        }
                    }

                    alpha = (rand.nextFloat() * (0.5f - 0.1f)) + 0.1f;
                    colr = new Color(r, gr, b, alpha);
                    g.setColor(colr);

                    g.fillOval((int) ((s_clus[j].center_lat - s_clus[j].radius_y / 2) + 0.5),
                            (int) ((s_clus[j].center_lon - s_clus[j].radius_x / 2) + 0.5),
                            (int) (s_clus[j].radius_y + 0.5), (int) (s_clus[j].radius_x + 0.5));
                    // g.setColor(Color.RED);
                    // g.drawRect((int)clus[j].center_lat, (int)clus[j].center_lon, 5, 5);

                    for (int i = 0; i < s_clus[j].mems.length - 1; i++) {
                        if (s_clus[j].mems[i] != -1)
                            g.drawLine((int) (data_m[s_clus[j].mems[i]].nor_lat + 0.5),
                                    (int) (data_m[s_clus[j].mems[i]].nor_lon + 0.5),
                                    (int) (data_m[s_clus[j].mems[i + 1]].nor_lat + 0.5),
                                    (int) (data_m[s_clus[j].mems[i + 1]].nor_lon + 0.5));
                    }

                    g.setColor(new Color(255, 255, 255));

                    for (int i = 0; i < data_m.length; i++) {
                        int x = (int) (data_m[i].nor_lat + 0.5);
                        int y = (int) (data_m[i].nor_lon + 0.5);

                        if (data_m[i].s_cl_id == -1) {
                            g.fillOval(x, y, 5, 5);
                        }
                    }
                }
            }
        }
    }
}