package com.example.CIT_OurNodeProject;

import org.json.JSONException;
import org.json.JSONObject;

public class Response {
    public String status;
    public JSONObject body;

    public Response(String responseString) {

        try {
            JSONObject json = new JSONObject(responseString);
            JSONObject jsonBody = new JSONObject(json.getString("Body"));

            this.status = json.getString("Status");
            this.body = jsonBody;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Response(String status, JSONObject body) {
        this.status = status;
        this.body = body;
    }

    public Response() {

    }

    public String toString() {
        System.out.println("CORRECT TO STRING");
        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put("Status", status);
            responseJson.put("Body", body);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        String responseString = responseJson.toString();

        return responseString;
    }

    /*
    public String toString(){
        System.out.println("THIS IS THE WRONG TO STRING");
        return( "Header: HTTP/1.1\n" +
                "Status: " + this.status + "\n" +
                "Body : {\n" +
                this.body + "\n" +
                "  }");
    }
    */
}
