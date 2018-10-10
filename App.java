package com.nlptools.corenlp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException 
    {
    	
    	double [][] centrioids;
    	int [] predictedLabels; //predicted labels/cluster. index correspond to row/doc in docMatrix and TFIDFMatrix
    	
    	
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
    	
    	
    	
    	//System.out.println(documentTopic);
    	
    	
    	double [][] tfidfMatrix = docMatrix.getTFIDFMatrix();
    	
    	//System.out.println("Building Document-Word Matrix Complete");
    	System.out.println("-------------------------\n");
    	
    	System.out.println("Starting Kmeans, please wait\n");
    	
    	KMeans KM = new KMeans(tfidfMatrix);
    	KM.clustering(3, 15000, null, "CS");
    	
    	//KM.printResults();
    	centrioids = KM.getCentroids();
    	predictedLabels =KM.getLabel();
    	
    	PredictionMetrics pm = new PredictionMetrics(predictedLabels, 3, docMatrix.getTFIDFMatrix(), docMatrix.getTermColsArray());
    	//pm.getClusterTopics();
    	//pm.getPredictedDocTopics();
    	
    	pm.testAccuracy(actualDocumentTopic, folderTopicArray);
    	
    	
    	
    	
//    	for(int i = 0; i < labels.length; i++) {
//    		System.out.println("predicted cluster: "+ labels[i]);
//    	}
    	
    
           
        
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
