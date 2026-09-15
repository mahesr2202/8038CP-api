package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
    List<FormSubmission> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<FormSubmission> findByIdAndUserId(Long id, Long userId);
}
