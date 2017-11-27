package com.latuarisposta;

import java.util.ArrayList;

public class ProbFuse {

    public static void main(String[] args){
        double t= 1;
        ArrayList<Integer> train_queries = new ArrayList<>();

        while(train_queries.size()<t*50){
            int tmp = (int)(Math.random()*50);
            if(!train_queries.contains(tmp)){
                train_queries.add(tmp);
            }
        }


        for(int i=0; i<10;i++){
        }

    }
}
