package com.nlptools.corenlp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Properties;
import java.util.Scanner;



import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class Preprocess {

	private String nameOfDoc;
	static HashMap<String, Integer> globalWordCount = new HashMap<>();
	private HashMap<String, Integer> documentWordCount = new HashMap<>();
	private ArrayList <String> stopWordsList;
	private String docString;
	private String originalDocString;
	private ArrayList <String> processedWordList;
	private HashMap<String, Integer> ngramsMap = new HashMap<String, Integer>();
	String folderName; 


	Preprocess(String textFile, int folderIndex, String path) throws IOException{

		this.stopWordsList = readFileStopWords("src/test/java/com/nlptools/corenlp/stopwords.txt");
		this.docString = docToString(textFile).toLowerCase();
		this.originalDocString = docToString(textFile).toLowerCase();
		this.processedWordList = new ArrayList<String>();
		this.folderName = path;

	}

	public void processDocument() {
		removeStopWords();
		tokenLemmaNer();


	}
	
	

	private ArrayList <String> readFileStopWords(String fileName) throws IOException {
		Scanner s = new Scanner(new File(fileName));
		ArrayList<String> list = new ArrayList<String>();

		while (s.hasNext()){
			list.add(s.next());
		}
		s.close();

		return list;
	}


	private String docToString(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		String outputLine = "";
		int lineCount = 0;
		while((line = in.readLine()) != null)
		{
			if(lineCount == 0) {
				nameOfDoc = line;
			}
			
			//System.out.println(line);
			outputLine = outputLine + line + " ";
			lineCount++;
		}
		in.close();

		//remove extra whitespace
		outputLine = outputLine.replaceAll("\\s+", " ");
		return outputLine;		
	}


	private void removeStopWords() {

		for(int i = 0; i < stopWordsList.size(); i++) {

			String currentStopWord = stopWordsList.get(i);
			//System.out.println(currentStopWord);

			if(i < 41) {
				docString = docString.replace(currentStopWord, "");

			} else {

				String regex = "\\s*\\b" + currentStopWord + "\\b\\s*";
				//System.out.println(regex);
				docString = docString.replaceAll(regex, " ");
			}

		}

	}

	private void removeNgrams(){

		Document docNGrams = new Document(docString);
		
		//fill ngram freqency hashmap. this is acutally one sentence. .sentence creates a list.
		for (Sentence sent : docNGrams.sentences()) {
			
			//this is a sliding window. since I converted the entire doc into a single string.
			for(int i = 0; i < sent.length()-1; i++) {
				String currentWord = sent.lemma(i) + " " + sent.lemma(i+1);
				ngramsMap.merge(currentWord, 1, Integer::sum);
				//System.out.println(currentWord);
				
				if(ngramsMap.get(currentWord) > 1) {
					processedWordList.add(currentWord);
					String regex = currentWord;
					documentWordCount.put(currentWord,ngramsMap.get(currentWord));

					if(globalWordCount.get(currentWord) == null) {
						globalWordCount.put(currentWord, ngramsMap.get(currentWord));
					} else {
						//int curGlobalValue = globalWordCount.get(key);
						globalWordCount.put(currentWord, ngramsMap.get(currentWord) + globalWordCount.get(currentWord));		
					}
					//System.out.println(regex);
					docString = docString.replace(regex, " ");
					//System.out.println(docString);
				}


			}
		}
		
		
	}


	private void tokenLemmaNer() {

		//token and lemma text
		Document tokenized = new Document(docString);
		String lemmaedString = "";
		
		for (Sentence sent : tokenized.sentences()) {
			for(int i = 0; i < sent.length(); i++) {
				String currentWord = sent.lemma(i);
				lemmaedString = lemmaedString + currentWord + " ";

			}

		}
		//udpate docString as with lemmanized string
		docString = lemmaedString;
		
		

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
		props.setProperty("ner.applyFineGrained", "false");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		CoreDocument doc = new CoreDocument(docString);
		pipeline.annotate(doc);

		//add name entities to document/global word count hashmaps. then removed from docString
		for (CoreEntityMention em : doc.entityMentions()) {
			globalWordCount.merge(em.text(), 1, Integer::sum);
			documentWordCount.merge(em.text(), 1, Integer::sum);
			processedWordList.add(em.text());  //add to list of words processed
			String regex = "\\s*\\b" + em.text() + "\\b\\s*";
			//remove name entities from docString
			//System.out.println("Entity text "+ regex);
			docString = docString.replaceAll(regex, " ");

		}

		//remove ngrams from docString, add ngrams to processedWordList, add ngrams to global/document wordlist hashmap
		removeNgrams();


		//reload docSstring
		Document doc2 = new Document(docString);

		//add remaining words to hashmap
		for (Sentence sent : doc2.sentences()) {
			for(int i = 0; i < sent.length(); i++) {
				String currentWord = sent.lemma(i);
				processedWordList.add(currentWord);
				globalWordCount.merge(currentWord, 1, Integer::sum);
				documentWordCount.merge(currentWord, 1, Integer::sum);

			}

		}

	}

	public String getDocName() {
		return nameOfDoc;
	}
	
	public HashMap<String, Integer> getDocumentWordMap(){
		return documentWordCount;
	}

	public ArrayList<String> getStopWords() {
		return stopWordsList;
	}

	public ArrayList<String> getProcessedWordsList() {
		return processedWordList;
	}

	public String getOriginalDocString() {
		return originalDocString;
	}

	public HashMap<String, Integer> getNgramsCount(){
		return ngramsMap;
	}

	public String getFolderName() {
		return folderName;
	}
	
	public void printCounts() {
		
		System.out.println("Processed word count: " +processedWordList.size());
		
		int count = 0;
		
		for (String key : documentWordCount.keySet()) {
			count += documentWordCount.get(key);
			if(!processedWordList.contains(key)) {
				System.out.println("Missing: " + key);
			}
			
		}
		System.out.println("Doc count: " + count);
		
		
	}

}
