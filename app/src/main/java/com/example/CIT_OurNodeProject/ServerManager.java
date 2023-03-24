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

    ClientManager clientManager;

    public ServerManager(Node node){
        this.node = node;
        clientManager = new ClientManager(node);
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
            case "deletedata":
                try {
                    generateResponse_DeleteData(request);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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

        // TODO check if request is correct

        Response response = new Response();

        response.status = "200 OK";

        JSONObject jsonBody = request.body;
        JSONArray jsonArrayOfIP;
        JSONObject jsonBodyData;
        PhoneBook phoneBook = new PhoneBook();

        // Getting side and phonebook from the request:
        try {
            // outer most layer of json
            jsonBodyData = jsonBody.getJSONObject("Data");

            // inner json
            String side = jsonBodyData.getString("Side");
            jsonArrayOfIP = new JSONArray(jsonBodyData.getString("PhoneBook"));

            // foreach json in jsonArrayOfIP add it to the new phonebook
            for (int i = 0; i < jsonArrayOfIP.length(); i++) {

                JSONObject IpJsonObject = jsonArrayOfIP.getJSONObject(i);
                String IP = IpJsonObject.getString("IP");

                phoneBook.IPs.add(IP);
            }

            // replacing the old phonebook with the new
            if (side.equalsIgnoreCase("left")) {

                // checking if first element in the new phonebook is not my neighbor
                if (phoneBook.IPs.get(0).equals(node.neighborLeft.IP)) {
                    System.out.println("I got a new phonebook, and I dont need to swap my left neighbor");
                } else {
                    System.out.println("I got a new phonebook, and I need to swap left neighbor");
                    node.neighborLeft.IP = phoneBook.IPs.get(0);
                    node.neighborLeft.removeAllData();
                }
                node.phoneBookLeft = phoneBook;
                System.out.println("New phonebookLeft: " + node.phoneBookLeft.IPs);

            } else if (side.equalsIgnoreCase("right")) {

                if (phoneBook.IPs.get(0).equals(node.neighborRight.IP)) {
                    System.out.println("I got a new phonebook, and I dont need to swap my right neighbor");
                } else {
                    System.out.println("I got a new phonebook, and I need to swap right neighbor");
                    node.neighborRight.IP = phoneBook.IPs.get(0);
                    node.neighborRight.removeAllData();
                }

                node.phoneBookRight = phoneBook;
                System.out.println("New phonebookRight:" + node.phoneBookLeft.IPs);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        response.body = new JSONObject();
        return response;
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

        Response response = new Response();

        String id;
        JSONObject jsonDataFromRequest;

        try {
            jsonDataFromRequest = request.body.getJSONObject("Data");
            id = jsonDataFromRequest.getString("Id");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Data data : node.listOfData) {
            System.out.println(data.id);
            if (data.id.equals(id)) {
                System.out.println("into we have the data");
                JSONObject jsonBody = new JSONObject();
                JSONObject jsonData = new JSONObject();

                try {
                    jsonData.put("Id", data.id);
                    jsonData.put("Value", data.value);

                    jsonBody.put("data", jsonData);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                response.status = "200 OK";
                response.body = jsonBody;
                return response;
            } else {
                response.status = "404 Not Found";
                response.body = new JSONObject();
            }
        }
        return response;
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

                JSONObject responseBody = new JSONObject();
                responseBody.put("Neighbor","ADDEDSUCCESS");
                Response response = new Response("200", responseBody);

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
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("Data Replicated on neighbors", "True");
                if (r1.status.contains("200") && r2.status.contains("200")) {
                    Response response = new Response("OK 200", jsonBody);
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
    public Response generateResponse_DeleteData(Request request) throws JSONException  {

        JSONObject RequestData = request.body.getJSONObject("Data");
        String id = RequestData.getString("Id");
        Response response = new Response();

        boolean hasData = node.checkForData(id);

        if (!hasData) {

            JSONObject jsonBody = new JSONObject();
            JSONObject innerJson = new JSONObject();

            innerJson.put("Id", id);
            innerJson.put("Value", "EmptyValue");
            innerJson.put("isParent", true);

            jsonBody.put("Data", innerJson);

            Request getDataRequest = new Request("get", "getData", jsonBody);

            Response responseEmpty = new Response("123", new JSONObject());

            response = clientManager.getDataHandler(getDataRequest);

            if (response.status.contains("200 OK")) {
                String IP = response.body.getString("IP");

                String value = RequestData.getString("Value");

                Request requestToDelete = clientManager.generateRequest_DeleteData(value, true);

                sendRequestToNeighbor(IP, requestToDelete);
            }

            return response;
        }

        //delete own data if we have it
        if (RequestData.getBoolean("isParent")) {
            // if parent ask us to delete, we delete if we can. Will always give 200 even if we dont hold data
            boolean isDeleted = node.deleteDataLocally(id);

            // IF we are parent of the data, ask children to delete the data
            // create request for children with isParent=True
            Request requestForChild = clientManager.generateRequest_DeleteData_For_Child_Data(request, false);
            // send requests to children
            Response responseLeft = sendRequestToNeighbor(node.neighborLeft.IP, requestForChild);
            Response responseRight = sendRequestToNeighbor(node.neighborRight.IP, requestForChild);

            if (responseLeft.status == "200" && responseRight.status == "200") {
                response = new Response("200", new JSONObject());
            } else {
                response = new Response("400", new JSONObject());
            }

            if (isDeleted) {
                response = new Response("200 - deleted", new JSONObject());
            } else {
                response = new Response("404 Not Found - I dont have it", new JSONObject());
            }
        } else {
            node.neighborRight.deleteDataLocally(id);
            node.neighborLeft.deleteDataLocally(id);
        }
        return response;
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

        jsonData.put("Id", data.id);
        jsonData.put("Value", data.value);
        jsonData.put("isParent", false);
        jsonData.put("senderIP", senderIP);

        jsonBody.put("Data", jsonData);

        Request newRequest = new Request("get", originalRequest.path, jsonBody);
        return newRequest;
    }


}
