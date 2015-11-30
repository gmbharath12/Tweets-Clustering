/**
 * 
 */
package com.assignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TwitterKMeans {

	public static float jaccardDistance(Set<String> a, Set<String> b) {

		if (a.size() == 0 || b.size() == 0) {
			return 0;
		}

		Set<String> unionXY = new HashSet<String>(a);
		unionXY.addAll(b);

		Set<String> intersectionXY = new HashSet<String>(a);
		intersectionXY.retainAll(b);
		float retValue = 1 - ((float) intersectionXY.size() / (float) unionXY
				.size());
		return retValue;

	}

	public static <K, V extends Comparable<? super V>> Map<K, V>
            sortByValue(Map<K, V> map) {
        Map<K, V> results = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> streamMap = map.entrySet().stream();

        streamMap.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e -> results.put(e.getKey(), e.getValue()));

        return results;
    }

	public static void main(String[] args) throws FileNotFoundException {

		int k = Integer.parseInt(args[0]);
		Scanner scanner = new Scanner(new File(args[1])).useDelimiter(",");
		JSONParser jsonParser = new JSONParser();
		Set<Cluster> clusterSet = new HashSet<Cluster>();
		HashMap<String, Tweet> tweetsMap = new HashMap();

		try {

			Object object = jsonParser.parse(new FileReader(args[2]));

			JSONArray jsonArray = (JSONArray) object;

			for (int i = 0; i < jsonArray.size(); i++) {

				Tweet twet = new Tweet();
				JSONObject jObj = (JSONObject) jsonArray.get(i);
				String text = jObj.get("text").toString();

				long sum = 0;
				for (int y = 0; y < text.toCharArray().length; y++) {

					sum += (int) text.toCharArray()[y];
				}

				// System.out.println(sum);
				String[] token = text.split(" ");
				String twetID = jObj.get("id").toString();

				Set<String> mySet = new HashSet<String>(Arrays.asList(token));
				twet.setAttributeValue(sum);
				twet.setText(mySet);
				twet.setTweetID(twetID);
				tweetsMap.put(twetID, twet);

			}

			int i = 0;
			while (scanner.hasNext()) {
				String id = scanner.next();
				Tweet t = tweetsMap.get(id.trim());
				clusterSet.add(new Cluster(i + 1, t, new LinkedList()));
				i++;
				if (i == k)
					break;
			}

			Iterator it = tweetsMap.entrySet().iterator();

			for (int l = 0; l < 25; l++) { 

				while (it.hasNext()) {
					Map.Entry me = (Map.Entry) it.next();

					Tweet p = (Tweet) me.getValue();
					HashMap<Cluster, Float> distMap = new HashMap();

					for (Cluster clust : clusterSet) {

						distMap.put(
								clust,
								jaccardDistance(p.getText(), clust
										.getCentroid().getText()));
					}

					HashMap<Cluster, Float> sorted = (HashMap<Cluster, Float>) sortByValue(distMap);

					sorted.keySet().iterator().next().getMembers().add(p);

				}

				for (Cluster clust : clusterSet) {

					TreeMap<String, Long> tDistMap = new TreeMap();

					Tweet newCentroid = null;
					Long avgSumDist = new Long(0);
					for (int j = 0; j < clust.getMembers().size(); j++) {

						avgSumDist += clust.getMembers().get(j)
								.getAttributeValue();
						tDistMap.put(clust.getMembers().get(j).getTweetID(),
								clust.getMembers().get(j).getAttributeValue());
					}
					if (clust.getMembers().size() != 0) {
						avgSumDist /= (clust.getMembers().size());
					}

					ArrayList<Long> listValues = new ArrayList<Long>(
							tDistMap.values());

					if (tDistMap.containsValue(findClosestNumber(listValues,
							avgSumDist))) {
						newCentroid = tweetsMap.get(getKeyByValue(tDistMap,
								findClosestNumber(listValues, avgSumDist)));
						clust.setCentroid(newCentroid);
					}

				}

			}
			// create an iterator
			// Iterator iterator = clusterSet.iterator();

			// check values

			// BufferedWriter out = new BufferedWriter(new
			// FileWriter("result.txt"));
			BufferedWriter out = new BufferedWriter(new FileWriter(args[3]));
			for (int itr = 1; itr <= 25; itr++) {
				Iterator iterator = clusterSet.iterator();
				while (iterator.hasNext()) {

					Cluster c = (Cluster) iterator.next();
					if (c.getId() == itr) {
						System.out.print(c.getId() + "\t");
						out.write("Cluster:" + itr + "\t");

						for (Tweet t : c.getMembers()) {
							// System.out.print(t.getTweetID()+ ",");
							out.write(t.getTweetID() + ",");
							System.out.println("\t" + t.getTweetID() + "\t"
									+ t.getText().toString());
						}

						System.out.println("");
						out.newLine();
					}
				}
			}
			System.out.println("");
			out.newLine();

			System.out.println("SumSquaredError: " + sumSquaredError(clusterSet));
			out.write("SumSquaredError : " + sumSquaredError(clusterSet));
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static double sumSquaredError(Set<Cluster> clusterSet) {

		double sse = 0;

		for (Cluster clust : clusterSet) {

			for (Tweet p : clust.getMembers()) {

				double dist = jaccardDistance(p.getText(), clust.getCentroid()
						.getText());
				sse += dist * dist;
			}

		}
		return sse;
	}

	public static Long findClosestNumber(List list, Long num) {
		if (list.size() > 0) { // Check list does not empty
			Long smaller = (Long) Collections.min(list); // get min number from
															// the list
			Long larger = (Long) Collections.max(list); // get max number from
														// the list

			for (int i = 0; i < list.size(); i++) { // Traverse list
				if (num == (Long) list.get(i)) // if find the passed number in
												// the list
				{
					return num; // than return num
				}
				if (num > (Long) list.get(i) && smaller < (Long) list.get(i)) // find
																				// nearest
																				// smaller
				{
					smaller = (Long) list.get(i);
				}
				if (num < (Long) list.get(i) && larger > (Long) list.get(i)) // find
																				// nearest
																				// larger
				{
					larger = (Long) list.get(i);
				}
			}
			return (num - smaller < larger - num ? smaller : larger); // return
																		// closest
																		// number
		}
		return new Long(0);
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	private static class Tweet {

		private long attributeValue;

		public long getAttributeValue() {
			return attributeValue;
		}

		public void setAttributeValue(long attributeValue) {
			this.attributeValue = attributeValue;
		}

		private Set text;
		private String tweetID;

		public Set getText() {
			return text;
		}

		public void setText(Set text) {
			this.text = text;
		}

		public String getTweetID() {
			return tweetID;
		}

		public void setTweetID(String tweetID) {
			this.tweetID = tweetID;
		}

	}

	private static class Cluster {

		private int id;
		private Tweet centroid;
		private List<Tweet> members;

		public Cluster(int id, Tweet centroid, List<Tweet> members) {
			this.id = id;
			this.centroid = centroid;
			this.members = members;
		}

		public int getId() {
			return id;
		}

		public void addPoint(Tweet tweet) {
			members.add(tweet);
		}

		public void setId(int id) {
			this.id = id;
		}

		public Tweet getCentroid() {
			return centroid;
		}

		public void setCentroid(Tweet centroid) {
			this.centroid = centroid;
		}

		public List<Tweet> getMembers() {
			return members;
		}

		public void setMembers(List<Tweet> members) {
			this.members = members;
		}

		@Override
		public String toString() {
			// return "Cluster{" + "centroid=" + centroid + ", members=" +
			// members + '}';

			String ret = "[Cluster: " + id + "]" + "[Centroid: " + centroid
					+ "]" + "[Tweetids: \n";

			for (Tweet t : members) {
				ret = ret + t.getTweetID();
			}
			ret = ret + "]";

			return ret;
		}

	}

}
