package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Notice;
import com.example.OAuthBankingBackendApplication.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Public notices. No ownership check: this content is the same for everyone,
 * which is why the method takes no {@code Authentication}.
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * @return notices whose display window covers today
     */
    @Transactional(readOnly = true)
    public List<Notice> findActiveNotices() {
        return noticeRepository.findAllActiveNotices();
    }
}
