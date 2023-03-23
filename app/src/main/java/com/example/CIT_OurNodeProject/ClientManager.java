package com.example.CIT_OurNodeProject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ClientManager {

    Node node;


    public ClientManager(Node node) {
        this.node = node;
    }

    public Response handleResponseFromServer(Request request, Response response) throws JSONException, IOException {

        return response;
    }

    public Response buildResponseToGetId(Response response) {
        response.status = "200 OK";
        JSONObject jsonBody = new JSONObject();

        // creating the json body for the response:
        try {
            jsonBody.put("Id", node.IP);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        response.body = jsonBody;
        return response;
    }







}
