package com.example.CIT_OurNodeProject;

import java.util.ArrayList;

public class Neighbor {

    String IP;

    ArrayList<Data> listOfData = new ArrayList<Data>();
    public boolean deleteDataLocally(String Value) {
        for (int i = 0; i < listOfData.size(); i++) {
            //if our node has the data
            if (listOfData.get(i).equals(Value)) {
                listOfData.remove(listOfData.get(i));
                return true;
            }
        }
        return false;
    }

}


