package com.example.CIT_OurNodeProject;

import org.json.JSONException;

import java.io.IOException;

public interface IClientManager {

    Node node = null;

    Response handleResponseFromServer
            (Request request, Response response)
            throws JSONException, IOException;


    Request generateRequest_UpdatePhoneBook(PhoneBook phoneBook, String side);
        // public Request buildRequestToUpdatePhoneBook(PhoneBook phoneBook, String side) {

    Request generateRequest_GetData(String value);
        // public Request buildRequestToGetData(String value)

    Request generateRequest_AddData(String value, Boolean isParent);
        // public Request buildRequestToAddData(String value, Boolean isParent) {

    Response generateRequest_DeleteData(Request request) throws IOException, JSONException;
        // public Response DeleteData(Request request) throws IOException, JSONException {








    }
