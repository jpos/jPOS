/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

public enum CMF implements IRC {
    // Approved
    APPROVED         (0, true),
    HONOR_WITH_ID    (1, true),
    APPROVED_PARTIAL (2, true),
    APPROVED_VIP     (3, true),
    APPROVED_UPDATE_TRACK3 (4, true),
    APPROVED_ISSUER_SPECIFIED_ACCOUNT (5, true),
    APPROVED_PARTIAL_ISSUER_SPECIFIED_ACCOUNT (6, true),
    APPROVED_FEES_DISPUTED(8, true),
    APPROVED_WITH_OVERDRAFT(9, true),
    APPROVED_CUSTOMER_REACTIVATED(10, true),
    APPROVED_TERMINAL_UNABLE_TO_PROCESS_ONLINE(11),
    APPROVED_OFFLINE (12),
    APPROVED_OFFLINE_REFERRAL (13),

    // Denied Authorization
    DO_NOT_HONOUR(1000),
    EXPIRED (1001),
    SUSPECTED_FRAUD(1002),
    CONTACT_ACQUIRER(1003),
    RESTRICTED_CARD(1004),
    CONTACT_ACQUIRER_SECURITY(1005),
    MAX_PIN_TRIES_EXCEEDED(1006),
    REFER_TO_ISSUER(1007),
    REFER_TO_ISSUER_SPECIAL(1008),
    INVALID_CARD_ACCEPTOR(1009),
    INVALID_AMOUNT(1010),
    INVALID_CARD_NUMBER(1011),
    PIN_DATA_REQUIRED(1012),
    UNACCEPTABLE_FEE(1013),
    NO_ACCCOUNT_TYPE(1014),
    UNSUPPORTED_FUNCTION(1015),
    NOT_SUFFICIENT_FUNDS(1016),
    INCORRECT_PIN(1017),
    NO_CARD_RECORD(1018),
    NOT_PERMITTED_TO_CARDHOLDER(1019),
    NOT_PERMITTED_TO_TERMINAL(1020),
    EXCEEDS_WITHDRAWAL_AMOUNT_LIMIT(1021),
    SECURITY_VIOLATION(1022),
    EXCEEDS_WITHDRAWAL_FREQUENCY_LIMIT(1023),
    LAW_VIOLATION(1024),
    CARD_NOT_EFFECTIVE(1025),
    INVALID_PINBLOCK(1026),
    PIN_LENGTH_ERROR(1027),
    PIN_KEY_SYNC_ERROR(1028),
    SUSPECTED_COUNTERFEIT_CARD(1029),
    UNACCEPTED_CURRENCY(1030),
    DECLINED_FEES_DISPUTED(1031),
    LOST_OR_STOLEN_CARD(1032),
    AUTHORIZATION_LIFECYCLE_UNACCEPTABLE(1033),
    AUTHORIZATION_LIFECYCLE_EXPIRED(1034),
    CLOSED_ACCOUNT(1035),
    CLOSED_SAVINGS_ACCOUNT(1036),
    CLOSED_CREDIT_ACCOUNT(1037),
    CLOSED_ACCOUNT_TYPE(1038),
    CLOSED_CHEQUE_ACCOUNT(1039),
    BAD_DEBT(1040),
    FROM_ACCOUNT_BAD_STATUS(1041),
    TO_ACCOUNT_BAD_STATUS(1042),
    CHEQUE_ALREADY_POSTED(1043),
    INFORMATION_NOT_ON_FILE(1044),
    CARD_VERIFICATION_DATA_FAILED(1045),
    AMOUNT_NOT_FOUND(1046),
    PIN_CHANGE_REQUIRED(1047),
    NEW_PIN_INVALID(1048),
    BANK_NOT_FOUND(1049),
    BANK_NOT_EFFECTIVE(1050),
    CUSTOMER_VENDOR_NOT_FOUND(1051),
    CUSTOMER_VENDOR_NOT_EFFECTIVE(1052),
    CUSTOMER_VENDOR_ACCOUNT_INVALID(1053),
    VENDOR_NOT_FOUND(1054),
    VENDOR_NOT_EFFECTIVE(1055),
    VENDOR_DATA_INVALID(1056),
    PAYMENT_DATE_INVALID(1057),
    PERSONAL_ID_NOT_FOUND(1058),
    SCHEDULED_TRANSACTION_EXISTS(1059),
    ABORTED_AT_TERMINAL(1060),
    UNSUPPORTED_TRANSACTION(1061),
    CASHBACK_NOT_ALLOWED(1062),
    CASHBACK_AMOUNT_EXCEEDED(1063),
    DECLINED_PROCESSED_OFFLINE(1064),
    DECLINED_UNABLE_TO_PROCESS(1065),
    DECLINED_PROCESSED_OFFLINE_REFERRAL(1066),
    ID_NUMBER_INVALID(1068),
    DRIVER_NUMBER_INVALID(1069),
    VID_INVALID(1070),
    CERTIFICATE_EXPIRED(1071),
    MISSING_FIELD(1802),
    EXTRA_FIELD(1803),
    INVALID_CARD(1804),
    CARD_NOT_ACTIVE(1806),
    CARD_NOT_CONFIGURED(1808),
    SYSTEM_ERROR_DB(1811),
    SYSTEM_ERROR_TXN(1812),
    INVALID_FIELD(1830),
    MISCONFIGURED_ENDPOINT(1831),
    INVALID_REQUEST(1832),
    HOST_UNREACHABLE(1833),


