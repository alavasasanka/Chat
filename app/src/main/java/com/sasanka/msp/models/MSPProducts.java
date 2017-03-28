package com.sasanka.msp.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Model for getting hardcoded products list.
 */
public class MSPProducts {

    List<HashMap<String, String>> mList;
    private static MSPProducts sInstance;

    private MSPProducts() {
        mList = new ArrayList<>();
        HashMap<String, String> map1 = new HashMap<>();
        map1.put("name", "HP Laptop");
        map1.put("price", "Rs.50000");
        HashMap<String, String> map2 = new HashMap<>();
        map2.put("name", "iPhone 5S");
        map2.put("price", "Rs.40000");
        HashMap<String, String> map3 = new HashMap<>();
        map3.put("name", "iPod");
        map3.put("price", "Out of Stock");
        HashMap<String, String> map4 = new HashMap<>();
        map4.put("name", "Bose");
        map4.put("price", "Rs.10000");
        HashMap<String, String> map5 = new HashMap<>();
        map5.put("name", "One Plus One");
        map5.put("price", "Rs.22000");
        mList.add(map1);
        mList.add(map2);
        mList.add(map3);
        mList.add(map4);
        mList.add(map5);
    }

    public static MSPProducts getInstance() {
        if (sInstance == null) {
            sInstance = new MSPProducts();
        }
        return sInstance;
    }

    public List<HashMap<String, String>> getList() {
        return mList;
    }

}
