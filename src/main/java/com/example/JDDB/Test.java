package com.example.JDDB;

import com.example.JDDB.app.personalData.PersonalData;
import com.example.JDDB.lib.core.codec.Codec;
import com.example.JDDB.lib.utils.AllowedType;


import java.util.Date;

public class Test {
    public static void main(String[] args) {
        Codec<PersonalData> codec = new Codec<>(PersonalData.class);

        PersonalData personalData = new PersonalData(
                "Adam",
                null,
                true,
                2500,
                null
        );

        String data = codec.encode(personalData);




        PersonalData personalData1 = codec.decode("123", data);

        System.out.println(personalData1);
    }
}
