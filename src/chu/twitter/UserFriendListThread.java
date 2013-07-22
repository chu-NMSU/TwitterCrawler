package chu.twitter;

import java.io.File;
import java.util.Set;

import org.json.JSONObject;

import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

public class UserFriendListThread implements Runnable {
	
	Set<String> jobSet;
	
	public UserFriendListThread(Set<String> jobSet){
		this.jobSet = jobSet;
	}
	
	@Override
	public void run(){
		for(String user : jobSet){
			JSONObject friendListJson = this.crawlUserFriendList(user);
			if(friendListJson!=null)
				Utility.writeUserFriendListJsonObj(friendListJson);
		}
	}
	
	/**
	 * Get the user list of screenName user
	 * @param screenName the screenName of the user
	 * @return the JSONObject of that user.  {user_name:"screenName", }.  Return null if user already exists.
	 */
	public JSONObject crawlUserFriendList(String screenName){
		//user friend list jason
		JSONObject userJson = null;
		
		try {
			if(screenName==null || screenName.length()==0)
				return null;
			File userJsonFile = new File(Constants.friendListDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
			
			if(userJsonFile.exists())
				return null;
			
			userJson = new JSONObject();
			userJson.put("user_name", screenName);
			
			//crawl user's friend list 
			TwitterCrawler.updateResRemMap("/lists/list", -1);
			ResponseList<UserList> lists = TwitterCrawler.twitter.getUserLists(screenName);
			
			
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
	                listMemberList = TwitterCrawler.twitter.getUserListMembers(listId, cursor);
	                TwitterCrawler.updateResRemMap("/lists/members", -1);
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
//			End crawl user following list
			
			System.out.println(Debugger.getCallerPosition()+"\n"+userJson);
			System.out.println("Crawling USER FRIENDS LIST");
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return userJson;
		} 
		return userJson;
	}
}
