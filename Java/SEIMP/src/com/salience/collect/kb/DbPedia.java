package com.salience.collect.kb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.yaml.snakeyaml.util.UriEncoder;

import com.salience.commons.AppGlobals;
import com.salience.util.Utilities;

public class DbPedia {

	public static List<String> searchInDbPediaRest(final String keyword)
			throws IOException {
		/*
		 * Searches the keyword using the dbpedia sparql rest endpoint.
		 */
		// Hit the dbpedia server.
		final String query = Utilities.readFromStream(
				"data/kb/dbpediaSparqlQuery").replaceAll("\\$", keyword);
		final String content = Utilities.makeGetCall(
				"http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query="
						+ UriEncoder.encode(query), AppGlobals.HTTP_PROXY);

		// Process the output.
		if (content == null)
			return null;
		final org.jsoup.nodes.Document document = Jsoup.parse(content);
		final List<String> resList = new ArrayList<String>();
		int count = 0;
		for (final Element tr : document.select("tr")) {
			if (count != 0) {
				Element elmt = tr.select("td").iterator().next();
				//System.out.println(elmt != null ? elmt.text() : "");
				if(elmt!=null){
					resList.add(elmt.text());
				}
			}
			++count;
		}

		return resList;
	}

	public static void main(final String[] argv) throws Exception {
		System.out.println(searchInDbPediaRest("India"));
	}

}
