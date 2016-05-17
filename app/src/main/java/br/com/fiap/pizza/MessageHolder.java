package br.com.fiap.pizza;

import java.util.HashMap;

/**
 * Created by VyMajoriss on 5/11/2016.
 */
public class MessageHolder {

   public HashMap<String, HashMap<String, String>> getHashMap() {
      return hashMap;
   }

   public void setHashMap(HashMap<String, HashMap<String, String>> hashMap) {
      this.hashMap = hashMap;
   }

   public  HashMap<String,HashMap<String,String>> hashMap;

}
