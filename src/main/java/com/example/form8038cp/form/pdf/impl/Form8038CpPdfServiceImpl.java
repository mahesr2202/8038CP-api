package com.example.form8038cp.form.pdf.impl;

import com.example.form8038cp.exception.ResourceNotFoundException;
import com.example.form8038cp.form.entity.FormDirectDeposit;
import com.example.form8038cp.form.entity.FormPaidPreparer;
import com.example.form8038cp.form.entity.FormPartI;
import com.example.form8038cp.form.entity.FormPartII;
import com.example.form8038cp.form.entity.FormPartIII;
import com.example.form8038cp.form.entity.FormScheduleA;
import com.example.form8038cp.form.entity.FormSignature;
import com.example.form8038cp.form.entity.FormSubmission;
import com.example.form8038cp.form.pdf.Form8038CpPdfDataAggregationService;
import com.example.form8038cp.form.pdf.Form8038CpPdfFillService;
import com.example.form8038cp.form.pdf.Form8038CpPdfService;
import com.example.form8038cp.form.pdf.PdfFieldValue;
import com.example.form8038cp.form.pdf.ScheduleAPdfService;
import com.example.form8038cp.form.repository.FormDirectDepositRepository;
import com.example.form8038cp.form.repository.FormPaidPreparerRepository;
import com.example.form8038cp.form.repository.FormPartIIIRepository;
import com.example.form8038cp.form.repository.FormPartIIRepository;
import com.example.form8038cp.form.repository.FormPartIRepository;
import com.example.form8038cp.form.repository.FormScheduleARepository;
import com.example.form8038cp.form.repository.FormSignatureRepository;
import com.example.form8038cp.form.repository.FormSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Form8038CpPdfServiceImpl implements Form8038CpPdfService {

    private final FormSubmissionRepository   submissionRepo;
    private final FormPartIRepository        partIRepo;
    private final FormPartIIRepository       partIIRepo;
    private final FormPartIIIRepository      partIIIRepo;
    private final FormDirectDepositRepository depositRepo;
    private final FormSignatureRepository    signatureRepo;
    private final FormPaidPreparerRepository preparerRepo;
    private final FormScheduleARepository    scheduleARepo;

    private final Form8038CpPdfFillService            fillService;
    private final Form8038CpPdfDataAggregationService aggregationService;
    private final ScheduleAPdfService                 scheduleAPdfService;

    @Value("${app.irs.pdf.f8038cp-template}")
    private String mainTemplatePath;

    @Override
    public byte[] generateMainPdf(Long userId, Long submissionId) throws IOException {
        FormSubmission submission = load(userId, submissionId);

        FormPartI       partI    = partIRepo.findBySubmission(submission).orElse(null);
        FormPartII      partII   = partIIRepo.findBySubmission(submission).orElse(null);
        FormPartIII     partIII  = partIIIRepo.findBySubmission(submission).orElse(null);
        FormDirectDeposit deposit = depositRepo.findBySubmission(submission).orElse(null);
        FormSignature   sig      = signatureRepo.findBySubmission(submission).orElse(null);
        FormPaidPreparer preparer = preparerRepo.findBySubmission(submission).orElse(null);

        List<PdfFieldValue> fields = aggregationService.aggregateMainForm(
                submission, partI, partII, partIII, deposit, sig, preparer);

        ClassPathResource resource = new ClassPathResource(mainTemplatePath);
        if (!resource.exists()) {
            throw new IllegalStateException("Main PDF template not found: " + mainTemplatePath);
        }
        return fillService.fill(resource.getContentAsByteArray(), fields);
    }

    @Override
    public byte[] generateScheduleAPdf(Long userId, Long submissionId) throws IOException {
        FormSubmission submission = load(userId, submissionId);

        FormPartII          partII  = partIIRepo.findBySubmission(submission).orElse(null);
        FormPartIII         partIII = partIIIRepo.findBySubmission(submission).orElse(null);
        List<FormScheduleA> rows    = scheduleARepo.findBySubmissionOrderByRowOrder(submission);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("No Schedule A rows found for this submission.");
        }
        return scheduleAPdfService.generateScheduleA(partII, partIII, rows);
    }

    private FormSubmission load(Long userId, Long submissionId) {
        return submissionRepo.findByIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found."));
    }
}
