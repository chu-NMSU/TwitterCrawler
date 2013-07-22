package chu.twitter;

import java.io.File;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;


/**
 * user friend list crawl thread
 * @author chu
 *
 */
public class UserFriendThread implements Runnable{
	Set<String> jobSet;
	
	public UserFriendThread(Set<String> jobSet){
		this.jobSet = jobSet;
	}
	
	@Override
	public void run(){
		for(String user : jobSet){
			JSONObject friendJson = this.crawlUserFriend(user);
			if(friendJson!=null)
				Utility.writeUserFriendJsonObj(friendJson);
		}
	}
	/**
	 * get user friends
	 * @param screenName
	 * @return
	 */
	public JSONObject crawlUserFriend(String screenName){
		JSONObject friendJson = null;
		
		if(screenName==null || screenName.length()==0)
			return null;
		File userJsonFile = new File(Constants.friendDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");

		if(userJsonFile.exists())
			return null;

		try {
			friendJson = new JSONObject();
			friendJson.put("user_name", screenName);
			
			JSONArray friendArray = new JSONArray();
			
			//pageable list of  friends
			PagableResponseList<User> friendList;
			long cursor = -1;
			do {
				TwitterCrawler.updateResRemMap("/friends/list", -1);
				friendList = TwitterCrawler.twitter.getFriendsList(screenName, cursor);
				
				for (User friend : friendList) {
					//                System.out.println("@" + following.getScreenName());
					friendArray.put(friend.getScreenName());
				}
				
				System.out.println("cursor num "+cursor);
				
			} while ((cursor = friendList.getNextCursor()) != 0);//get all the friends

			friendJson.put("friend_list", friendArray);
			
			System.out.println(Debugger.getCallerPosition()+"\n"+friendJson);
			System.out.println("Crawling USER FRIENDS");
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return friendJson;
	}
	
}
