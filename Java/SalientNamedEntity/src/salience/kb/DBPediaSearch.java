package salience.kb;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
//import org.yaml.snakeyaml.util.UriEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import Variables.Variables;

import com.google.common.collect.ComputationException;

import salience.AppGlobals;
import salience.db.DbpediaIndex;
import utility.WebUtility;

public class DBPediaSearch {

        public HashMap<String, Double> search(String keyword, String tweet, int kbCutoff) {
            HashMap<String, Double> wikiTitleAbstractMap = new HashMap<String, Double>();
            DbpediaIndex dbpediaIndex = new DbpediaIndex();
            /*
             * Searches the keyword using the dbpedia sparql rest endpoint.
             */
            // Hit the dbpedia server.
            try{
	            final BufferedReader br = new BufferedReader(new FileReader(Variables.LibDir.concat(Variables.dbpediaStructure)));
	            final String query = WebUtility.readFromStream(br).replaceAll("\\$", keyword);
	            //System.out.println(query);System.out.println(URLEncoder.encode(query));
	            final String content = WebUtility.makeGetCall(
	                  "http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query="
	                          + URLEncoder.encode(query), AppGlobals.HTTP_PROXY);
//	            final String content = WebUtility.makeGetCall(
//	                    "http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query="
//	                            + URLEncoder.encode(query), null);
	            
	            // Process the output.
	            if (content == null)
	                return null;
	            final org.jsoup.nodes.Document document = Jsoup.parse(content);
	            List<String> resList = new ArrayList<String>();
	            int count = 0;
	            for (final Element tr : document.select("tr")) {
	                String wiki_title = null, wiki_abstract = null;
	                if (count != 0 && count <= kbCutoff) {
	                    Element elmt = tr.select("td").iterator().next();
	
	                    if(elmt!=null){
	                        wiki_title = elmt.text().replace("http://dbpedia.org/resource/", "");
	                        resList.add(elmt.text());
	                        String webStr = utility.WebUtility.makeGetCall(elmt.text(), AppGlobals.HTTP_PROXY);
	                        //String webStr = utility.WebUtility.makeGetCall(elmt.text(), null);
	                        if(webStr!=null){
	                            Document doc=Jsoup.parse(webStr); //System.out.println(doc);
	                            for (Element table: doc.select("span")){
	                                if(table.attr("property").equals("dbpedia-owl:abstract")){
	                                    wiki_abstract =  table.text().toString();
	                                    if(!dbpediaIndex.isTitle(wiki_title)){
	                                    	dbpediaIndex.indexDoc(wiki_title, wiki_abstract);
	                                    }
	                                    wikiTitleAbstractMap.put(wiki_title, computeScore(keyword,wiki_abstract));
	                                    break;
	                                }
	                            }
	                        }//if
	                    }
	                }
	                
	                ++count;
	            }//for
            } catch(IOException ioe){
            	ioe.printStackTrace();
            }

            return wikiTitleAbstractMap;
        }
        
        /*Jaccard similarity between ne and query string*/
		static Double computeScore(String entity,String document){
			entity  = entity.toLowerCase();
			document = document.toLowerCase();
			entity = entity.replaceAll("_", " ");
			document = document.replaceAll("_", " ");
			String[] entityWords = entity.split(" ");
			String[] docWords = document.split(" ");
			HashSet<String> entityhs = new HashSet<>();
			HashSet<String> dochs = new HashSet<>();
			HashSet<String> union = new HashSet<>();
			for(String i:entityWords){
				entityhs.add(i);
				union.add(i);
			}
			for(String i:docWords){
				dochs.add(i);
				union.add(i);
			}
			entityhs.retainAll(dochs);
			Double score = entityhs.size()*1.0/union.size();
			//System.out.println(entity+" "+ document+" "+score);
			return score;
		}
		
		public HashMap<String, Double> entriesSortedByValues(Map<String,Double> map) {
			if(map==null){
				return null;
			}
			List<Entry<String,Double>> sortedEntries = new ArrayList<Entry<String,Double>>(map.entrySet());
			
			Collections.sort(sortedEntries, new Comparator<Entry<String,Double>>() {
			            @Override
			            public int compare(Entry<String,Double> e1, Entry<String,Double> e2) {
			                return e2.getValue().compareTo(e1.getValue());
			            }
			        }
			);
			
			HashMap<String, Double> result = new LinkedHashMap<String, Double>();
		    for (Iterator<Entry<String, Double>> it = sortedEntries.iterator(); it.hasNext();) {
		        Map.Entry<String, Double> entry = it.next();
		        result.put(entry.getKey(), entry.getValue());
		    }

		    return result;

		}


        public static void main(final String[] argv) throws Exception {
        	DBPediaSearch dbPediaSearch = new DBPediaSearch();
        	 HashMap<String, Double> dbpResults = dbPediaSearch.entriesSortedByValues(dbPediaSearch.search("India", "India is my country",2));
        	 System.out.println(dbpResults);
        }
    
}//class
