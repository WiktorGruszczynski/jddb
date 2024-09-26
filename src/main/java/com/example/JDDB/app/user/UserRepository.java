package com.example.JDDB.app.user;

import com.example.JDDB.lib.core.repository.DiscordRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends DiscordRepository<User, Long> {
}
