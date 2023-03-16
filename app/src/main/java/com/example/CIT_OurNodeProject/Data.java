package com.example.CIT_OurNodeProject;

import java.util.Random;

public class Data {

    Random numGen = new Random();
    String id;
    String data;

    public Data(String data) {

        this.id =  hash(data);
        this.data = data;
    }


    public String hash(String data) {

        String hashedData = data + " (Hashed)";
        return hashedData;
    }
}
