# Descriptive Modeling and Clustering of Textual Data (in progress)
Descriptive Modeling and Clustering of Textual Data in Java

A document clustering application built in Java using Stanford's NLP Library.
The application will read, preprocess, tokenize, lemmanize text documents.
Then it will create a TF-IDF matrix to be used for clustering via k-means algorithm.
Currently, the K-means algorithm can use both euclidian distance and cosine similarity. 
If text files were grouped togther by topics. The application will measure itself's accuracy, precision and recall.
FVT and PCA was used reduce dimensions before plotting.

Both the clusters will be plotted with both original and predicted labels.

