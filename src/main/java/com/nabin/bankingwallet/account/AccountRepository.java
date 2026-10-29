package com.nabin.bankingwallet.account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);

    List<Account> findAllByUser_IdOrderByIdAsc(Long userId);

    Optional<Account> findByIdAndUser_Id(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT acc from Account acc where acc.id=:id and acc.user.id=:userId")
    Optional<Account> findByIdAndUser_IdForUpdate(
            @Param("id")
            Long id,
            @Param("userId")
            Long userId);
}
