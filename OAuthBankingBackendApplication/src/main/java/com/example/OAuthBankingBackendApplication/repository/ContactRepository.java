package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.entity.Contact;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Only the inherited {@code save} is used; enquiries are never read back yet.
 */
@Repository
public interface ContactRepository extends CrudRepository<Contact, String> {
}
