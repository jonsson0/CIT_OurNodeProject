package com.example.CIT_OurNodeProject;

import org.json.*;

public class Request {

    public String header = "HTTP/1.1";
    public String method;
    public String path;
    public JSONObject body;

    public Request(){ }

    public Request(String requestString) {
        try {

            JSONObject json = new JSONObject(requestString);
            JSONObject jsonBody = new JSONObject(json.getString("Body"));

            this.header = json.getString("Header");
            this.method = json.getString("Method");
            this.path = json.getString("Path");
            this.body = jsonBody;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Request(String method, String path, JSONObject body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        this.body = new JSONObject();
    }


    public Request copy(){
        Request newRequest = new Request();
        newRequest.body = this.body;
        newRequest.header = this.header;
        newRequest.method = method;
        newRequest.path = path;
        return newRequest;

    }


    public String toString() {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("Header", "HTTP/1.1");
            requestJson.put("Method", method);
            requestJson.put("Path", path);
            requestJson.put("Body", body);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }





        String requestString = requestJson.toString();

        return  requestString;
       /*
        return("{Header: HTTP/1.1" + ",\n" +
                "Method: " + this.method + ",\n"+
                "Path: " + this.path + ",\n" +
                "Body: " + this.body.toString() + "\n" +
                "}");
    }

        */
    }

}
