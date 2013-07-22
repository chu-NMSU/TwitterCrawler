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
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserList;

/**
 * crawler all the target user, begin from the root user I follow in public_star list.
 * @author chu
 *
 */
@Deprecated
public class UserCrawler {
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
	 * Get the user list of screenName user
	 * @param screenName the screenName of the user
	 * @return the JSONObject of that user.  {user_name:"screenName", }.  Return null if user already exists.
	 */
	public static JSONObject getUserListUser(String screenName){

		//user friend list jason
		JSONObject userJson = new JSONObject();
		
		//user tweets json
		JSONObject  userTweetJson = new JSONObject();
		JSONArray tweetArray = new JSONArray();
		
		try {
			if(screenName==null || screenName.length()==0)
				return null;
			File userJsonFile = new File(Constants.friendListDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
			File tweetJsonFile = new File(Constants.tweetDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");

			if(!userJsonFile.exists() || tweetJsonFile.exists())
				return null;
			
			//crawl user's friend list 
			ResponseList<UserList> lists = twitter.getUserLists(screenName);
			UserCrawler.updateResRemMap("/lists/list", -1);
			
			userJson.put("user_name", screenName);
			
			//all the list of user screenName
			for (UserList list : lists) {
				int listId = list.getId();
				
				JSONObject listJson = new JSONObject();
//				listJson.put("list_name", list.getName());
				
				//pageable list of list members
				PagableResponseList<User> listMemberList;
				long cursor = -1;
				//string of list members, NOTE: NO SPACE among the string
				String listMemberString = new String("");
	            do {
	                listMemberList = twitter.getUserListMembers(listId, cursor);
	                UserCrawler.updateResRemMap("/lists/members", -1);
//	                System.out.println("list nanme \t"+list.getName());
//	                System.out.println("remain list/members \t"+resourceRemainMap.get("/lists/members"));
//	                System.out.println("list size \t"+listMemberList.size());
	                
	                for (User following : listMemberList) {
//	                    System.out.println("@" + following.getScreenName());
	                	listMemberString+=following.getScreenName()+",";
	                	following.getDescription();
	                }
	                
	                System.out.println("cursor num "+cursor);
	            } while ((cursor = listMemberList.getNextCursor()) != 0);//get all the user in the list
//	            System.out.println("root user list "+listMembers);
	            listJson.put("list_id", listId);
	            listJson.put("list_name", list.getName());
	            listJson.put("list_member", listMemberString);
	            userJson.put("list "+list.getName(), listJson);
//	            System.out.println(Debugger.getCallerPosition()+"\n"+listJson);
			}
//			UserCrawler.printRateLimit();
			System.out.println(Debugger.getCallerPosition()+"\n"+userJson);
			Utility.writeUserFriendListJsonObj(userJson);
			
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return userJson;
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
			UserCrawler.updateResRemMap("/application/rate_limit_status", -1);
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
			int resetTime = UserCrawler.getResetTime(res);
			try {
				System.out.println("resource "+res +" is exhausted.  Sleep "+resetTime+" seconds");
				Thread.sleep(resetTime*1000);
				UserCrawler.initResRemMap();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * start from chuanHu, get all following users.
	 */
	public static void getAllRootUser(){
			JSONObject chuJson = Utility.readUserFriendListJsonObj("chuanHu");
//			System.out.println(chuJson);
			
			JSONObject listJson = chuJson.getJSONObject("list public star");
			String[] members = listJson.getString("list_member").split(",");
			for(String member : members)
				UserCrawler.getUserListUser(member);
	}
	/**
	 * get user's following based on current users.
	 */
	public static void getMoreUser(int assNum){
//		JobAssignment.assignUser(Constants.appNum);
		
		//assignment json object
		JSONObject assObj = Utility.readUserAssignmentObject();
		//corresponding assNum's user
		JSONArray arr = assObj.getJSONArray(String.valueOf(assNum));
		
		//set of users
		Set<String> users = new HashSet<String>();
		for(int i=0; i<arr.length(); i++)
			users.add(arr.getString(i));
		
		for(String u : users){
			JSONObject userJson = Utility.readUserFriendListJsonObj(u);
			System.out.println("finished user count "+count++);
			//key value set of this user
			Set<String> keySet = userJson.keySet();
			for(String key : keySet){
				//if the key is following list
				if(key.startsWith("list")){
					JSONObject listJson = (JSONObject)userJson.getJSONObject(key);
					String[] members = listJson.getString("list_member").split(",");
					for(String member : members)
						UserCrawler.getUserListUser(member);
				}
			}
		}
	}
	
	public static void main(String args[]){
//		UserCrawler.getUserListUser("chuanHu");
		
//		UserCrawler.initResRemMap();
//		UserCrawler.getUserListUser("Reuters");
//		UserCrawler.getAllRootUser();
//		UserCrawler.getMoreUser(ass);
		
		if(args[0].equals("printRate"))
			UserCrawler.printRateLimit();
		else{
			UserCrawler.initResRemMap();
			Integer ass = Integer.parseInt(args[0]);
			UserCrawler.getMoreUser(ass);
		}
		
		UserCrawler.printRateLimit();
		
	}
	
}
