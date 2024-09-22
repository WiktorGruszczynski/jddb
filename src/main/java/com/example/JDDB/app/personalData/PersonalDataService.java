package com.example.JDDB.app.personalData;

import com.example.JDDB.lib.core.repository.DiscordRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PersonalDataService {
    private final DiscordRepository<PersonalData, String> personalDataRepository;

    public PersonalDataService() {
        this.personalDataRepository = new DiscordRepository<>(PersonalData.class);
    }

    public PersonalData save(){
        return personalDataRepository.save(new PersonalData(
                "John",
                "Pork",
                false,
                25_000,
                new Date()
        ));
    }

    public PersonalData getById(String id) {
        return personalDataRepository.findById(id).orElse(null);
    }
}
