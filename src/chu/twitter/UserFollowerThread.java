package chu.twitter;

import java.io.File;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;


/**
 * user follower crawl thread
 * @author chu
 *
 */
public class UserFollowerThread implements Runnable{
	Set<String> jobSet;
	
	public UserFollowerThread(Set<String> jobSet){
		this.jobSet = jobSet;
	}
	
	@Override
	public void run(){
		for(String user : jobSet){
			JSONObject followerJson = this.crawlUserFollower(user);
			if(followerJson!=null)
				Utility.writeUserFollowerJsonObj(followerJson);
		}
	}
	/**
	 * get user followers
	 * @param screenName
	 * @return
	 */
	public JSONObject crawlUserFollower(String screenName){
		JSONObject followerJson = null;
		
		if(screenName==null || screenName.length()==0)
			return null;
		File userJsonFile = new File(Constants.followerDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");

		if(userJsonFile.exists())
			return null;

		try {
			followerJson = new JSONObject();
			followerJson.put("user_name", screenName);
			
			JSONArray followerArray = new JSONArray();
			
			//pageable list of list members
			PagableResponseList<User> followerList;
			long cursor = -1;
			do {
				TwitterCrawler.updateResRemMap("/followers/list", -1);
				followerList = TwitterCrawler.twitter.getFollowersList(screenName, cursor);
				
				for (User following : followerList) {
					//                System.out.println("@" + following.getScreenName());
					followerArray.put(following.getScreenName());
				}

				System.out.println("cursor num "+cursor);
				
			} while ((cursor = followerList.getNextCursor()) != 0);//get all the user in the list

			followerJson.put("follower_list", followerArray);
			
			System.out.println(Debugger.getCallerPosition()+"\n"+followerJson);
			System.out.println("Crawling USER FOLLOWERS");
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return followerJson;
	}
	
}	
