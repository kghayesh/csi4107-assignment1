package ca.uottawa.csi4107.assignment1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.cli.*;
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
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class App {
	
	Directory index;
	StandardAnalyzer analyzer;
	IndexSearcher searcher;
	
	public App()
	{
		analyzer = new StandardAnalyzer(Version.LUCENE_46);
	}
	

	public static void main(String[] args) throws ParseException, IOException
	{
		App app = new App();
		
		app.initCLI(args);
	}
	
	
	private void initCLI(String[] args)
	{
		// CLI Options
		Option index = new Option("index", true, "Index a set of tweets");
		index.setArgName("tweets");
		index.setArgs(1);
		
		Option querySet = new Option("queryset", true, "Perform a batch set of queries from a file");
		index.setArgName("queryfile");
		index.setArgs(1);
		
		Option query = new Option("query", true, "Make a query on the index of tweets");
		index.setArgName("query");
		index.setArgs(1);
		
		Option help = new Option("help", "Print usage information");
	
		
		Options options = new Options();
		
		options.addOption(index);
		options.addOption(querySet);
		options.addOption(query);
		options.addOption(help);
		
		
		CommandLineParser parser = new BasicParser();
		
		try {
			CommandLine line = parser.parse(options, args);
			
			if(line.hasOption("index"))
			{
				indexTweets(line.getOptionValue("index"));
			}
			
			if(line.hasOption("queryset"))
			{
				if(indexExists())
				{
					initIndex();
					searchTweetsFromBatch(line.getOptionValue("queryset"));
				}
				else
					System.out.println("Index has not been built");
			}
			
			if(line.hasOption("query"))
			{
				if(indexExists())
				{
					initIndex();
					searchTweets(line.getOptionValue("query"));
				}
				else
					System.out.println("Index has not been built");
			}
			if(line.hasOption("help"))
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "tweetSearch", options );
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
	
	
	
	private List<Document> parseTweets(String filePath) throws IOException
	{
		LinkedList<Document> docs = new LinkedList<Document>();
		
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
	
	
	private void indexTweets(String tweetsPath) throws IOException
	{
		// Parse the tweets from the file into Documents objects
		List<Document> tweets = parseTweets(tweetsPath);
		
		
		// Initialize the index file if it doesn't already exist then build the index. If id does exist, we don't rebuild the index
		initIndex();
		
		if (!indexExists())
		{
			// Configure and initialize the IndexWriter
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
			IndexWriter writer = new IndexWriter(index, config);
					
			
			// Go through the list of tweet Documents and add them to the index
			ListIterator<Document> iterator = tweets.listIterator();
			while (iterator.hasNext())
			{
				writer.addDocument(iterator.next());
			}
			writer.close();
		}
	}
	
	private void initIndex()
	{
		
		try {
				index = FSDirectory.open(new File("res/index"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private boolean indexExists()
	{
		return new File("res/index").exists();
	}
	
	
	private ScoreDoc[] searchTweets(Query q) throws IOException
	{
		int hitsPerPage = 1000;
		searcher = new IndexSearcher(DirectoryReader.open(index));
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		
		searcher.search(q, collector);
		
		return collector.topDocs().scoreDocs;
	}
	
	
	private void searchTweets(String queryString) throws IOException
	{
		ScoreDoc[] hits = searchTweets(getQuery(queryString));
		
		displayResults(hits, searcher);
	}
	
	
	
	private void searchTweetsFromBatch(String queryFilePath)
	{
		// Parse the queries text file and make a list of Query objects
		
		
		// Make a search for each query and output the results
		
		
		
	}
	
	
	private Query getQuery(String queryString)
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
	
	
	private void outputResults(ScoreDoc[] hits, IndexSearcher searcher)
	{
		// Make the Results file out of search results
	}
	
	
	// Displays a sample of indexed vocabulary. May or may not be possible programmatically 
	private void displayIndexTerms()
	{
		
		
	}

}
