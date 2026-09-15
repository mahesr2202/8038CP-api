package com.example.form8038cp.form.pdf;

import com.example.form8038cp.form.entity.FormDirectDeposit;
import com.example.form8038cp.form.entity.FormPaidPreparer;
import com.example.form8038cp.form.entity.FormPartI;
import com.example.form8038cp.form.entity.FormPartII;
import com.example.form8038cp.form.entity.FormPartIII;
import com.example.form8038cp.form.entity.FormSignature;
import com.example.form8038cp.form.entity.FormSubmission;

import java.util.List;

public interface Form8038CpPdfDataAggregationService {
    List<PdfFieldValue> aggregateMainForm(
            FormSubmission submission,
            FormPartI partI,
            FormPartII partII,
            FormPartIII partIII,
            FormDirectDeposit deposit,
            FormSignature sig,
            FormPaidPreparer preparer);
}
