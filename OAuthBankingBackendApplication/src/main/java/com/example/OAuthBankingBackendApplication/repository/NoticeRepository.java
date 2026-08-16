package com.example.OAuthBankingBackendApplication.repository;

import com.example.OAuthBankingBackendApplication.entity.Notice;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends CrudRepository<Notice, Long> {

    // FIXME: two portability problems with this JPQL.
    //   1. CURDATE() is a MySQL function, not JPQL. The portable form is
    //      current_date, which Hibernate translates per dialect.
    //   2. The alias n is declared but the fields are unqualified.
    // Portable version:
    //   select n from Notice n where current_date between n.noticBegDt and n.noticEndDt
    @Query(value = "from Notice n where CURDATE() BETWEEN noticBegDt AND noticEndDt")
    List<Notice> findAllActiveNotices();
}
