/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.rc;

import java.util.HashMap;
import java.util.Map;

/** Common Message Format result codes used by jPOS Transaction Manager participants. */
public enum CMF implements IRC {
    // Approved
    /** Approved. */
    APPROVED         (0, true),
    /** Honor With Id. */
    HONOR_WITH_ID    (1, true),
    /** Approved Partial. */
    APPROVED_PARTIAL (2, true),
    /** Approved Vip. */
    APPROVED_VIP     (3, true),
    /** Approved Update Track3. */
    APPROVED_UPDATE_TRACK3 (4, true),
    /** Approved Issuer Specified Account. */
    APPROVED_ISSUER_SPECIFIED_ACCOUNT (5, true),
    /** Approved Partial Issuer Specified Account. */
    APPROVED_PARTIAL_ISSUER_SPECIFIED_ACCOUNT (6, true),
    /** Approved Fees Disputed. */
    APPROVED_FEES_DISPUTED(8, true),
    /** Approved With Overdraft. */
    APPROVED_WITH_OVERDRAFT(9, true),
    /** Approved Customer Reactivated. */
    APPROVED_CUSTOMER_REACTIVATED(10, true),
    /** Approved Terminal Unable To Process Online. */
    APPROVED_TERMINAL_UNABLE_TO_PROCESS_ONLINE(11),
    /** Approved Offline. */
    APPROVED_OFFLINE (12),
    /** Approved Offline Referral. */
    APPROVED_OFFLINE_REFERRAL (13),
    /** No Reason To Decline. */
    NO_REASON_TO_DECLINE(85, true),
    /** Name Format Error. */
    NAME_FORMAT_ERROR(7),

