package com.nlptools.corenlp;

/*
 * Jason Chan
 * Descriptive-Modeling-and-Clustering-of-Textual-Data
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;



public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException 
    {
    	
    	double [][] centrioids;
    	int [] predictedLabels; //predicted labels/cluster. index correspond to row/doc in docMatrix and TFIDFMatrix
    	
    	String mode = "";
    	Scanner sc = new Scanner(System.in);
    	System.out.println("Welcome to Jason Chan's Document Clustering Program!");
    	System.out.println("How would you like to cluster?");
   	 	System.out.println("Type 'CS' for cosine similarty mode or 'EU' for euclidean distance mode");
	   	 System.out.print("Input: ");
	   	 
	     mode = sc.nextLine().toUpperCase();
	     
        //System.out.println(mode);
        
       while(!mode.equals("CS") && !mode.equals("EU")) {
    	   System.out.print("incorrect mode, please try again:");
    	    mode = sc.nextLine().toUpperCase();
  	     	
       } 
        
    	//BasicConfigurator.configure();
    	//Load documents that need to be preprocessed into a list
    	//String[] directoryList = {"src/test/java/com/nlptools/corenlp/C1/"};
    	String[] directoryList = {"src/test/java/com/nlptools/corenlp/C1/", "src/test/java/com/nlptools/corenlp/C4/", "src/test/java/com/nlptools/corenlp/C7/"};
    	
    	ArrayList <Preprocess> ppList = loadAllFiles(directoryList);
        
    	System.out.println("Preprocessing starting, please wait\n");
    	
    	
    	//Preprocess each document
    	int ppcount = 0;
    	
    	//ppList.size()
    	for(int i = 0; i < ppList.size(); i++){
    		ppList.get(i).processDocument();
        	ppcount++;
        	System.out.println("\"" + ppList.get(i).getDocName() + "\" processed to index: " + i);
        	
    	}
    	
    	System.out.println("Text Files Preprocessed: " + ppcount);
    	
    	System.out.println("Preprocessing Complete");
    	
    	System.out.println("Total Unique Words: " + Preprocess.globalWordCount.size());
    	
    	System.out.println("-------------------------\n");
    	
    	//create document matrix
    	System.out.println("Building Document-Word Matrix");
	
    	documentMatrix docMatrix = new documentMatrix(Preprocess.globalWordCount, ppList, directoryList);
    	
    	docMatrix.buildMatrix();
	
    	docMatrix.buildTFIDFMatrix();
    	
    	docMatrix.generateTopics();
    	
    	HashMap <String, String[]> folderTopicsMap = docMatrix.getFolderTopicMap();
    	
    	int[] actualDocumentTopic = docMatrix.getDocumentTopicArray();
    	String [][] folderTopicArray = docMatrix.getFolderTopicArray();
    	//String [] folderTopicString = docMatrix.getFolderTopicString();
    	
    	//print2DMatrix(folderTopicArray);
    	//System.out.println(documentTopic);
    	
    	
    	double [][] tfidfMatrix = docMatrix.getTFIDFMatrix();
    	
    	//System.out.println("Building Document-Word Matrix Complete");
    	System.out.println("-------------------------\n");
    	
    	System.out.println("Starting Kmeans, please wait\n");
    	
    	KMeans KM = new KMeans(tfidfMatrix);
    	KM.clustering(3, 15000, null, mode);
    	
    	//KM.printResults();
    	centrioids = KM.getCentroids();
    	predictedLabels =KM.getLabel();
    	
    	PredictionMetrics pm = new PredictionMetrics(predictedLabels, 3, docMatrix.getTFIDFMatrix(), docMatrix.getTermColsArray());
    	//pm.getClusterTopics();
    	//pm.getPredictedDocTopics();
    	
    	
    	
    	pm.testAccuracy(actualDocumentTopic, folderTopicArray);
    	int [] normalizedLabels = pm.getNormalizedPredictedLabels();
    	
    	PCA pc = new PCA(tfidfMatrix);
    	double [][] reducedMatrix = pc.getReducedMatrix();
    	
    	System.out.println("-------------------------\n");
    	
    	//
    	SwingUtilities.invokeLater(() -> {  
    	      clusterPlot example = new clusterPlot("Predicted Clusters", reducedMatrix, normalizedLabels, folderTopicArray);  
    	      example.setSize(1024, 768);  
    	      example.setLocationRelativeTo(null);  
    	      example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  
    	      example.setVisible(true);  
    	    });
    	
    	SwingUtilities.invokeLater(() -> {  
  	      clusterPlot original = new clusterPlot("Acutal Clusters", reducedMatrix, actualDocumentTopic, folderTopicArray);  
  	    original.setSize(1024, 768);  
  	    original.setLocationRelativeTo(null);  
  	    original.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  
  	    original.setVisible(true);  
  	    });
    	
    	
    	
    	System.out.println("Plotting");
    	System.out.println("-------------------------\n");
           
        System.out.println("\nDone!");
    }
    
    public static void print2DMatrix(double [][] inputMatrix){

		for (int i = 0; i < inputMatrix.length; i++){

			for(int j = 0; j < inputMatrix[0].length; j++){
				System.out.print(inputMatrix[i][j] + " ");
			}
			System.out.println();
		}

	} 
    
    public static void print2DMatrix(String [][] inputMatrix){

		for (int i = 0; i < inputMatrix.length; i++){

			for(int j = 0; j < inputMatrix[0].length; j++){
				System.out.print(inputMatrix[i][j] + " ");
			}
			System.out.println();
		}

	} 
    
    
    //load all the files of filepaths in array
    public static ArrayList <Preprocess> loadAllFiles (String [] directoryList) throws IOException{
    	
    	ArrayList <Preprocess> documentObjectsList = new ArrayList<Preprocess>();
    	
    	for(int i = 0; i < directoryList.length; i++){
    		
    		File folder = new File(directoryList[i]);
        	File[] listOfFiles = folder.listFiles();

        	for (File file : listOfFiles) {
        	    if (file.isFile()) {
        	        String textFileName = file.getName();
        	        //System.out.println(textFileName);
        	        documentObjectsList.add(new Preprocess(directoryList[i] + textFileName, i, directoryList[i]));
        	    }
        	}
    		
    	}	
    	
    	
    	
    	
    	return documentObjectsList;
    }
    
    
    
}
