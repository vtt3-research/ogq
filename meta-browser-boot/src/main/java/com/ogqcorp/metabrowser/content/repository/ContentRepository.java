package com.ogqcorp.metabrowser.content.repository;

import com.ogqcorp.metabrowser.domain.Content;
import com.ogqcorp.metabrowser.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends PagingAndSortingRepository<Content, Long>, JpaSpecificationExecutor<Content> {

    Page<Content> findAllByUserId(Pageable pageable, Integer userId);

}