    // Denied Authorization
    /** Do Not Honour. */
    DO_NOT_HONOUR(1000),
    /** Expired. */
    EXPIRED (1001),
    /** Suspected Fraud. */
    SUSPECTED_FRAUD(1002),
    /** Contact Acquirer. */
    CONTACT_ACQUIRER(1003),
    /** Restricted Card. */
    RESTRICTED_CARD(1004),
    /** Contact Acquirer Security. */
    CONTACT_ACQUIRER_SECURITY(1005),
    /** Max Pin Tries Exceeded. */
    MAX_PIN_TRIES_EXCEEDED(1006),
    /** Refer To Issuer. */
    REFER_TO_ISSUER(1007),
    /** Refer To Issuer Special. */
    REFER_TO_ISSUER_SPECIAL(1008),
    /** Invalid Card Acceptor. */
    INVALID_CARD_ACCEPTOR(1009),
    /** Invalid Amount. */
    INVALID_AMOUNT(1010),
    /** Invalid Card Number. */
    INVALID_CARD_NUMBER(1011),
    /** Pin Data Required. */
    PIN_DATA_REQUIRED(1012),
    /** Unacceptable Fee. */
    UNACCEPTABLE_FEE(1013),
    /** No Account Type. */
    NO_ACCOUNT_TYPE(1014),
    /** Unsupported Function. */
    UNSUPPORTED_FUNCTION(1015),
    /** Not Sufficient Funds. */
    NOT_SUFFICIENT_FUNDS(1016),
    /** Incorrect Pin. */
    INCORRECT_PIN(1017),
    /** No Card Record. */
    NO_CARD_RECORD(1018),
    /** Not Permitted To Cardholder. */
    NOT_PERMITTED_TO_CARDHOLDER(1019),
    /** Not Permitted To Terminal. */
    NOT_PERMITTED_TO_TERMINAL(1020),
    /** Exceeds Withdrawal Amount Limit. */
    EXCEEDS_WITHDRAWAL_AMOUNT_LIMIT(1021),
    /** Security Violation. */
    SECURITY_VIOLATION(1022),
    /** Exceeds Withdrawal Frequency Limit. */
    EXCEEDS_WITHDRAWAL_FREQUENCY_LIMIT(1023),
    /** Law Violation. */
    LAW_VIOLATION(1024),
    /** Card Not Effective. */
    CARD_NOT_EFFECTIVE(1025),
    /** Invalid Pinblock. */
    INVALID_PINBLOCK(1026),
    /** Pin Length Error. */
    PIN_LENGTH_ERROR(1027),
    /** Pin Key Sync Error. */
    PIN_KEY_SYNC_ERROR(1028),
    /** Suspected Counterfeit Card Dont Pickup. */
    SUSPECTED_COUNTERFEIT_CARD_DONT_PICKUP(1029),
    /** Unaccepted Currency. */
    UNACCEPTED_CURRENCY(1030),
    /** Declined Fees Disputed. */
    DECLINED_FEES_DISPUTED(1031),
    /** Lost Or Stolen Card. */
    LOST_OR_STOLEN_CARD(1032),
    /** Authorization Lifecycle Unacceptable. */
    AUTHORIZATION_LIFECYCLE_UNACCEPTABLE(1033),
    /** Authorization Lifecycle Expired. */
    AUTHORIZATION_LIFECYCLE_EXPIRED(1034),
    /** Closed Account. */
    CLOSED_ACCOUNT(1035),
    /** Closed Savings Account. */
    CLOSED_SAVINGS_ACCOUNT(1036),
    /** Closed Credit Account. */
    CLOSED_CREDIT_ACCOUNT(1037),
    /** Closed Account Type. */
    CLOSED_ACCOUNT_TYPE(1038),
    /** Closed Cheque Account. */
    CLOSED_CHEQUE_ACCOUNT(1039),
    /** Bad Debt. */
    BAD_DEBT(1040),
    /** From Account Bad Status. */
    FROM_ACCOUNT_BAD_STATUS(1041),
    /** To Account Bad Status. */
    TO_ACCOUNT_BAD_STATUS(1042),
    /** Cheque Already Posted. */
    CHEQUE_ALREADY_POSTED(1043),
    /** Information Not On File. */
    INFORMATION_NOT_ON_FILE(1044),
    /** Card Verification Data Failed. */
    CARD_VERIFICATION_DATA_FAILED(1045),
    /** Amount Not Found. */
    AMOUNT_NOT_FOUND(1046),
    /** Pin Change Required. */
    PIN_CHANGE_REQUIRED(1047),
    /** New Pin Invalid. */
    NEW_PIN_INVALID(1048),
    /** Bank Not Found. */
    BANK_NOT_FOUND(1049),
    /** Bank Not Effective. */
    BANK_NOT_EFFECTIVE(1050),
    /** Customer Vendor Not Found. */
    CUSTOMER_VENDOR_NOT_FOUND(1051),
    /** Customer Vendor Not Effective. */
    CUSTOMER_VENDOR_NOT_EFFECTIVE(1052),
    /** Customer Vendor Account Invalid. */
    CUSTOMER_VENDOR_ACCOUNT_INVALID(1053),
    /** Vendor Not Found. */
    VENDOR_NOT_FOUND(1054),
    /** Vendor Not Effective. */
    VENDOR_NOT_EFFECTIVE(1055),
    /** Vendor Data Invalid. */
    VENDOR_DATA_INVALID(1056),
    /** Payment Date Invalid. */
    PAYMENT_DATE_INVALID(1057),
    /** Personal Id Not Found. */
    PERSONAL_ID_NOT_FOUND(1058),
    /** Scheduled Transaction Exists. */
    SCHEDULED_TRANSACTION_EXISTS(1059),
    /** Indicates the transaction was aborted at the terminal. */
    ABORTED_AT_TERMINAL(1060),
    /** Unsupported Transaction. */
    UNSUPPORTED_TRANSACTION(1061),
    /** Cashback Not Allowed. */
    CASHBACK_NOT_ALLOWED(1062),
    /** Cashback Amount Exceeded. */
    CASHBACK_AMOUNT_EXCEEDED(1063),
    /** Declined Processed Offline. */
    DECLINED_PROCESSED_OFFLINE(1064),
    /** Declined Unable To Process. */
    DECLINED_UNABLE_TO_PROCESS(1065),
    /** Declined Processed Offline Referral. */
    DECLINED_PROCESSED_OFFLINE_REFERRAL(1066),
    /** Id Number Invalid. */
    ID_NUMBER_INVALID(1068),
    /** Driver Number Invalid. */
    DRIVER_NUMBER_INVALID(1069),
    /** Vid Invalid. */
    VID_INVALID(1070),
    /** Certificate Expired. */
    CERTIFICATE_EXPIRED(1071),
    /** Additional Authentication Required. */
    ADDITIONAL_AUTH_REQUIRED(1067),
    /** Surcharge Not Permitted For Card. */
    SURCHARGE_NOT_PERMITTED_FOR_CARD(1072),
    /** Surcharge Not Permitted By Network. */
    SURCHARGE_NOT_PERMITTED_BY_NETWORK(1073),
    /** Exceeds Pre Authorizationorized Amount. */
    EXCEEDS_PRE_AUTHORIZED_AMOUNT(1074),
    /** Stop Payment Specific. */
    STOP_PAYMENT_SPECIFIC(1075),
    /** Stop Payment All Merchant. */
    STOP_PAYMENT_ALL_MERCHANT(1076),
    /** Stop Payment Account. */
    STOP_PAYMENT_ACCOUNT(1077),
    /** AML Requirements Not Met. */
    AML_REQUIREMENTS_NOT_MET(1078),
    /** Exceeds Withdrawal Limit. */
    EXCEEDS_WITHDRAWAL_LIMIT(1079),
    /** Pin Not Allowed. */
    PIN_NOT_ALLOWED(1080),
    /** Message Number Out Of Sequence. */
    MESSAGE_NUMBER_OUT_OF_SEQUENCE(1081),
    /** Original Transaction Declined. */
    ORIGINAL_TRANSACTION_DECLINED(1082),

