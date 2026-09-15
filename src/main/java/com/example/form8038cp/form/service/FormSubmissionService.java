package com.example.form8038cp.form.service;

import com.example.form8038cp.form.dto.DirectDepositRequest;
import com.example.form8038cp.form.dto.FormSubmissionResponse;
import com.example.form8038cp.form.dto.PaidPreparerRequest;
import com.example.form8038cp.form.dto.PartIIIRequest;
import com.example.form8038cp.form.dto.PartIIRequest;
import com.example.form8038cp.form.dto.PartIRequest;
import com.example.form8038cp.form.dto.ScheduleARowRequest;
import com.example.form8038cp.form.dto.ScheduleARowResponse;
import com.example.form8038cp.form.dto.SignatureRequest;
import com.example.form8038cp.form.dto.SubmitRequest;

import java.util.List;

public interface FormSubmissionService {
    FormSubmissionResponse createDraft(Long userId);
    FormSubmissionResponse savePartI(Long userId, Long id, PartIRequest req);
    FormSubmissionResponse savePartII(Long userId, Long id, PartIIRequest req);
    FormSubmissionResponse savePartIII(Long userId, Long id, PartIIIRequest req);
    FormSubmissionResponse saveDirectDeposit(Long userId, Long id, DirectDepositRequest req);
    FormSubmissionResponse saveSignature(Long userId, Long id, SignatureRequest req);
    FormSubmissionResponse savePreparer(Long userId, Long id, PaidPreparerRequest req);
    List<ScheduleARowResponse> saveScheduleA(Long userId, Long id, List<ScheduleARowRequest> rows);
    List<ScheduleARowResponse> getScheduleA(Long userId, Long id);
    FormSubmissionResponse submit(Long userId, Long id, SubmitRequest req);
    String previewXml(Long userId, Long id);
    List<FormSubmissionResponse> getMySubmissions(Long userId);
    FormSubmissionResponse getById(Long userId, Long id);
    void deleteDraft(Long userId, Long id);
}
