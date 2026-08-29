package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Notice;
import com.example.OAuthBankingBackendApplication.service.NoticeService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoticeService noticeService;

    @Test
    @DisplayName("active notices are returned with a cache header")
    void returnsNoticesWithCacheHeader() throws Exception {
        Notice notice = new Notice();
        notice.setNoticeId(1L);
        notice.setNoticeSummary("Interest rates revised");
        when(noticeService.findActiveNotices()).thenReturn(List.of(notice));

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=60"))
                .andExpect(jsonPath("$[0].noticeSummary").value("Interest rates revised"));
    }

    @Test
    @DisplayName("the corrected field names are still published under the original JSON keys")
    void preservesLegacyJsonKeys() throws Exception {
        Notice notice = new Notice();
        notice.setNoticeId(1L);
        notice.setNoticeBegDt(java.sql.Date.valueOf("2026-01-01"));
        notice.setNoticeEndDt(java.sql.Date.valueOf("2026-12-31"));
        when(noticeService.findActiveNotices()).thenReturn(List.of(notice));

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].noticBegDt").exists())
                .andExpect(jsonPath("$[0].noticEndDt").exists());
    }

    @Test
    @DisplayName("no active notices yields an empty array, not an error")
    void returnsEmptyArray() throws Exception {
        when(noticeService.findActiveNotices()).thenReturn(List.of());

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
