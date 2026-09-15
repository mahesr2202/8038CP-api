package com.example.form8038cp.form.xml;

import com.example.form8038cp.config.IrsProperties;
import com.example.form8038cp.form.entity.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds IRS e-file XML for Form 8038-CP per the 2027v1.0 schema.
 * Element ordering in every sequence strictly follows the XSD definition.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IrsXmlGeneratorService {

    private static final String NS = "http://www.irs.gov/efile";

    /** Pattern for "City, ST XXXXX" or "City, ST XXXXX-XXXX" */
    private static final Pattern CITY_STATE_ZIP =
            Pattern.compile("^(.+),\\s+([A-Z]{2})\\s+(\\d{5}(?:-\\d{4})?)$");

    private final IrsProperties irs;

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    public String generateXml(
            FormSubmission sub,
            FormPartI partI,
            FormPartII partII,
            FormPartIII partIII,
            FormDirectDeposit deposit,
            FormSignature sig,
            FormPaidPreparer preparer,
            List<FormScheduleA> scheduleARows,
            String clientIp) {

        try {
            var dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            var doc = dbf.newDocumentBuilder().newDocument();

            Element returnEl = doc.createElementNS(NS, "Return");
            returnEl.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", NS);
            returnEl.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:efile", NS);
            returnEl.setAttribute("returnVersion", "2027v1.0");
            doc.appendChild(returnEl);

            returnEl.appendChild(buildReturnHeader(doc, partI, partII, sig, preparer, deposit, clientIp));
            returnEl.appendChild(buildReturnData(doc, sub, partI, partII, partIII, deposit, scheduleARows));

            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            var sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate IRS XML for submission " + sub.getId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // ReturnHeader
    // -------------------------------------------------------------------------

    private Element buildReturnHeader(Document doc,
                                      FormPartI p1, FormPartII p2,
                                      FormSignature sig, FormPaidPreparer prep,
                                      FormDirectDeposit dep, String clientIp) {
        Element header = el(doc, "ReturnHeader");
        header.setAttribute("binaryAttachmentCnt", "0");

        String ts = OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        text(doc, header, "ReturnTs", ts);
        text(doc, header, "TaxPeriodEndDt", irs.getTaxPeriodEnd());

        text(doc, header, "SoftwareId", irs.getSoftwareId());
        text(doc, header, "MultSoftwarePackagesUsedInd", "false");

        Element originator = el(doc, "OriginatorGrp");
        text(doc, originator, "EFIN", irs.getEfin());
        text(doc, originator, "OriginatorTypeCd", "OnlineFiler");
        Element practPinGrp = el(doc, "PractitionerPINGrp");
        text(doc, practPinGrp, "EFIN", irs.getEfin());
        text(doc, practPinGrp, "PIN", irs.getPractitionerPin());
        originator.appendChild(practPinGrp);
        header.appendChild(originator);

        text(doc, header, "PINEnteredByCd", "Taxpayer");
        text(doc, header, "SignatureOptionCd", "PIN Number");
        text(doc, header, "ReturnTypeCd", "8038CP");

        header.appendChild(buildFiler(doc, p1, p2));
        header.appendChild(buildIssuerSignatureGrp(doc, p1, p2, sig));
        header.appendChild(buildSigningOfficerGrp(doc, sig));

        if (prep != null && hasValue(prep.getPrepName())) {
            header.appendChild(buildPreparerPersonGrp(doc, prep));
        }

        header.appendChild(buildAdditionalFilerInformation(doc, dep));
        header.appendChild(buildFilingSecurityInformation(doc, clientIp));
        text(doc, header, "TaxYr", irs.getTaxYear());

        return header;
    }

    // ── Filer ─────────────────────────────────────────────────────────────────

    private Element buildFiler(Document doc, FormPartI p1, FormPartII p2) {
        // Sequence: EIN, BusinessName, [InCareOfNm], BusinessNameControlTxt, [PhoneNum], USAddress
        boolean sameEntity = p2 != null && "SAME".equals(p2.getLine7());

        String ein     = sameEntity ? safe(p1, FormPartI::getLine2) : safe(p2, FormPartII::getLine8);
        String name    = sameEntity ? safe(p1, FormPartI::getLine1) : safe(p2, FormPartII::getLine7);
        String street  = sameEntity ? safe(p1, FormPartI::getLine3Street) : safe(p2, FormPartII::getLine9Street);
        String room    = sameEntity ? safe(p1, FormPartI::getLine3Room)   : safe(p2, FormPartII::getLine9Room);
        String cityLn  = sameEntity ? safe(p1, FormPartI::getLine4)       : safe(p2, FormPartII::getLine11);

        Element filer = el(doc, "Filer");
        text(doc, filer, "EIN", coalesce(ein, "000000000"));

        Element bizName = el(doc, "BusinessName");
        text(doc, bizName, "BusinessNameLine1Txt", sanitizeBusinessName(coalesce(name, "UNKNOWN")));
        filer.appendChild(bizName);

        text(doc, filer, "BusinessNameControlTxt", toNameControl(coalesce(name, "UNKN")));
        filer.appendChild(buildUSAddress(doc, street, room, cityLn));

        return filer;
    }

    // ── IssuerSignatureGrp ────────────────────────────────────────────────────

    private Element buildIssuerSignatureGrp(Document doc,
                                             FormPartI p1, FormPartII p2,
                                             FormSignature sig) {
        // Sequence: PersonNm, PersonTitleTxt, PhoneNum, [EmailAddressTxt], SignatureDt, [TaxpayerPIN]
        Element g = el(doc, "IssuerSignatureGrp");

        String personNm = sig != null ? sig.getSigSignature() : null;
        text(doc, g, "PersonNm",       sanitizePersonName(coalesce(personNm, "AUTHORIZED OFFICER")));
        text(doc, g, "PersonTitleTxt", sanitizePersonTitle(coalesce(
                sig != null ? sig.getSigNameTitle() : null, "OFFICER")));

        String phone = p2 != null && hasValue(p2.getLine16()) ? p2.getLine16()
                : (p1 != null ? p1.getLine6() : null);
        text(doc, g, "PhoneNum", coalesce(phone, "0000000000"));

        String sigDate = sig != null ? sig.getSigDate() : null;
        text(doc, g, "SignatureDt", coalesce(sigDate, LocalDate.now().toString()));

        if (sig != null && hasValue(sig.getTaxpayerPin())) {
            text(doc, g, "TaxpayerPIN", sig.getTaxpayerPin());
        }

        return g;
    }

    // ── SigningOfficerGrp ─────────────────────────────────────────────────────

    private Element buildSigningOfficerGrp(Document doc, FormSignature sig) {
        Element g = el(doc, "SigningOfficerGrp");
        String firstName = sig != null ? sig.getSigFirstName() : null;
        String lastName  = sig != null ? sig.getSigLastName()  : null;
        if (hasValue(firstName) || hasValue(lastName)) {
            Element personFullName = el(doc, "PersonFullName");
            text(doc, personFullName, "PersonFirstNm", sanitizePersonName(coalesce(firstName, "OFFICER")));
            text(doc, personFullName, "PersonLastNm",  sanitizePersonName(coalesce(lastName, "OFFICER")));
            g.appendChild(personFullName);
        }
        return g;
    }

    // ── PreparerPersonGrp ─────────────────────────────────────────────────────

    private Element buildPreparerPersonGrp(Document doc, FormPaidPreparer prep) {
        // Sequence: [PreparerPersonNm], ([SSN]|[PTIN]), [PhoneNum], [EmailAddressTxt],
        //           [PreparationDt], [SelfEmployedInd]
        Element g = el(doc, "PreparerPersonGrp");

        if (hasValue(prep.getPrepName())) {
            text(doc, g, "PreparerPersonNm", sanitizePersonName(prep.getPrepName()));
        }
        if (hasValue(prep.getPrepPtin())) {
            boolean isPtin = prep.getPrepPtin().startsWith("P");
            text(doc, g, isPtin ? "PTIN" : "SSN", prep.getPrepPtin());
        }
        if (hasValue(prep.getPrepPhone())) {
            text(doc, g, "PhoneNum", prep.getPrepPhone());
        }
        if (hasValue(prep.getPrepDate())) {
            text(doc, g, "PreparationDt", prep.getPrepDate());
        }
        if (Boolean.TRUE.equals(prep.getPrepSelfEmployed())) {
            text(doc, g, "SelfEmployedInd", "X");
        }

        return g;
    }

    // ── AdditionalFilerInformation ────────────────────────────────────────────

    private Element buildAdditionalFilerInformation(Document doc, FormDirectDeposit dep) {
        Element afi = el(doc, "AdditionalFilerInformation");

        boolean hasDirectDeposit = dep != null && hasValue(dep.getRoutingNumber());
        String rtn    = hasDirectDeposit ? dep.getRoutingNumber() : null;
        String acctNo = hasDirectDeposit && hasValue(dep.getAccountNumber()) ? dep.getAccountNumber() : null;

        // AtSubmissionCreationGrp — bank account data captured at form-fill time
        if (hasDirectDeposit) {
            String capturedTs = OffsetDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
            Element creation = el(doc, "AtSubmissionCreationGrp");
            text(doc, creation, "RoutingTransitNum",        rtn);
            text(doc, creation, "DepositorAccountNum",      acctNo);
            text(doc, creation, "BankAccountDataCapturedTs", capturedTs);
            afi.appendChild(creation);
        }

        // AtSubmissionFilingGrp
        Element filing = el(doc, "AtSubmissionFilingGrp");
        text(doc, filing, "RefundProductElectionInd", "false");

        Element disbGrp = el(doc, "RefundDisbursementGrp");
        text(doc, disbGrp, "RefundDisbursementCd", hasDirectDeposit ? "2" : "3");
        if (hasDirectDeposit) {
            text(doc, disbGrp, "RoutingTransitNum",   rtn);
            text(doc, disbGrp, "DepositorAccountNum", acctNo);
        }
        filing.appendChild(disbGrp);

        afi.appendChild(filing);
        return afi;
    }

    // ── FilingSecurityInformation ─────────────────────────────────────────────

    private Element buildFilingSecurityInformation(Document doc, String clientIp) {
        Element fsi = el(doc, "FilingSecurityInformation");

        Element ip = el(doc, "IPAddress");
        String resolvedIp = (clientIp != null && !clientIp.isBlank()) ? clientIp : irs.getClientIp();
        text(doc, ip, "IPv4AddressTxt", resolvedIp);
        fsi.appendChild(ip);

        text(doc, fsi, "TotActiveTimePrepSubmissionTs", "60");
        text(doc, fsi, "VendorControlNum",              irs.getVendorControlNum());
        text(doc, fsi, "AtSubmissionCreationDeviceId",  irs.getDeviceId());
        text(doc, fsi, "AtSubmissionFilingDeviceId",    irs.getDeviceId());

        return fsi;
    }

    // -------------------------------------------------------------------------
    // ReturnData / IRS8038CP
    // -------------------------------------------------------------------------

    private static final java.util.Set<String> SCHEDULE_A_BOND_TYPES = java.util.Set.of("102", "103", "104", "105");

    private Element buildReturnData(Document doc, FormSubmission sub,
                                    FormPartI p1, FormPartII p2,
                                    FormPartIII p3, FormDirectDeposit dep,
                                    List<FormScheduleA> scheduleARows) {
        boolean includeScheduleA = p2 != null
                && hasValue(p2.getLine17c())
                && SCHEDULE_A_BOND_TYPES.contains(p2.getLine17c())
                && scheduleARows != null
                && !scheduleARows.isEmpty();

        Element rd = el(doc, "ReturnData");
        rd.setAttribute("documentCnt", includeScheduleA ? "2" : "1");
        rd.appendChild(buildIRS8038CP(doc, sub, p1, p2, p3, dep));
        if (includeScheduleA) {
            rd.appendChild(buildScheduleA(doc, p2, scheduleARows));
        }
        return rd;
    }

    // ── IRS8038CPScheduleA ────────────────────────────────────────────────────

    private Element buildScheduleA(Document doc, FormPartII p2, List<FormScheduleA> rows) {
        // Sequence per IRS8038CPScheduleAType:
        // ReportNum, EligibleInterestComputationGrp (1..*), TotalEligibleInterestCmptAmt
        Element schedA = el(doc, "IRS8038CPScheduleA");
        schedA.setAttribute("documentId", "IRS8038CPScheduleA");

        text(doc, schedA, "ReportNum", coalesce(p2.getLine10(), "800"));

        boolean needsColD = "102".equals(p2.getLine17c()) || "103".equals(p2.getLine17c());
        BigDecimal total = BigDecimal.ZERO;

        for (FormScheduleA row : rows) {
            BigDecimal colB = row.getColBActualInterest() != null ? row.getColBActualInterest() : BigDecimal.ZERO;
            BigDecimal colC = row.getColCCreditRateInterest() != null ? row.getColCCreditRateInterest() : BigDecimal.ZERO;
            BigDecimal colD = needsColD
                    ? colC.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP)
                    : null;
            BigDecimal colE = needsColD ? colB.min(colD) : colB.min(colC);
            total = total.add(colE);

            Element grp = el(doc, "EligibleInterestComputationGrp");
            text(doc, grp, "BondMaturityDt", coalesce(row.getColAMaturityDate(), LocalDate.now().toString()));
            text(doc, grp, "IntPdBondMaturityPymtDtAmt",     colB.setScale(2, RoundingMode.HALF_UP).toPlainString());
            text(doc, grp, "IntPdBondMaturityCalcPymtDtAmt", colC.setScale(2, RoundingMode.HALF_UP).toPlainString());
            if (needsColD && colD != null) {
                text(doc, grp, "IntPdBondMaturityCalc70PctAmt", colD.toPlainString());
            }
            text(doc, grp, "SmallerInterestPaidOrCalcAmt", colE.setScale(2, RoundingMode.HALF_UP).toPlainString());
            schedA.appendChild(grp);
        }

        text(doc, schedA, "TotalEligibleInterestCmptAmt", total.setScale(2, RoundingMode.HALF_UP).toPlainString());
        return schedA;
    }

    private Element buildIRS8038CP(Document doc, FormSubmission sub,
                                   FormPartI p1, FormPartII p2,
                                   FormPartIII p3, FormDirectDeposit dep) {
        // Sequence per IRS8038CPType:
        // [SpecialConditionDesc], [AmendedReturnInd], [EntityReceivingPaymentGrp],
        // ReportingAuthorityGrp, InterestPaymentDt, InterestPayableAmt,
        // [Section54Ab3CreditRt], [TotalEligibleInterestCmptAmt],
        // choice(20a-20f), [NetIncreaseDecreaseGrp], CreditPaymentRequestedAmt,
        // DebtServiceScheduleChangeInd, [DebtServiceScheduleExplnGrp],
        // InterestPaidBeforePaymentDtInd, [IntNotPaidBfrPymtDtExplnGrp],
        // FinalInterestPaymentDtInd, [RoutingTransitNum], [BankAccountTypeCd], [DepositorAccountNum]

        Element form = el(doc, "IRS8038CP");
        form.setAttribute("documentId", "IRS8038CP");

        if (Boolean.TRUE.equals(sub.getIsAmended())) {
            text(doc, form, "AmendedReturnInd", "X");
        }

        // Part I — optional in schema; only include when data is present
        if (p1 != null && (hasValue(p1.getLine1()) || hasValue(p1.getLine2()))) {
            form.appendChild(buildEntityReceivingPayment(doc, p1));
        }

        // Part II — always include (required by schema)
        form.appendChild(buildReportingAuthority(doc, p2));

        // Part III
        if (p3 != null) {
            text(doc, form, "InterestPaymentDt",  coalesce(p3.getLine18(), LocalDate.now().toString()));
            text(doc, form, "InterestPayableAmt",  formatAmt(p3.getLine19a()));

            if (hasValue(p3.getLine19b())) {
                text(doc, form, "Section54Ab3CreditRt", p3.getLine19b());
            }
            if (hasValue(p3.getLine19c())) {
                Element tic = el(doc, "TotalEligibleInterestCmptAmt");
                tic.setTextContent(formatAmt(p3.getLine19c()));
                form.appendChild(tic);
            }

            form.appendChild(buildLine20Element(doc, p3));

            appendNetIncreaseDecreaseGrp(doc, form, p3);

            text(doc, form, "CreditPaymentRequestedAmt",
                    p3.getLine22() != null
                            ? p3.getLine22().setScale(2, RoundingMode.HALF_UP).toPlainString()
                            : "0.00");

            // DebtServiceScheduleChangeInd (boolean)
            Element dsci = el(doc, "DebtServiceScheduleChangeInd");
            dsci.setTextContent(coalesce(p3.getLine23a(), "false"));
            form.appendChild(dsci);

            if ("true".equals(p3.getLine23a()) && hasValue(p3.getLine23b())) {
                Element dsg = el(doc, "DebtServiceScheduleExplnGrp");
                text(doc, dsg, "DebtServiceScheduleExplnCd", p3.getLine23b());
                form.appendChild(dsg);
            }

            text(doc, form, "InterestPaidBeforePaymentDtInd", coalesce(p3.getLine24a(), "false"));

            if ("false".equals(p3.getLine24a()) && hasValue(p3.getLine24b())) {
                Element ipg = el(doc, "IntNotPaidBfrPymtDtExplnGrp");
                text(doc, ipg, "IntNotPaidBfrPymtDtExplnCd", p3.getLine24b());
                form.appendChild(ipg);
            }

            text(doc, form, "FinalInterestPaymentDtInd", coalesce(p3.getLine25(), "false"));
        }

        // Direct deposit
        if (dep != null && hasValue(dep.getRoutingNumber())) {
            text(doc, form, "RoutingTransitNum",  dep.getRoutingNumber());
            text(doc, form, "BankAccountTypeCd",  accountTypeCode(dep.getAccountType()));
            text(doc, form, "DepositorAccountNum", dep.getAccountNumber());
        }

        return form;
    }

    // ── EntityReceivingPaymentGrp (Part I) ────────────────────────────────────

    private Element buildEntityReceivingPayment(Document doc, FormPartI p1) {
        // Sequence: BusinessName, EIN, USAddress, ContactPersonNm,
        //           ContactPersonTitleTxt, ContactPersonPhoneNum
        Element grp = el(doc, "EntityReceivingPaymentGrp");

        Element bizName = el(doc, "BusinessName");
        text(doc, bizName, "BusinessNameLine1Txt",
                sanitizeBusinessName(coalesce(p1.getLine1(), "UNKNOWN")));
        grp.appendChild(bizName);

        text(doc, grp, "EIN", coalesce(p1.getLine2(), "000000000"));
        grp.appendChild(buildUSAddress(doc, p1.getLine3Street(), p1.getLine3Room(), p1.getLine4()));

        text(doc, grp, "ContactPersonNm",
                sanitizePersonName(coalesce(p1.getLine5(), "AUTHORIZED OFFICER")));
        text(doc, grp, "ContactPersonTitleTxt",
                sanitizePersonTitle(coalesce(p1.getLine5Title(), "OFFICER")));
        text(doc, grp, "ContactPersonPhoneNum", coalesce(p1.getLine6(), "0000000000"));

        return grp;
    }

    // ── ReportingAuthorityGrp (Part II) ───────────────────────────────────────

    private Element buildReportingAuthority(Document doc, FormPartII p2) {
        // Sequence: [ReceiverAndIssuerSameEntityCd], ReportNum, BondIssueDt, BondIssueNm,
        //           ([CUSIPNum]|[MissingCUSIPReasonCd]), ContactPersonNm,
        //           ContactPersonTitleTxt, ContactPersonPhoneNum,
        //           (VariableRateBondInd|FixedRateBondInd), [IssuePriceAmt], [BondTypeCd]
        Element grp = el(doc, "ReportingAuthorityGrp");

        if (p2 == null) {
            // Provide mandatory placeholders so schema validation still sees required elements
            text(doc, grp, "ReportNum",       "800");
            text(doc, grp, "BondIssueDt",     "2009-02-17");
            text(doc, grp, "BondIssueNm",     "UNKNOWN");
            text(doc, grp, "ContactPersonNm",    "AUTHORIZED OFFICER");
            text(doc, grp, "ContactPersonTitleTxt", "OFFICER");
            text(doc, grp, "ContactPersonPhoneNum", "0000000000");
            text(doc, grp, "FixedRateBondInd", "X");
            return grp;
        }

        if ("SAME".equals(p2.getLine7())) {
            text(doc, grp, "ReceiverAndIssuerSameEntityCd", "SAME");
        }

        text(doc, grp, "ReportNum",   coalesce(p2.getLine10(), "800"));
        text(doc, grp, "BondIssueDt", coalesce(p2.getLine12(), "2009-02-17"));
        text(doc, grp, "BondIssueNm", coalesce(p2.getLine13(), "UNKNOWN"));

        if (hasValue(p2.getLine14())) {
            if ("NONE".equals(p2.getLine14())) {
                text(doc, grp, "MissingCUSIPReasonCd", "NONE");
            } else {
                text(doc, grp, "CUSIPNum", p2.getLine14());
            }
        }

        text(doc, grp, "ContactPersonNm",
                sanitizePersonName(coalesce(p2.getLine15(), "AUTHORIZED OFFICER")));
        text(doc, grp, "ContactPersonTitleTxt",
                sanitizePersonTitle(coalesce(p2.getLine15Title(), "OFFICER")));
        text(doc, grp, "ContactPersonPhoneNum", coalesce(p2.getLine16(), "0000000000"));

        if ("VARIABLE".equals(p2.getLine17a())) {
            text(doc, grp, "VariableRateBondInd", "X");
        } else {
            text(doc, grp, "FixedRateBondInd", "X");
        }

        if (hasValue(p2.getLine17b())) {
            text(doc, grp, "IssuePriceAmt", formatAmt(p2.getLine17b()));
        }
        if (hasValue(p2.getLine17c())) {
            text(doc, grp, "BondTypeCd", p2.getLine17c());
        }

        return grp;
    }

    // ── Line 20 choice element ────────────────────────────────────────────────

    private Element buildLine20Element(Document doc, FormPartIII p3) {
        BigDecimal amt = computeLine20Amount(p3);
        String amtStr  = amt.setScale(2, RoundingMode.HALF_UP).toPlainString();

        String elementName = switch (coalesce(p3.getLine20Type(), "20A")) {
            case "20B" -> "RcvryZoneEconomicDevBondCrAmt";
            case "20C" -> "NewCleanRenewableEgyBondCrAmt";
            case "20D" -> "QualifiedEnergyCnsrvBondCrAmt";
            case "20E" -> "QualifiedZoneAcademyBondCrAmt";
            case "20F" -> "QualifiedSchoolConstrBondCrAmt";
            default    -> "BuildAmericaBondCreditAmt";   // 20A
        };

        Element e = el(doc, elementName);
        e.setTextContent(amtStr);
        return e;
    }

    /**
     * line22 = line20 ± line21.  Reverse-compute line20 from stored values.
     * If no adjustments, line20 = line22.
     */
    private BigDecimal computeLine20Amount(FormPartIII p3) {
        BigDecimal line22 = p3.getLine22() != null ? p3.getLine22() : BigDecimal.ZERO;
        BigDecimal result = line22;
        if (hasValue(p3.getLine21a())) {
            try { result = result.subtract(new BigDecimal(p3.getLine21a())); }
            catch (NumberFormatException ignored) { /* keep line22 */ }
        }
        if (hasValue(p3.getLine21b())) {
            try { result = result.add(new BigDecimal(p3.getLine21b())); }
            catch (NumberFormatException ignored) { /* keep */ }
        }
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    // ── NetIncreaseDecreaseGrp (line 21) ─────────────────────────────────────

    private void appendNetIncreaseDecreaseGrp(Document doc, Element form, FormPartIII p3) {
        boolean has21a = hasValue(p3.getLine21a());
        boolean has21b = hasValue(p3.getLine21b());
        if (!has21a && !has21b) return;

        Element grp = el(doc, "NetIncreaseDecreaseGrp");

        if (has21a) {
            text(doc, grp, "NetIncreasePreviousPaymentAmt", formatAmt(p3.getLine21a()));
        } else {
            // XSD type is USDecimalAmountNonPos15DgtType — must be <= 0
            BigDecimal dec = BigDecimal.ZERO;
            try { dec = new BigDecimal(p3.getLine21b()).abs().negate(); }
            catch (NumberFormatException ignored) { /* use 0 */ }
            text(doc, grp, "NetDecreasePreviousPaymentAmt",
                    dec.setScale(2, RoundingMode.HALF_UP).toPlainString());
        }

        if (hasValue(p3.getLine21cCode())) {
            text(doc, grp, "NetIncreaseDecreaseExplnCd", p3.getLine21cCode());
        }

        form.appendChild(grp);
    }

    // ── USAddress ─────────────────────────────────────────────────────────────

    private Element buildUSAddress(Document doc, String street, String room, String cityStateLine) {
        // Sequence: AddressLine1Txt, [AddressLine2Txt], CityNm, StateAbbreviationCd, ZIPCd
        Element addr = el(doc, "USAddress");
        text(doc, addr, "AddressLine1Txt", sanitizeStreet(coalesce(street, "UNKNOWN ST")));
        if (hasValue(room)) {
            text(doc, addr, "AddressLine2Txt", sanitizeStreet(room));
        }
        String[] csz = parseCityStateLine(cityStateLine);
        text(doc, addr, "CityNm",               csz[0]);
        text(doc, addr, "StateAbbreviationCd",   csz[1]);
        text(doc, addr, "ZIPCd",                 csz[2]);
        return addr;
    }

    // -------------------------------------------------------------------------
    // Address parser
    // -------------------------------------------------------------------------

    private String[] parseCityStateLine(String line) {
        if (line != null) {
            Matcher m = CITY_STATE_ZIP.matcher(line.trim());
            if (m.matches()) {
                String city  = sanitizeCity(m.group(1).trim());
                String state = m.group(2);
                String zip   = m.group(3);
                return new String[]{city, state, zip};
            }
        }
        log.warn("Could not parse city/state/ZIP from: '{}' — using placeholder values", line);
        return new String[]{"UNKNOWN", "TX", "00000"};
    }

    // -------------------------------------------------------------------------
    // Sanitization helpers (enforce IRS XSD character patterns)
    // -------------------------------------------------------------------------

    /**
     * BusinessNameLine1Type: A-Z a-z 0-9 # - ( ) & ' plus single interior space.
     */
    private String sanitizeBusinessName(String name) {
        if (name == null) return "UNKNOWN";
        String s = name.replaceAll("[^A-Za-z0-9#\\-()&']", " ")
                       .replaceAll(" {2,}", " ")
                       .trim();
        return s.isEmpty() ? "UNKNOWN" : s.substring(0, Math.min(75, s.length()));
    }

    /**
     * PersonNameType: A-Z a-z 0-9 ' - plus single interior space.
     */
    private String sanitizePersonName(String name) {
        if (name == null) return "UNKNOWN";
        String s = name.replaceAll("[^A-Za-z0-9'\\-]", " ")
                       .replaceAll(" {2,}", " ")
                       .trim();
        if (s.isEmpty()) return "UNKNOWN";
        // Pattern requires end with alphanumeric/'/–, strip trailing spaces already done by trim()
        return s.substring(0, Math.min(35, s.length()));
    }

    /**
     * PersonTitleType: printable ASCII \x21-\x7E plus single interior space.
     */
    private String sanitizePersonTitle(String title) {
        if (title == null) return "OFFICER";
        String s = title.replaceAll("[^\\x21-\\x7E ]", "")
                        .replaceAll(" {2,}", " ")
                        .trim();
        return s.isEmpty() ? "OFFICER" : s.substring(0, Math.min(35, s.length()));
    }

    /**
     * StreetAddressType: A-Z a-z 0-9 - / plus single interior space; starts with alnum.
     */
    private String sanitizeStreet(String street) {
        if (street == null) return "UNKNOWN ST";
        String s = street.replaceAll("[^A-Za-z0-9\\-/]", " ")
                         .replaceAll(" {2,}", " ")
                         .trim()
                         // Ensure first char is alphanumeric
                         .replaceAll("^[^A-Za-z0-9]+", "");
        return s.isEmpty() ? "UNKNOWN ST" : s.substring(0, Math.min(35, s.length()));
    }

    /**
     * CityType: A-Z a-z plus single interior space.
     */
    private String sanitizeCity(String city) {
        if (city == null) return "UNKNOWN";
        String s = city.replaceAll("[^A-Za-z]", " ")
                       .replaceAll(" {2,}", " ")
                       .trim();
        return s.isEmpty() ? "UNKNOWN" : s.substring(0, Math.min(22, s.length()));
    }

    /**
     * BusinessNameControlType: A-Z 0-9 - & ; 1-4 chars.
     */
    private String toNameControl(String name) {
        if (name == null || name.isBlank()) return "UNKN";
        String ctrl = name.toUpperCase()
                         .replaceAll("[^A-Z0-9\\-&]", "");
        if (ctrl.isEmpty()) return "UNKN";
        return ctrl.substring(0, Math.min(4, ctrl.length()));
    }

    // ── Amount formatting ─────────────────────────────────────────────────────

    private String formatAmt(String raw) {
        if (raw == null || raw.isBlank()) return "0.00";
        try {
            return new BigDecimal(raw).setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * BankAccountType: "1" = Checking, "2" = Savings.
     */
    private String accountTypeCode(String accountType) {
        if (accountType == null) return "1";
        return switch (accountType.trim().toUpperCase()) {
            case "SAVINGS", "SAVING", "S", "2" -> "2";
            default -> "1";
        };
    }

    // -------------------------------------------------------------------------
    // DOM helpers
    // -------------------------------------------------------------------------

    private Element el(Document doc, String localName) {
        return doc.createElementNS(NS, localName);
    }

    private void text(Document doc, Element parent, String localName, String value) {
        Element e = el(doc, localName);
        e.setTextContent(value);
        parent.appendChild(e);
    }

    // -------------------------------------------------------------------------
    // Null-safety helpers
    // -------------------------------------------------------------------------

    @FunctionalInterface
    private interface Getter<T, R> {
        R get(T t);
    }

    private <T> String safe(T obj, Getter<T, String> getter) {
        return obj != null ? getter.get(obj) : null;
    }

    private String coalesce(String value, String fallback) {
        return hasValue(value) ? value : fallback;
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