    // ICC / chip offline decision codes (ISO 8583:2023, 1500-1511)
    /** ICC Application Unable To Process. */
    ICC_APPLICATION_UNABLE_TO_PROCESS(1500),
    /** ICC Random Selection. */
    ICC_RANDOM_SELECTION(1502),
    /** Terminal Random Selection. */
    TERMINAL_RANDOM_SELECTION(1503),
    /** Terminal Not Able To Process ICC. */
    TERMINAL_NOT_ABLE_TO_PROCESS_ICC(1504),
    /** Online Forced By ICC. */
    ONLINE_FORCED_BY_ICC(1505),
    /** Online Forced By Card Acceptor. */
    ONLINE_FORCED_BY_CARD_ACCEPTOR(1506),
    /** Online Forced By CAD. */
    ONLINE_FORCED_BY_CAD(1507),
    /** Online Forced By Terminal. */
    ONLINE_FORCED_BY_TERMINAL(1508),
    /** Online Forced By Card Issuer. */
    ONLINE_FORCED_BY_CARD_ISSUER(1509),
    /** Over Floor Limit. */
    OVER_FLOOR_LIMIT(1510),
    /** Card Acceptor Suspicious. */
    CARD_ACCEPTOR_SUSPICIOUS(1511),

    // CMF private-use codes (ISO 8583 1800-1999 reserved for private use)
    /** Missing Field. */
    MISSING_FIELD(1802),
    /** Extra Field. */
    EXTRA_FIELD(1803),
    /** Invalid Card. */
    INVALID_CARD(1804),
    /** Card Not Active. */
    CARD_NOT_ACTIVE(1806),
    /** Card Not Configured. */
    CARD_NOT_CONFIGURED(1808),
    /** System Error Db. */
    SYSTEM_ERROR_DB(1811, false, true),
    /** System Error Txn. */
    SYSTEM_ERROR_TXN(1812, false, true),
    /** Cardholder Not Active. */
    CARDHOLDER_NOT_ACTIVE(1813),
    /** Cardholder Not Configured. */
    CARDHOLDER_NOT_CONFIGURED(1814),
    /** Cardholder Expired. */
    CARDHOLDER_EXPIRED(1815),
    /** Original Transaction Not Found. */
    ORIGINAL_TRANSACTION_NOT_FOUND(1816),
    /** Usage Limit Reached. */
    USAGE_LIMIT_REACHED(1817),
    /** Configuration Error. */
    CONFIGURATION_ERROR(1818),

    /** Invalid Terminal. */
    INVALID_TERMINAL(1819),
    /** Inactive Terminal. */
    INACTIVE_TERMINAL(1820),
    /** Invalid Merchant. */
    INVALID_MERCHANT(1821),
    /** Duplicate Entity. */
    DUPLICATE_ENTITY(1822),
    /** Invalid Acquirer. */
    INVALID_ACQUIRER(1823),
    /** Previously Reversed. */
    PREVIOUSLY_REVERSED(1824),
    /** Further Activity Prevents Reversal. */
    FURTHER_ACTIVITY_PREVENTS_REVERSAL(1825),
    /** Further Activity Prevents Void. */
    FURTHER_ACTIVITY_PREVENTS_VOID(1826),
    /** Original Voided. */
    ORIGINAL_VOIDED(1827),
    /** Card Tokenization Not Supported. */
    CARD_TOKENIZATION_NOT_SUPPORTED(1828),

