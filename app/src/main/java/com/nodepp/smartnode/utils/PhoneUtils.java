package com.nodepp.smartnode.utils;

public class PhoneUtils {
      public static String changPhoneNum(String phoneNum){
    	  String phoneString = phoneNum;
    	  if (phoneString.length() == 11) {
    		  phoneString = phoneNum.substring(0, 3)+"****"+phoneNum.substring(7, 11);
		}
			return phoneString;
      }
}
