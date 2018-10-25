package com.ogqcorp.metabrowser.account.repository;

import com.ogqcorp.metabrowser.domain.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Integer> {

    Optional<User> findByEmail(String email);

}
