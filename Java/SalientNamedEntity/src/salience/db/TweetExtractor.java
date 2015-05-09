package salience.db;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;











import edu.berkeley.nlp.util.Logger;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import utility.*;
import salience.MentionDetector;
import salience.kb.WikipediaSearch;
import salience.ner.AlanRitter;

public class TweetExtractor {
	
	private static Twitter twitter=TwitterManager.getTwitterInstance();
	
	public static void main(final String[] args) throws Exception{
		//System.out.println(isEnglishTweet("187281022927384577"));
		scale(args[0]);
	}//main
	public static void scale(String repLabQuery ) throws Exception{
		
		FileLoader fileLoader = new FileLoader();
		TweetIndexer tweetIndexer = new TweetIndexer();
		HashSet<String> NEbyExperts = new HashSet<String>();//NEs from expert chat.
		HashSet<String> equivalentCognosTopics = fileLoader.getCognosTopic(repLabQuery);
		//equivalentCognosTopics.remove("ikebukuro");
		/*ArrayList<String> equivalentCognosTopics = fileLoader.getCognosTopicsForReplab("automotive",2);//"banking",1);//
		equivalentCognosTopics.remove("automotive"); equivalentCognosTopics.remove("automotriz"); equivalentCognosTopics.remove("boyband"); 
		equivalentCognosTopics.remove("car dealer"); equivalentCognosTopics.remove("client");equivalentCognosTopics.remove("mazda");
		equivalentCognosTopics.remove("motorbikes"); equivalentCognosTopics.remove("musictech"); equivalentCognosTopics.remove("otomotif");
		equivalentCognosTopics.remove("penske"); equivalentCognosTopics.remove("porsche"); equivalentCognosTopics.remove("sponsor");
		equivalentCognosTopics.remove("fuel");equivalentCognosTopics.remove("marca");equivalentCognosTopics.remove("lbc");
		equivalentCognosTopics.remove("manufacturers"); equivalentCognosTopics.remove("automobilismo");
		equivalentCognosTopics.remove("vehicle");equivalentCognosTopics.remove("kuis");		
		academia 
		banking
		equivalentCognosTopics.remove("ikebukuro"); equivalentCognosTopics.remove("mit sloan"); equivalentCognosTopics.remove("nudist"); equivalentCognosTopics.remove("pistons");
		equivalentCognosTopics.remove("academias"); equivalentCognosTopics.remove("college"); equivalentCognosTopics.remove("dcuo"); equivalentCognosTopics.remove("epi");
		equivalentCognosTopics.remove("fiksiminiers"); equivalentCognosTopics.remove("familiar"); equivalentCognosTopics.remove("patent");
		equivalentCognosTopics.remove("cricket"); equivalentCognosTopics.remove("equipos"); equivalentCognosTopics.remove("feedback");
		equivalentCognosTopics.remove("ke chiefs"); equivalentCognosTopics.remove("kora"); equivalentCognosTopics.remove("el salvador");
		*/
		
		//System.out.println("Eq cognos topics = "+equivalentCognosTopics.size());
		for(String topic : equivalentCognosTopics){//for topic
		//String topic=repLabQuery.toLowerCase();
			System.out.println("Current topic = "+topic);
			ArrayList<String> expertUser = fileLoader.loadCognosExperts(topic);
			System.out.println("Expert count = "+expertUser.size());
			
			for(String user : expertUser){//expertUser
				if(tweetIndexer.getTweetCountByExpert(user)==0){
					HashMap<Long, String> expertTweets = collectExpertTweets(user);
					NEbyExperts.clear();//reset
					int tweetWithoutNE_AR = 0;
					for(Long t:expertTweets.keySet()){
						String tweet = expertTweets.get(t);
						if(tweet!=null){
				 			Thread.currentThread().sleep(500);//100ms for AWS cloud, 500ms otherwise			 			
							List<String> mentionList = MentionDetector.tokenizeMax(tweet);
							if(!mentionList.isEmpty()){
								ArrayList<String> neList = (ArrayList)mentionList;							
								if(neList.size()>0){
									NEbyExperts.addAll(neList);
									System.out.println(tweet +" --> "+neList);
									//Indexing t,tweet,topic,user,neList
									tweetIndexer.indexDoc(t, tweet, topic, user, neList);
								}
							} else {
								++tweetWithoutNE_AR;
							}
						} else {
							System.out.println("Empty tweet");
						}
					}//for t
					StringBuilder status = new StringBuilder().append("topic,").append(topic).append(",user,").append(user).append(",blank,").append(tweetWithoutNE_AR).append(",Valid,").append(expertTweets.size()).append(",NEbyexperts,").append(NEbyExperts.size()).append("\n");				
					utility.Logger.StatusOut(status);
				}//if new user
		    }//for expert
		}//for topic
	}//scale
	
