package com.youngwon.backoffice.repository;

import com.youngwon.backoffice.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByShopIdAndEmail(Long shopId, String email);

    Optional<User> findByIdAndShopId(Long id, Long shopId);
}