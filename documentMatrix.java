package com.nlptools.corenlp;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class documentMatrix {


	private HashMap <String, Integer> globaWordFrequencyMap;

	private ArrayList <Preprocess> preprocessedList; //arraylist of preprocessed document objects
	private int[][] docTermMatrix;
	String [] termColsArray;  //array of terms
	int [] termColsCountArray;// total count of the terms for whole document space
	private double [][] tfidfMatrix;
	private double [] wordsPerDoc; //index correspond to row in docTermMatrix
	private int [] topWordIndex; //index for the top words. correspond to termColsArray and tfidMatrix
	private String [] folderName;
	private HashMap <String,String[]> folderTopicMap;
	private int [] documentTopic; //each element points to the index of the folderTopicArray
	private String [][] folderTopicArray;
	

	//constructor requires the globalwordcount map and a list of proprocessed documents
	documentMatrix(HashMap<String, Integer> globalWordCount, ArrayList<Preprocess> ppList, String[] directoryList){
		this.globaWordFrequencyMap = globalWordCount;
		this.preprocessedList = ppList;
		this.folderName = directoryList;
		
	}

	public void buildMatrix(){

		int rows = preprocessedList.size();
		int cols = globaWordFrequencyMap.size();

		//create empty matrix
		docTermMatrix = new int [rows][cols];

		termColsArray = new String [cols];  //array with all the terms
		termColsCountArray = new int [cols]; //array with all frequencies of the terms
		wordsPerDoc = new double[cols];

		for (String key : globaWordFrequencyMap.keySet()) {
			termColsArray[cols-1] = key;
			termColsCountArray[cols-1] = globaWordFrequencyMap.get(key);
			cols--;
		}


		//System.out.println("rows: " + docTermMatrix.length);
		//System.out.println("cols: " + docTermMatrix[0].length);


		//fill matrix with frequency by matching the index with words in colsArray with preprocessed doc's word frequency hashmap
		for (int i = 0; i < rows; i++) {	

			for(int j = 0; j < termColsArray.length; j++) {
				String currentCol = termColsArray[j];
				if(preprocessedList.get(i).getDocumentWordMap().containsKey(currentCol) == true) {
					//System.out.println("Found Key: "+ currentCol);
					//System.out.println("Value: " + preprocessedList.get(i).getDocumentWordMap().get(currentCol));
					docTermMatrix[i][j] = preprocessedList.get(i).getDocumentWordMap().get(currentCol);

					//System.out.println(preprocessedList.get(i).getDocumentWordMap().get(currentCol));
					wordsPerDoc[i] += preprocessedList.get(i).getDocumentWordMap().get(currentCol); 

				} else {
					//System.out.println("NotFound: "+ currentCol);
				}
			}
		}

		System.out.println("Finished Building Matrix");
		//		
		//		for(int i = 0; i < rows; i++) {
		//			for(int j = 0; j <termColsArray.length; j++) {
		//				System.out.print(docTermMatrix[i][j]);
		//			}
		//			System.out.println();
		//		
		//		}
	}



	public void buildTFIDFMatrix() {

		int[] numDocsWithTerms = new int[termColsArray.length]; //count of docs with the term matching termColsArray
		double [] idf = new double [termColsArray.length];


		double docSpace = docTermMatrix.length;

		//find the number of documents with term
		for (int col = 0; col <termColsArray.length; col++ ) {
			int count = 0;

			for (int row = 0; row < docSpace; row++) { //iterate over rows
				if(docTermMatrix[row][col] > 0) {
					count++;
				}	
			}
			numDocsWithTerms[col] = count;		
		}

		for (int col = 0; col <termColsArray.length; col++) {

			idf[col] = Math.log10(docSpace/(numDocsWithTerms[col]));

			if(idf[col]< 0 || Double.isInfinite(idf[col])) {
				System.out.println("error with idf at idf array index: " + idf[col]);
			}


		}


		if(docTermMatrix != null) {
			tfidfMatrix = new double[docTermMatrix.length][docTermMatrix[0].length];
		}

		for(int row = 0; row < docTermMatrix.length; row++ ) {

			for(int col = 0; col < docTermMatrix[0].length; col++) {
				//System.out.println(docTermMatrix[row][col] + "/" + wordsPerDoc[row] +": "+ (docTermMatrix[row][col]/wordsPerDoc[row]));
				tfidfMatrix[row][col] = idf[col]*(docTermMatrix[row][col]/wordsPerDoc[row]);
			}
		}

	}
	
	


	public void generateTopics() throws FileNotFoundException {

		folderTopicMap = new HashMap<String, String[]>();
		//add up vectors. the index of this array will still match with the original, termsCol
		
		documentTopic = new int [tfidfMatrix.length];
		
		
		folderTopicArray = new String [folderName.length][];

		//loop through tfidfMatrix row by row and add to the combinedTDIDF, for each folder
		
		String text = "";
		
		for(int folderIndex = 0; folderIndex < folderName.length; folderIndex++) {
			
			String [] folderTopic = new String[5];
			
			double [] combinedTFIDF = new double [tfidfMatrix[0].length];
			
			for(int row = 0; row <tfidfMatrix.length; row++ ) {
				for(int col = 0; col <tfidfMatrix[0].length; col++ ) {
					
					
					if(folderName[folderIndex].equals(preprocessedList.get(row).folderName)) {
						//System.out.println(folderName[folderIndex]);
						//System.out.println(preprocessedList.get(row));
						combinedTFIDF[col] = combinedTFIDF[col] + tfidfMatrix[row][col];
					}
					
				}

			}	
			
			topWordIndex = TopKWords(combinedTFIDF, 5); 
			
		
			text = text + "\nTopics for " + folderName[folderIndex]+"\n";
			//System.out.println("\nTopics " + topKtopics + " of " + folderName[folderIndex]);
			for(int index = 0; index < topWordIndex.length; index++) {
				text = text + termColsArray[topWordIndex[index]] + "\n";
				folderTopic[index]=termColsArray[topWordIndex[index]];
				//System.out.println(termColsArray[topWordIndex[index]] +": " + combinedTFIDF[topWordIndex[index]]);
			}	
			
			//Arrays.sort(folderTopic);
			
			folderTopicArray[folderIndex] = folderTopic;
			folderTopicMap.put(folderName[folderIndex],folderTopic);
		}
		try (PrintWriter out = new PrintWriter("src/test/java/com/nlptools/corenlp/topics.txt")) {
		    out.println(text);
		}
		
		// fill documentTopic array with document's topic
		for(int row = 0; row <documentTopic.length; row++ ) {
			String[] topic = folderTopicMap.get(preprocessedList.get(row).folderName);
			for(int i = 0; i <folderTopicArray.length; i++) {
				if(Arrays.equals(folderTopicArray[i], topic)) {
					documentTopic[row] = i;
				}
			}
	
			//System.out.println("Doc " + row + ": " + preprocessedList.get(row).folderName);
		}
	}
	
	


	//find the top 5 values, returns an array of index
	private int [] TopKWords(double input[], int k) 
	{ 
		//cloned original
		double [] arr = input.clone();
		int [] wordIndex = new int[k];

		for(int iter = 0; iter < k; iter++) {
			
			double maxValue = arr[0];
			int maxIndex = 0;
			
			for (int col = 1; col < arr.length; col++) {
				
				//System.out.println(col);
				if(arr[col] > maxValue) {
					//System.out.println(col);
					//System.out.println(arr[col]);
					maxValue = arr[col];
					maxIndex = col;
					arr[col] = -1.0;
				}

			}

			wordIndex[iter] = maxIndex;

		}


		return wordIndex;

	} 



	//setters

	public int [][] getDocTermMatrix() {
		return docTermMatrix;
	}

	public String [] getTermColsArray() {
		return termColsArray;
	}

	public int [] getTermColsCountArray() {
		return termColsCountArray;
	}

	public double [][]getTFIDFMatrix() {
		return tfidfMatrix;
	}
	
	public HashMap<String, String []> getFolderTopicMap() {
		return folderTopicMap;
	}
	
	public int [] getDocumentTopicArray(){
		
//		for(int i = 0; i <documentTopic.length; i++) {
//			System.out.println(documentTopic[i]);
//		}
		return documentTopic;
	}
	
	public String [][] getFolderTopicArray(){
		
		for(int i = 0; i < folderTopicArray.length; i++) {
			System.out.print("Topics for folder " + i + ": ");
			for(int j = 0; j < folderTopicArray[0].length; j++) {
				System.out.print(folderTopicArray[i][j] +" ");
			}
			System.out.println();
		}
		
		
		return folderTopicArray;
	}
	
	


}
