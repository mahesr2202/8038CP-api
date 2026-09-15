package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormDirectDeposit;
import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormDirectDepositRepository extends JpaRepository<FormDirectDeposit, Long> {
    Optional<FormDirectDeposit> findBySubmission(FormSubmission submission);
}
