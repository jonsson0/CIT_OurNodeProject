package com.example.CIT_OurNodeProject;

import java.util.Random;

public class Data {

    Random numGen = new Random();
    String id;
    String data;

    boolean isParentData;

    public Data(String data) {

        this.id =  hash(data);
        this.data = data;
    }

    public void getDataObject(String dataId){

    }

    public String hash(String data) {
        String hashedData = "";
        try {
            hashedData = SHA256.toHexString(SHA256.getSHA(data));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return hashedData;
    }
}
