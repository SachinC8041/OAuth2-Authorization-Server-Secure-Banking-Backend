package com.example.OAuthBankingBackendApplication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Entity
@Getter
@Setter
@Table(name = "notice_details")
public class Notice {

    @Id
    @Column(name = "notice_id")
    private long noticeId;

    @Column(name = "notice_summary")
    private String noticeSummary;

    @Column(name = "notice_details")
    private String noticeDetails;

    // NOTE: field names are misspelled (noticBegDt / noticEndDt). Renaming them
    // changes the JSON keys the frontend receives, so they are left as-is.
    @Column(name = "notice_beg_dt")
    private Date noticBegDt;

    @Column(name = "notice_end_dt")
    private Date noticEndDt;

    @JsonIgnore
    @Column(name = "create_dt")
    private Date createDt;

    @JsonIgnore
    @Column(name = "update_dt")
    private Date updateDt;
}
