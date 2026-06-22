package com.example.OAuthBankingBackendApplication.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController
{
    @PostMapping("/mycontact")
    public String getContactDetails()
    {
        return "Contact Controller";
    }
}
