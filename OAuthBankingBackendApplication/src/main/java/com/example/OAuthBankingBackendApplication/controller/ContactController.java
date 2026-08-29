package com.example.OAuthBankingBackendApplication.controller;

import com.example.OAuthBankingBackendApplication.entity.Contact;
import com.example.OAuthBankingBackendApplication.service.ContactService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public contact form. No authentication required.
 */
@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Records an enquiry and returns it with its generated service request number.
     */
    @PostMapping("/contact")
    public ResponseEntity<Contact> saveContactInquiryDetails(@RequestBody Contact contact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.saveInquiry(contact));
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original controller, with the reference-number generation and the
     *     timestamping done inline. Returned 200 rather than 201 for a create.
     *
     *     The Random-versus-SecureRandom point is archived in ContactService.
     *
     * Imports:
     *   com.example.OAuthBankingBackendApplication.repository.ContactRepository
     *   java.util.Date
     *   java.util.Random
     * ----------------------------------------------------------------------
     *
     * private static final Random RANDOM = new Random();
     * private final ContactRepository contactRepository;
     *
     * @PostMapping("/contact")
     * public Contact saveContactInquiryDetails(@RequestBody Contact contact) {
     *     contact.setContactId(getServiceReqNumber());
     *     contact.setCreateDt(new Date(System.currentTimeMillis()));
     *
     *     return contactRepository.save(contact);
     * }
     *
     * private String getServiceReqNumber() {
     *     int ranNum = RANDOM.nextInt(999999999 - 9999) + 9999;
     *     return "SR" + ranNum;
     * }
     */
}
