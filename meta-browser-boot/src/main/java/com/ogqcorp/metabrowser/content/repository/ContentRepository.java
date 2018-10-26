package com.ogqcorp.metabrowser.content.repository;

import com.ogqcorp.metabrowser.domain.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends PagingAndSortingRepository<Content, Long> {

    Page<Content> findAllByUserId(Pageable pageable, Integer userId);
}
