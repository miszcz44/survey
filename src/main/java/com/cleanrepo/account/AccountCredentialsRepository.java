package com.cleanrepo.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountCredentialsRepository extends JpaRepository<AccountCredentials, Integer> {

    Optional<AccountCredentials> findByEmail_Email(String email);
}




