package com.cleanrepo.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByEmail_Email(String email);
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.surveys WHERE a.host.accountId = ?1")
    List<Account> findAllUsersConnectedToHostN1(int adminId);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.surveys WHERE a.admin.accountId = ?1")
    List<Account> findAllUsersConnectedToAdminN1(int adminId);
}