    /** Invalid Field. */
    INVALID_FIELD(1830),
    /** Misconfigured Endpoint. */
    MISCONFIGURED_ENDPOINT(1831),
    /** Invalid Request. */
    INVALID_REQUEST(1832),
    /** Host Unreachable. */
    HOST_UNREACHABLE(1833),


    // Denied Financial
    /** Financial Do Not Honour. */
    FINANCIAL_DO_NOT_HONOUR (2000),
    /** Financial Expired. */
    FINANCIAL_EXPIRED (2001),
    /** Financial Suspected Fraud. */
    FINANCIAL_SUSPECTED_FRAUD(2002),
    /** Financial Contact Acquirer. */
    FINANCIAL_CONTACT_ACQUIRER(2003),
    /** Financial Restricted Card. */
    FINANCIAL_RESTRICTED_CARD(2004),
    /** Financial Contact Acquirer Security. */
    FINANCIAL_CONTACT_ACQUIRER_SECURITY(2005),
    /** Financial Max Pin Tries Exceeded. */
    FINANCIAL_MAX_PIN_TRIES_EXCEEDED(2006),
    /** Special Conditions. */
    SPECIAL_CONDITIONS(2007),
    /** Lost Card. */
    LOST_CARD(2008),
    /** Stolen Card. */
    STOLEN_CARD(2009),
    /** Suspected Counterfeit Card Pickup. */
    SUSPECTED_COUNTERFEIT_CARD_PICKUP(2010),
    /** Max Daily Withdrawal. */
    MAX_DAILY_WITHDRAWAL(2011),
    /** Max Daily Amount. */
    MAX_DAILY_AMOUNT(2012),
    // Chargeback pick-up resolution codes (ISO 8583:2023)
    /** Chargeback Remedied. */
    CHARGEBACK_REMEDIED(2013),
    /** Duplicate Chargeback. */
    DUPLICATE_CHARGEBACK(2014),
    /** Past Chargeback Time Limit. */
    PAST_CHARGEBACK_TIME_LIMIT(2015),
    /** Chargeback Documents Provided Hardship. */
    CHARGEBACK_DOCS_PROVIDED_HARDSHIP(2016),
    /** Invalid Member Message Text. */
    INVALID_MEMBER_MESSAGE_TEXT(2017),
    /** Correct Card Acceptor Category Provided. */
    CORRECT_CARD_ACCEPTOR_CATEGORY_PROVIDED(2018),
    /** Authorizationorization Advised Suspicious. */
    AUTHORIZATION_ADVISED_SUSPICIOUS(2019),
    /** No Authorization Required. */
    NO_AUTHORIZATION_REQUIRED(2020),
    /** Account Not on Warning Bulletin. */
    ACCOUNT_NOT_ON_WARNING_BULLETIN(2021),
    /** Chargeback Documents Illegible 2022. */
    CHARGEBACK_DOCS_ILLEGIBLE_2022(2022),
    /** Chargeback Documents Invalid 2023. */
    CHARGEBACK_DOCS_INVALID_2023(2023),

    // File action
    /** Successful. */
    SUCCESSFUL(3000, true),
    /** Not Supported By Receiver. */
    NOT_SUPPORTED_BY_RECEIVER(3001),
    /** Unable To Locate Record. */
    UNABLE_TO_LOCATE_RECORD(3002),
    /** Updated Record. */
    UPDATED_RECORD(3003),
    /** Field Edit Error. */
    FIELD_EDIT_ERROR(3004),
    /** File Locked Out. */
    FILE_LOCKED_OUT(3005),
    /** Not Successful. */
    NOT_SUCCESSFUL(3006),
    /** Format Error. */
    FORMAT_ERROR(3007),
    /** Duplicate. */
    DUPLICATE(3008),
    /** Unknown File. */
    UNKNOWN_FILE(3009),
    /** Invalid Card Or Cardholder Number. */
    INVALID_CARD_OR_CARDHOLDER_NUMBER(3010),

