package com.example.CIT_OurNodeProject;

import org.json.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ApiHandler {

    Node node;

    public ApiHandler(Node node) {
        this.node = node;
    }

    public Request readHttpRequest(String input) {

        Request request = new Request(input);

        return request;
    }

    public Response readHttpResponse(String input) {

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
            case "updatephonebook":
                response = buildResponseToNewNeighbor(response, IP);
                break;

            case "getphonebook":
                response = buildResponseToGetPhonebook(response);
                break;
            case "getdata":
                response = buildResponseToGetData(response, request);
                break;
            case "adddata":

                break;
            default:
                buildResponseBadRequest(response);
                break;
        }

        return response;
    }

    public Response respondHandler(Request request, Response response) {

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

                Request originalRequest = new Request(request.method, request.path, request.body);

                if (response.status.contains("OK")) {
                    return response;
                } else {
                    boolean hasGottenData = false;
                    PhoneBook copyPhonebook = node.phoneBookLeft.copy();

                    System.out.println("THIS IS THE IPS:");
                    for (String IP: copyPhonebook.IPs) {
                        System.out.println(IP);
                    }
                    while (!hasGottenData) {
                        System.out.println("1");
                        String IP = copyPhonebook.IPs.get(0);
                        Socket connectionToServer;
                        try {
                            connectionToServer = new Socket(IP, 4444);
                            System.out.println("2");
                            DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                            System.out.println("3");
                            DataOutputStream outstream = new DataOutputStream(connectionToServer.getOutputStream());
                            System.out.println("4");
                            System.out.println(instream.readUTF());
                            String messageFromServer = instream.readUTF();
                            System.out.println("5");
                            response = new Response(messageFromServer);

                            System.out.println("2");
                            /*
                            if (node.IP.equals(copyPhonebook.IPs.get(0))) {
                                response = new Response();
                                response.status = "404 Not Found";
                                response.body = new JSONObject();
                            }
                            */

                            if (copyPhonebook.IPs.size() == 1) {
                                request = new Request("get", "getPhoneBook", new JSONObject());
                                String newMessage = request.toString();
                                System.out.println("3");
                                outstream.writeUTF(newMessage);
                                outstream.flush();
                                messageFromServer = instream.readUTF();
                                response = new Response(messageFromServer);
                                JSONArray jsonPhoneBookCopy;
                                try {
                                    jsonPhoneBookCopy = response.body.getJSONArray("LeftNeighbors");
                                    for (int i = 0; i < jsonPhoneBookCopy.length(); i++) {
                                        copyPhonebook.IPs.add(jsonPhoneBookCopy.getString(i));
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }

                                System.out.println("4");
                            } else {
                                JSONObject jsonBody = new JSONObject();
                                try {
                                    jsonBody.put("Id", "left");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                request = new Request("get", "getData", jsonBody);
                                System.out.println("5");
                                outstream.writeUTF(originalRequest.toString());
                                outstream.flush();
                                System.out.println();
                                messageFromServer = instream.readUTF();
                                System.out.println("Response from server: " + messageFromServer);

                                response = new Response(messageFromServer);
                                if (response.status.contains("OK")) {
                                    hasGottenData = true;
                                    break;
                                }
                            }
                            connectionToServer.close();
                            copyPhonebook.IPs.remove(copyPhonebook.IPs.get(0));
                            System.out.println("Phonebook: " + copyPhonebook.IPs );

                        } catch (IOException e) {
                            System.out.println(e.toString());
                            throw new RuntimeException(e);
                        }
                    }
                }
            break;
            case "adddata":

                break;
            default:

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
    public Response buildResponseToGetData(Response response, Request request) {

        String id;
        try {
            id = request.body.getString("Id");
          //  hashedValue = SHA256.toHexString(SHA256.getSHA(value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Data data : node.listOfData) {
            if (data.id.equals(id)) {

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
            }else {
                response.status = "404 Not Found";
                response.body = new JSONObject();
            }
        }
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
