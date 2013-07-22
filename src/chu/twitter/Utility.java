package chu.twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
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
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
	
	public static Map<String, JSONObject> inMemoryUserJson = new HashMap<String, JSONObject>();
	
	public static Map<String, JSONObject> inMemoryTweetJson = new HashMap<String, JSONObject>();
	
	/**
	 * write a user JSONObject to file.
	 * @param obj
	 */
	public static void writeUserProfileJsonObj(JSONObject obj){
		String screenName = obj.getString("user_name");
		File target = new File(Constants.profileDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(target);
			bw = new BufferedWriter(fw);
			bw.write(obj.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * write a user JSONObject to file.
	 * @param obj
	 */
	public static void writeUserFriendListJsonObj(JSONObject obj){
		String screenName = obj.getString("user_name");
		File target = new File(Constants.friendListDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(target);
			bw = new BufferedWriter(fw);
			bw.write(obj.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * read a user JSONObject from file of the corresponding screenName user
	 * @param screenName
	 * @return
	 */
	public static JSONObject readUserFriendListJsonObj(String screenName){
//		if(inMemoryUserJson.get(screenName)!=null)
//			return inMemoryUserJson.get(screenName);
		
		JSONObject obj = null;
		FileReader fr;
		BufferedReader br;
		File target = new File(Constants.friendListDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		try {
			fr = new FileReader(target);
			br = new BufferedReader(fr);
			String line = br.readLine();
			String jsonString = "";
			while(line!=null){
				
				jsonString+=line;
				line = br.readLine();
			}
			
			obj = new JSONObject(jsonString);
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e){
			//some chaos code in the data file
			System.out.println(screenName);
			e.printStackTrace();
			return null;
		}
		
//		inMemoryUserJson.put(screenName, obj);
		
		return obj;
	}
	
	public static JSONObject readTweetJsonObj(String screenName){
//		if(inMemoryTweetJson.get(screenName)!=null)
//			return inMemoryTweetJson.get(screenName);
		
		JSONObject obj = null;
		FileReader fr;
		BufferedReader br;
		try {
			File target = new File(Constants.tweetDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
			if(!target.exists())
				return null;
			fr = new FileReader(target);
			br = new BufferedReader(fr);
			String line = br.readLine();
			String jsonString = "";
			while(line!=null){
				
				jsonString+=line;
				line = br.readLine();
			}
			
			obj = new JSONObject(jsonString);
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(screenName);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(screenName);
			e.printStackTrace();
		} catch(StringIndexOutOfBoundsException e){
			System.out.println(screenName);
			e.printStackTrace();
		}
		
		return obj;
	}
	
	/**
	 * read the user crawler job assignment object from file
	 * @return
	 */
	public static JSONObject readUserAssignmentObject(){
		File assFile = new File(Constants.friendListDataDir+"ass.json");
		System.out.println(assFile.getPath()+"\n"+assFile.exists());
		FileReader fr;
		BufferedReader br;
		JSONObject assObj = null;
		try {
			fr = new FileReader(assFile);
			br = new BufferedReader(fr);
			String line = br.readLine();
			String jsonString = "";
			while(line!=null){
				jsonString+=line;
				line = br.readLine();
			}
//			System.out.println(jsonString);
			assObj = new JSONObject(jsonString);
			
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return assObj;
	}
	
	public static void writeUserAssignmentObject(JSONObject obj){
		File assignment = new File(Constants.friendListDataDir+"ass.json");
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(assignment);
			bw = new BufferedWriter(fw);
			bw.write(obj.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * read the user crawler job assignment object from file
	 * @return
	 */
	public static JSONObject readTweetAssignmentObject(){
		File assFile = new File(Constants.tweetDataDir+"ass.json");
		System.out.println(assFile.getPath()+"\n"+assFile.exists());
		FileReader fr;
		BufferedReader br;
		JSONObject assObj = null;
		try {
			fr = new FileReader(assFile);
			br = new BufferedReader(fr);
			String line = br.readLine();
			String jsonString = "";
			while(line!=null){
				jsonString+=line;
				line = br.readLine();
			}
//			System.out.println(jsonString);
			assObj = new JSONObject(jsonString);
			
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return assObj;
	}
	
	public static void writeTweetAssignmentObject(JSONObject obj){
		File assignment = new File(Constants.tweetDataDir+"ass.json");
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(assignment);
			bw = new BufferedWriter(fw);
			bw.write(obj.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * get the list name containing friendName in screenName's user json file
	 * @param screenName 
	 * @param friendName friend's name
	 * @return
	 */
	public static String getUserListName(String screenName, String friendName){
//		JSONObject userObj = inMemoryUserJson.get(screenName);
//		if(userObj==null){
//			userObj = Utility.readUserJsonObj(screenName);
//			inMemoryUserJson.put(screenName, userObj);
//		}
		
		JSONObject userObj = Utility.readUserFriendListJsonObj(screenName);
		Set<String> userKeySet = userObj.keySet();
		
		for(String userKey : userKeySet){
			if(userKey.startsWith("list")){
				JSONObject listObj = userObj.getJSONObject(userKey);

				String listMember = listObj.getString("list_member");
				if(listMember.contains(friendName))
					return listObj.getString("list_name");
			}
		}
		
		return null;
	}
	/**
	 * write user's tweet Json object to file
	 * @param obj
	 */
	public static void writeUserTweetJsonObj(JSONObject obj){
		String screenName = obj.getString("user_name");
		File tweetJsonFile = new File(Constants.tweetDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(tweetJsonFile);
			bw = new BufferedWriter(fw);
			bw.write(obj.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeUserFollowerJsonObj(JSONObject obj){
		String screenName = obj.getString("user_name");
		File tweetJsonFile = new File(Constants.followerDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(tweetJsonFile);
			bw = new BufferedWriter(fw);
			bw.write(obj.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeUserFriendJsonObj(JSONObject obj){
		String screenName = obj.getString("user_name");
		File tweetJsonFile = new File(Constants.friendDataDir+screenName.substring(0, 1).toLowerCase()+"/"+screenName+".json");
		
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(tweetJsonFile);
			bw = new BufferedWriter(fw);
			bw.write(obj.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
//		System.out.println("_chuanHu".substring(0, 1).toLowerCase());
//		System.out.println(Utility.readAssignmentObject());
		
//		System.out.println(Utility.getUserListName("chuanHu", "WSJ"));

//		Utility.convertDataToSamplingFormat();
	}
	
}
