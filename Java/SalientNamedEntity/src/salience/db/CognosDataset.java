package salience.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import salience.AppGlobals;
import utility.FileLoader;
import utility.WebUtility;

public class CognosDataset {
	public URL url = null;
	public URLConnection urlConnection = null;
	
	
	public CognosDataset(){
//		System.getProperties().put("proxySet", true);
//		System.getProperties().put("proxyHost", "proxy.iiit.ac.in");
//		System.getProperties().put("proxyPort", "8080");
//		System.getProperties().put("nonProxyHosts",  "localhost|10.2.4.192|10.2.4.210");
	}	
	
 	public static void main(String args[]){
		CognosDataset cognosDataset = new CognosDataset();
		//System.out.println("Expert count = "+ cognosDataset.getExperts("miobi").size());
		FileLoader fileLoader = new FileLoader();
		ArrayList<String> topics = fileLoader.loadCognosTopics();
		System.out.println("Topics size = "+topics.size());
		for(String topic : topics){ System.out.println("Current topic : "+topic);
			HashSet<String> experts = cognosDataset.getExperts(topic); System.out.println("Current topic experts = "+experts.size());
			if(experts.size()>0){
				if(fileLoader.storeCognosExpert(topic, experts) == false){
					System.out.println("Failed to store experts for the topic "+topic);
				}
			} else{
				System.out.println("NO EXPERT : "+topic);
			}
		}//for topic
		
	}//End main
 	
 	public HashSet<String> getExperts(String topic){
 		HashSet<String> expertList = new HashSet<String>();
 		BufferedReader in  = null;
 		String inputLine, expertName, pageNum, data = "";
 		Pattern pUser, pNextPage;
 		ArrayList<Integer> pendingPages = new ArrayList<Integer>();
 		
 		try {
		    //first page
 			Thread.currentThread().sleep(3000);
 			/*
			url = new URL("http://twitter-app.mpi-sws.org/whom-to-follow/users.php?q="+topic+"&l=Worldwide");
			urlConnection = url.openConnection();
			urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
			in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			while((inputLine = in.readLine()) != null ){
				data += inputLine;
			}
			in.close();
			*/
 			
 			String url="http://twitter-app.mpi-sws.org/whom-to-follow/users.php?q="+topic+"&l=Worldwide";
 			data=WebUtility.makeGetCall(url, AppGlobals.HTTP_PROXY);
			
			if(!data.isEmpty()){ //System.out.println(data);
				expertName = null; pageNum = null;
				pUser = Pattern.compile("<a href=\"http:\\/\\/twitter-app.mpi-sws.org\\/who-is-who\\/users.php\\?id=([a-zA-Z0-9])+\"");
				pNextPage = Pattern.compile("<a href=\"users.php\\?p=([0-9])\">");
				
				Matcher m1 = pUser.matcher(data);
				while(m1.find()){
					expertName = m1.group().substring(64, m1.group().length()-1); //System.out.println("Expert "+expertName);
					expertList.add(expertName);
				}
				
				Matcher m2 = pNextPage.matcher(data);
				while(m2.find()){
					pageNum = m2.group().substring(21, m2.group().length()-2); //System.out.println("Pages "+pageNum);
					pendingPages.add(Integer.valueOf(pageNum));
				}
				
			} else {
				System.out.println("no expert found");
			}

			//successive pages
 			Thread.currentThread().sleep(3000);
			for(Integer pgNum : pendingPages){
				url = "http://twitter-app.mpi-sws.org/whom-to-follow/users.php?q="+topic+"&l=Worldwide&p="+pgNum ;
				data = null;//reset
				data = WebUtility.makeGetCall(url, AppGlobals.HTTP_PROXY);
				/*urlConnection = url.openConnection();
				urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
				in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				data = null;//reset
				while((inputLine = in.readLine()) != null ){
					data += inputLine;
				}
				in.close();
				*/
				if(!data.isEmpty()){ //System.out.println(data);
					expertName = null;
					pUser = Pattern.compile("<a href=\"http:\\/\\/twitter-app.mpi-sws.org\\/who-is-who\\/users.php\\?id=([a-zA-Z0-9])+\"");
					Matcher m1 = pUser.matcher(data);
					while(m1.find()){
						expertName = m1.group().substring(64, m1.group().length()-1); //System.out.println("Expert2 "+expertName);
						expertList.add(expertName);
					}				
				} else {
					System.out.println("no expert found in successive pages");
				}
			}//for pgNum
			
		} catch (Exception e) {
			e.printStackTrace();
		}
 		
 		return expertList;
 	}//End getExperts
 	
}//End class
