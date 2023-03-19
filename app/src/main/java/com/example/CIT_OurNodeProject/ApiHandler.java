package com.example.CIT_OurNodeProject;

import org.json.*;

import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class ApiHandler {

    Node node;

    public ApiHandler(Node node){
        this.node = node;
    }

    public Request readHttpRequest(String input) {

        System.out.println("inside the readHttpRequest");
        System.out.println(input);
        Request request = new Request(input);

      //  Request request = new Request();
       // System.out.println("44444444444444444444");
       // request.fromString(input);

        return request;
    }

    public  Response readHttpResponse(String input) {

        Response response = new Response(input);
        return response;
    }


    // For server:
    public Response requestHandler(Request request) {
        Response response = new Response();
        switch (request.path.toLowerCase()) {
            case "getid":
                response.status = "200 OK";
                JSONObject jsonBody = new JSONObject();

                // creating the json body for the response:
                try {
                    jsonBody.put("id", node.IP);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                response.body = jsonBody;

                break;
            case "newneighbor(left)":

            case "getphonebook":

            case "getdata":

            case "adddata":

        }

        return response;
    }

    // for client:

    public void getId() {

    }
    public void NewNeighbor() {

    }
    public void GetPhonebook() {

    }
    public void getData(String value) {

    }
    public void AddData() {

    }
    public void DeleteData() {

    }





}
