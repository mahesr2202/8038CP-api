package com.example.form8038cp.form.controller;

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
import com.example.form8038cp.form.service.FormSubmissionService;
import com.example.form8038cp.security.ClientIpResolver;
import com.example.form8038cp.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Forms", description = "Form 8038-CP — create draft, save each section, and submit")
public class FormSubmissionController {

    private final FormSubmissionService formService;

    @Operation(summary = "Create a new DRAFT submission")
    @PostMapping
    public ResponseEntity<FormSubmissionResponse> createDraft() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(formService.createDraft(SecurityUtil.currentUserId()));
    }

    @Operation(summary = "Save Part I — Entity Receiving Payment")
    @PatchMapping("/{id}/part-i")
    public ResponseEntity<FormSubmissionResponse> savePartI(
            @PathVariable Long id, @Valid @RequestBody PartIRequest req) {
        return ResponseEntity.ok(formService.savePartI(SecurityUtil.currentUserId(), id, req));
    }

    @Operation(summary = "Save Part II — Reporting Authority")
    @PatchMapping("/{id}/part-ii")
    public ResponseEntity<FormSubmissionResponse> savePartII(
            @PathVariable Long id, @Valid @RequestBody PartIIRequest req) {
        return ResponseEntity.ok(formService.savePartII(SecurityUtil.currentUserId(), id, req));
    }

    @Operation(summary = "Save Part III — Payment of Credit")
    @PatchMapping("/{id}/part-iii")
    public ResponseEntity<FormSubmissionResponse> savePartIII(
            @PathVariable Long id, @Valid @RequestBody PartIIIRequest req) {
        return ResponseEntity.ok(formService.savePartIII(SecurityUtil.currentUserId(), id, req));
    }

    @Operation(summary = "Save Direct Deposit information")
    @PatchMapping("/{id}/deposit")
    public ResponseEntity<FormSubmissionResponse> saveDeposit(
            @PathVariable Long id, @Valid @RequestBody DirectDepositRequest req) {
        return ResponseEntity.ok(formService.saveDirectDeposit(SecurityUtil.currentUserId(), id, req));
    }

    @Operation(summary = "Save Signature section")
    @PatchMapping("/{id}/signature")
    public ResponseEntity<FormSubmissionResponse> saveSignature(
            @PathVariable Long id, @Valid @RequestBody SignatureRequest req) {
        return ResponseEntity.ok(formService.saveSignature(SecurityUtil.currentUserId(), id, req));
    }

    @Operation(summary = "Save Paid Preparer information")
    @PatchMapping("/{id}/preparer")
    public ResponseEntity<FormSubmissionResponse> savePreparer(
            @PathVariable Long id, @Valid @RequestBody PaidPreparerRequest req) {
        return ResponseEntity.ok(formService.savePreparer(SecurityUtil.currentUserId(), id, req));
    }

    @Operation(summary = "Save Schedule A rows (replaces all existing rows) — required for bond types 102, 103, 104, 105; auto-computes line 19c")
    @PutMapping("/{id}/schedule-a")
    public ResponseEntity<List<ScheduleARowResponse>> saveScheduleA(
            @PathVariable Long id, @Valid @RequestBody List<ScheduleARowRequest> rows) {
        return ResponseEntity.ok(formService.saveScheduleA(SecurityUtil.currentUserId(), id, rows));
    }

    @Operation(summary = "Get Schedule A rows with computed columns d and e")
    @GetMapping("/{id}/schedule-a")
    public ResponseEntity<List<ScheduleARowResponse>> getScheduleA(@PathVariable Long id) {
        return ResponseEntity.ok(formService.getScheduleA(SecurityUtil.currentUserId(), id));
    }

    @Operation(summary = "Submit the form after successful payment")
    @PostMapping("/{id}/submit")
    public ResponseEntity<FormSubmissionResponse> submit(
            @PathVariable Long id, @RequestBody SubmitRequest req, HttpServletRequest httpReq) {
        req.setClientIp(ClientIpResolver.resolve(httpReq));
        return ResponseEntity.ok(formService.submit(SecurityUtil.currentUserId(), id, req));
    }

    @Operation(summary = "Preview the IRS e-file XML without submitting")
    @GetMapping(value = "/{id}/preview-xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> previewXml(@PathVariable Long id) {
        return ResponseEntity.ok(formService.previewXml(SecurityUtil.currentUserId(), id));
    }

    @Operation(summary = "List all my submissions")
    @GetMapping
    public ResponseEntity<List<FormSubmissionResponse>> list() {
        return ResponseEntity.ok(formService.getMySubmissions(SecurityUtil.currentUserId()));
    }

    @Operation(summary = "Get a submission by ID")
    @GetMapping("/{id}")
    public ResponseEntity<FormSubmissionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formService.getById(SecurityUtil.currentUserId(), id));
    }

    @Operation(summary = "Delete a DRAFT submission")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDraft(@PathVariable Long id) {
        formService.deleteDraft(SecurityUtil.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
