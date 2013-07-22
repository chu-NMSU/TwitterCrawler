package chu.twitter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;


/**
 * Twitter user profile crawler
 * @author chu
 *
 */
@Deprecated
public class ProfileCrawler {
	/**
	 * twitter manipulation interface
	 */
	private static Twitter twitter = new TwitterFactory().getInstance();
	/**
	 * resource remaining access time map
	 */
	private static Map<String, Integer> resourceRemainMap = null;
	
	/**
	 * 
	 * @param assNum job assignment number
	 */
	public static void crawlProfile(int assNum){
		//assignment json object
		JSONObject assObj = Utility.readUserAssignmentObject();
		//corresponding assNum's user
		JSONArray arr = assObj.getJSONArray(String.valueOf(assNum));
		
		Set<String> users = new HashSet<String>();
		for(int i=0; i<arr.length(); i++)
			users.add(arr.getString(i));
		
		System.out.println("user set size "+users.size());
		
		for(String u : users)
			ProfileCrawler.getUserProfile(u);
	}
	/**
	 * crawl one user's profile
	 * @param screenName
	 */
	public static void getUserProfile(String screenName){
		try {
			if(screenName==null || screenName.length()==0)
				return ;
			File userJsonFile = new File(Constants.profileDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");

			if(userJsonFile.exists())
				return ;
			
			User u = twitter.showUser(screenName);
			ProfileCrawler.updateResRemMap("/users/show/:id", -1);
			
			//user friend list jason
			JSONObject userJson = new JSONObject();
			userJson.put("user_name", screenName);
			userJson.put("profile", u.getDescription());
			
			System.out.println(userJson+"\n"+resourceRemainMap.get("/users/show/:id"));
			
			Utility.writeUserProfileJsonObj(userJson);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			ProfileCrawler.updateResRemMap("/users/show/:id", -1);
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
			int resetTime = ProfileCrawler.getResetTime(res);
			try {
				System.out.println("resource "+res +" is exhausted.  Sleep "+resetTime+" seconds");
				Thread.sleep(resetTime*1000);
				ProfileCrawler.initResRemMap();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
			ProfileCrawler.updateResRemMap("/application/rate_limit_status", -1);
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
	
	public static void main(String[] args){
		if(args[0].equals("printRate"))
			ProfileCrawler.printRateLimit();
		else{
			ProfileCrawler.initResRemMap();
			Integer ass = Integer.parseInt(args[0]);
			ProfileCrawler.crawlProfile(ass);
		}
	}
}
