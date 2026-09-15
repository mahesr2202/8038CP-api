package com.example.form8038cp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.irs")
@Getter
@Setter
public class IrsProperties {

    /** Electronic Filing Identification Number — 6 digits assigned by IRS. */
    private String efin = "212103";

    /** Software ID registered with IRS — exactly 8 digits. */
    private String softwareId = "26026014";

    /** Software version string — max 20 characters. */
    private String softwareVersion = "1.0.0";

    /** Vendor control number — exactly 16 alphanumeric characters assigned by IRS. */
    private String vendorControlNum = "ABCD1234EFGH5678";

    /**
     * Device identifier — exactly 40 uppercase hex characters (SHA-1 length)
     * used for AtSubmissionCreationDeviceId and AtSubmissionFilingDeviceId.
     */
    private String deviceId = "0000000000000000000000000000000000000001";

    /** Tax year for which the return is filed (e.g. "2027"). */
    private String taxYear = "2027";

    /** Tax period end date in YYYY-MM-DD format. */
    private String taxPeriodEnd = "2027-12-31";

    /** IPv4 address reported in FilingSecurityInformation. Can be overridden per-request. */
    private String clientIp = "8.8.8.8";

    /** 5-digit Practitioner PIN registered with IRS for the software vendor/originator. */
    private String practitionerPin = "11111";
}
