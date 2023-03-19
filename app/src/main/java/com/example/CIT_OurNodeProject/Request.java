package com.example.CIT_OurNodeProject;

import org.json.*;

public class Request {
    public String method;
    public String path;
    public JSONObject body;

    public Request(String requestString) {
        try {

            System.out.println("before");
            System.out.println(requestString);
            JSONObject json = new JSONObject(requestString);
            System.out.println("000000");

           // fromString(requestString);


            JSONObject jsonBody = new JSONObject();

            String str = jsonBody.getString("Body");
            System.out.println("1");
            System.out.println(str);

            this.method = json.getString("Method");
            this.path = json.getString("Path");
            this.body = jsonBody;





          //  fromString(requestString);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Request(String method, String path, JSONObject body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }

    public Request() {

    }


    public void fromString(String requestString) {
        JSONObject json;
        try {

            json = new JSONObject(requestString);
            System.out.println("5555555555555555");
            System.out.println(json);
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

        System.out.println("11111111111111111111111111111111111");

        System.out.println(body);
        JSONObject json;
        try {
            json = new JSONObject(body);
            System.out.println(json);
            return json;
        }catch (Exception e) {
            System.out.println(e);
            System.out.println(body.charAt(24));
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        return null;
    }


    public String toString(){

        String str = "{Header: HTTP/1.1" + "\n" +
                "Method: " + method + "\n"+
                "Path: " + path + "\n" +
                "Body: " + body.toString() + "\n" +
                "}";

        System.out.println("oooooooooooooooooo");
        System.out.println(str.charAt(14));

        return("{Header: HTTP/1.1" + ",\n" +
                "Method: " + this.method + ",\n"+
                "Path: " + this.path + ",\n" +
                "Body: " + this.body.toString() + "\n" +
                "}");
    }
}
