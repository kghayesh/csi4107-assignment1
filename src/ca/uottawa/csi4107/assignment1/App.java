package ca.uottawa.csi4107.assignment1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class App {

	public static void main(String[] args) throws ParseException, IOException
	{
		App app = new App();
		
		String path = app.getClass().getClassLoader().getResource(".").getPath();
		String fileName = "Trec_microblog11.txt";
		path = "/Volumes/Media/dev/workspace/csi-4107-assignment1-tokenizer-test/res/Trec_microblog11.txt";
		System.out.println("Path to tweets file: " + path);
		
		
		// Convert the tweets into Documents
		List<Document> tweets = null;
		try {
			tweets = app.parseTweets(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		
		
		// Create the Analyzer, Directory, and IndexWriter
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
		// Initialize the index file
		
		// Path where index will be stored
		String tweetIndexPath = "/Volumes/Media/dev/workspace/csi-4107-assignment1-tokenizer-test/res/tweet_index";
		System.out.println("Path to index directoy: " + tweetIndexPath);
		
		File tweetIndex = new File(tweetIndexPath);
		Directory index = new SimpleFSDirectory(tweetIndex);
		
		
		// Index the parsed tweet Documents
		app.indexTweets(tweets, analyzer, index);
		
		
		
		// Create a Query
		String queryString = "BBC"; // Hard coded keyword query for now
		System.out.println("Querying index for keywords: " +  queryString);
		Query q = app.getQuery(queryString, analyzer);
		
		
		
		// Search
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(index);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int hitsPerPage = 30;
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		
		
		app.displayResults(hits, searcher);
		
	}
	
	
	private void displayResults(ScoreDoc[] hits, IndexSearcher searcher) throws IOException
	{
		// Display the results
		System.out.println("Found " + hits.length + " hits.");
		for(int i = 0; i < hits.length; ++i) {
		    int docId = hits[i].doc;
		    Document d = searcher.doc(docId);
		    System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("text"));
		}
	}
	
	
	private Query getQuery(String queryString, Analyzer analyzer)
	{
		Query q = null;
		
		try {
			q = new QueryParser(Version.LUCENE_46, "text", analyzer).parse(queryString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return q;
	}
	
	
	private List<Document> parseTweets(String filePath) throws IOException
	{
		List<Document> docs = new ArrayList<Document>();
		
		FileReader input = null;
		BufferedReader bufferedRead = null;
		
		String tweet;
		try 
		{
			input = new FileReader(filePath);
			
			bufferedRead = new BufferedReader(input);
				
			while( (tweet = bufferedRead.readLine()) != null)
			{
				String [] tokens = tweet.split("\t", 2);
				
				Document tweetDoc = new Document();
				tweetDoc.add(new TextField("text", tokens[1], Field.Store.YES));
				tweetDoc.add(new StringField("id", tokens[0], Field.Store.YES));
				
				docs.add(tweetDoc);
			}
		} 
		finally
		{
			if (bufferedRead != null)  { bufferedRead.close();  }
		}
		
		
		
		return docs;
	}
	
	
	private Directory indexTweets(List<Document> tweets, Analyzer analyzer,Directory index) throws IOException 
	{
		// Configure and initialize the IndexWriter
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
		IndexWriter writer = null;
			writer = new IndexWriter(index, config);
		
		
		// Go through the list of tweet Documents and add them to the index
		ListIterator<Document> iterator = tweets.listIterator();
		while (iterator.hasNext())
		{
			writer.addDocument(iterator.next());
		}
		writer.close();
		
		return index;
	}

}
