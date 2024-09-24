package com.example.JDDB;

import com.example.JDDB.app.user.User;
import com.example.JDDB.lib.core.Codec;

public class Test {
    public static void main(String[] args) {
        Codec<User> codec = new Codec<>(User.class);

        User user = codec.decode("123", "4$Bill14$bill@gmail.com2$35");

        System.out.println(user);
    }
}
