package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormPartIII;
import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormPartIIIRepository extends JpaRepository<FormPartIII, Long> {
    Optional<FormPartIII> findBySubmission(FormSubmission submission);
}
