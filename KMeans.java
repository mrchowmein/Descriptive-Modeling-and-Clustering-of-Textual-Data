package com.nlptools.corenlp;


import java.util.ArrayList;


public class KMeans
{
    // Data members
    private double [][] dataSetMatrix; // Array of all records in dataset
    private int [] label;  // generated cluster labels
 
    
    private double [][] centroids; 
    private int nrows;
    private int ndims; // the number of rows and dimensions
    private int numClusters; // the number of clusters;
    private String similarityMode; //use CS for cosine sim. use ED for euculdian distance.

    // Constructor; loads records from file <fileName>.
    // if labels do not exist, set labelname to null
    public KMeans(double [][] dataSetMatrix)
    {
     
    	this.dataSetMatrix = dataSetMatrix;
    	this.nrows = dataSetMatrix.length;
    	this.ndims = dataSetMatrix[0].length;
    	
    	

    }

    
    public void clustering(int numClusters, int niter, double [][] clusters, String similaryMeasure){
    	
    	System.out.println("please wait for " + niter + " iterations\n");
    	similarityMode = similaryMeasure;
    	System.out.println("Similarity measure mode: " + similarityMode);
    	
        this.numClusters = numClusters;
        if (clusters !=null)
            centroids = clusters;
        else{
            // randomly selected centroids from one of the rows
            centroids = new double[numClusters][];
            System.out.println("Generating random centroids");
            ArrayList idx= new ArrayList<>();
            for (int i=0; i<numClusters; i++){
                int c;
                do{
                    c = (int) (Math.random()*nrows);
                }while(idx.contains(c)); // avoid duplicates
                idx.add(c);

                // copy the value from _data[c]
                centroids[i] = new double[ndims];
                for (int j=0; j<ndims; j++)
                    centroids[i][j] = dataSetMatrix[c][j];
            }
            System.out.println("selected random centroids");

        }

        double [][] c1 = centroids;
        //double threshold = 0.001;
        int round=0;

        while (true){
            // update centroids with the last round results
            centroids = c1;

            //assign record to the closest centroid
            label = new int[nrows];
            for (int i=0; i<nrows; i++){
            	
            	if(similarityMode.equals("EU")) {
            		//System.out.println("EU Mode");
                label[i] = closest(dataSetMatrix[i]);//using euclidean distance
            	} 
            	if (similarityMode.equals("CS")) {
            		//System.out.println("CS Mode");
            		label[i] = mostSimilar(dataSetMatrix[i]); //use cosinesimilarity
            	} 
            }
            	

            // recompute centroids based on the assignments
            c1 = updateCentroids();
            round ++;
            if(similarityMode.equals("ED")) {
            	if ((niter >0 && round >=niter)){ // || convergeEU(centroids, c1, .001))
            		break;
           
            	}
                    
            } else {
            	if ((niter >0 && round >=niter)) {// ||convergeCS(centroids, c1, .9))
            		break;
            		
            	}
                    
            }
            
            
            
        }

        //System.out.println("Clustering converges at round " + round);
    }

    // find the closest centroid for the record v
    private int closest(double [] v){
        double mindist = dist(v, centroids[0]);
        int label =0;
        for (int i=1; i<numClusters; i++){
            double t = dist(v, centroids[i]);
            if (mindist>t){
                mindist = t;
                label = i;
            }
        }
        return label;
    }

    // compute Euclidean distance between two vectors v1 and v2
    private double dist(double [] v1, double [] v2){
        double sum=0;
        for (int i=0; i<ndims; i++){
            double d = v1[i]-v2[i];
            sum += d*d;
        }
        return Math.sqrt(sum);
    }
    
 // compute cosineSimilary distance between two vectors v1 and v2
    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }   
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
 // find the most similar centroid for the record v  
    private int mostSimilar(double [] v){
        double sim = cosineSimilarity(v, centroids[0]);
        int label =0;
        for (int i=1; i<numClusters; i++){
            double t = cosineSimilarity(v, centroids[i]);
            if (sim<t){
                sim = t;
                label = i;
            }
        }
        return label;
    }

    
    
    

    // according to the cluster labels, recompute the centroids
    

    private double [][] updateCentroids(){
    	//System.out.println("Updating Centroids");
        // initialize centroids and set to 0
        double [][] newc = new double [numClusters][]; //new centroids
        int [] counts = new int[numClusters]; // sizes of the clusters

        // intialize
        for (int i=0; i<numClusters; i++){
            counts[i] =0;
            newc[i] = new double [ndims];
            for (int j=0; j<ndims; j++)
                newc[i][j] =0;
        }


        for (int i=0; i<nrows; i++){
            int cn = label[i]; // the cluster membership id for record i
            for (int j=0; j<ndims; j++){
                newc[cn][j] += dataSetMatrix[i][j]; // update that centroid by adding the member data record
            }
            counts[cn]++;
        }

        // finally get the average
        for (int i=0; i< numClusters; i++){
            for (int j=0; j<ndims; j++){
                newc[i][j]/= counts[i];
            }
        }

        return newc;
    }

    // check convergence condition
//
//    private boolean convergeEU(double [][] c1, double [][] c2, double threshold){
//        // c1 and c2 are two sets of centroids
//        
//    	double maxv = 0;
//        for (int i=0; i< numClusters; i++){
//        	
//        		double d= dist(c1[i], c2[i]);
//                if (maxv<d)
//                    maxv = d; 
//        }
//
//        if (maxv <threshold)
//            return true;
//        else
//            return false;
//
//    }
//    
//    
//    private boolean convergeCS(double [][] c1, double [][] c2, double threshold){
//        // c1 and c2 are two sets of centroids
//        double maxv = 0;
//        for (int i=0; i< numClusters; i++){
//            double d= cosineSimilarity(c1[i], c2[i]);
//            if (maxv>d)
//                maxv = d;
//        }
//
//        if (maxv >threshold)
//            return true;
//        else
//            return false;
//
//    }
    
    
    
    
    
    
    public double[][] getCentroids()
    {
        return centroids;
    }

    public int [] getLabel()
    {
        return label;
    }

    public int nrows(){
        return nrows;
    }

    public void printResults(){
        
    	System.out.println("Clustered Label:");
        for (int i=0; i<nrows; i++)
            System.out.println("Document " + i + ": " + label[i]);
            
        //print centroids
//        System.out.println("Centroids:");
//        for (int i=0; i<numClusters; i++){
//            for(int j=0; j<ndims; j++)
//                System.out.print(centroids[i][j] + " ");
//            System.out.println();
//        }

    }



}

