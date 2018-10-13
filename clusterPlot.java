package com.nlptools.corenlp;
import java.awt.Color;  
import javax.swing.JFrame;  
import org.jfree.chart.ChartFactory;  
import org.jfree.chart.ChartPanel;  
import org.jfree.chart.JFreeChart;  
import org.jfree.chart.plot.XYPlot;  
import org.jfree.data.xy.XYDataset;  
import org.jfree.data.xy.XYSeries;  
import org.jfree.data.xy.XYSeriesCollection; 

public class clusterPlot extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String[][]folderTopicArray;
	double [][] reducedMatrix;
	int []normalizedLabels;
	String title;
	
	public clusterPlot(String title, double [][] reducedMatrix, int []normalizedLabels, String[][]folderTopicArray ) {
        super(title);
        this.title = title;
        this.folderTopicArray  = folderTopicArray;
        this.reducedMatrix = reducedMatrix;
        this.normalizedLabels = normalizedLabels;
        
        // Create dataset  
        XYDataset dataset = createDataset();

        // Create chart  
        JFreeChart chart = ChartFactory.createScatterPlot(
                title,
                "PC1", "PC2", dataset);


        //Changes background color  
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(new Color(255,228,196));


        // Create Panel  
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private XYDataset createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();

        
        for(int i = 0; i < folderTopicArray.length; i++) {
        	
        	String title = "Predicted: ";
        	for(int k = 0; k < folderTopicArray[0].length; k++) {
        		title = title + folderTopicArray[i][k] + " ";
        	}
        	XYSeries series = new XYSeries(title);
        	
        	for(int j = 0; j < normalizedLabels.length; j++) {
        		if(normalizedLabels[j]==i) {
        			series.add(reducedMatrix[j][0],reducedMatrix[j][1]);
        		}
        		
        	}
            dataset.addSeries(series);
 	
        }
        

        return dataset;
    }
	

}
