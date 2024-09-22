package com.example.JDDB.app.order;

import com.example.JDDB.lib.core.repository.DiscordRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderService {
    private final DiscordRepository<Order, String> orderRepository;

    public OrderService() {
        this.orderRepository = new DiscordRepository<>(Order.class);
    }

    public Order addOrder() {
        return orderRepository.save(new Order(
                "2314212114142",
                new Date(),
                5000
        ));
    }
}
