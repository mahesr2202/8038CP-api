package com.example.form8038cp.form.service.impl;

import com.example.form8038cp.auth.entity.User;
import com.example.form8038cp.auth.repository.UserRepository;
import com.example.form8038cp.exception.FieldErrorDetail;
import com.example.form8038cp.exception.ResourceNotFoundException;
import com.example.form8038cp.exception.ValidationException;
import com.example.form8038cp.form.dto.*;
import com.example.form8038cp.form.entity.*;
import com.example.form8038cp.form.repository.*;
import com.example.form8038cp.form.service.FormSubmissionService;
import com.example.form8038cp.form.validation.Form8038CPSubmitValidator;
import com.example.form8038cp.form.xml.IrsXmlGeneratorService;
import com.example.form8038cp.form.xml.IrsXmlSchemaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FormSubmissionServiceImpl implements FormSubmissionService {

    private static final Set<String> SCHEDULE_A_BOND_TYPES = Set.of("102", "103", "104", "105");

    private final FormSubmissionRepository    formRepo;
    private final UserRepository              userRepository;
    private final FormPartIRepository         partIRepo;
    private final FormPartIIRepository        partIIRepo;
    private final FormPartIIIRepository       partIIIRepo;
    private final FormDirectDepositRepository depositRepo;
    private final FormSignatureRepository     signatureRepo;
    private final FormPaidPreparerRepository  preparerRepo;
    private final FormScheduleARepository     scheduleARepo;
    private final Form8038CPSubmitValidator   submitValidator;
    private final IrsXmlGeneratorService      xmlGenerator;
    private final IrsXmlSchemaValidator       xmlSchemaValidator;

    @Override @Transactional
    public FormSubmissionResponse createDraft(Long userId) {
        User user = findUser(userId);
        FormSubmission sub = FormSubmission.builder().user(user).build();
        return toResponse(formRepo.save(sub));
    }

    @Override @Transactional
    public FormSubmissionResponse savePartI(Long userId, Long id, PartIRequest req) {
        FormSubmission sub = findOwned(userId, id);
        FormPartI p = partIRepo.findBySubmission(sub).orElse(FormPartI.builder().submission(sub).build());
        p.setLine1(req.getLine1());
        p.setLine2(normalizeEin(req.getLine2()));
        p.setLine3Street(req.getLine3Street());
        p.setLine3Room(req.getLine3Room());
        p.setLine4(req.getLine4());
        p.setLine5(req.getLine5());
        p.setLine5Title(req.getLine5Title());
        p.setLine6(normalizePhone(req.getLine6()));
        partIRepo.save(p);
        return toResponse(sub);
    }

    @Override @Transactional
    public FormSubmissionResponse savePartII(Long userId, Long id, PartIIRequest req) {
        FormSubmission sub = findOwned(userId, id);
        FormPartII p = partIIRepo.findBySubmission(sub).orElse(FormPartII.builder().submission(sub).build());
        p.setLine7(req.getLine7());
        p.setLine8(normalizeEin(req.getLine8()));
        p.setLine9Street(req.getLine9Street());
        p.setLine9Room(req.getLine9Room());
        p.setLine10(req.getLine10());
        p.setLine11(req.getLine11());
        p.setLine12(normalizeDate(req.getLine12()));
        p.setLine13(req.getLine13());
        p.setLine14(req.getLine14());
        p.setLine15(req.getLine15());
        p.setLine15Title(req.getLine15Title());
        p.setLine16(normalizePhone(req.getLine16()));
        p.setLine17a(req.getLine17a());
        p.setLine17b(req.getLine17b());
        p.setLine17c(req.getLine17c());
        partIIRepo.save(p);
        return toResponse(sub);
    }

    @Override @Transactional
    public FormSubmissionResponse savePartIII(Long userId, Long id, PartIIIRequest req) {
        FormSubmission sub = findOwned(userId, id);
        FormPartIII p = partIIIRepo.findBySubmission(sub).orElse(FormPartIII.builder().submission(sub).build());
        p.setLine18(normalizeDate(req.getLine18()));
        p.setLine19a(req.getLine19a());
        p.setLine19b(req.getLine19b());
        p.setLine19c(req.getLine19c());
        p.setLine20Type(req.getLine20Type());
        p.setLine21a(req.getLine21a());
        p.setLine21b(req.getLine21b());
        p.setLine21cCode(req.getLine21cCode());
        p.setLine21cDate(req.getLine21cDate());
        p.setLine22(req.getLine22());
        p.setLine23a(req.getLine23a());
        p.setLine23b(req.getLine23b());
        p.setLine24a(req.getLine24a());
        p.setLine24b(req.getLine24b());
        p.setLine25(req.getLine25());
        partIIIRepo.save(p);
        return toResponse(sub);
    }

    @Override @Transactional
    public FormSubmissionResponse saveDirectDeposit(Long userId, Long id, DirectDepositRequest req) {
        FormSubmission sub = findOwned(userId, id);
        FormDirectDeposit d = depositRepo.findBySubmission(sub).orElse(FormDirectDeposit.builder().submission(sub).build());
        d.setRoutingNumber(req.getRoutingNumber());
        d.setAccountType(req.getAccountType());
        d.setAccountNumber(req.getAccountNumber());
        depositRepo.save(d);
        return toResponse(sub);
    }

    @Override @Transactional
    public FormSubmissionResponse saveSignature(Long userId, Long id, SignatureRequest req) {
        FormSubmission sub = findOwned(userId, id);
        FormSignature sig = signatureRepo.findBySubmission(sub).orElse(FormSignature.builder().submission(sub).build());
        sig.setSigSignature(req.getSigSignature());
        sig.setSigDate(normalizeDate(req.getSigDate()));
        sig.setSigNameTitle(req.getSigNameTitle());
        sig.setTaxpayerPin(req.getTaxpayerPin());
        sig.setSigFirstName(req.getSigFirstName());
        sig.setSigLastName(req.getSigLastName());
        signatureRepo.save(sig);
        return toResponse(sub);
    }

    @Override @Transactional
    public FormSubmissionResponse savePreparer(Long userId, Long id, PaidPreparerRequest req) {
        FormSubmission sub = findOwned(userId, id);
        FormPaidPreparer prep = preparerRepo.findBySubmission(sub).orElse(FormPaidPreparer.builder().submission(sub).build());
        prep.setPrepName(req.getPrepName());
        prep.setPrepSignature(req.getPrepSignature());
        prep.setPrepDate(normalizeDate(req.getPrepDate()));
        prep.setPrepSelfEmployed(req.getPrepSelfEmployed());
        prep.setPrepPtin(req.getPrepPtin());
        prep.setPrepFirmName(req.getPrepFirmName());
        prep.setPrepFirmEin(normalizeEin(req.getPrepFirmEin()));
        prep.setPrepPhone(normalizePhone(req.getPrepPhone()));
        prep.setPrepFirmAddress(req.getPrepFirmAddress());
        preparerRepo.save(prep);
        return toResponse(sub);
    }

    @Override @Transactional
    public List<ScheduleARowResponse> saveScheduleA(Long userId, Long id, List<ScheduleARowRequest> rows) {
        FormSubmission sub = findOwned(userId, id);
        FormPartII     p2  = partIIRepo.findBySubmission(sub).orElse(null);
        String bondType = p2 != null ? p2.getLine17c() : null;

        scheduleARepo.deleteBySubmission(sub);

        List<FormScheduleA> entities = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ScheduleARowRequest r = rows.get(i);
            entities.add(FormScheduleA.builder()
                    .submission(sub)
                    .rowOrder(i)
                    .colAMaturityDate(normalizeDate(r.getColAMaturityDate()))
                    .colBActualInterest(r.getColBActualInterest())
                    .colCCreditRateInterest(r.getColCCreditRateInterest())
                    .build());
        }
        List<FormScheduleA> saved = scheduleARepo.saveAll(entities);

        // Auto-update line19c on Part III when the bond type requires Schedule A
        if (bondType != null && SCHEDULE_A_BOND_TYPES.contains(bondType)) {
            BigDecimal total = computeLine19cTotal(saved, bondType);
            FormPartIII p3 = partIIIRepo.findBySubmission(sub).orElse(null);
            if (p3 != null) {
                p3.setLine19c(total.setScale(2, RoundingMode.HALF_UP).toPlainString());
                partIIIRepo.save(p3);
            }
        }

        return toScheduleARowResponses(saved, bondType);
    }

    @Override @Transactional(readOnly = true)
    public List<ScheduleARowResponse> getScheduleA(Long userId, Long id) {
        FormSubmission sub = findOwned(userId, id);
        FormPartII     p2  = partIIRepo.findBySubmission(sub).orElse(null);
        String bondType = p2 != null ? p2.getLine17c() : null;
        List<FormScheduleA> rows = scheduleARepo.findBySubmissionOrderByRowOrder(sub);
        return toScheduleARowResponses(rows, bondType);
    }

    @Override @Transactional
    public FormSubmissionResponse submit(Long userId, Long id, SubmitRequest req) {
        FormSubmission    sub  = findOwned(userId, id);

        // Load all form sections
        FormPartI         p1   = partIRepo.findBySubmission(sub).orElse(null);
        FormPartII        p2   = partIIRepo.findBySubmission(sub).orElse(null);
        FormPartIII       p3   = partIIIRepo.findBySubmission(sub).orElse(null);
        FormDirectDeposit dep  = depositRepo.findBySubmission(sub).orElse(null);
        FormSignature     sig  = signatureRepo.findBySubmission(sub).orElse(null);
        FormPaidPreparer  prep = preparerRepo.findBySubmission(sub).orElse(null);
        List<FormScheduleA> scheduleA = scheduleARepo.findBySubmissionOrderByRowOrder(sub);

        // 1. Cross-field business validation
        submitValidator.validate(p2, p3, dep, sig, scheduleA);

        // 2. Generate IRS e-file XML from validated form data
        String xml = xmlGenerator.generateXml(sub, p1, p2, p3, dep, sig, prep, scheduleA, req.getClientIp());

        // 3. Validate generated XML against the 2027v1.0 XSD schema before accepting payment
        List<String> schemaErrors = xmlSchemaValidator.validate(xml);
        if (!schemaErrors.isEmpty()) {
            List<FieldErrorDetail> details = schemaErrors.stream()
                    .map(msg -> FieldErrorDetail.of("XML_SCHEMA_ERROR", "generatedXml", msg))
                    .toList();
            throw new ValidationException("IRS XML schema validation failed — please review your form data.", details);
        }

        // 4. Record payment and finalize submission
        sub.setStripePaymentIntentId(req.getStripePaymentIntentId());
        sub.setPaymentStatus("succeeded");
        sub.setStatus("SUBMITTED");
        sub.setSubmittedAt(OffsetDateTime.now());
        sub.setGeneratedXml(xml);
        return toResponse(formRepo.save(sub));
    }

    @Override @Transactional(readOnly = true)
    public String previewXml(Long userId, Long id) {
        FormSubmission sub  = findOwned(userId, id);
        FormPartI       p1  = partIRepo.findBySubmission(sub).orElse(null);
        FormPartII      p2  = partIIRepo.findBySubmission(sub).orElse(null);
        FormPartIII     p3  = partIIIRepo.findBySubmission(sub).orElse(null);
        FormDirectDeposit dep = depositRepo.findBySubmission(sub).orElse(null);
        FormSignature   sig = signatureRepo.findBySubmission(sub).orElse(null);
        FormPaidPreparer prep = preparerRepo.findBySubmission(sub).orElse(null);
        List<FormScheduleA> scheduleA = scheduleARepo.findBySubmissionOrderByRowOrder(sub);
        return xmlGenerator.generateXml(sub, p1, p2, p3, dep, sig, prep, scheduleA, null);
    }

    @Override @Transactional(readOnly = true)
    public List<FormSubmissionResponse> getMySubmissions(Long userId) {
        return formRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public FormSubmissionResponse getById(Long userId, Long id) {
        return formRepo.findByIdAndUserId(id, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found."));
    }

    @Override @Transactional
    public void deleteDraft(Long userId, Long id) {
        FormSubmission sub = findOwned(userId, id);
        if (!"DRAFT".equals(sub.getStatus())) {
            throw new ValidationException(FieldErrorDetail.of("INVALID_STATUS", "status", "Only DRAFT submissions can be deleted."));
        }
        formRepo.delete(sub);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private FormSubmission findOwned(Long userId, Long id) {
        return formRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found."));
    }

    private FormSubmissionResponse toResponse(FormSubmission s) {
        FormPartI       p1   = partIRepo.findBySubmission(s).orElse(null);
        FormPartII      p2   = partIIRepo.findBySubmission(s).orElse(null);
        FormPartIII     p3   = partIIIRepo.findBySubmission(s).orElse(null);
        FormDirectDeposit dep = depositRepo.findBySubmission(s).orElse(null);
        FormSignature   sig  = signatureRepo.findBySubmission(s).orElse(null);
        FormPaidPreparer prep = preparerRepo.findBySubmission(s).orElse(null);
        List<FormScheduleA> scheduleA = scheduleARepo.findBySubmissionOrderByRowOrder(s);
        String bondType = p2 != null ? p2.getLine17c() : null;

        return FormSubmissionResponse.builder()
                .id(s.getId()).userId(s.getUser().getId())
                .status(s.getStatus()).paymentStatus(s.getPaymentStatus())
                .submittedAt(s.getSubmittedAt())
                .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
                .isAmended(s.getIsAmended())
                .generatedXml(s.getGeneratedXml())
                // Part I
                .line1(p1 != null ? p1.getLine1() : null)
                .line2(p1 != null ? p1.getLine2() : null)
                .line3Street(p1 != null ? p1.getLine3Street() : null)
                .line3Room(p1 != null ? p1.getLine3Room() : null)
                .line4(p1 != null ? p1.getLine4() : null)
                .line5(p1 != null ? p1.getLine5() : null)
                .line5Title(p1 != null ? p1.getLine5Title() : null)
                .line6(p1 != null ? p1.getLine6() : null)
                // Part II
                .line7(p2 != null ? p2.getLine7() : null)
                .line8(p2 != null ? p2.getLine8() : null)
                .line9Street(p2 != null ? p2.getLine9Street() : null)
                .line9Room(p2 != null ? p2.getLine9Room() : null)
                .line10(p2 != null ? p2.getLine10() : null)
                .line11(p2 != null ? p2.getLine11() : null)
                .line12(p2 != null ? p2.getLine12() : null)
                .line13(p2 != null ? p2.getLine13() : null)
                .line14(p2 != null ? p2.getLine14() : null)
                .line15(p2 != null ? p2.getLine15() : null)
                .line15Title(p2 != null ? p2.getLine15Title() : null)
                .line16(p2 != null ? p2.getLine16() : null)
                .line17a(p2 != null ? p2.getLine17a() : null)
                .line17b(p2 != null ? p2.getLine17b() : null)
                .line17c(p2 != null ? p2.getLine17c() : null)
                // Part III
                .line18(p3 != null ? p3.getLine18() : null)
                .line19a(p3 != null ? p3.getLine19a() : null)
                .line19b(p3 != null ? p3.getLine19b() : null)
                .line19c(p3 != null ? p3.getLine19c() : null)
                .line20Type(p3 != null ? p3.getLine20Type() : null)
                .line21a(p3 != null ? p3.getLine21a() : null)
                .line21b(p3 != null ? p3.getLine21b() : null)
                .line21cCode(p3 != null ? p3.getLine21cCode() : null)
                .line21cDate(p3 != null ? p3.getLine21cDate() : null)
                .line22(p3 != null ? p3.getLine22() : null)
                .line23a(p3 != null ? p3.getLine23a() : null)
                .line23b(p3 != null ? p3.getLine23b() : null)
                .line24a(p3 != null ? p3.getLine24a() : null)
                .line24b(p3 != null ? p3.getLine24b() : null)
                .line25(p3 != null ? p3.getLine25() : null)
                // Direct Deposit
                .routingNumber(dep != null ? dep.getRoutingNumber() : null)
                .accountType(dep != null ? dep.getAccountType() : null)
                .accountNumber(dep != null ? dep.getAccountNumber() : null)
                // Signature
                .sigSignature(sig != null ? sig.getSigSignature() : null)
                .sigDate(sig != null ? sig.getSigDate() : null)
                .sigNameTitle(sig != null ? sig.getSigNameTitle() : null)
                .taxpayerPin(sig != null ? sig.getTaxpayerPin() : null)
                .sigFirstName(sig != null ? sig.getSigFirstName() : null)
                .sigLastName(sig != null ? sig.getSigLastName() : null)
                // Paid Preparer
                .prepName(prep != null ? prep.getPrepName() : null)
                .prepSignature(prep != null ? prep.getPrepSignature() : null)
                .prepDate(prep != null ? prep.getPrepDate() : null)
                .prepSelfEmployed(prep != null ? prep.getPrepSelfEmployed() : null)
                .prepPtin(prep != null ? prep.getPrepPtin() : null)
                .prepFirmName(prep != null ? prep.getPrepFirmName() : null)
                .prepFirmEin(prep != null ? prep.getPrepFirmEin() : null)
                .prepPhone(prep != null ? prep.getPrepPhone() : null)
                .prepFirmAddress(prep != null ? prep.getPrepFirmAddress() : null)
                .scheduleARows(!scheduleA.isEmpty() ? toScheduleARowResponses(scheduleA, bondType) : null)
                .build();
    }

    // -------------------------------------------------------------------------
    // Schedule A computation helpers
    // -------------------------------------------------------------------------

    private List<ScheduleARowResponse> toScheduleARowResponses(List<FormScheduleA> rows, String bondType) {
        List<ScheduleARowResponse> result = new ArrayList<>();
        BigDecimal runningTotal = BigDecimal.ZERO;
        for (FormScheduleA row : rows) {
            BigDecimal colB = row.getColBActualInterest() != null ? row.getColBActualInterest() : BigDecimal.ZERO;
            BigDecimal colC = row.getColCCreditRateInterest() != null ? row.getColCCreditRateInterest() : BigDecimal.ZERO;
            BigDecimal colD = computeColD(colC, bondType);
            BigDecimal colE = computeColE(colB, colC, colD, bondType);
            runningTotal = runningTotal.add(colE);

            result.add(ScheduleARowResponse.builder()
                    .rowOrder(row.getRowOrder())
                    .colAMaturityDate(row.getColAMaturityDate())
                    .colBActualInterest(colB)
                    .colCCreditRateInterest(colC)
                    .colD70PctAmount(colD)
                    .colEEligibleInterest(colE)
                    .line19cTotal(null)
                    .build());
        }
        // Stamp the running total only on the last row
        if (!result.isEmpty()) {
            BigDecimal finalTotal = runningTotal;
            ScheduleARowResponse last = result.get(result.size() - 1);
            result.set(result.size() - 1, ScheduleARowResponse.builder()
                    .rowOrder(last.getRowOrder())
                    .colAMaturityDate(last.getColAMaturityDate())
                    .colBActualInterest(last.getColBActualInterest())
                    .colCCreditRateInterest(last.getColCCreditRateInterest())
                    .colD70PctAmount(last.getColD70PctAmount())
                    .colEEligibleInterest(last.getColEEligibleInterest())
                    .line19cTotal(finalTotal.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        return result;
    }

    private BigDecimal computeLine19cTotal(List<FormScheduleA> rows, String bondType) {
        BigDecimal total = BigDecimal.ZERO;
        for (FormScheduleA row : rows) {
            BigDecimal colB = row.getColBActualInterest() != null ? row.getColBActualInterest() : BigDecimal.ZERO;
            BigDecimal colC = row.getColCCreditRateInterest() != null ? row.getColCCreditRateInterest() : BigDecimal.ZERO;
            BigDecimal colD = computeColD(colC, bondType);
            total = total.add(computeColE(colB, colC, colD, bondType));
        }
        return total;
    }

    /** Column (d): col_c × 70%, only for bond types 102 (NCREBs) and 103 (QECBs). */
    private BigDecimal computeColD(BigDecimal colC, String bondType) {
        if ("102".equals(bondType) || "103".equals(bondType)) {
            return colC.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /**
     * Column (e): eligible interest.
     * For 102/103: min(col_b, col_d).
     * For 104/105: min(col_b, col_c).
     */
    private BigDecimal computeColE(BigDecimal colB, BigDecimal colC, BigDecimal colD, String bondType) {
        if ("102".equals(bondType) || "103".equals(bondType)) {
            return colB.min(colD != null ? colD : BigDecimal.ZERO);
        }
        return colB.min(colC);
    }

    /** Strip the IRS display dash from EIN (XX-XXXXXXX → XXXXXXXXX) before storage. */
    private static String normalizeEin(String ein) {
        return ein != null ? ein.replace("-", "") : null;
    }

    /**
     * Normalize a date to YYYY-MM-DD for IRS DateType storage.
     * Converts MM/DD/YYYY (browser locale display format) to YYYY-MM-DD.
     */
    private static String normalizeDate(String date) {
        if (date == null) return null;
        if (date.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            // MM/DD/YYYY → YYYY-MM-DD
            String[] p = date.split("/");
            return p[2] + "-" + p[0] + "-" + p[1];
        }
        return date; // already YYYY-MM-DD or null
    }

    /**
     * Normalize a US phone to 10 bare digits for IRS PhoneNumberType storage.
     * Accepts (555) 123-4567, 555-123-4567, 555.123.4567, 5551234567, etc.
     */
    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("[^0-9]", "");
        // IRS requires exactly 10 digits; trim leading 1 (country code) if present
        if (digits.length() == 11 && digits.startsWith("1")) {
            digits = digits.substring(1);
        }
        return digits;
    }
}
