package com.ogqcorp.metabrowser.analysis.repository;

import com.ogqcorp.metabrowser.domain.Shot;
import com.ogqcorp.metabrowser.domain.Shot2;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VideoAnalysisRepository extends CrudRepository<Shot2, Long> {

    Iterable<Shot2> findAllByContentId(Long contentId);
}
