package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormSignature;
import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormSignatureRepository extends JpaRepository<FormSignature, Long> {
    Optional<FormSignature> findBySubmission(FormSubmission submission);
}
