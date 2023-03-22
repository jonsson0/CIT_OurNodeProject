package com.example.CIT_OurNodeProject;

import java.util.Random;

public class Data {

    Random numGen = new Random();
    String id;
    String value;

    Boolean isParent;



    public Data(String value, Boolean isParent) {

        this.id =  hash(value);
        this.isParent=isParent;
        this.value = value;
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
