package com.example.CIT_OurNodeProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerManager implements IServerManager{


    Node node;


    public ServerManager(Node node){
        this.node = node;
    }
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
                response = generateResponse_GetPhonebook(request);
                break;
            case "getdata":
                response = generateResponse_GetData(request);
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
                generateResponse_BadRequest();
                break;
        }

        return response;

    }
    @Override
    public Response generateResponse_GetId() {
        Response response = new Response();
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
    public Response generateResponse_GetData(Request request) {
        return null;
    }

    @Override
    public Response generateResponse_AddData(Request request) throws IOException, JSONException {

        JSONObject requestData = null;
        try {
            requestData = request.body.getJSONObject("Data");
            String value = requestData.getString("Value");
            boolean isParent = requestData.getBoolean("isParent");

            if (!isParent){
                //if not parent
                Data data = new Data(value,false);

                String senderIP = requestData.getString("senderIP");

                if (senderIP.equals(node.neighborLeft.IP)) {
                    node.neighborLeft.listOfData.add(data);
                } else if (senderIP.equals(node.neighborRight.IP)) {
                    node.neighborRight.listOfData.add(data);
                }

                JSONObject responseBody=new JSONObject();
                responseBody.put("Neighbor","ADDEDSUCCESS");
                Response response =new Response("200", responseBody);

                return response;
            } else if(isParent){
                //Adds to own
                System.out.println("REQUEST TO PARENTS:");

                Data data = new Data(value,true);
                node.listOfData.add(data);

//                Request requestForNeighbor =
                // make new request with isParent=false
                Request requestForNeighbor = createRequestForNeighborReplication(data, request, node.IP);




                Response r1 = sendRequestToNeighbor(node.neighborLeft.IP,requestForNeighbor);
                System.out.println("PASSING ONTO FIRST CHILD" + requestForNeighbor.toString());
                Response r2 = sendRequestToNeighbor(node.neighborLeft.IP,requestForNeighbor);
                System.out.println("PASSING ONTO SECOND CHILD" + requestForNeighbor.toString());
                JSONObject j = new JSONObject();
                j.put("Data Replicated on neighbors", "True");
                if (r1.status=="200"&& r2.status=="200"){
                    Response response =new Response("OK 200", j);
                    return response;
                }

            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Response response =new Response("400",new JSONObject());
        return response;
    }


    @Override
    public Response generateResponse_DeleteData(Response response) {
        return null;
    }

    public Response generateResponse_BadRequest() {
        Response response = new Response();
        return response;
    }

    public  Response sendRequestToNeighbor(String IP, Request request) {
        Response response;
        try {
            Socket connectionToNeighbor;
            connectionToNeighbor = new Socket(IP, 4444);
            DataOutputStream outputStream = new DataOutputStream(connectionToNeighbor.getOutputStream());
            outputStream.writeUTF(request.toString());

            // TODO: if we have time actaully get the response instead of making a "fake" one

            connectionToNeighbor.close();

            // DataInputStream instream = new DataInputStream(connectionToNeighbor.getInputStream());
            String value=request.body.getJSONObject("Data").getString("Value");
            JSONObject responseObject=new JSONObject();
            responseObject.put("Added",value);
            response = new Response("200 OK", responseObject);

        }catch (Exception e){
            response = new Response("400", new JSONObject());

        }

        return response;
    }

    public  Request createRequestForNeighborReplication(Data data, Request originalRequest, String senderIP) throws JSONException {
        JSONObject jsonBody=new JSONObject();

        JSONObject jsonData = new JSONObject();

        jsonData.put("ID",data.id);
        jsonData.put("Value",data.value);
        jsonData.put("isParent",false);
        jsonData.put("senderIP", senderIP);

        jsonBody.put("Data",jsonData);

        Request newRequest = new Request("get", originalRequest.path, jsonBody);
        return newRequest;


    }


}
