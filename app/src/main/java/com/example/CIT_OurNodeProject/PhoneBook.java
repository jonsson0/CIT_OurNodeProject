package com.example.CIT_OurNodeProject;

import java.util.ArrayList;
import org.json.*;

public class PhoneBook {

    ArrayList<String> IPs = new ArrayList<>();


    public PhoneBook copy(){
        PhoneBook newPhonebook = new PhoneBook();
        for (String ip: this.IPs) {
            newPhonebook.IPs.add(ip);
        }
        return newPhonebook;
    }


    public String toString(){



        for (String Ip: IPs ) {
            JSONObject json = new JSONObject();

        }

        return IPs.toString();
    }
}