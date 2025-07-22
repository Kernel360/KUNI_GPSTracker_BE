package com.example.car_emulator_server.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class CSV {
    //mdn,tid,mid,pv,did,cCnt,timestamp,sec,gcd,lat,lon,ang,spd,sum,bat

    @CsvBindByName(column = "sec")
    private String sec;


    @CsvBindByName(column = "gcd")
    private String gcd;


    @CsvBindByName(column = "lat")
    private String lat;


    @CsvBindByName(column = "lon")
    private String lon;


    @CsvBindByName(column = "ang")
    private String ang;


    @CsvBindByName(column = "spd")
    private String spd;


    @CsvBindByName(column = "sum")
    private String sum;


    @CsvBindByName(column = "bat")
    private String bat;
}
