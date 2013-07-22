package chu.twitter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
	
@Deprecated
public class TweetCrawler {
	/**
	 * twitter manipulation interface
	 */
	private static Twitter twitter = new TwitterFactory().getInstance();
	/**
	 * resource remaining access time map
	 */
	private static Map<String, Integer> resourceRemainMap = null;
	
	private static long count = 0;
	
	/**
	 * Get the user time line top 200 tweets.
	 * @param screenName screen Name of the target user.
	 * @return
	 */
	public static JSONObject getUserTweets(String screenName){
		JSONObject  userTweetJson = new JSONObject();
		JSONArray tweetArray = new JSONArray();
		
		File userJsonFile = new File(Constants.friendListDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		File tweetJsonFile = new File(Constants.tweetDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		
		if(!userJsonFile.exists() || tweetJsonFile.exists())
			return null;
		
		userTweetJson.put("user_name", screenName);
		
		List<Status> statuses = null;
		
		int page = 1;
		Paging p = new Paging(page);
		
		try{
			statuses = twitter.getUserTimeline(screenName, p);
			TweetCrawler.updateResRemMap("/statuses/user_timeline", -1);
		}
		catch(TwitterException e){
			e.printStackTrace();
		}
		
		TweetCrawler.updateResRemMap("/statuses/user_timeline", -1);
		//for each page
		//now we crawl all data for the user...
		while(statuses!=null && statuses.size()!=0&&page<11){
			//for each status
			for(Status s : statuses){
				String tweet = s.getText();
				String aspect = null;
				//if this status is retweeted
				if(s.isRetweet()){
					//retweet from friend
					String friendName = s.getRetweetedStatus().getUser().getScreenName();
					//in which list the friend is
					String listName = Utility.getUserListName(screenName, friendName);
					aspect = listName;
				}
				//if this status is replying to a friend
				if(s.getInReplyToScreenName()!=null){
					String friendName = s.getInReplyToScreenName();
					String listName = Utility.getUserListName(screenName, friendName);
					aspect = listName;
					
//					try{
//
//						long replyTo = s.getInReplyToStatusId();
//						String replyToName = s.getInReplyToScreenName();
//						System.out.println(s.getInReplyToScreenName()+" - "+replyTo);
//						User u = twitter.showUser(replyToName);
//						TweetCrawler.updateResRemMap("/users/show/:id", -1);
//
//						if(!u.isProtected()&&replyTo!=-1){
//							Status replyToStatus = twitter.showStatus(replyTo);
//							TweetCrawler.updateResRemMap("/statuses/show/:id", -1);
//
//							tweet+=replyToStatus.getText();
//							//reply to friend
//							String friendName = replyToStatus.getUser().getScreenName();
//							//in which list the friend is
//							String listName = Utility.getUserListName(screenName, friendName);
//							aspect = listName;
//						}
//					}
//					catch(TwitterException e){
//						e.printStackTrace();
//					}
				}
				
				JSONObject tweetJson = new JSONObject();
				tweetJson.put("tweet", tweet);
				//if this tweet is not retweet or replying, it's self created.
				tweetJson.put("aspect", aspect==null?"self_created":aspect);
				tweetJson.put("date", s.getCreatedAt());
				tweetArray.put(tweetJson);
			}
			
			//increase page
			p = new Paging(++page);
			
			try{
				statuses = twitter.getUserTimeline(screenName, p);
				TweetCrawler.updateResRemMap("/statuses/user_timeline", -1);
			}
			catch(TwitterException e){
				e.printStackTrace();
			}
			
		}

		userTweetJson.put("user_timeline", tweetArray);
		System.out.println(userTweetJson);

		Utility.writeUserTweetJsonObj(userTweetJson);
		
		return userTweetJson;
	}
	
	/**
	 * check the API usage.  
	 * @param res the exhausted resource name
	 * @return If some method used exhausted, return maximum reset time; else return -1.
	 */
	private static int getResetTime(String res){
		Map<String, RateLimitStatus> status;
		int resetTime = 60*15;//default sleep 15 minutes
		try {
			status = twitter.getRateLimitStatus();
			TweetCrawler.updateResRemMap("/application/rate_limit_status", -1);
			RateLimitStatus sta = status.get(res);
			
			//since check the rate_limit_status also has rate limits.  So we check it here.
			RateLimitStatus rateSta = status.get("/application/rate_limit_status");			
			
			if(rateSta.getRemaining()<=1)
				resetTime = Math.max(sta.getSecondsUntilReset(), rateSta.getSecondsUntilReset());
			else
				resetTime = sta.getSecondsUntilReset();
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("resource /application/rate_limit_status is exhausted.  Sleep "+resetTime+" seconds");
			return resetTime;
		}
		//sleep 10 more seconds
		return resetTime+10;
	}
	/**
	 * check and print API usage rate limit
	 */
	public static void printRateLimit(){
		Map<String, RateLimitStatus> status;
		try {
			status = twitter.getRateLimitStatus();
			
			Set<Map.Entry<String, RateLimitStatus>> entries = status.entrySet();
			Iterator<Map.Entry<String, RateLimitStatus>> ite = entries.iterator();
			ArrayList<Map.Entry<String, RateLimitStatus>> arr = new ArrayList<Map.Entry<String, RateLimitStatus>>();
			while(ite.hasNext())
				arr.add(ite.next());
			
			//sort resource name in lexi order
			Collections.sort(arr, new Comparator<Map.Entry<String, RateLimitStatus>>(){
				@Override
				public int compare(Entry<String, RateLimitStatus> o1,
						Entry<String, RateLimitStatus> o2) {
					// TODO Auto-generated method stub
					return o1.getKey().compareTo(o2.getKey());
				}
			});
			
			for(Map.Entry<String, RateLimitStatus> entry : arr){
				RateLimitStatus sta = entry.getValue();
				String resource = entry.getKey();
				
				System.out.println("resource "+resource+" \t"+sta.getRemaining()+"/"+sta.getLimit()+" reset time "+sta.getSecondsUntilReset());
			}
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * initial the ResourceRemainingMap
	 */
	private static void initResRemMap(){
		
		if(resourceRemainMap==null)
			resourceRemainMap = new HashMap<String, Integer>();
		else
			resourceRemainMap.clear();
		
		Map<String, RateLimitStatus> status;
		try {
			status = twitter.getRateLimitStatus();
			
			for(Map.Entry<String, RateLimitStatus> entry : status.entrySet()){
				RateLimitStatus sta = entry.getValue();
				String resource = entry.getKey();
				int remain = sta.getRemaining();
				resourceRemainMap.put(resource, remain);
			}
			
			updateResRemMap("/application/rate_limit_status", -1);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * update ResourceRemaining count map
	 * @param res the name of the resource
	 * @param increment the number to decrement from initial count
	 */
	public static void updateResRemMap(String res, int increment){
		int count = resourceRemainMap.get(res);
		count+=increment;
		resourceRemainMap.put(res, count);
		if(count<=1){
			int resetTime = TweetCrawler.getResetTime(res);
			try {
				System.out.println("resource "+res +" is exhausted.  Sleep "+resetTime+" seconds");
				Thread.sleep(resetTime*1000);
				TweetCrawler.initResRemMap();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * get user's following based on current users.
	 */
	public static void getMoreUser(int assNum){
//		JobAssignment.assignUser(Constants.appNum);
		
		//assignment json object
		JSONObject assObj = Utility.readTweetAssignmentObject();
		//corresponding assNum's user
		JSONArray arr = assObj.getJSONArray(String.valueOf(assNum));
		
		//set of users
		Set<String> users = new HashSet<String>();
		for(int i=0; i<arr.length(); i++)
			users.add(arr.getString(i));
		
		for(String u : users){
			TweetCrawler.getUserTweets(u);
			System.out.println("finished user count " + count++);
		}
	}
	
	public static void main(String[] args){
		TweetCrawler.initResRemMap();
		
		if(args[0].equals("printRate"))
			TweetCrawler.printRateLimit();
		else{
			TweetCrawler.initResRemMap();
			Integer ass = Integer.parseInt(args[0]);
			TweetCrawler.getMoreUser(ass);
		}
		
//		TweetCrawler.getUserTweets("nparmalee");
//		TweetCrawler.getMoreUser(1);
//		System.out.println("/:");
	}
	
}
