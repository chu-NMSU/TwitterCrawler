package chu.twitter;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

public class JobAssignment {
	/**
	 * uniformly assign users to appNum of apps.  Store the assignment schedule in ./data/user/ass.json
	 * @param appNum number of apps.
	 */
	public static void assignUser(int appNum){
		File dataDir = new File(Constants.friendListDataDir);
		File[] alphabetDir = dataDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				File f = new File(arg0.getAbsolutePath()+"/"+arg1);
				return (!arg1.startsWith("."))&&(f.isDirectory());
			}
		});
		
//		File[] alphabetDir = dataDir.listFiles();
		JSONObject ass = new JSONObject();
		
		int i = 1;
		for( ; i<=appNum; i++)
			ass.put(String.valueOf(i), new JSONArray());
		
		i = 0;
		for(File alphabet : alphabetDir){
			File[] users = alphabet.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File arg0, String arg1) {
					// TODO Auto-generated method stub
					return arg1.endsWith(".json");
				}
			});
//			System.out.println(alphabet.getName());
			for(File u : users){
				ass.getJSONArray(String.valueOf(i+1)).put(u.getName().replace(".json", ""));
				i=(i+1)%appNum;
			}
		}
		
		Utility.writeUserAssignmentObject(ass);
		System.out.println(ass);
	}
	
	/**
	 * uniformly assign users to appNum of apps.  Store the assignment schedule in ./data/user/ass.json
	 * @param appNum number of apps.
	 */
	public static void assignTweet(int appNum){
		File dataDir = new File(Constants.friendListDataDir);
		File[] alphabetDir = dataDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				File f = new File(arg0.getAbsolutePath()+"/"+arg1);
				return !f.isHidden()&&f.isDirectory();
			}
		});
		
//		File[] alphabetDir = dataDir.listFiles();
		
		JSONObject ass = new JSONObject();
		
		int i = 1;
		for( ; i<=appNum; i++)
			ass.put(String.valueOf(i), new JSONArray());
		
		i = 0;
		for(File alphabet : alphabetDir){
			File[] users = alphabet.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File arg0, String arg1) {
					// TODO Auto-generated method stub
					return arg1.endsWith(".json");
				}
			});
//			System.out.println(alphabet.getName());
			for(File u : users){
				ass.getJSONArray(String.valueOf(i+1)).put(u.getName().replace(".json", ""));
				i=(i+1)%appNum;
			}
		}
		
		Utility.writeTweetAssignmentObject(ass);
		
		System.out.println(ass);
		
	}
	
	public static void main(String[] args){
//		assignUser(Constants.appNum);
		Constants.initConstants();
		
		if(args[0].equals("user"))
			assignUser(3);
		else if(args[0].equals("tweet"))
			assignTweet(3);
		else if(args[0].equals("profile"))
			;//assign profile crawling
		else
			System.out.println("no such argument");
	}
	
}
