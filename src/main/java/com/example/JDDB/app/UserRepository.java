package com.example.JDDB.app;

import com.example.JDDB.lib.repository.DiscordRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends DiscordRepository<Long, User> {
}
