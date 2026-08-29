package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Notice;
import com.example.OAuthBankingBackendApplication.service.NoticeService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * Public notices. No authentication required.
 */
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private static final Duration CACHE_DURATION = Duration.ofSeconds(60);

    private final NoticeService noticeService;

    /**
     * @return notices whose display window covers today, cached briefly since the
     *         same list is served to every visitor
     */
    @GetMapping("/notices")
    public ResponseEntity<List<Notice>> getNotices() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(CACHE_DURATION))
                .body(noticeService.findActiveNotices());
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original controller, with the repository injected directly and
     *     the cache duration expressed as a long plus a TimeUnit.
     *
     * Imports:
     *   com.example.OAuthBankingBackendApplication.repository.NoticeRepository
     *   java.util.concurrent.TimeUnit
     * ----------------------------------------------------------------------
     *
     * private static final long CACHE_SECONDS = 60L;
     * private final NoticeRepository noticeRepository;
     *
     * @GetMapping("/notices")
     * public ResponseEntity<List<Notice>> getNotices() {
     *     List<Notice> notices = noticeRepository.findAllActiveNotices();
     *
     *     return ResponseEntity.ok()
     *             .cacheControl(CacheControl.maxAge(CACHE_SECONDS, TimeUnit.SECONDS))
     *             .body(notices);
     * }
     */
}
