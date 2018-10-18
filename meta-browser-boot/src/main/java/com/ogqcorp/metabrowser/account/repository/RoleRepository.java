package com.ogqcorp.metabrowser.account.repository;

import com.ogqcorp.metabrowser.domain.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
}
