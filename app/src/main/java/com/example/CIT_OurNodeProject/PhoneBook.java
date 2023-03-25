package com.example.CIT_OurNodeProject;

import java.util.ArrayList;
import org.json.*;

public class PhoneBook {

    ArrayList<String> IPs = new ArrayList<>();

    public PhoneBook(String ... ips){
        for (String ip: ips ) {
            this.IPs.add(ip);
        }
    }
    public PhoneBook(){
    }

    public PhoneBook copy(){
        PhoneBook newPhonebook = new PhoneBook();
        for (String ip: this.IPs) {
            newPhonebook.IPs.add(ip);
        }
        return newPhonebook;
    }

    public String toString(){
        String result = "";
        for (String string: this.IPs) {
            result = result + ", " + string;
        }
        return result;
//        return "" + IPs.get(0) + ", " + IPs.get(1)+ ", "  + IPs.get(2);
    }

    public String toJsonArrayString(){

        JSONArray jsonArrayOfIps = new JSONArray();
        for (String Ip: IPs ) {
            JSONObject IpAsJson = new JSONObject();
            try {
                IpAsJson.put("IP", Ip);
                jsonArrayOfIps.put(IpAsJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonArrayOfIps.toString();
    }



    public JSONArray toJsonArray(){

        JSONArray jsonArrayOfIps = new JSONArray();
        for (String Ip: IPs ) {
            JSONObject IpAsJson = new JSONObject();
            try {
                IpAsJson.put("IP", Ip);
                jsonArrayOfIps.put(IpAsJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonArrayOfIps;
    }


}