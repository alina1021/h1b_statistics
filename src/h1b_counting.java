// package h1b_statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * @author Gianina Alina Negoita
 * 
 * Analyze past years immigration data trends on H1B(H-1B, H-1B1, E-3) visa application processing, 
 * specifically calculate two metrics: Top 10 Occupations and Top 10 States for certified visa applications.
 */
public class h1b_counting {

	public static void main(String[] args) throws FileNotFoundException {

		String inputFilePath = args[0];   // "h1b_input.csv";
		String outputFilePath1 = args[1]; // "top_10_occupations.txt";
		String outputFilePath2 = args[2]; // "top_10_states.txt";
		
		File file = new File(inputFilePath);

		// store occupation names and states where the work will take place for certified visa applications
		ArrayList<String> dataSoc_Name = new ArrayList<>();
		ArrayList<String> dataWorksite_State = new ArrayList<>();
		
		Scanner fileScanner = new Scanner(file);

		String delims = "[;]";
		String line = fileScanner.nextLine();
		String[] tokens = line.split(delims);

		int indexCaseStatus = 0;
		int indexWorksiteState = 0;
		int indexSocName = 0;
		
		// find indices for case status, occupation names and states where the work will take place
		for (int i=0 ; i < tokens.length; i++)
		{

			if (tokens[i].contains("STATUS")){ // CASE_STATUS
				indexCaseStatus = i;
			}

			if (tokens[i].contains("SOC_NAME")){
				indexSocName = i;
			}

			if (tokens[i].contains("WORKSITE_STATE") || tokens[i].contains("WORKLOC1_STATE")){
				indexWorksiteState = i;
			}

		}
		//System.out.println(tokens.length);
		//System.out.println(Arrays.toString(tokens));
		
		
		int count1 = 1;
		while (fileScanner.hasNext()){
			line = fileScanner.nextLine();		
			tokens = line.split(delims);
			count1++;
			//System.out.println(count1); // 134493
			//System.out.println(indexWorksiteState);
			//System.out.println(Arrays.toString(tokens));

			if (tokens[indexCaseStatus].equals("CERTIFIED")) {
				//System.out.println(tokens.length);
				//System.out.println(Arrays.toString(tokens));
				// trim a beginning and ending double quote (") from a string -- string.replaceAll("^\"|\"$", "")
				dataSoc_Name.add(tokens[indexSocName].replaceAll("^\"|\"$", ""));
				dataWorksite_State.add(tokens[indexWorksiteState]);
			}
			
		}
		fileScanner.close();

		/*
		 for (String s : dataSoc_Name) {
			System.out.println(s);
		}
		 */
		
		/*
		 for (String s : dataWorksite_State) {
			System.out.println(s);
		}
		 */

		List<String> content1 = sortingMethod(dataSoc_Name, "OCCUPATIONS");
		System.out.println();
		List<String> content2 = sortingMethod(dataWorksite_State, "STATES");

		useByfferedWriter(content1, outputFilePath1);
		useByfferedWriter(content2, outputFilePath2);

	}

	/**
	 * A helper method that sorts an ArrayList<String> array 
	 * The array must be sorted by NUMBER_CERTIFIED_APPLICATIONS field, and in case of a tie, alphabetically by TOP_STATES.
	 * @param array
	 * 	an ArrayList<String> array 
	 * @param string
	 * 	a String given to be used for generating the headline fields for file output
	 * @return
	 * 	a List<String> array which represents lines as content to be written to file
	 *  should not be more than 10 lines in each file 
	 */
	public static List<String> sortingMethod(ArrayList<String> array, String string) {
		// Frequency map -- create a map and count frequency with it
		// Use a Map<String, Integer> to store both the String as key and the frequency as value, with initial value of 1. 
		// If the String already exists, update the value by increasing it by 1.
		Map<String, Integer> map = new HashMap<String, Integer>();
		// array is dataSoc_Name or dataWorksite_State
		for (String s : array) {
			if (map.containsKey(s)) {
				map.put(s, map.get(s) + 1);
			} else {
				map.put(s, 1);
			}
		}

		// convert this map into a List<Tuple>
		// use the Integer values as keys and the String keys to store them as values.
		List<Tuple> al = new ArrayList<Tuple>();
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			al.add(new Tuple(entry.getValue(), entry.getKey()));
		}

