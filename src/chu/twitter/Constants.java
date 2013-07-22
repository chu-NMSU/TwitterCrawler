package chu.twitter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

public class Constants {
//	public final static String userDataDir = "/home/grad11/chu/workspace/tweetData1/data/user/";
//	public final static String tweetDataDir = "/home/grad11/chu/workspace/tweetData1/data/tweet/";
//	public final static String profileDataDir = "/home/grad11/chu/workspace/tweetData1/data/profile/";
	
	public static String friendListDataDir;
	public static String friendDataDir;
	public static String tweetDataDir;
	public static String profileDataDir;
	public static String followerDataDir;
	public static int jobAssign;
	
	public static void initConstants(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("twitter4j.properties"));
			friendListDataDir = prop.getProperty("data.friendList");
			friendDataDir = prop.getProperty("data.friend");
			followerDataDir = prop.getProperty("data.follower");
			tweetDataDir = prop.getProperty("data.tweet");
			profileDataDir = prop.getProperty("data.profile");
			jobAssign = Integer.parseInt( prop.getProperty("job"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