	public static void query(final String queryStr,final PrintWriter writer) throws Exception{
		Query query = new Query(queryStr+" -filter:retweets").lang("en"); //+exclude:retweets
	    QueryResult result=null;
	    int count=0;
	    do{
	    	result= twitter.search(query);
		    for (Status status : result.getTweets()) {
		        //System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
		        MediaEntity[] media = status.getMediaEntities(); //get the media entities from the status
		        
		        boolean isJpg=false;
		        for(MediaEntity m : media){ //search trough your entities
		        	if(m.getMediaURL().contains("jpg")){
		        		if(!isJpg) {
		        			writer.print("<tr><td>"+queryStr+"</td><td>"+status.getId()+"</td><td>"+status.getText()+"</td>");
		        		}
		        		isJpg=true;
		        		writer.print("<td><img src='"+m.getMediaURL()+"' width=500 height=500/></td>");
		        	}
		            //System.out.println(m.getMediaURL()); //get your url!
		        }
		        if(isJpg) {
		        	writer.print("</tr>");
			        ++count;
		        }
		    }
	        if(count>10) break;
	        System.out.println(count);

	    } while((query = result.nextQuery()) != null);
	}
	

	public static HashMap<Long ,String> collectExpertTweets(String expertUser) throws InterruptedException{
		HashMap<Long, String> tweetCollection = new HashMap<Long, String>();
		int pgNum = 1;//start page
		//StringBuilder sb = new StringBuilder();
		while(pgNum > 0){
			Paging paging = new Paging(pgNum , 200);
			try{
				List<Status> statuses = twitter.getUserTimeline(expertUser,paging);
				if(statuses.size()>0){
					System.out.println(statuses.size() +" tweets collected for "+expertUser+" Page "+pgNum);
					for(Status status : statuses){
						if(status.getLang().equalsIgnoreCase("en")){
							tweetCollection.put(status.getId(),status.getText());
							//	sb.append(status.getText()); //System.out.println(status.getText());
						}
					}//for status
					++pgNum;
				} else {
					pgNum = 0;
				}
				
			} catch (TwitterException te){
				te.printStackTrace();
				System.out.println("Twitter Exception :: User ="+expertUser+" Page "+pgNum);
				pgNum = 0;
			}
			Thread.currentThread().sleep(8000);
		}//while
		System.out.println(" Expert Tweet collection size = "+tweetCollection.size());
		return tweetCollection;
	}//collectExpertTweets()
	
	public static boolean isEnglishTweet(String tweetId) throws InterruptedException{
		HashMap<Long, String> tweetCollection = new HashMap<Long, String>();
		//StringBuilder sb = new StringBuilder();
			try{
				
				Status status = twitter.showStatus(Long.parseLong(tweetId));
		        if (status != null) { // 
		            if(status.getLang().equalsIgnoreCase("en")){
		            	return true;
		            }		                     
		        }
				
			} catch (TwitterException te){
				te.printStackTrace();
				System.out.println("Twitter Exception ");
			}
		return false;
	}//isEnglishTweet
}//class