    // Reversals
    /** Reversal Accepted. */
    REVERSAL_ACCEPTED(4000, true),
    /** Reversal Unspecified. */
    REVERSAL_UNSPECIFIED(4001),
    /** Reversal Suspected Malfunction. */
    REVERSAL_SUSPECTED_MALFUNCTION(4002),
    /** Reversal Format Error. */
    REVERSAL_FORMAT_ERROR(4003),
    /** Reversal Completed Partially. */
    REVERSAL_COMPLETED_PARTIALLY(4004),
    /** Reversal Original Amount Incorrect. */
    REVERSAL_ORIGINAL_AMOUNT_INCORRECT(4005),
    /** Reversal Response Too Late. */
    REVERSAL_RESPONSE_TOO_LATE(4006),
    /** Reversal Device Unable To Complete. */
    REVERSAL_DEVICE_UNABLE_TO_COMPLETE(4007),
    /** Reversal Deposit Out Of Balance. */
    REVERSAL_DEPOSIT_OUT_OF_BALANCE(4008),
    /** Reversal No Check In Envelope. */
    REVERSAL_NO_CHECK_IN_ENVELOPE(4009),
    /** Reversal Payment Out Of Balance. */
    REVERSAL_PAYMENT_OUT_OF_BALANCE(4010),
    /** Reversal Deposit Out-of-Balance Applied. */
    REVERSAL_DEPOSIT_OOB_APPLIED(4011),
    /** Reversal Payment Out-of-Balance Applied. */
    REVERSAL_PAYMENT_OOB_APPLIED(4012),
    /** Reversal Unable To Deliver. */
    REVERSAL_UNABLE_TO_DELIVER(4013),
    /** Reversal Suspected Malfunction Card Retained. */
    REVERSAL_SUSPECTED_MALFUNCTION_CARD_RETAINED(4014),
    /** Reversal Suspected Malfunction Card Returned. */
    REVERSAL_SUSPECTED_MALFUNCTION_CARD_RETURNED(4015),
    /** Reversal Suspected Malfunction Track3 Not Updated. */
    REVERSAL_SUSPECTED_MALFUNCTION_TRACK3_NOT_UPDATED(4016),
    /** Reversal Suspected Malfunction No Cash. */
    REVERSAL_SUSPECTED_MALFUNCTION_NO_CASH(4017),
    /** Reversal Timeout No Cash. */
    REVERSAL_TIMEOUT_NO_CASH(4018),
    /** Reversal Timeout Card Retained No Cash. */
    REVERSAL_TIMEOUT_CARD_RETAINED_NO_CASH(4019),
    /** Reversal Invalid Response. */
    REVERSAL_INVALID_RESPONSE(4020),
    /** Reversal Timeout Waiting Response. */
    REVERSAL_TIMEOUT_WAITING_RESPONSE(4021),
    // Chargeback / retrieval result codes (ISO 8583:2023 4xxx range)
    /** Chargeback Information Not Received. */
    CHARGEBACK_INFO_NOT_RECEIVED(4501),
    /** Chargeback Information Illegible Or Missing. */
    CHARGEBACK_INFO_ILLEGIBLE_OR_MISSING(4502),
    /** Chargeback Warning Bulletin. */
    CHARGEBACK_WARNING_BULLETIN(4507),
    /** Chargeback Authorization Not Obtained. */
    CHARGEBACK_AUTH_NOT_OBTAINED(4508),
    /** Chargeback Account Not On File. */
    CHARGEBACK_ACCOUNT_NOT_ON_FILE(4512),
    /** Chargeback Earlier Warning Protection. */
    CHARGEBACK_EARLIER_WARNING_PROTECTION(4524),
    /** Chargeback Amount Differs. */
    CHARGEBACK_AMOUNT_DIFFERS(4531),
    /** Chargeback Duplicate Processing. */
    CHARGEBACK_DUPLICATE_PROCESSING(4534),
    /** Chargeback Card Invalid Or Expired. */
    CHARGEBACK_CARD_INVALID_OR_EXPIRED(4535),
    /** Chargeback No Cardholder Authorizationorization. */
    CHARGEBACK_NO_CARDHOLDER_AUTHORIZATION(4537),
    /** Chargeback Fraudulent Processing. */
    CHARGEBACK_FRAUDULENT_PROCESSING(4540),
    /** Chargeback Cancelled Recurring. */
    CHARGEBACK_CANCELLED_RECURRING(4541),
    /** Chargeback Late Presentment. */
    CHARGEBACK_LATE_PRESENTMENT(4542),
    /** Chargeback Wrong Currency. */
    CHARGEBACK_WRONG_CURRENCY(4546),
    /** Chargeback Exceeds Floor Limit Fraud. */
    CHARGEBACK_EXCEEDS_FLOOR_LIMIT_FRAUD(4547),
    /** Chargeback Questionable Acceptor. */
    CHARGEBACK_QUESTIONABLE_ACCEPTOR(4549),
    /** Chargeback Credit As Purchase. */
    CHARGEBACK_CREDIT_AS_PURCHASE(4550),
    /** Chargeback Not As Described. */
    CHARGEBACK_NOT_AS_DESCRIBED(4553),
    /** Chargeback Cardholder Dispute. */
    CHARGEBACK_CARDHOLDER_DISPUTE(4554),
    /** Chargeback Non-Receipt Merchandise. */
    CHARGEBACK_NON_RECEIPT_MERCHANDISE(4555),
    /** Chargeback Defective Merchandise. */
    CHARGEBACK_DEFECTIVE_MERCHANDISE(4556),
    /** Chargeback Card Activated Telephone. */
    CHARGEBACK_CARD_ACTIVATED_TELEPHONE(4557),
    /** Chargeback Services Not Rendered. */
    CHARGEBACK_SERVICES_NOT_RENDERED(4559),
    /** Chargeback Credit Not Processed. */
    CHARGEBACK_CREDIT_NOT_PROCESSED(4560),
    /** Chargeback Counterfeit Magnetic Stripe. */
    CHARGEBACK_COUNTERFEIT_MAGNETIC_STRIPE(4562),
    /** Chargeback Documents Not Received. */
    CHARGEBACK_DOCS_NOT_RECEIVED(4563),
    /** Chargeback Documents Illegible. */
    CHARGEBACK_DOCS_ILLEGIBLE(4564),
    /** Chargeback Documents Invalid. */
    CHARGEBACK_DOCS_INVALID(4565),
    /** Chargeback Valid ARN. */
    CHARGEBACK_VALID_ARN(4566),
    /** Chargeback Invalid ARN Documents Different. */
    CHARGEBACK_INVALID_ARN_DOCS_DIFFERENT(4567),
    /** Chargeback Invalid ARN No Documents. */
    CHARGEBACK_INVALID_ARN_NO_DOCS(4568),
    /** Chargeback Expired Card. */
    CHARGEBACK_EXPIRED_CARD(4573),
    /** Chargeback Ineligible Transaction. */
    CHARGEBACK_INELIGIBLE_TRANSACTION(4578),
    /** Chargeback Receipt Not Received. */
    CHARGEBACK_RECEIPT_NOT_RECEIVED(4579),
    /** Chargeback Processing Error. */
    CHARGEBACK_PROCESSING_ERROR(4580),
    /** Chargeback Missing Imprint. */
    CHARGEBACK_MISSING_IMPRINT(4581),
    /** Chargeback Non-Possession Of Card. */
    CHARGEBACK_NON_POSSESSION_OF_CARD(4583),
    /** Chargeback Missing Signature. */
    CHARGEBACK_MISSING_SIGNATURE(4584),
    /** Chargeback Amount Altered. */
    CHARGEBACK_AMOUNT_ALTERED(4586),
    /** Chargeback Domestic Processing Violation. */
    CHARGEBACK_DOMESTIC_PROCESSING_VIOLATION(4587),
    /** Chargeback Non-Receipt ATM. */
    CHARGEBACK_NON_RECEIPT_ATM(4590),
    /** Chargeback Cancelled Reservation. */
    CHARGEBACK_CANCELLED_RESERVATION(4594),
    /** Chargeback Advance Lodging Deposit. */
    CHARGEBACK_ADVANCE_LODGING_DEPOSIT(4595),
    /** Chargeback Exceeds Limited Amount. */
    CHARGEBACK_EXCEEDS_LIMITED_AMOUNT(4596),

