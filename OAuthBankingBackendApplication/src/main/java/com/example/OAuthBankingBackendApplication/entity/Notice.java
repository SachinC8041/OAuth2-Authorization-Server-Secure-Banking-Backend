package com.example.OAuthBankingBackendApplication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * A public announcement shown on the landing page for a fixed date range.
 */
@Entity
@Table(name = "notice_details")
@Getter
@Setter
public class Notice {

    @Id
    @Column(name = "notice_id")
    private long noticeId;

    @Column(name = "notice_summary")
    private String noticeSummary;

    @Column(name = "notice_details")
    private String noticeDetails;

    /*
     * These two fields were called noticBegDt / noticEndDt. The Java names are now
     * spelled correctly; @JsonProperty pins the wire format to the old keys so no
     * client has to change at the same time. Drop the annotations once the
     * frontend reads the corrected names.
     */
    @JsonProperty("noticBegDt")
    @Column(name = "notice_beg_dt")
    private Date noticeBegDt;

    @JsonProperty("noticEndDt")
    @Column(name = "notice_end_dt")
    private Date noticeEndDt;

    @JsonIgnore
    @Column(name = "create_dt")
    private Date createDt;

    @JsonIgnore
    @Column(name = "update_dt")
    private Date updateDt;


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original field names, misspelled.
     *
     *     Jackson derives JSON keys from the field names, so renaming these
     *     would have changed the payload the Angular client receives. That is
     *     what @JsonProperty is for: it pins the wire format while the Java name
     *     gets fixed, letting the two change independently. Drop the annotations
     *     once the frontend reads the corrected keys.
     *
     *     The JPQL in NoticeRepository refers to these by their Java names, so
     *     it has to change at the same time - see the archived block there.
     * ----------------------------------------------------------------------
     *
     * @Column(name = "notice_beg_dt")
     * private Date noticBegDt;
     *
     * @Column(name = "notice_end_dt")
     * private Date noticEndDt;
     */
}
