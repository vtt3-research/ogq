package com.ogqcorp.metabrowser.content.repository;

import com.ogqcorp.metabrowser.domain.Contents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends PagingAndSortingRepository<Contents, Long> {

    Page<Contents> findAllByAccountId(Pageable pageable, String accountId);
}
