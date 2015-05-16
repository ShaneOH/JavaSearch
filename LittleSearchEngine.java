package search;

import java.io.*;
import java.util.*;

class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		HashMap<String, Occurrence> hasher = new HashMap<String, Occurrence>(1000, 2.0f);
			Scanner scan = new Scanner(new File(docFile));
			while(scan.hasNext()){
				String keyword = scan.next();
				keyword = getKeyWord(keyword);
				if(keyword != null){
					if(!hasher.containsKey(keyword)){
						Occurrence hit = new Occurrence(docFile, 1);
						hasher.put(keyword, hit);
					}
					else{
						Occurrence hit = hasher.remove(keyword);
						hit.frequency++;
						hasher.put(keyword, hit);
					}
				}
				
			}
						
		return hasher;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		Set<String> keywords = kws.keySet();
		Iterator<String> iterate = keywords.iterator();
		
		while(iterate.hasNext()){
			String keyword = iterate.next();
			if(keywordsIndex.containsKey(keyword)){
				ArrayList<Occurrence> occs = new ArrayList<Occurrence>();
				Occurrence temp = new Occurrence(kws.get(keyword).document, kws.get(keyword).frequency);
				keywordsIndex.get(keyword).add(temp);
				insertLastOccurrence(keywordsIndex.get(keyword));
			}
			
			else{
				ArrayList<Occurrence> occs = new ArrayList<Occurrence>();
				Occurrence temp = new Occurrence(kws.get(keyword).document, kws.get(keyword).frequency);
				occs.add(temp);
				keywordsIndex.put(keyword, occs);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		
		
		if(word == null){
		return null;
		}
		
		String target = word;
		target = target.trim();
		target = target.toLowerCase();
		
		for(int i = 0; i < target.length(); i++){
		char trail = target.charAt(target.length() - 1);
		if(trail == ',' || trail == '.' || trail == '?' || trail == '!' || trail == ':' || trail == ';' ){
			
			if(target.length() == 1){
				return null;
			}
			
			target = target.substring(0, target.length() - 1);
		}
		
		else{
			break;
		}
		}
	
	
	for(int j = 0; j < target.length(); j++){
		char punct = target.charAt(j);
		if(!Character.isLetter(punct)){
			return null;
		}
	}
	
	if(noiseWords.containsValue(target)){
		return null;
	}
	
	return target;
	
	}
		
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		ArrayList<Integer> arr = new ArrayList<Integer>();
		
		int low = 0;
		int mid = 0;
		int high = occs.size() - 2;
		
		if(occs.size() == 1)
			return null;
		
		
		else{
			Occurrence occurr = occs.get(occs.size() - 1);
			while(low <= high){
				mid = (low+high) / 2;
				arr.add(mid);
				if(occs.get(mid).frequency == occurr.frequency)
					break;
				
				if(occurr.frequency > occs.get(mid).frequency)
					high = mid - 1;
				
				else{
					low = mid + 1;
					if(high <= mid)
						mid++;
					
				}
				
			}
			
			occs.add(mid, occurr);
			occs.remove(occs.size() - 1);
			return arr;
			
		}
	}
		

	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> arrstring = new ArrayList<String>();
		ArrayList<Occurrence> arroccurr1 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> arroccurr2 = new ArrayList<Occurrence>();
		
		if(keywordsIndex.containsKey(kw1)){
			for(int i = 0; i < 5; i++){
				if(i >= keywordsIndex.get(kw1).size())
					break;
					arroccurr1.add(keywordsIndex.get(kw1).get(i));
				}
			}
		
		if(keywordsIndex.containsKey(kw2)){
			for(int i = 0; i < 5; i++){
				if(i >= keywordsIndex.get(kw2).size())
					break;
					arroccurr2.add(keywordsIndex.get(kw2).get(i));
			}
		}
		
		if((arroccurr1.size() == 0) && (arroccurr2.size() == 0))
				return arrstring;
		int pointer1 = 0;
		int pointer2 = 0;
		
		while(true){
			boolean doc = true;
			boolean exists = false;
			
			if(arroccurr1.size() == 0){
				for(int i = 0; i < arrstring.size(); i++){
					if(arrstring.get(i).equals(arroccurr2.get(pointer2).document))
						exists = true;
						break;
				}
				
				if(exists == false)
					arrstring.add(arroccurr2.get(pointer2).document);
					pointer2++;
					
				if(pointer2 >= arroccurr2.size())
					break;
			}
			
			else if (arroccurr2.size() == 0){
				for(int i = 0; i < arrstring.size(); i++){
					if(arrstring.get(i).equals(arroccurr1.get(pointer1).document))
						exists = true;
						break;
				}
				
				if(exists == false)
					arrstring.add(arroccurr1.get(pointer1).document);
					pointer1++;
					if(pointer1 >= arroccurr1.size())
						break;
				}
				
				else{
					if(arroccurr1.get(pointer1).frequency < arroccurr2.get(pointer2).frequency){
						for(int i = 0; i < arrstring.size(); i++){
							if(arrstring.get(i).equals(arroccurr2.get(pointer2).document))
								exists = true;
								break;
						}
						
						doc = true;
						
					}
					
					else{
						for(int i = 0; i < arrstring.size(); i++){
							if(arrstring.get(i).equals(arroccurr1.get(pointer1).document))
								exists = true;
								break;
						}
						
						doc = false;
						
					}
					
					if(!exists && doc)
						arrstring.add(arroccurr2.get(pointer2).document);
					else if(!exists && !doc)
						arrstring.add(arroccurr1.get(pointer1).document);
					if(arroccurr1.get(pointer1).document.equals(arroccurr2.get(pointer2).document)){
						pointer1++;
						pointer2++;
					}
					else{
						if(doc)
							pointer2++;
						else if (!doc)
							pointer1++;
					}
					
					if(arrstring.size() == 5 || pointer1 >= arroccurr1.size() || pointer2 >= arroccurr2.size())
						break;
				}
			}
		
			
			if(arrstring.size() != 5 && (pointer1 < arroccurr1.size() || pointer2 < arroccurr2.size())){
				while(true){
					boolean exists = false;
					if(pointer2 < arroccurr2.size()){
						for(int i = 0; i < arrstring.size(); i++){
							if(arrstring.get(i).equals(arroccurr2.get(pointer2).document))
								exists = true;
								break;
						}
						
						if(!exists)
							arrstring.add(arroccurr2.get(pointer2).document);
							pointer2++;
						
						if(pointer2 == arroccurr2.size())
							break;
					}
					
					else if(pointer1 < arroccurr1.size()){
						for(int i = 0; i < arrstring.size(); i++){
							if(arrstring.get(i).equals(arroccurr1.get(pointer1).document))
								exists = true;
								break;
						}
						
						if(!exists)
							arrstring.add(arroccurr1.get(pointer1).document);
							pointer1++;
						
						if(pointer1 == arroccurr1.size())
							break;
				}
					
				if(arrstring.size() == 5)
					break;
			}
		}
			
			return arrstring;
	}
}
