package com.example.CIT_OurNodeProject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ClientManager implements IClientManager{

    Node node;


    public ClientManager(Node node) {
        this.node = node;
    }

    public Response handleResponseFromServer(Request request, Response response) throws JSONException, IOException {

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
        return null;
    }

    @Override
    public Response generateRequest_DeleteData(Request request) throws IOException, JSONException {
        return null;
    }








}
