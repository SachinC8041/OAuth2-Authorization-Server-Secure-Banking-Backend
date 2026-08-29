package com.example.OAuthBankingBackendApplication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * A support enquiry raised through the public contact form.
 */
@Entity
@Table(name = "contact_messages")
@Getter
@Setter
public class Contact {

    /** Service request number, generated on save, e.g. {@code SR123456}. */
    @Id
    @Column(name = "contact_id")
    private String contactId;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    private String subject;

    private String message;

    @Column(name = "create_dt")
    private Date createDt;
}
