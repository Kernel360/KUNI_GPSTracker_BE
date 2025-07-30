package com.example.emulator.util;

import java.io.IOException;
import java.util.*;

public class GenerateNumber {
    public static void main(String[] args) throws IOException {
        Set<String> set = new HashSet<String>();

        List<String> list = List.of("하","허","호");
        String number = "";

        while(set.size() < 500){
            Random random = new Random();
            int first = random.nextInt(10,100);
            int last =  random.nextInt(1000,10000);
            int middle = random.nextInt(3);

            number = String.valueOf(first) + list.get(middle) + String.valueOf(last);
            set.add(number);
        }

        Iterator<String> iterator = set.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }

    }


}
