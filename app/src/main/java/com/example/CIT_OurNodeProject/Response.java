package com.example.CIT_OurNodeProject;

import org.json.JSONException;
import org.json.JSONObject;

public class Response {
    public String status;
    public String body;

    public void fromString(String responseString) {
        JSONObject json = null;
        try {

            json = new JSONObject(responseString);
            this.status = json.getString("method");
            this.body = json.getString("body");

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public JSONObject toJson(String status, String body) {
        JSONObject json = new JSONObject();
        try {
            json.put("header", "HTTP/1.1");
            json.put("status", status);
            json.put("body", body);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json;
    }





    public String toString(){
        return( "Header: HTTP/1.1\n" +
                "Status: " + this.status + "\n" +
                "Body : {\n" +
                this.body + "\n" +
                "  }");
    }
}
