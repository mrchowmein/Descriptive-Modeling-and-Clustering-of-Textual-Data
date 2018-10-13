package com.nlptools.corenlp;
import java.util.ArrayList;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.optim.MaxIter;

public class PCA {

	private double [][]	inputMatrix; //input must be a normalized matrix

	private double [][] fvtReducedMatrix;// reduced through FVT

	private double [][] outputMatrix; //final maxtrix with 2 pc

	private double [] eigenValues;

	private double [] eigenVectors;

	private double [] variance;


	private double[] mean;

	PCA(double [][] inputMatrix){
		//this.kPrincipals = kPrincipals;
		this.inputMatrix = inputMatrix;
		
		System.out.println("\nStarting PCA to 2 Principal Components");
		FVT();
		computeEigenVectors();



	}

	private void FVT(){

		variance();
		double avgVariance = avgVar();

		ArrayList <Integer> colsToKeep = new ArrayList<Integer>();

		for(int i = 0; i < variance.length; i++) {

			if(variance[i]>avgVariance) {
				colsToKeep.add(i);
			}

		}

		//System.out.println(colsToKeep);

		fvtReducedMatrix = new double[inputMatrix.length][colsToKeep.size()];

		for(int row = 0; row < inputMatrix.length; row++) {

			for(int col = 0; col < colsToKeep.size(); col++) {
				fvtReducedMatrix[row][col] = inputMatrix[row][colsToKeep.get(col)];
			}


		}

		System.out.println("FVT reduced Dims to: " + fvtReducedMatrix.length + "x" + fvtReducedMatrix[0].length);



	}

	private double avgVar() {

		double meanVar = 0.0;
		double sum = 0.0;

		for (int i = 0; i < variance.length; i++ ) {
			sum += variance[i];
		}

		meanVar = sum/variance.length;
		//System.out.println("Mean Variance: " + meanVar);

		return meanVar;
	}

	private void variance(){

		colMeans();

		variance = new double[inputMatrix[0].length];

		int totalRows = inputMatrix.length;


		for(int col = 0; col < inputMatrix[0].length; col++){

			double sum = 0.0;

			for(int row = 0; row < totalRows; row++){

				sum = sum + Math.pow(inputMatrix[row][col]-mean[col], 2);
			}
			variance[col] = sum/totalRows;

		}



	}

	private void colMeans() {

		mean = new double[inputMatrix[0].length];

		int totalRows = inputMatrix.length;

		for (int col = 0; col < inputMatrix[0].length; col++){

			double sum = 0.0;

			for(int row = 0; row < totalRows; row++){
				sum = sum + inputMatrix[row][col];
			}
			mean[col] = sum/totalRows;

		}

	}

	private void computeEigenVectors() {


		double[][] reducedMatrixTransposed = transposeMatrix(fvtReducedMatrix);

		double [][] product = matrixMul(reducedMatrixTransposed, fvtReducedMatrix);



		RealMatrix realMatrix = MatrixUtils.createRealMatrix(product);
		//Covariance covariance = new Covariance(realMatrix);
		//RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
		EigenDecomposition ed = new EigenDecomposition(realMatrix);
		eigenValues = ed.getRealEigenvalues();
		//System.out.println(eigenValues.length);


		double [][] eMatrix = new double [ed.getEigenvector(0).getDimension()][2];

		for(int i = 0; i < 2; i++){
			for(int j = 0; j < ed.getEigenvector(0).getDimension(); j++){
				//System.out.print(ed.getEigenvector(i).getEntry(j) + " ");
				//System.out.println(i + " " + j);
				eMatrix[j][i] = ed.getEigenvector(i).getEntry(j);
			}

			//print2DMatrix(eMatrix);
			//System.out.println();
			//System.out.println(ed.getEigenvector(0));
		}

		outputMatrix = matrixMul(fvtReducedMatrix, eMatrix);
	}

	private double[][] matrixMul(double [][] array1, double [][] array2) {
		int m1 = array1.length;
		int n1 = array1[0].length;
		int m2 = array2.length;
		int n2 = array2[0].length;
		double [][] output = new double[m1][n2];

		if (n1 != m2) throw new RuntimeException("Illegal matrix dimensions.");

		for (int i = 0; i < m1; i++)
			for (int j = 0; j < n2; j++)
				for (int k = 0; k < n1; k++)
					output[i][j] += array1[i][k] * array2[k][j];

		return output;
	}

	private double[][] transposeMatrix(double [][] inputMatrix){

		int mRow = inputMatrix.length;
		int mCol = inputMatrix[0].length;

		double [][]inputMatrixTransposed = new double[mCol][mRow];
		//        System.out.println(inputMatrixTransposed.length);
		//        System.out.println(inputMatrixTransposed[0].length);

		for(int i = 0; i < mRow; i++){
			for(int j = 0; j < mCol; j++) {
				inputMatrixTransposed[j][i] = inputMatrix[i][j];
			}
		}
		return inputMatrixTransposed;
	}


	private void print2DMatrix(double [][] inputMatrix){

		for (int i = 0; i < inputMatrix.length; i++){

			for(int j = 0; j < inputMatrix[0].length; j++){
				System.out.print(inputMatrix[i][j] + " ");
			}
			System.out.println();
		}

	} 

	public double[] getEigenValues(){

		for(int i = 0; i < eigenValues.length; i++){
			System.out.println(eigenValues[i]);
		}

		return eigenValues;
	}

	public double[] getEigenVectors(){

		for(int i =0; i < eigenValues.length; i++){
			System.out.println(eigenValues[i]);
		}

		return eigenVectors;
	}

	public double[] getVariance(){
		//	
		//	for(int i =0; i < variance.length; i++){
		//        System.out.println(variance[i]);
		//    }
		//	
		return variance;
	}

	public double [][] getReducedMatrix(){
		
		//print2DMatrix(inputMatrix);
		
		return outputMatrix;
	}


}
