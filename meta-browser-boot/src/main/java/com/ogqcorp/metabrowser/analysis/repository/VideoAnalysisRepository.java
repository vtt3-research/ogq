package com.ogqcorp.metabrowser.analysis.repository;

import com.ogqcorp.metabrowser.domain.Shot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VideoAnalysisRepository extends CrudRepository<Shot, Long> {

    Iterable<Shot> findAllByContentId(Long contentId);
}
