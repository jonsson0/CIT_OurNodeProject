package com.example.CIT_OurNodeProject;

import org.json.JSONException;
import org.json.JSONObject;

public class Response {
    public String status;
    public JSONObject body;

    public Response(String requestString) {

        try {
            JSONObject json = new JSONObject(requestString);
            JSONObject jsonBody = new JSONObject(json.getString("Body"));

            this.status = json.getString("Status");
            this.body = jsonBody;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Response() {
        try {
            JSONObject json = new JSONObject();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString(){
        return( "Header: HTTP/1.1\n" +
                "Status: " + this.status + "\n" +
                "Body : {\n" +
                this.body + "\n" +
                "  }");
    }
}
