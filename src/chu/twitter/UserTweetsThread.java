package chu.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;


public class UserTweetsThread implements Runnable{
	Set<String> jobSet;
	
	public UserTweetsThread(Set<String> jobSet){
		this.jobSet = jobSet;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(String user : jobSet){
			JSONObject tweetJson = this.crawlUserTweets(user);
			if(tweetJson!=null)
				Utility.writeUserTweetJsonObj(tweetJson);
		}
	}
	
	/**
	 * Get the user time line top 200 tweets.
	 * @param screenName screen Name of the target user.
	 * @return
	 */
	public JSONObject crawlUserTweets(String screenName){
		
		if(screenName==null || screenName.length()==0)
			return null;
		
		JSONObject  userTweetObj = new JSONObject();
		userTweetObj.put("user_name", screenName);

		//new tweets array
		JSONArray tweetArray = new JSONArray();
		//existing oldUserTweetsObj
		JSONObject oldUserTweetsObj = null;
		JSONArray oldTweetsArray = null;
		//last update date.  Default many years ago
		Date lastUpdate = new Date(1000);
		
		//data format
		//Fri Apr 19 23:17:07 MDT 2013
		DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		
		File tweetJsonFile = new File(Constants.tweetDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		
		if(tweetJsonFile.exists()){
			oldUserTweetsObj = Utility.readTweetJsonObj(screenName);
			if (oldUserTweetsObj.opt("user_timeline")!=null)
				oldTweetsArray = oldUserTweetsObj.getJSONArray("user_timeline");
			
			if ( oldTweetsArray!=null && oldTweetsArray.length()!=0 ){
				String lastTweetDate = oldTweetsArray.getJSONObject(0).getString("date");
				try {
					lastUpdate = format.parse(lastTweetDate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		List<Status> statuses = null;
		
		int page = 1;
		Paging p = new Paging(page);
		
		try{
			TwitterCrawler.updateResRemMap("/statuses/user_timeline", -1);
			statuses = TwitterCrawler.twitter.getUserTimeline(screenName, p);
		}
		catch(TwitterException e){
			e.printStackTrace();
		}
		
//		TwitterCrawler.updateResRemMap("/statuses/user_timeline", -1);
		//for each page
		//now we crawl all data for the user...
		outerloop:
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
//					String listName = Utility.getUserListName(screenName, friendName);
//					aspect = listName;
				}
				//if this status is replying to a friend
				if(s.getInReplyToScreenName()!=null){
					String friendName = s.getInReplyToScreenName();
//					String listName = Utility.getUserListName(screenName, friendName);
//					aspect = listName;
				}
				
				JSONObject tweetJson = new JSONObject();
				tweetJson.put("tweet", tweet);
				//if this tweet is not retweet or replying, it's self created.
//				tweetJson.put("aspect", aspect==null?"self_created":aspect);
				tweetJson.put("date", s.getCreatedAt());
//				System.out.println("crawled "+s.getCreatedAt());
				if(s.getCreatedAt().before(lastUpdate))
					break outerloop;
				tweetArray.put(tweetJson);
			}
			
			//increase page
			p = new Paging(++page);
			
			try{
				TwitterCrawler.updateResRemMap("/statuses/user_timeline", -1);
				statuses = TwitterCrawler.twitter.getUserTimeline(screenName, p);
			}
			catch(TwitterException e){
				e.printStackTrace();
			}
		}
		//final output tweets array
		JSONArray mergeArray = new JSONArray();
		
		//if tweets obj already exists
		if(oldTweetsArray!=null){
			//no new update tweets
			if(tweetArray.length()==0)
				return null;
			//merge old and new tweets
			for(int i=0;i<tweetArray.length();i++)
				mergeArray.put( tweetArray.getJSONObject(i) );
			for(int i=0; i<oldTweetsArray.length(); i++)
				mergeArray.put( oldTweetsArray.getJSONObject(i) );
		}
		else
			mergeArray = tweetArray;
		
		userTweetObj.put("user_timeline", mergeArray);
		
		System.out.println(Debugger.getCallerPosition()+": "+userTweetObj.getString("user_name"));
		System.out.println("Crawling USER TWEETS");
		
		return userTweetObj;
	}
	
}
