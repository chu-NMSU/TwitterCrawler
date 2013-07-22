package org.json;


public class JSONExample {

	public static void main(String[] args){
//		JSONObject obj=new JSONObject();
//		obj.put("name","foo");
//		obj.put("name","foo");
//		obj.put("num",new Integer(100));
//		obj.put("balance",new Double(1000.21));
//		obj.put("is_vip",new Boolean(true));
//		//		obj.put("nickname",null);
//		System.out.print(obj);

		JSONArray arr = new JSONArray();
		arr.put("a");
		arr.put("b");
		arr.put("b");
		arr.put("d");
		arr.put("c");
		System.out.println(arr);
	}
}
