package com.example.CIT_OurNodeProject;

import org.json.JSONException;

import java.io.IOException;

public interface IServerManager {



    // switch cases that looks at different types of requests
    Response handleRequestFromClient(Request request, String clientIP);

    /* Taking care of what happens with the different client requests */
    Response generateResponse_GetId();
        //Response buildResponseToGetId(Response response);

    Response generateResponse_UpdatePhonebook(Request request);
        // public Response buildResponseToUpdatePhonebook(Request request) {

    Response generateResponse_GetPhonebook(Request request);
        // public Response buildResponseToGetPhonebook() {

    Response generateResponse_GetData(Request request);
        // public Response buildResponseToGetData(Response response, Request request) {

    Response generateResponse_AddData(Request request)
            throws IOException, JSONException;
        // public Response buildResponseToAddData(Request request) throws IOException, JSONException {

    Response generateResponse_DeleteData(Response response);



}
