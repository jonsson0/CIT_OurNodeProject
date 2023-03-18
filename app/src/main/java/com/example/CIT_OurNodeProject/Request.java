package com.example.CIT_OurNodeProject;

import org.json.*;

public class Request {
    public String method;
    public String path;
    public JSONObject body;

    public void fromString(String requestString) {
        JSONObject json;
        try {

            json = new JSONObject(requestString);
            this.method = json.getString("method");
            this.path = json.getString("path");
            this.body = bodyToJson(json.getString("body"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject toJson(String method, String path, String body) {
        JSONObject json = new JSONObject();
        try {
            json.put("header", "HTTP/1.1");
            json.put("path", path);
            json.put("method", method);
            json.put("body", body);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    private JSONObject bodyToJson(String body) {

        JSONObject json;
        try {
            json = new JSONObject(body);
            return json;
        }catch (Exception e) {
            System.out.println(e);
            System.out.println(body.charAt(24));
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        return null;
    }

    public String toString(){
        return( "Header: HTTP/1.1\n" +
                "Method: " + this.method + "\n"+
                "Path: " + this.path + "\n" +
                "Body: {\n" +
                this.body + "\n" +
                "  }");
    }
}
