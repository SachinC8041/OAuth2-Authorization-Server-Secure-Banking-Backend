package com.example.OAuthBankingBackendApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticeController
{
    @GetMapping("/mynotices")
    public String getNotices()
    {
        return "Notice Controller";
    }
}
