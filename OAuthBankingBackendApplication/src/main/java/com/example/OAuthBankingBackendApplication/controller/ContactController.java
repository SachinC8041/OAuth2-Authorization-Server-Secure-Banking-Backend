package com.example.OAuthBankingBackendApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController
{
    @GetMapping("/mycontact")
    public String getContactDetails()
    {
        return "Contact Controller";
    }
}
