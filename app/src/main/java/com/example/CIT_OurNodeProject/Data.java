package com.example.CIT_OurNodeProject;

import java.util.Random;

public class Data {

    Random numGen = new Random();
    String id;
    String value;

    public Data(String value) {

        this.id =  hash(value);
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
