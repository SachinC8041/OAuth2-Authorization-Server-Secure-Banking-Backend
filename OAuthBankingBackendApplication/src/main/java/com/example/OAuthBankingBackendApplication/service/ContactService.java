package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.Contact;
import com.example.OAuthBankingBackendApplication.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;

/**
 * Public contact form handling.
 *
 * <p>Generating the service request number is business logic, not request
 * handling, which is why it moved out of the controller.
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SERVICE_REQUEST_BOUND = 1_000_000_000;

    private final ContactRepository contactRepository;

    /**
     * Stamps an enquiry with a reference number and a timestamp, then saves it.
     */
    @Transactional
    public Contact saveInquiry(Contact contact) {
        contact.setContactId(nextServiceRequestNumber());
        contact.setCreateDt(new Date(System.currentTimeMillis()));

        return contactRepository.save(contact);
    }

    /**
     * Builds a reference such as {@code SR417203918}.
     *
     * <p>{@link SecureRandom} rather than {@code Random}: the number is handed to
     * the customer as their reference, so one reference should not let you guess
     * the next.
     */
    private String nextServiceRequestNumber() {
        return "SR" + RANDOM.nextInt(SERVICE_REQUEST_BOUND);
    }


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original generator, which lived in ContactController.
     *
     *     Two things to notice. java.util.Random is seeded predictably, so
     *     reference numbers are guessable from one another. And the bound
     *     arithmetic reads oddly: nextInt(999999999 - 9999) + 9999 produces a
     *     number in [9999, 999999999), not the "at least 4 digits" it looks like.
     *
     * Imports:
     *   java.util.Random
     * ----------------------------------------------------------------------
     *
     * private static final Random RANDOM = new Random();
     *
     * private String getServiceReqNumber() {
     *     int ranNum = RANDOM.nextInt(999999999 - 9999) + 9999;
     *     return "SR" + ranNum;
     * }
     */
}
