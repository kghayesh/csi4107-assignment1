package ca.uottawa.csi4107.assignment1;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

public class IndexTermReader {

	
	private Directory index;
	
	
	public IndexTermReader(Directory index)
	{
		this.index = index;
	}
	

	public static void main(String[] args)
	{
		App app = new App();
		
		app.initIndex();
		
		
		IndexTermReader termReader = new IndexTermReader(app.getIndex());
		
		try {
			
			termReader.printIndexTerms();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void printIndexTerms() throws IOException
	{
		// Get the IndexReader
		IndexReader reader = DirectoryReader.open(index);
		
		// Get the list of indexed Terms for the field "text" (the text of each tweet)
		Terms terms = MultiFields.getTerms(reader, "text");
		
		TermsEnum termIterator = terms.iterator(null);
		BytesRef term = null;
		int termCount = 0;
		
		// Loop through index terms printing out each one
		while((term = termIterator.next()) != null)
		{
		System.out.println(++termCount + ": " + term.utf8ToString());
		}
		
		System.out.println("Terms size: " + terms.size());
	}
		
}
