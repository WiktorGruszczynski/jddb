package com.example.JDDB.app.personalData;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/personalData")
public class PersonalDataController {
    private final PersonalDataService personalDataService;

    public PersonalDataController(PersonalDataService personalDataService) {
        this.personalDataService = personalDataService;
    }

    @PostMapping
    public PersonalData add(){
        return personalDataService.save();
    }

    @GetMapping
    public PersonalData getById(@RequestParam("id") String id){
        return personalDataService.getById(id);
    }
}
