package com.example.form8038cp.form.repository;

import com.example.form8038cp.form.entity.FormScheduleA;
import com.example.form8038cp.form.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormScheduleARepository extends JpaRepository<FormScheduleA, Long> {
    List<FormScheduleA> findBySubmissionOrderByRowOrder(FormSubmission submission);
    void deleteBySubmission(FormSubmission submission);
}
