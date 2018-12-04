package com.ogqcorp.metabrowser.content.repository;

import com.ogqcorp.metabrowser.domain.Shot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShotRepository extends CrudRepository<Shot, Long> {
}
