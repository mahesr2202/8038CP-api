package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormPartI;
import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormPartIRepository extends JpaRepository<FormPartI, Long> {
    Optional<FormPartI> findBySubmission(FormSubmission submission);
}
