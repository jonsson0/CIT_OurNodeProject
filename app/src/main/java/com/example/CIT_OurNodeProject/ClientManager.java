package com.example.CIT_OurNodeProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class ClientManager implements IClientManager{

    Node node;

    ServerManager serverManager;

    public ClientManager(Node node) {
        this.node = node;
    }

    public Response handleResponseFromServer(Request request, Response response) throws JSONException, IOException {

        switch (request.path.toLowerCase()) {
            case "getid":
                // Do nothing
                break;
            case "updatephonebook":
                // Do nothing
                break;
            case "getphonebook":
                // Do nothing
                break;
            case "getdata":
                handleResponse_GetData(request, response);
                break;
            case "adddata":
                // Do nothing
                break;
            default:

                break;
        }
        return response;
    }

    @Override
    public Request generateRequest_UpdatePhoneBook(PhoneBook phoneBook, String side) {
        return null;
    }

    @Override
    public Request generateRequest_GetData(String value) {
        return null;
    }

    @Override
    public Request generateRequest_AddData(String value, Boolean isParent) {
        String hashedValue;

        try {
            hashedValue = SHA256.toHexString(SHA256.getSHA(value));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        JSONObject json = new JSONObject();
        JSONObject innerJson = new JSONObject();

        try {
            innerJson.put("Id", hashedValue );
            innerJson.put("Value", value);
            innerJson.put("isParent", isParent);
            //   innerJson.put("SenderIP", )
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            json.put("Data", innerJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request("put", "addData", json);
        return request;
    }

    private Request generateRequest_DeleteData(Request request, boolean isParent) throws JSONException {
        JSONObject jsonBody = new JSONObject();
        JSONObject RequestData = request.body.getJSONObject("Data");

        jsonBody.put("ID",RequestData.get("id"));
        jsonBody.put("Value",RequestData.get("Value"));
        jsonBody.put("isParent",isParent);

        Request newRequest = new Request("get", request.path, jsonBody);
        return newRequest;
    }


    @Override
    public Response DeleteData(Request request) {
        Response response = new Response();
        JSONObject RequestData = null;
        try {
            RequestData = request.body.getJSONObject("Data");
            String id = RequestData.getString("Id");

        boolean hasData = node.checkForData(id);

        if (!hasData) {

            JSONObject jsonBody = new JSONObject();
            JSONObject innerJson = new JSONObject();

            innerJson.put("Id", id);
            innerJson.put("Value", "EmptyValue");
            innerJson.put("isParent", true);

            jsonBody.put("Data", innerJson);

            Request getDataRequest = new Request("get", "getData", jsonBody);

            response = getDataHandler(getDataRequest, new Response());

            if (response.status.contains("200")) {
                String IP = response.body.getString("IP");

                Request requestToDelete = generateRequest_DeleteData(request, true);

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
            Request requestForChild = generateRequest_DeleteData(request, false);
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
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public Response handleResponse_GetData(Request request, Response response) {

        System.out.println("Inside getDataHandler");

        Request originalRequest = request.copy();

        if (response.status.contains("OK")) {
            System.out.println("Response status contains OK");
            return response;
        } else {
            System.out.println("Response didnt have the Data");
            boolean hasGottenData = false;
            PhoneBook copyPhonebook = node.phoneBookLeft.copy();

            System.out.println("THIS IS THE IPS:");
            System.out.println(copyPhonebook.IPs);

            while (!hasGottenData) {
                System.out.println("while we dont have data");
                String IP = copyPhonebook.IPs.get(0);
                System.out.println(" the IP is we ask for data now: " + IP);
                Socket connectionToServer;
                try {
                    connectionToServer = new Socket(IP, 4444);
                    DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                    DataOutputStream outstream = new DataOutputStream(connectionToServer.getOutputStream());


                            /*
                            if (node.IP.equals(copyPhonebook.IPs.get(0))) {
                                response = new Response();
                                response.status = "404 Not Found";
                                response.body = new JSONObject();
                            }
                            */

                    System.out.println("Is the copyPhoneBook size == 1? ");
                    if (copyPhonebook.IPs.size() == 1) {
                        System.out.println("The size of copyPhoneBook is 1, we get a new phonebook");
                        Request phonebookRequest = new Request("get", "getPhoneBook", new JSONObject());
                        // request = new Request("get", "getPhoneBook", new JSONObject());
//                        String newMessage = request.toString();
                        System.out.println("Sending this request: " + phonebookRequest.toString());
                        outstream.writeUTF(phonebookRequest.toString());
                        outstream.flush();

                        // TODO implement something to check if response is coming or not if not ask new person
                        String messageFromServer = instream.readUTF();
                        System.out.println("This is the response: " + messageFromServer);
                        response = new Response(messageFromServer);

                        JSONArray jsonPhoneBookCopy;
                        try {
                            jsonPhoneBookCopy = response.body.getJSONArray("LeftNeighbors");
                            for (int i = 0; i < jsonPhoneBookCopy.length(); i++) {
                                copyPhonebook.IPs.add(jsonPhoneBookCopy.getJSONObject(i).getString("IP"));
//                                        System.out.println(" - " + copyPhonebook.IPs.get());
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("The copyPhonebook is not == 1");

                        outstream.writeUTF(originalRequest.toString());
                        outstream.flush();
                        System.out.println("This is the request we send: " + originalRequest.toString());
                        String messageFromServer = instream.readUTF();
                        System.out.println("This is the response we got: " + messageFromServer);
                        response = new Response(messageFromServer);

                        if (response.status.contains("OK")) {
                            hasGottenData = true;

                            JSONObject jsonBody = new JSONObject();

                            try {
                                jsonBody.put("IP", node.IP);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            response.body = jsonBody;
                        }
                    } // else for copyPhoneBook == 1
                    connectionToServer.close();
                    copyPhonebook.IPs.remove(0);

                } catch (IOException e) {
                    System.out.println("problem is: " + e.toString());
                    throw new RuntimeException(e);
                }
            } // while we havent gotten the data
            return response;
        } // else for response.status contain ok)
    } // getDataHandler


    public Response getDataHandler(Request request, Response response) {

        System.out.println("Inside getDataHandler");

        Request originalRequest = new Request(request.method, request.path, request.body);

        if (response.status.contains("OK")) {
            System.out.println("Response status contains OK");
            return response;
        } else {
            System.out.println("Response didnt have the Data");
            boolean hasGottenData = false;
            PhoneBook copyPhonebook = node.phoneBookLeft.copy();

            System.out.println("THIS IS THE IPS:");
            System.out.println(copyPhonebook.IPs);

            while (!hasGottenData) {
                System.out.println("while we dont have data");
                String IP = copyPhonebook.IPs.get(0);
                System.out.println(" the IP is we ask for data now: " + IP);
                Socket connectionToServer;
                try {
                    connectionToServer = new Socket(IP, 4444);
                    DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                    DataOutputStream outstream = new DataOutputStream(connectionToServer.getOutputStream());

//                            System.out.println(instream.readUTF());
//                            String messageFromServer = instream.readUTF();
//                            response = new Response(messageFromServer);

                            /*
                            if (node.IP.equals(copyPhonebook.IPs.get(0))) {
                                response = new Response();
                                response.status = "404 Not Found";
                                response.body = new JSONObject();
                            }
                            */

                    System.out.println("Is the copyPhoneBook size == 1? ");
                    if (copyPhonebook.IPs.size() == 1) {
                        System.out.println("The size of copyPhoneBook is 1, we get a new phonebook");
                        request = new Request("get", "getPhoneBook", new JSONObject());
                        String newMessage = request.toString();
                        System.out.println("Sending this request: " + newMessage);
                        outstream.writeUTF(newMessage);
                        outstream.flush();

                        // TODO implement something to check if response is coming or not if not ask new person
                        String messageFromServer = instream.readUTF();
                        System.out.println("This is the response: " + messageFromServer);
                        response = new Response(messageFromServer);

                        JSONArray jsonPhoneBookCopy;
                        try {
                            jsonPhoneBookCopy = response.body.getJSONArray("LeftNeighbors");
                            for (int i = 0; i < jsonPhoneBookCopy.length(); i++) {
                                copyPhonebook.IPs.add(jsonPhoneBookCopy.getJSONObject(i).getString("IP"));
//                                        System.out.println(" - " + copyPhonebook.IPs.get());
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("The copyPhonebook is not == 1");

                        outstream.writeUTF(originalRequest.toString());
                        outstream.flush();
                        System.out.println("This is the request we send: " + originalRequest.toString());
                        String messageFromServer = instream.readUTF();
                        System.out.println("This is the response we got: " + messageFromServer);
                        response = new Response(messageFromServer);

                        if (response.status.contains("OK")) {
                            hasGottenData = true;

                            JSONObject jsonBody = new JSONObject();

                            try {
                                jsonBody.put("IP", node.IP);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            response.body = jsonBody;
                        }
                    } // else for copyPhoneBook == 1
                    connectionToServer.close();
                    copyPhonebook.IPs.remove(0);

                } catch (IOException e) {
                    System.out.println("problem is: " + e.toString());
                    throw new RuntimeException(e);
                }
            } // while we havent gotten the data
            return response;
        } // else for response.status contain ok)
    } // getDataHandler

    public  Response sendRequestToNeighbor(String IP, Request request) throws IOException, JSONException {
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




}