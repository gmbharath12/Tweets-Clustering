/**
 * 
 */
package com.assignment;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class KMeans {

	    //Number of Clusters
	    private int numberOfClusters = 2;
	    private static int numberPoints = 15;
	    private static int NumberCLUSTERS = 0;

	    private static ArrayList<DataPoint> dpoints = new ArrayList<DataPoint>(); // List of Data Points.
	    private static ArrayList<Centroid> centroids = new ArrayList<Centroid>(); // List of centroids

	    public static void main(String args[]) throws FileNotFoundException {

	        File ifile = new File(args[1]);
	        NumberCLUSTERS = Integer.parseInt(args[0]);
	        File ofile = new File(args[2]);
	        Scanner input = new Scanner(ifile);
	        int num_rows = 0;
	        String temp;
	        String[] s;
	        while (input.hasNextLine()) {
	            temp = input.nextLine();

	            if (temp.contains("id")) {

	            } else {
	                s = temp.split("\\s+");
	                DataPoint newPoint = new DataPoint(Integer.parseInt(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]));
	                dpoints.add(newPoint); 
	                num_rows++;
	            }
	        }
	        int total_data = num_rows - 1;
	        Integer min = 0;
	        List<Integer> l = new ArrayList<Integer>();
	        Random randomGenerator = new Random();
	        Centroid center = new Centroid(0.0, 0.0);
	        //choosing at random
	        for (int i = 0; i < NumberCLUSTERS; i++) {
	            if (i == 0) {
	                while (min == 0) {
	                    min = randomGenerator.nextInt(NumberCLUSTERS) + 1;
	                }
	            } else {
	                while (l.contains(min) || min == 0) {
	                    min = randomGenerator.nextInt((NumberCLUSTERS) + 1); // First one is chosen at random.
	                }
	            }
	            l.add(min);
	            center = new Centroid(dpoints.get(min).getX(), dpoints.get(min).getY());
	            System.out.println(i + ". " + "(" + center.getX() + "," + center.getY() + ")");

	            centroids.add(center); 
	        }

	        System.out.println("Cluster Size is : " + centroids.size());
	        for (int t = 0; t < centroids.size(); t++) {
	            System.out.println(t + ". " + "(" + centroids.get(t).getX() + "," + centroids.get(t).getY() + ")");
	        }
	        kmeansCluster();
	        
	    }

	    private static void kmeansCluster() {

	        final double bignumber = Math.pow(10, 10);
	        double minimum = bignumber;
	        double distance = 0.0;
	        int sampleNumber = 0;
	        int cluster = 0;
	        int i = 0;
	        boolean processing = true;
	        DataPoint newpoint = null;
	        ArrayList<DataPoint> datapoints = new ArrayList<DataPoint>();

	        System.out.println("Inside K Means function.");
	        while (i < dpoints.size()) {

	            newpoint = dpoints.get(i); // Pick the ith data point.
	            minimum = bignumber;
	            for (int j = 0; j < centroids.size(); j++) {
	                distance = eDistance(newpoint, centroids.get(j));
	                if (distance < minimum) {
	                    minimum = distance;
	                    cluster = j;
	                }
	            }
	            newpoint.setClusterNumber(cluster);
	            dpoints.get(i).setClusterNumber(cluster);
	            datapoints.add(newpoint);
	            for (int j = 0; j < centroids.size(); j++) {

	                double totalX = 0;
	                double totalY = 0;
	                int totalClusterPoints = 0;
	                for (int k = 0; k < datapoints.size(); k++) {
	                    if (datapoints.get(k).getClusterNumber() == j) {
	                        totalX += datapoints.get(k).getX();
	                        totalY += datapoints.get(k).getY();
	                        totalClusterPoints++;
	                    }
	                }
	                if (totalClusterPoints > 0) {
	                    /*Setting the value of centroid for the respective cluster*/
	                    centroids.get(j).setX(totalX / totalClusterPoints);
	                    centroids.get(j).setY(totalY / totalClusterPoints);
	                }

	            }
	            i++;

	        }

	        int samples = i;

	        processing = true;
	        while (processing) {
	            /*Calculating new centroids*/

	            for (int j = 0; j < centroids.size(); j++) {

	                double totalX = 0;
	                double totalY = 0;
	                int totalClusterPoints = 0;

	                // calculate new centroids.
	                for (int k = 0; k < dpoints.size(); k++) {
	                    if (dpoints.get(k).getClusterNumber() == j) {
	                        totalX += dpoints.get(k).getX();// add the X values.

	                        totalY += dpoints.get(k).getY();// add the Y values.
	                        totalClusterPoints++;// Count the total cluster dpoints.
	                    }
	                }

	                if (totalClusterPoints > 0) {
	                    System.out.println("cluster " + j);
	                    centroids.get(j).setX((double) (totalX / totalClusterPoints));// set the value to new centroid
	                    System.out.println("X:" + centroids.get(j).getX());
	                    centroids.get(j).setY((double) (totalY / totalClusterPoints));
	                    System.out.println("Y:" + centroids.get(j).getY());
	                    // set the value to new centroid Y value.
	                }
	            }

	            processing = false;

	            int m = 0;

	            for (int j = 0; j < dpoints.size(); j++) {

	                DataPoint tpoint = dpoints.get(j); // get the current data 
	                minimum = bignumber;
	                m = dpoints.get(j).getClusterNumber();

	                double newmin = eDistance(dpoints.get(j), centroids.get(m)); // distance to the centroid of the current cluster.
	                int newcluster = m;

	                System.out.println("Point " + j + " : (" + dpoints.get(j).getX() + "," + dpoints.get(j).getY() + ")");
	                for (int k = 0; k < NumberCLUSTERS; k++) {
	                    distance = eDistance(dpoints.get(j), centroids.get(k));
	                    if (distance < minimum) {
	                        minimum = distance;
	                        newcluster = k;
	                    }
	                }
	                dpoints.get(j).setClusterNumber(newcluster);
	                if (dpoints.get(j).getClusterNumber() != newcluster) {
	                    dpoints.get(j).setClusterNumber(newcluster);
	                    processing = true;
	                }
	            }

	        }

	        for (int k = 0; k < centroids.size(); k++) {
	            for (int j = 0; j < dpoints.size(); j++) {
	                if (dpoints.get(j).getClusterNumber() == k) {
	                    System.out.println(dpoints.get(j).getId());
	                }
	            }
	            System.out.println("\n");
	        }

	        System.out.println(" The SSE for this run is :" + calSSE(dpoints, centroids));

	        return;

	    }

	    private static double eDistance(DataPoint p, Centroid c) {
	        return Math.sqrt((Math.pow((p.getX() - c.mX), 2)) + (Math.pow((p.getY() - c.mY), 2)));
	    }

	    private static double calSSE(ArrayList<DataPoint> points, ArrayList<Centroid> clusters) {
	        double SSE = 0.0;
	        for (int k = 0; k < clusters.size(); k++) {
	            double dist;
	            double squaresum = 0.0;
	            for (int j = 0; j < points.size(); j++) {
	                if (points.get(j).getClusterNumber() == k) {
	                    dist = eDistance(points.get(j), clusters.get(k)); // The distance from centroid
	                    squaresum = squaresum + Math.pow(dist, 2);

	                }
	            }
	            SSE = SSE + squaresum;
	        }
	        return SSE;
	    }

	    private static class DataPoint {

	        private int id;
	        private double mX;
	        private double mY;
	        private int clusterNumber;

	        public DataPoint(int id, double x, double y) {
	            this.id = id;
	            this.mX = x;
	            this.mY = y;
	        }

	        public void setId(int i) {
	            this.id = i;
	        }

	        public int getId() {
	            return this.id;
	        }

	        public void setX(double x) {
	            this.mX = x;
	        }

	        public void setY(double y) {
	            this.mY = y;
	        }

	        public double getX() {
	            return this.mX;
	        }

	        public double getY() {
	            return this.mY;
	        }

	        public void setClusterNumber(int cluster) {
	            this.clusterNumber = cluster;
	        }

	        public int getClusterNumber() {
	            return this.clusterNumber;
	        }
	    }

	    private static class Centroid {

	        private double mX;
	        private double mY;
	        private int myCluster;

	        public Centroid(double x, double y) {
	            this.mX = x;
	            this.mY = y;
	            return;
	        }

	        public double getX() {
	            return this.mX;
	        }

	        public double getY() {
	            return this.mY;
	        }

	        public void setX(double x) {
	            this.mX = x;
	            return;
	        }

	        public void setY(double y) {
	            this.mY = y;
	            return;
	        }
	    }
	}
