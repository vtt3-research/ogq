package com.ogqcorp.metabrowser.content.repository;

import com.ogqcorp.metabrowser.domain.Tag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends CrudRepository<Tag, Long> {
    Boolean existsByStr(String str);
}