    // Denied Financial
    FINANCIAL_DO_NOT_HONOUR (2000),
    FINANCIAL_EXPIRED (2001),
    FINANCIAL_SUSPECTED_FRAUD(2002),
    FINANCIAL_CONTACT_ACQUIRER(2003),
    FINANCIAL_RESTRICTED_CARD(2004),
    FINANCIAL_CONTACT_ACQUIRER_SECURITY(2005),
    FINANCIAL_MAX_PIN_TRIES_EXCEEDED(2006),
    SPECIAL_CONDITIONA(2007),
    LOST_CARD(2008),
    STOLEN_CARD(2009),
    SUSPECTED_COUNTERFELT_CARD(2010),
    MAX_DAILY_WITHDRAWAL(2011),
    MAX_DAILY_AMOUNT(2012),

    // File action
    SUCCESSFUL(3000, true),
    NOT_SUPPORTED_BY_RECEIVER(3001),
    UNABLE_TO_LOCATE_RECORD(3002),
    UPDATED_RECORD(3003),
    FIELD_EDIT_ERROR(3004),
    FILE_LOCKED_OUT(3005),
    NOT_SUCCESSFUL(3006),
    FORMAT_ERROR(3007),
    DUPLICATE(3008),
    UNKNOWN_FILE(3009),
    INVALID_CARD_OR_CARDHOLDER_NUMBER(3010),

    // Reversals
    REVERSAL_ACCEPTED(4000, true),

    // Reconciliation
    RECONCILED_IN_BALANCE(5000, true),
    RECONCILED_OUT_OF_BALANCE(5001),
    AMOUNT_NOT_RECONCILED_TOTALS_PROVIDED(5002),
    TOTALS_NOT_AVAILABLE(5003),
    NOT_RECONCILED_TOTALS_PROVIDED(5004),

    // Administrative messages
    ADMIN_MESSAGE_ACCEPTED(6000, true),

    // Fee collection
    FEE_COLLECTION_ACCEPTED (7000, true),

    // Network Management
    NET_ACCEPTED (8000, true),
    NET_RETRY(8001),

    // Misc
    ADVICE_ACK_NO_FINANCIAL(9000, true),
    ADVICE_ACCEPTED(9001, true),
    MESSAGE_ERROR(9100),
    INVALID_TRANSACTION(9102),
    RETRY_TRANSACTION(9103),
    ACQUIRER_NOT_SUPPORTED(9105),
    CUTOVER_IN_PROCESS(9106),
    ISSUER_NOT_AVAILABLE(9107),
    ROUTING_ERROR(9108),
    SYSTEM_ERROR(9109),
    ISSUER_SIGNED_OFF(9110),
    ISSUER_TIMEOUT(9111),
    ISSUER_UNAVAILABLE(9112),
    DUPLICATE_TRANSMISSION(9113),
    ORIGINAL_NOT_FOUND(9114),
    RECONCILIATION_ERROR(9115),
    MAC_INCORRECT(9116),
    MAC_KEY_SYNC_ERROR(9117),
    ZMK_NOT_AVAILABLE(9118),
    CRYPTO_ERROR(9119),
    HSM_ERROR_RETRY(9120),
    HSM_ERROR (9121),
    OUT_OF_SEQUENCE(9122),
    REQUEST_IN_PROGRESS(9123),
    INVALID_SECURITY_CODE(9124),
    DATABASE_ERROR(9125),
    CUSTOMER_VENDOR_FORMAT_ERROR(9128),
    RECURRING_DATA_ERROR(9132),
    UPDATE_NOT_ALLOWED(9133),
    AGREEMENT_VIOLATION(9350),

    GENERAL_DECLINE(9999),

    // jPOS specific result codes
    JPOS(10000),
    INTERNAL_ERROR(19999,false,true),

    // User specific result codes
    USER(90000);

    int irc;
    String ircStr;

    boolean success;
    boolean inhibit;

    private static Map<Integer,IRC> lookupInt = new HashMap<>();
    private static Map<String,IRC>  lookupStr = new HashMap<>();
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
