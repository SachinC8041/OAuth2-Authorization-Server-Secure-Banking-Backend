package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.entity.Notice;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends CrudRepository<Notice, Long> {

    /**
     * Notices whose display window covers today.
     *
     * <p>Rewritten from {@code CURDATE() BETWEEN noticBegDt AND noticEndDt}, which
     * had two problems: {@code CURDATE()} is MySQL syntax rather than JPQL, tying
     * the query to one database, and the fields were left unqualified even though
     * the alias {@code n} was declared. {@code current_date} is standard JPQL and
     * Hibernate translates it per dialect, so this now also runs on H2 under test.
     */
    @Query("select n from Notice n where current_date between n.noticeBegDt and n.noticeEndDt")
    List<Notice> findAllActiveNotices();


    /* ======================================================================
     * ARCHIVED - nothing below this line is active. Kept for revision.
     * ======================================================================
     */

    /* ----------------------------------------------------------------------
     * [1] The original query.
     *
     *     Two portability problems. CURDATE() is a MySQL function, not JPQL, so
     *     Hibernate passes it through untranslated and the query dies on any
     *     other database - which is exactly what breaks the H2 test suite. And
     *     the alias n is declared but the fields are left unqualified, which
     *     Hibernate tolerates but no other JPA provider has to.
     *
     *     current_date is the standard JPQL form and Hibernate renders it per
     *     dialect.
     * ----------------------------------------------------------------------
     *
     * @Query(value = "from Notice n where CURDATE() BETWEEN noticBegDt AND noticEndDt")
     * List<Notice> findAllActiveNotices();
     */
}