    // Reconciliation
    /** Reconciled In Balance. */
    RECONCILED_IN_BALANCE(5000, true),
    /** Reconciled Out Of Balance. */
    RECONCILED_OUT_OF_BALANCE(5001),
    /** Amount Not Reconciled Totals Provided. */
    AMOUNT_NOT_RECONCILED_TOTALS_PROVIDED(5002),
    /** Totals Not Available. */
    TOTALS_NOT_AVAILABLE(5003),
    /** Not Reconciled Totals Provided. */
    NOT_RECONCILED_TOTALS_PROVIDED(5004),

    // Administrative messages
    /** Administrative message accepted. */
    ADMIN_MESSAGE_ACCEPTED(6000, true),
    // Retrieval / copy request reason codes (ISO 8583:2023)
    /** Cardholder Disputes Amount. */
    CARDHOLDER_DISPUTES_AMOUNT(6005),
    /** Cardholder Does Not Recognize. */
    CARDHOLDER_DOES_NOT_RECOGNIZE(6021),
    /** ICC Certificate Requested. */
    ICC_CERTIFICATE_REQUESTED(6022),
    /** Cardholder Needs Records. */
    CARDHOLDER_NEEDS_RECORDS(6023),
    /** Copy With Signature Requested. */
    COPY_WITH_SIGNATURE_REQUESTED(6028),
    /** Travel And Entertainment Docs Requested. */
    TRAVEL_AND_ENTERTAINMENT_DOCS_REQUESTED(6029),
    /** Copy Original Lost In Transit. */
    COPY_ORIGINAL_LOST_IN_TRANSIT(6032),
    /** Written Request Inadequate Copy. */
    WRITTEN_REQUEST_INADEQUATE_COPY(6035),
    /** Legal Process Request. */
    LEGAL_PROCESS_REQUEST(6036),
    /** Received Copy Illegible. */
    RECEIVED_COPY_ILLEGIBLE(6037),
    /** Paper Handwriting Analysis Request. */
    PAPER_HANDWRITING_ANALYSIS_REQUEST(6038),
    /** Fraud Investigation. */
    FRAUD_INVESTIGATION(6041),
    /** Arbitration or Compliance Documentation Required. */
    ARBITRATION_OR_COMPLIANCE_DOCS_REQUIRED(6042),
    /** Retrieval Not Fulfilled Cannot Trace. */
    RETRIEVAL_NOT_FULFILLED_CANNOT_TRACE(6043),
    /** Retrieval Not Fulfilled Invalid Ref. */
    RETRIEVAL_NOT_FULFILLED_INVALID_REF(6044),
    /** Retrieval Not Fulfilled Ref PAN Incompatible. */
    RETRIEVAL_NOT_FULFILLED_REF_PAN_INCOMPATIBLE(6045),
    /** Retrieval Docs Supplied. */
    RETRIEVAL_DOCS_SUPPLIED(6046),
    /** Retrieval Cannot Fulfill Docs Unavailable. */
    RETRIEVAL_CANNOT_FULFILL_DOCS_UNAVAILABLE(6047),
    /** Retrieval Will Not Fulfill Not Required. */
    RETRIEVAL_WILL_NOT_FULFILL_NOT_REQUIRED(6048),
    /** Retrieval Cannot Fulfill ICC Cert Unavailable. */
    RETRIEVAL_CANNOT_FULFILL_ICC_CERT_UNAVAILABLE(6049),

