package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Contact;
import com.example.OAuthBankingBackendApplication.service.ContactService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Filters are switched off here so the test covers the controller's own
 * behaviour. The security rules protecting these paths are covered end to end in
 * {@code SecurityFlowIntegrationTest}.
 */
@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    @Test
    @DisplayName("an enquiry is passed to the service and answered with 201")
    void savesEnquiry() throws Exception {
        when(contactService.saveInquiry(any(Contact.class)))
                .thenAnswer(invocation -> {
                    Contact submitted = invocation.getArgument(0);
                    submitted.setContactId("SR123456789");
                    return submitted;
                });

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactName": "Sachin",
                                  "contactEmail": "sachin@example.com",
                                  "subject": "Card not working",
                                  "message": "My card was declined at the ATM."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactId").value("SR123456789"))
                .andExpect(jsonPath("$.contactName").value("Sachin"));
    }

    @Test
    @DisplayName("the deserialised body reaches the service intact")
    void passesBodyToService() throws Exception {
        when(contactService.saveInquiry(any(Contact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contactName\":\"Sachin\",\"subject\":\"Query\"}"))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Contact> captor = org.mockito.ArgumentCaptor.forClass(Contact.class);
        verify(contactService).saveInquiry(captor.capture());

        org.assertj.core.api.Assertions.assertThat(captor.getValue().getContactName()).isEqualTo("Sachin");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getSubject()).isEqualTo("Query");
    }
}
