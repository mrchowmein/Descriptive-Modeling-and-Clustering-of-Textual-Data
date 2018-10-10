package com.nlptools.corenlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PredictionMetrics {


	private int [] predictedLabels;
	private int nClusters; 
	private double [][] tfidfMatrix;
	String [] termColsArray;
	private HashMap<Integer, String []> clusterTopicsMap;
	private String [] clusterTopicsArray;
	private String [] predictedDocTopics;
	private int [] normalizedPredictedLabels;
	private double accuracy;

	PredictionMetrics(int [] predictedLabels, int nClusters, double [][] tfidfMatrix, String [] termColsArray){
		this.predictedLabels = predictedLabels;
		this.nClusters = nClusters;
		this.tfidfMatrix = tfidfMatrix;
		this.termColsArray = termColsArray;
		findTopicsForRows();

	} 


	private void findTopicsForRows() {
		String[] predictedTopics = new String[nClusters];
		clusterTopicsMap = new HashMap<Integer, String[]>();
		clusterTopicsArray = new String[nClusters];
		predictedDocTopics = new String [predictedLabels.length];

		HashMap <Integer, double[]> clusterCombinedTFIDF = new HashMap <Integer, double[]>();

		//fill hashmap with each cluster as key
		for(int i = 0; i < nClusters; i++) {  
			double [] combinedTFIDF = new double [tfidfMatrix[0].length];
			clusterCombinedTFIDF.put(i, combinedTFIDF);
		}


		//go through each index of the and up the tfidf and save it back to the corresponding key in hashmap
		for(int row = 0; row <predictedLabels.length; row++) {

			double[] combTFIDF = clusterCombinedTFIDF.get(predictedLabels[row]);

			for(int col = 0; col < tfidfMatrix[0].length; col++) {
				combTFIDF[col] = combTFIDF[col] + tfidfMatrix[row][col];

			}
			clusterCombinedTFIDF.put(predictedLabels[row], combTFIDF);
		}



		for (HashMap.Entry<Integer, double[]> entry : clusterCombinedTFIDF.entrySet()) {
			int key = entry.getKey();
			double[] combinedTFIDF = entry.getValue();
			int [] wordIndex = TopKWords(combinedTFIDF);

			String [] clusterTopics = new String[wordIndex.length];
			//System.out.println("\nTopics " + topKtopics + " of " + folderName[folderIndex]);

			String clusterTopic = "";
			for(int index = 0; index < wordIndex.length; index++) {
				 clusterTopic = clusterTopic + termColsArray[wordIndex[index]] + " ";

			}	

			//Arrays.sort(clusterTopics);
			
			clusterTopicsArray[key] = clusterTopic;
			
			//System.out.print(clusterTopic);


		}

		for(int i =0; i < predictedLabels.length; i++) {


			predictedDocTopics[i] = clusterTopicsArray[predictedLabels[i]];


		}


	}

	//find topics
	
	private int [] TopKWords(double arr[]) 
	{ 
		
		//int wordIndex = -1;
			
		
		int[] maxIndex = new int[5];
			
		for (int iter = 0; iter < 5; iter++) {
			double maxValue = arr[0];
			
			for (int col = 1; col < arr.length; col++) {
				
				//System.out.println(col);
				if(arr[col] > maxValue) {
					//System.out.println(col);
					//System.out.println(arr[col]);
					maxValue = arr[col];
					maxIndex[iter] = col;
					arr[col] = -1;
				}

			}
		}
		


		return maxIndex;

	} 
	
	
	public void testAccuracy(int[] actualDocumentTopic, String [][]folderTopicArray) {
		System.out.println("\n----------------------------");
		System.out.println("\nPerformance Metrics:");
		int [][] confusionMatrix = new int[nClusters][nClusters];
		normalizedPredictedLabels = new int[actualDocumentTopic.length];
		
		//
		for(int docRows = 0; docRows< actualDocumentTopic.length; docRows++) {
			String predictedRowTopic = predictedDocTopics[docRows];
			for(int i = 0; i < folderTopicArray.length; i++) {
				int count = 0;
				for(int j = 0; j <folderTopicArray[0].length; j++) {
					String acutualLabel = folderTopicArray[i][j];
					if(predictedRowTopic.contains(acutualLabel)) {
						count++;
					}
					
				}
				if(count>2) {
					normalizedPredictedLabels[docRows] = i;
				}
			}
			
		}
		
		System.out.println("\nNormalized Predicted Labels");
		for(int i = 0; i < normalizedPredictedLabels.length; i++) {
    		System.out.println("Doc " + i + ": " +normalizedPredictedLabels[i]);
    		
    	}
		
		
		
		for(int docRows = 0; docRows< actualDocumentTopic.length; docRows++) {
			
			int actual = actualDocumentTopic[docRows];
			int predicted =  normalizedPredictedLabels[docRows];
			confusionMatrix[actual][predicted] += 1;
			
			
		}
		
		System.out.println("\nConfusion Matrix");
		for(int i = 0; i < confusionMatrix.length; i++) {

			for(int j = 0; j < confusionMatrix.length; j++) {
				System.out.print(confusionMatrix[i][j] + " ");
			}
			System.out.println();

		}
		
		double sum = 0.0;
		for(int i = 0; i < confusionMatrix.length; i++) {
			sum += confusionMatrix[i][i];
		}
		
		accuracy = computeAccuracy(confusionMatrix, actualDocumentTopic.length);
		
		System.out.println("\nAccuracy: " + accuracy);

		double [] recallArray = new double[confusionMatrix.length];
		for(int i = 0; i < confusionMatrix.length; i++) {
			double recall = computeRecall(confusionMatrix[i], i);
			recallArray[i] = recall;
			System.out.println("Recall for Label " + i + ": " + recall);
		}
		
		double [] precisionArray = new double[confusionMatrix.length];
		for(int i = 0; i < confusionMatrix.length; i++) {
			double precision = computePrecision(confusionMatrix, i);
			precisionArray[i]=precision;
			System.out.println("Precision for Label " + i + ": " + precision);
		}
		double [] fScoreArray = new double[confusionMatrix.length];
		
		for(int i = 0; i < fScoreArray.length; i++) {
			double fscore = (2 * recallArray[i]* precisionArray[i])/(recallArray[i]+ precisionArray[i]);
			fScoreArray[i] = fscore;
			System.out.println("F score for Lable " + i +": "+ fscore);
		}
		
		
	}
	
	private double computeAccuracy (int[][]confusionMatrix, int total) {
		
		double sum = 0.0;
		for(int i = 0; i < confusionMatrix.length; i++) {
			sum += confusionMatrix[i][i];
		}
		
		return sum/total;
	}
	
	private double computeRecall(int[]rowOfCMatrix, int index) {
		double sum = 0.0;
		for(int i = 0; i < rowOfCMatrix.length; i++) {
			sum += rowOfCMatrix[i];
		}
		
		return rowOfCMatrix[index]/sum;
	}

	private double computePrecision(int[][]confusionMatrix, int index) {
		
		double sum = 0.0;
		for(int i = 0; i < confusionMatrix.length; i++) {
			sum += confusionMatrix[i][index];
		}
		
		return confusionMatrix[index][index]/sum;
	}

	//use folderTopicArray as index for both rows and cols
//	public void testAccuracy(String [] acutalDocumentTopic, String []folderTopicArray) {
//		
//
//		HashMap<String, Integer> confusionMatrix
//		
//		for(int i = 0; i < folderTopicArray.length; i++) {
//			for(int j = 0; j <folderTopicArray.length; j++) {
//				
//				
//				
//				
//			}
//			
//			
//		}
//		
//
//		for(int i = 0; i < confusionMatrix.length; i++) {
//
//			for(int j = 0; j < confusionMatrix.length; j++) {
//				System.out.print(confusionMatrix[i][j] + " ");
//			}
//			System.out.println();
//
//		}
//	}
	
	
	
	

	public String [] getClusterTopics(){

		//rint hashmap
		for (int i = 0; i < clusterTopicsArray.length; i++) {
			System.out.println(clusterTopicsArray[i]);
		}

		return clusterTopicsArray;
	}

	public String [] getPredictedDocTopics(){

//		for(int i = 0; i < predictedDocTopics.length; i++) {
//    		System.out.println(predictedDocTopics[i]);
//    		
//    	}

		return predictedDocTopics;
	}
	
	public int [] getNormalizedPredictedLabels() {
		
//		System.out.println("Normalized Predicted Labels");
//		for(int i = 0; i < normalizedPredictedLabels.length; i++) {
//    		System.out.println("Doc " + i + ": " +normalizedPredictedLabels[i]);
//    		
//    	}
		
		return normalizedPredictedLabels;
	}
	
	public double getAccuracy() {
		return accuracy;
	}


}