    // Fee collection
    /** Fee Collection Accepted. */
    FEE_COLLECTION_ACCEPTED (7000, true),

    // Network Management
    /** Net Accepted. */
    NET_ACCEPTED (8000, true),
    /** Net Retry. */
    NET_RETRY(8001),
    /** Key Verification Failed. */
    KEY_VERIFICATION_FAILED(8002),
    // Key exchange / lifecycle management codes (ISO 8583:2023)
    /** Key Exchange Activation Lifecycle. */
    KEY_EXCHANGE_ACTIVATION_LIFECYCLE(8100),
    /** Key Exchange Activation Out Of Sync. */
    KEY_EXCHANGE_ACTIVATION_OUT_OF_SYNC(8101),
    /** Key Exchange Activation Security. */
    KEY_EXCHANGE_ACTIVATION_SECURITY(8102),
    /** Key Exchange Deactivation. */
    KEY_EXCHANGE_DEACTIVATION(8103),
    /** Key Exchange Verification Lifecycle. */
    KEY_EXCHANGE_VERIFICATION_LIFECYCLE(8104),
    /** Key Exchange Verification Out Of Sync. */
    KEY_EXCHANGE_VERIFICATION_OUT_OF_SYNC(8105),

    // Misc
    /** Advice acknowledged with no financial impact. */
    ADVICE_ACK_NO_FINANCIAL(9000, true),
    /** Advice accepted. */
    ADVICE_ACCEPTED(9001, true),
    /** Message Error. */
    MESSAGE_ERROR(9100),
    /** Invalid Transaction. */
    INVALID_TRANSACTION(9102),
    /** Retry Transaction. */
    RETRY_TRANSACTION(9103),
    /** Acquirer is not supported. */
    ACQUIRER_NOT_SUPPORTED(9105),
    /** Cutover In Process. */
    CUTOVER_IN_PROCESS(9106),
    /** Issuer Not Available. */
    ISSUER_NOT_AVAILABLE(9107),
    /** Routing Error. */
    ROUTING_ERROR(9108),
    /** System Error. */
    SYSTEM_ERROR(9109),
    /** Issuer Signed Off. */
    ISSUER_SIGNED_OFF(9110),
    /** Issuer Timeout. */
    ISSUER_TIMEOUT(9111),
    /** Issuer Unavailable. */
    ISSUER_UNAVAILABLE(9112),
    /** Duplicate Transmission. */
    DUPLICATE_TRANSMISSION(9113),
    /** Original Not Found. */
    ORIGINAL_NOT_FOUND(9114),
    /** Reconciliation Error. */
    RECONCILIATION_ERROR(9115),
    /** Mac Incorrect. */
    MAC_INCORRECT(9116),
    /** Mac Key Sync Error. */
    MAC_KEY_SYNC_ERROR(9117),
    /** Zmk Not Available. */
    ZMK_NOT_AVAILABLE(9118),
    /** Crypto Error. */
    CRYPTO_ERROR(9119),
    /** Hsm Error Retry. */
    HSM_ERROR_RETRY(9120),
    /** Hsm Error. */
    HSM_ERROR (9121),
    /** Out Of Sequence. */
    OUT_OF_SEQUENCE(9122),
    /** Request In Progress. */
    REQUEST_IN_PROGRESS(9123),
    /** Invalid Security Code. */
    INVALID_SECURITY_CODE(9124),
    /** Database Error. */
    DATABASE_ERROR(9125),
    /** Invalid IIN. */
    INVALID_IIN(9126),
    /** Customer Vendor Format Error. */
    CUSTOMER_VENDOR_FORMAT_ERROR(9128),
    /** Recurring Data Error. */
    RECURRING_DATA_ERROR(9132),
    /** Update Not Allowed. */
    UPDATE_NOT_ALLOWED(9133),
    /** Agreement Violation. */
    AGREEMENT_VIOLATION(9350),

