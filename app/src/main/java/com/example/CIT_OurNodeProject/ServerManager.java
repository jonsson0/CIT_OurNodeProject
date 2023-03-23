package com.example.CIT_OurNodeProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ServerManager implements IServerManager{


    Node node;


    @Override
    public Response handleRequestFromClient(Request request, String clientIP){
        Response response = new Response();

        switch (request.path.toLowerCase()) {
            case "getid":
                response = generateResponse_GetId();
                break;
            case "updatephonebook":
                response = generateResponse_UpdatePhonebook(request);
                break;
            case "getphonebook":
                response = generateResponse_GetPhonebook();
                break;
            case "getdata":
                response = enerateResponse_GetData(request);
                break;
            case "adddata":
                try {
                    response = generateResponse_AddData(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                break;
            default:
                buildResponseBadRequest(response);
                break;
        }

        return response;

    }
    @Override
    public Response generateResponse_GetId() {
        return null;
    }

    @Override
    public Response generateResponse_UpdatePhonebook(Request request) {
        return null;
    }

    @Override
    public Response generateResponse_GetPhonebook(Request request) {
        Response response = new Response();

        response.status = "200 OK";
        ArrayList<String> phoneBookRight = node.phoneBookRight.IPs;
        ArrayList<String> phoneBookLeft = node.phoneBookLeft.IPs;

        // creating the json body for the response:
        JSONObject jsonBody = new JSONObject();

        JSONArray jsonPhoneBookRight = new JSONArray();
        JSONArray jsonPhoneBookLeft  = new JSONArray();
        try {

            for (String IP : phoneBookRight) {
                JSONObject ipInPhoneBook = new JSONObject();
                ipInPhoneBook.put("Id", "Id???");
                ipInPhoneBook.put("IP", IP);
                jsonPhoneBookRight.put(ipInPhoneBook);
            }
            for (String IP : phoneBookLeft) {
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
        return response;
    }

    @Override
    public Response enerateResponse_GetData(Request request) {
        return null;
    }

    @Override
    public Response generateResponse_AddData(Request request) throws IOException, JSONException {
        return null;
    }

    @Override
    public Response generateResponse_DeleteData(Response response) {
        return null;
    }

    public Response generateResponse_BadRequest() {
        Response response = new Response();
        return response;
    }






}
