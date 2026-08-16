package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Notice;
import com.example.OAuthBankingBackendApplication.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private static final long CACHE_SECONDS = 60L;

    private final NoticeRepository noticeRepository;

    @GetMapping("/notices")
    public ResponseEntity<List<Notice>> getNotices() {
        List<Notice> notices = noticeRepository.findAllActiveNotices();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(CACHE_SECONDS, TimeUnit.SECONDS))
                .body(notices);
    }
}