    /** General Decline. */
    GENERAL_DECLINE(9999),

    // jPOS specific result codes
    /** Jpos. */
    JPOS(10000),
    /** Internal Error. */
    INTERNAL_ERROR(19999,false,true),

    // User specific result codes
    /** User-defined result code. */
    USER(90000);

    private final int irc;
    private final String ircStr;

    private final boolean success;
    private final boolean inhibit;

    private static final Map<Integer,IRC> lookupInt = new HashMap<>();
    private static final Map<String,IRC>  lookupStr = new HashMap<>();
    static {
        // This section executes after all the enum instances have been constructed
        for (IRC irc : values()) {
            lookupInt.put(irc.irc(), irc);
            lookupStr.put(irc.ircString(), irc);
        }
    }

    CMF(int irc) {
        this (irc, false, false);
    }
    CMF(int irc, boolean success) {
        this(irc, success, false);
    }
    CMF(int irc, boolean success, boolean inhibit) {
        this.irc = irc;
        this.ircStr = String.format("%04d", irc);
        this.success = success;
        this.inhibit = inhibit;
    }

    @Override
    public int irc() {
        return irc;
    }

    @Override
    public String ircString() {
        return ircStr;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public boolean inhibit() {
        return inhibit;
    }

    /**
     * Looks up a CMF value by its integer code.
     * @param i the integer code
     * @return the matching IRC, or null if not found
     */
    public static IRC valueOf(int i) {
        return lookupInt.get(i);
    }

    /**
     * Returns the {@code CMF} instance that has the given String as its jPOS-CMF Result Code
     * (usually transmitted in DE-39).
     *
     * @param irc a String representing a jPOS-CMF Result Code
     * @return the corresponding CMF instance or {@code null}
     */
    public static CMF fromIsoString(String irc) {
        return (irc == null) ? null : (CMF)lookupStr.get(irc.trim());
    }
}
