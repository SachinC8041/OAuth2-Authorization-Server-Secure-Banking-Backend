package com.example.OAuthBankingBackendApplication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

/**
 * A bank customer. The e-mail address doubles as the login name.
 */
@Entity
@Table(name = "customer")
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    /**
     * The stored password hash, prefixed with its algorithm, e.g. {@code {bcrypt}$2a$...}.
     *
     * <p>Write-only over JSON: accepted on registration, never serialised back out.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String pwd;

    /**
     * Free-text label shown in the UI.
     *
     * <p>Not used for access control - authorisation is driven entirely by the
     * {@code authorities} table via {@link #authorities}.
     */
    private String role;

    @JsonIgnore
    @Column(name = "create_dt")
    private Date createDt;

    /**
     * Eagerly fetched because {@code BankUserDetailsService} reads them immediately
     * after loading the customer, outside any open persistence context.
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Authority> authorities;
}
