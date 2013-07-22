package chu.twitter;

import java.io.File;
import java.util.Set;

import org.json.JSONObject;

import twitter4j.TwitterException;
import twitter4j.User;


/**
 * user profile crawl thread
 * @author chu
 *
 */
public class UserProfileThread implements Runnable{

	Set<String> jobSet;
	
	public UserProfileThread(Set<String> jobSet){
		this.jobSet = jobSet;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(String user : jobSet){
			JSONObject profileJson = this.crawlUserProfile(user);
			if(profileJson!=null)
				Utility.writeUserProfileJsonObj(profileJson);
		}
	}
	
	/**
	 * crawl one user's profile
	 * @param screenName
	 */
	public JSONObject crawlUserProfile(String screenName){
		try {
			if(screenName==null || screenName.length()==0)
				return null;
			File userJsonFile = new File(Constants.profileDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");

			if(userJsonFile.exists())
				return null;

			JSONObject userProfileJson = new JSONObject();
			userProfileJson.put("user_name", screenName);
			
			TwitterCrawler.updateResRemMap("/users/show/:id", -1);
			User u = TwitterCrawler.twitter.showUser(screenName);
			
			//user friend list jason
			userProfileJson.put("profile", u.getDescription());
			
			System.out.println(Debugger.getCallerPosition()+": "+userProfileJson+"\n"+TwitterCrawler.resourceRemainMap.get("/users/show/:id"));
			System.out.println("Crawling USER PROFILE");
			
			return userProfileJson;
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			TwitterCrawler.updateResRemMap("/users/show/:id", -1);
			e.printStackTrace();
			return null;
		}
	}
}
