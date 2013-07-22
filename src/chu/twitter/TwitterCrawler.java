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
 * Whole twitter crawler
 * @author chu
 *
 */
public class TwitterCrawler {
	/**
	 * twitter manipulation interface
	 */
	public static Twitter twitter = new TwitterFactory().getInstance();
	/**
	 * resource remaining access time map
	 */
	public static Map<String, Integer> resourceRemainMap = null;
	
	/**
	 * check the API usage.  
	 * @param res the exhausted resource name
	 * @return If some method used exhausted, return maximum reset time; else return -1.
	 */
	private static int getResetTime(String res){
		Map<String, RateLimitStatus> status;
		int resetTime = 60*15;//default sleep 15 minutes
		try {
			updateResRemMap("/application/rate_limit_status", -1);
			status = twitter.getRateLimitStatus();
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
//		else
//			resourceRemainMap.clear();
		
		while (true){
			try {
				Map<String, RateLimitStatus> status = twitter.getRateLimitStatus();

				for(Map.Entry<String, RateLimitStatus> entry : status.entrySet()){
					RateLimitStatus sta = entry.getValue();
					String resource = entry.getKey();
					int remain = sta.getRemaining();
					resourceRemainMap.put(resource, remain);
				}
				
				updateResRemMap("/application/rate_limit_status", -1);
				break;
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				System.out.println("Init Resource Rate Map Exception");
				e.printStackTrace();
				try {
					Thread.sleep(1000*900);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
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
		if(count<1){
			int resetTime = getResetTime(res);
			try {
				System.out.println("resource "+res +" is exhausted.  Sleep "+resetTime+" seconds");
//				Thread.currentThread().
				Thread.sleep(resetTime*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					Thread.sleep(900*1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			finally{
				initResRemMap();
			}
		}
	}
	
//	/**
//	 * Get the user list of screenName user
//	 * @param screenName the screenName of the user
//	 * @return the JSONObject of that user.  {user_name:"screenName", }.  Return null if user already exists.
//	 */
//	public static JSONObject crawlUserList(String screenName){
//		//user friend list jason
//		JSONObject userJson = null;
//		
//		try {
//			if(screenName==null || screenName.length()==0)
//				return null;
//			File userJsonFile = new File(Constants.friendDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
//			
//			if(userJsonFile.exists())
//				return null;
//			
//			userJson = new JSONObject();
//			
//			//crawl user's friend list 
//			ResponseList<UserList> lists = twitter.getUserLists(screenName);
//			updateResRemMap("/lists/list", -1);
//			
//			userJson.put("user_name", screenName);
//			
//			//all the list of user screenName
//			for (UserList list : lists) {
//				int listId = list.getId();
//				
//				JSONObject listJson = new JSONObject();
////				listJson.put("list_name", list.getName());
//				
//				//pageable list of list members
//				PagableResponseList<User> listMemberList;
//				long cursor = -1;
//				//string of list members, NOTE: NO SPACE among the string
//				String listMemberString = new String("");
//	            do {
//	                listMemberList = twitter.getUserListMembers(listId, cursor);
//	                updateResRemMap("/lists/members", -1);
////	                System.out.println("list nanme \t"+list.getName());
////	                System.out.println("remain list/members \t"+resourceRemainMap.get("/lists/members"));
////	                System.out.println("list size \t"+listMemberList.size());
//	                
//	                for (User following : listMemberList) {
////	                    System.out.println("@" + following.getScreenName());
//	                	listMemberString+=following.getScreenName()+",";
//	                	following.getDescription();
//	                }
//	                
//	                System.out.println("cursor num "+cursor);
//	                
//	                
//	            } while ((cursor = listMemberList.getNextCursor()) != 0);//get all the user in the list
////	            System.out.println("root user list "+listMembers);
//	            listJson.put("list_id", listId);
//	            listJson.put("list_name", list.getName());
//	            listJson.put("list_member", listMemberString);
//	            userJson.put("list "+list.getName(), listJson);
////	            System.out.println(Debugger.getCallerPosition()+"\n"+listJson);
//			}
////			End crawl user following list
//			
//			//get user following member
//			twitter.getFriendsList(screenName, 0);
//			updateResRemMap("friends/list", -1);
//			
//			//get user follower member
//			twitter.getFollowersList(screenName, 0);
//			updateResRemMap("followers/list", -1);
//			
//			
//
//			System.out.println(Debugger.getCallerPosition()+"\n"+userJson);
//			
//		} catch (TwitterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return userJson;
//		} 
//		return userJson;
//	}
//	
//	/**
//	 * Get the user time line top 200 tweets.
//	 * @param screenName screen Name of the target user.
//	 * @return
//	 */
//	public static JSONObject crawlUserTweets(String screenName){
//		JSONObject  userTweetJson = new JSONObject();
//		JSONArray tweetArray = new JSONArray();
//		
//		File userJsonFile = new File(Constants.friendDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
//		File tweetJsonFile = new File(Constants.tweetDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
//		
//		if(!userJsonFile.exists() || tweetJsonFile.exists())
//			return null;
//		
//		userTweetJson.put("user_name", screenName);
//		
//		List<Status> statuses = null;
//		
//		int page = 1;
//		Paging p = new Paging(page);
//		
//		try{
//			statuses = twitter.getUserTimeline(screenName, p);
//			updateResRemMap("/statuses/user_timeline", -1);
//		}
//		catch(TwitterException e){
//			e.printStackTrace();
//		}
//		
//		updateResRemMap("/statuses/user_timeline", -1);
//		//for each page
//		//now we crawl all data for the user...
//		while(statuses!=null && statuses.size()!=0&&page<11){
//			//for each status
//			for(Status s : statuses){
//				String tweet = s.getText();
//				String aspect = null;
//				//if this status is retweeted
//				if(s.isRetweet()){
//					//retweet from friend
//					String friendName = s.getRetweetedStatus().getUser().getScreenName();
//					//in which list the friend is
//					String listName = Utility.getUserListName(screenName, friendName);
//					aspect = listName;
//				}
//				//if this status is replying to a friend
//				if(s.getInReplyToScreenName()!=null){
//					String friendName = s.getInReplyToScreenName();
//					String listName = Utility.getUserListName(screenName, friendName);
//					aspect = listName;
//				}
//				
//				JSONObject tweetJson = new JSONObject();
//				tweetJson.put("tweet", tweet);
//				//if this tweet is not retweet or replying, it's self created.
//				tweetJson.put("aspect", aspect==null?"self_created":aspect);
//				tweetJson.put("date", s.getCreatedAt());
//				tweetArray.put(tweetJson);
//			}
//			
//			//increase page
//			p = new Paging(++page);
//			
//			try{
//				statuses = twitter.getUserTimeline(screenName, p);
//				updateResRemMap("/statuses/user_timeline", -1);
//			}
//			catch(TwitterException e){
//				e.printStackTrace();
//			}
//			
//		}
//
//		userTweetJson.put("user_timeline", tweetArray);
//		System.out.println(userTweetJson);
//
//		
//		return userTweetJson;
//	}
//	/**
//	 * crawl one user's profile
//	 * @param screenName
//	 */
//	public static JSONObject crawlUserProfile(String screenName){
//		try {
//			if(screenName==null || screenName.length()==0)
//				return null;
//			File userJsonFile = new File(Constants.profileDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
//
//			if(userJsonFile.exists())
//				return null;
//			
//			User u = twitter.showUser(screenName);
//			updateResRemMap("/users/show/:id", -1);
//			
//			//user friend list jason
//			JSONObject userProfileJson = new JSONObject();
//			userProfileJson.put("user_name", screenName);
//			userProfileJson.put("profile", u.getDescription());
//			
//			System.out.println(Debugger.getCallerPosition()+": "+userProfileJson+"\n"+resourceRemainMap.get("/users/show/:id"));
//			
//			return userProfileJson;
//			
//		} catch (TwitterException e) {
//			// TODO Auto-generated catch block
//			updateResRemMap("/users/show/:id", -1);
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	/**
	 * crawl more user's friend list, tweets, profile
	 */
	public static void runJobAssignment(int assNum){
		//		JobAssignment.assignUser(Constants.appNum);

		//assignment json object
		JSONObject assObj = Utility.readUserAssignmentObject();
		//corresponding assNum's user
		JSONArray arr = assObj.getJSONArray(String.valueOf(assNum));

		//set of users
		Set<String> users = new HashSet<String>();
		for(int i=0; i<arr.length(); i++)
			users.add(arr.getString(i));

		Set<String> jobSet = new HashSet<String>();
		
		for(String u : users){
			jobSet.add(u);
			JSONObject userJson = Utility.readUserFriendListJsonObj(u);
			if(userJson==null)
				continue;
//			System.out.println("finished user count "+count++);
			//key value set of this user
			Set<String> keySet = userJson.keySet();
			for(String key : keySet){
				//if the key is following list
				if(key.startsWith("list")){
					JSONObject listJson = (JSONObject)userJson.getJSONObject(key);
					String[] members = listJson.getString("list_member").split(",");
					for(String member : members){
						jobSet.add(member);
						
//						//TODO:: add crawling code
//						JSONObject profileJson = crawlUserProfile(member);
//						if(profileJson!=null)
//							Utility.writeUserProfileJsonObj(profileJson);
//						
//						JSONObject tweetJson = crawlUserTweets(member);
//						if(tweetJson!=null)
//							Utility.writeUserTweetJsonObj(tweetJson);
////						TODO:: Both following and followed
//						JSONObject friendListJson = crawlUserList(member);
//						if(friendListJson!=null)
//							Utility.writeUserFriendListJsonObj(friendListJson);
					}
				}
			}
		}
		
		Thread p1 = new Thread(new UserProfileThread(jobSet), "user profile crawler");
		Thread p2 = new Thread(new UserTweetsThread(jobSet), "user tweets crawler");
		Thread p3 = new Thread(new UserFriendListThread(jobSet), "user friend list crawler");
		Thread p4 = new Thread(new UserFriendThread(jobSet), "user friend crawler");
		Thread p5 = new Thread(new UserFollowerThread(jobSet), "user follower crawler");
		
		p1.start();
		p2.start();
		p3.start();
		p4.start();
		p5.start();
	}
	
	public static void crawl(int assNum){
		//TODO:: think about how to automatic run
		runJobAssignment(assNum);
	}
	
	public static void main(String[] args){
		Constants.initConstants();
		
		if(args==null || args.length==0){
			TwitterCrawler.initResRemMap();
			TwitterCrawler.crawl(Constants.jobAssign);
		}
		else if(args[0].equals("printRate"))
			TwitterCrawler.printRateLimit();
		
	}
}