		// sort using the criteria from compare() method
		//Collections.sort(al);
		Collections.sort(al, Tuple.freqComparator);

		// find total number of frequencies over all Tuples in list
		int totalCount = 0;
		//System.out.println(al);
		for (Tuple t : al) {
			totalCount+= t.getCount();
			//System.out.println(t);
		}
		
		//System.out.println(Integer.min(10, al.size()));
		// save content to be written to file as a List<String> of lines
		// should not be more than 10 lines in each file 
		List<String> content = new ArrayList<String>();
		content.add("TOP_" + string + ";NUMBER_CERTIFIED_APPLICATIONS;PERCENTAGE");
		System.out.println("TOP_" + string + ";NUMBER_CERTIFIED_APPLICATIONS;PERCENTAGE");
		double percentage = 0.0;
		for (int i = 0; i < Integer.min(10, al.size()); i++) {
			percentage = (double) al.get(i).getCount()/totalCount;
			content.add(al.get(i) + String.format(";%,.1f", percentage*100) + "%");
			//System.out.printf(al.get(i) + ";%.1f%% %n", percentage*100);
			System.out.println(al.get(i) + String.format(";%,.1f", percentage*100) + "%");
		}
		//System.out.println(Arrays.toString(content.toArray()));

		return content;
	}

	/**
	 * A helper method that writes a list of Strings to a file using BufferedWriter
	 */
	public static void useByfferedWriter(List<String> content, String filePath) {
		File file = new File(filePath);
		Writer fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			// Write the lines one by one
			for (String line : content) {
				//line += System.getProperty("line.separator");
				bufferedWriter.write(line);
                bufferedWriter.newLine();
			}
		} catch (IOException e) {
			System.err.println("Error writing the file : ");
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null && fileWriter != null) {
				try {
					bufferedWriter.close();
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * An inner class (Tuple class object) built as a <key, value> pair;
	 * key is the frequency of a word string in a list
	 * value is the word string from a list 
	 * 
	 */	 
	private static class Tuple  { // implements Comparable<Tuple>
		/**
		 * The frequency (number of times word appears in list)
		 */
		private int count;
		/**
		 * The string from the list
		 */
		private String word;

		/**
		 * Constructs a Tuple as a <key, value> pair having element as root
		 * @param count
		 * 		the frequency of word string in list
		 * @param word
		 * 		the word string in list
		 */
		public Tuple(int count, String word) {
			this.count = count;
			this.word = word;
		}

		/**
		 * A method that returns the frequency
		 * @return
		 * 	the frequency
		 */
		public int getCount() {         
			return count;     
		}   
		
		/**
		 * A method that returns the word string
		 * @return
		 * 	the word string
		 */
		public String getWord() {         
			return word;     
		} 

		/*
		@Override
		public int compareTo(Tuple o) {
			return new Integer(o.getCount()).compareTo(this.count);
		}
		 */

		/**
		 * Implement Comparator as anonymous inner class
		 */
		public static Comparator<Tuple> freqComparator = new Comparator<Tuple>() {     
			/**
			 * Implement the compare() method that will allow sorting Tuple objects by frequency in descending order.
			 * If frequency of two Tuple objects is the same, then sort by word string in ascending order.
			 */
			@Override         
			public int compare(Tuple t, Tuple t1) {    
				int ret = Integer.compare(t1.getCount(), t.getCount());
				if (ret == 0) {
					ret =  (int) (t.getWord().compareTo(t1.getWord()));
				}
				return ret;           
			}     
		};       

		/**
		 * Implement Comparator as anonymous inner class
		 */
		public static Comparator<Tuple> nameComparator = new Comparator<Tuple>() {    
			/**
			 * Implement the compare() method that will allow sorting Tuple objects by by word string in ascending order.
			 */
			@Override         
			public int compare(Tuple t, Tuple t1) {             
				return (int) (t.getWord().compareTo(t1.getWord()));         
			}     
		};   


		/**
		 * {@inheritDoc}
		 */
		@Override 
		public String toString() {
			return word + ";" + count;
		}
	}

}
