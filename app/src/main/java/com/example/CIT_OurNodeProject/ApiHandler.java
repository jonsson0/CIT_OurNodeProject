package com.example.CIT_OurNodeProject;

import org.json.*;

import java.util.ArrayList;

public class ApiHandler {

    Node node;

    public ApiHandler(Node node){
        this.node = node;
    }

    public Request readHttpRequest(String input) {

        Request request = new Request(input);

        return request;
    }

    public  Response readHttpResponse(String input) {

        Response response = new Response(input);
        return response;
    }


    // For server:
    public Response requestHandler(Request request, String IP) {
        Response response = new Response();
        switch (request.path.toLowerCase()) {
            case "getid":
                response = buildResponseToGetId(response);
                break;
            case "newneighbor":
                response = buildResponseToNewNeighbor(response, IP);
                break;

            case "getphonebook":
                response = buildResponseToGetPhonebook(response);
                break;
            case "getdata":

                break;
            case "adddata":

                break;
            default:
                buildResponseBadRequest(response);
                break;
        }

        return response;
    }

    // for client:

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
    public Response buildResponseToNewNeighbor(Response response, String IP) {
        response.status = "200 OK";

        // creating the json body for the response:
        JSONObject jsonBody = new JSONObject();
        JSONArray jsonArrayOfData = new JSONArray();



        try {

            if (jsonBody.getString("Side").equals("left")) {
                Neighbor neighbor = new Neighbor();
                neighbor.IP = IP;
                node.neighborLeft = neighbor;
            }


            for (Data data : node.listOfData) {
                JSONObject jsonDataObject = new JSONObject();
                jsonDataObject.put("Id", data.id);
                jsonDataObject.put("Value", data.value);
                jsonDataObject.put("IsParent", "False");
                jsonDataObject.put("IsGlobal", "False");

                jsonArrayOfData.put(jsonDataObject);
            }
            jsonBody.put("data", jsonArrayOfData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        response.body = jsonBody;
        return response;
    }

    public Response buildResponseToGetPhonebook(Response response) {
        response.status = "200 OK";
        ArrayList<String> phoneBookRight = node.phoneBookRight.IPs;
        ArrayList<String> phoneBookLeft = node.phoneBookLeft.IPs;

        // creating the json body for the response:
        JSONObject jsonBody = new JSONObject();

        JSONArray jsonPhoneBookRight = new JSONArray();
        JSONArray jsonPhoneBookLeft = new JSONArray();
        try {

            for (String IP: phoneBookRight) {
                JSONObject ipInPhoneBook = new JSONObject();
                ipInPhoneBook.put("Id", "Id???");
                ipInPhoneBook.put("IP", IP);
                jsonPhoneBookRight.put(ipInPhoneBook);
            }
            for (String IP: phoneBookLeft) {
                JSONObject ipInPhoneBook = new JSONObject();
                ipInPhoneBook.put("Id", "Id???");
                ipInPhoneBook.put("IP", IP);
                jsonPhoneBookLeft.put(ipInPhoneBook);
            }

            jsonBody.put("RightNeighbors", jsonPhoneBookRight);
            jsonBody.put("LeftNeighbors", jsonPhoneBookLeft);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        response.body = jsonBody;
        return  response;
    }
    public Response getData(Response response) {

        return response;
    }
    public Response AddData(Response response) {

        return response;
    }
    public Response DeleteData(Response response) {

        return response;
    }

    public Response buildResponseBadRequest(Response response) {

        return response;
    }





}
