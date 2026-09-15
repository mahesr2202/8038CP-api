package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormPaidPreparer;
import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormPaidPreparerRepository extends JpaRepository<FormPaidPreparer, Long> {
    Optional<FormPaidPreparer> findBySubmission(FormSubmission submission);
}
