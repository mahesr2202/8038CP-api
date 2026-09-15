package com.example.form8038cp.auth.repository;

import com.example.form8038cp.auth.entity.EmailVerificationChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationChallengeRepository extends JpaRepository<EmailVerificationChallenge, Long> {

    Optional<EmailVerificationChallenge> findByChallengeId(String challengeId);
}
