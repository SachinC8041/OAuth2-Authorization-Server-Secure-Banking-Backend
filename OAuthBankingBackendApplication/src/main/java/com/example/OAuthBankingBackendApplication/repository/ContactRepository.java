package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.entity.Contact;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Only the inherited CrudRepository methods are used (save).
 */
@Repository
public interface ContactRepository extends CrudRepository<Contact, String> {
}
