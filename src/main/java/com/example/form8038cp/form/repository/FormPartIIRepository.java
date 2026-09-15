package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormPartII;
import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormPartIIRepository extends JpaRepository<FormPartII, Long> {
    Optional<FormPartII> findBySubmission(FormSubmission submission);
}
