package com.example.JDDB.app.order;

import com.example.JDDB.lib.annotations.Table;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Table(name = "user_orders")
public class Order {
    @Id
    private String ownerId;
    private Date date;
    private Integer price;

    public Order(String ownerId, Date date, Integer price) {
        this.ownerId = ownerId;
        this.date = date;
        this.price = price;
    }
}
