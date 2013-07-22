/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.examples.account;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Gets rate limit status.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class GetRateLimitStatus {
    /**
     * Usage: java twitter4j.examples.account.GetRateLimitStatus
     *
     * @param args message
     */
    public static void main(String[] args) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            Map<String ,RateLimitStatus> status = twitter.getRateLimitStatus();
            
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
			
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get rate limit status: " + te.getMessage());
            System.exit(-1);
        }
    }
}
