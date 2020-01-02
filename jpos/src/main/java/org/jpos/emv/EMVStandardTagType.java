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

package org.jpos.emv;


import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVDataFormat;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Vishnu Pillai
 */
public enum EMVStandardTagType implements EMVTagType {

    ACCOUNT_TYPE_0x5F57(0x5F57, "Account Type",
            "Indicates the type of account selected on the terminal, coded as specified in Annex G",
            DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(2), 0x00,
            new FixedByteLength(1)),
    ACQUIRER_IDENTIFIER_0x9F01(0x9F01, "Acquirer Identifier",
            "Uniquely identifies the acquirer within each payment system", DataSource.TERMINAL,
            TLVDataFormat.PACKED_NUMERIC, new VariableDataLength(6, 11), 0x00, new FixedByteLength(6)),
    ADDITIONAL_TERMINAL_CAPABILITIES_0x9F40(0x9F40, "Additional Terminal Capabilities",
            "Indicates the data input and output capabilities of the terminal", DataSource.TERMINAL,
            TLVDataFormat.BINARY, new FixedDataLength(5), 0x00, new FixedByteLength(5)),
    AMOUNT_AUTHORISED_BINARY_0x81(0x81, "Amount, Authorised (Binary)",
            "Authorised amount of the transaction (excluding adjustments)", DataSource.TERMINAL,
            TLVDataFormat.BINARY, new FixedDataLength(4), 0x00, new FixedByteLength(4)),
    AMOUNT_AUTHORISED_NUMERIC_0x9F02(0x9F02, "Amount, Authorised (Numeric)",
            "Authorised amount of the transaction (excluding adjustments)", DataSource.TERMINAL,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(12), 0x00, new FixedByteLength(6)),
    AMOUNT_OTHER_BINARY_0x9F04(0x9F04, "Amount, Other (Binary)",
            "Secondary amount associated with the transaction representing a cashback amount",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new FixedDataLength(4), 0x00,
            new FixedByteLength(4)),
    AMOUNT_OTHER_NUMERIC_0x9F03(0x9F03, "Amount, Other (Numeric)",
            "Secondary amount associated with the transaction representing a cashback amount",
            DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(12), 0x00,
            new FixedByteLength(6)),
    AMOUNT_REFERENCE_CURRENCY_0x9F3A(0x9F3A, "Amount, Reference Currency",
            "Authorised amount expressed in the reference currency", DataSource.TERMINAL,
            TLVDataFormat.BINARY, new FixedDataLength(4), 0x00, new FixedByteLength(4)),
    APPLICATION_CRYPTOGRAM_0x9F26(0x9F26, "Application Cryptogram",
            "Cryptogram returned by the ICC in response of the GENERATE AC command", DataSource.ICC,
            TLVDataFormat.BINARY, new FixedDataLength(8), 0x77, new FixedByteLength(8)),
    APPLICATION_CURRENCY_CODE_0x9F42(0x9F42, "Application Currency Code",
            "Indicates the currency in which the account is managed according to ISO 4217",
            DataSource.ICC, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(3), 0x70,
            new FixedByteLength(2)),
    APPLICATION_CURRENCY_EXPONENT_0x9F44(
            0x9F44,
            "Application Currency Exponent",
            "Indicates the implied position of the decimal point from the right of the amount represented according to ISO 4217",
            DataSource.ICC, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(1), 0x70,
            new FixedByteLength(1)),
    APPLICATION_DEDICATED_FILE_NAME_0x4F(0x4F, "Application Dedicated File (ADF) Name",
            "Identifies the application as described in ISO/IEC 7816-5", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(5, 16), 0x61, new VariableByteLength(5, 16)),
    APPLICATION_DISCRETIONARY_DATA_0x9F05(0x9F05, "Application Discretionary Data",
            "Issuer or payment system specified data relating to the application", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(1, 32), 0x70, new VariableByteLength(1, 32)),
    APPLICATION_EFFECTIVE_DATE_0x5F25(0x5F25, "Application Effective Date",
            "Date from which the application may be used", DataSource.ICC, TLVDataFormat.PACKED_NUMERIC_DATE_YYMMDD,
            new FixedDataLength(6), 0x70, new FixedByteLength(3)),
    APPLICATION_EXPIRATION_DATE_0x5F24(0x5F24, "Application Expiration Date",
            "Date after which application expires", DataSource.ICC, TLVDataFormat.PACKED_NUMERIC_DATE_YYMMDD,
            new FixedDataLength(6), 0x70, new FixedByteLength(3)),
    APPLICATION_FILE_LOCATOR_0x94(
            0x94,
            "Application File Locator (AFL)",
            "Indicates the location (SFI, range of records) of the AEFs related to a given application",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(0, 252), 0x77,
            new VariableByteLength(0, 252)),
    APPLICATION_IDENTIFIER_TERMINAL_0x9F06(0x9F06, "Application Identifier (AID) – terminal",
            "Identifies the application as described in ISO/IEC 7816-5", DataSource.TERMINAL,
            TLVDataFormat.BINARY, new VariableDataLength(5, 16), 0x00, new VariableByteLength(5, 16)),
    APPLICATION_INTERCHANGE_PROFILE_0x82(0x82, "Application Interchange Profile",
            "Indicates the capabilities of the card to support specific functions in the application",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(2), 0x77, new FixedByteLength(2)),
    APPLICATION_LABEL_0x50(0x50, "Application Label",
            "Mnemonic associated with the AID according to ISO/IEC 7816-5", DataSource.ICC,
            TLVDataFormat.ASCII_ALPHA_NUMERIC_SPACE, new VariableDataLength(1, 16), 0x61,
            new VariableByteLength(1, 16)),
    APPLICATION_PREFERRED_NAME_0x9F12(0x9F12, "Application Preferred Name",
            "Preferred mnemonic associated with the AID", DataSource.ICC,
            TLVDataFormat.ASCII_ALPHA_NUMERIC_SPECIAL, new VariableDataLength(1, 16), 0x61,
            new VariableByteLength(1, 16)),
    APPLICATION_PRIMARY_ACCOUNT_NUMBER_0x5A(0x5A, "Application Primary Account Number (PAN)",
            "Valid cardholder account number", DataSource.ICC, TLVDataFormat.COMPRESSED_NUMERIC,
            new VariableDataLength(1, 19), 0x70, new VariableByteLength(1, 10)),
    APPLICATION_PRIMARY_ACCOUNT_NUMBER_SEQUENCE_NUMBER_0x5F34(0x5F34,
            "Application Primary Account Number (PAN) Sequence Number",
            "Identifies and differentiates cards with the same PAN", DataSource.ICC,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(2), 0x70, new FixedByteLength(1)),
    APPLICATION_PRIORITY_INDICATOR_0x87(0x87, "Application Priority Indicator",
            "Indicates the priority of a given application or group of applications in a directory",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(1), 0x61, new FixedByteLength(1)),
    APPLICATION_REFERENCE_CURRENCY_0x9F3B(
            0x9F3B,
            "Application Reference Currency",
            "1-4 currency codes used between the terminal and the ICC when the Transaction Currency Code is "
                    + "different from the Application Currency Code; each code is 3 digits according to ISO 4217",
            DataSource.ICC, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(3), 0x70,
            new VariableByteLength(2, 8)),
    APPLICATION_REFERENCE_CURRENCY_EXPONENT_0x9F43(
            0x9F43,
            "Application Reference Currency Exponent",
            "Indicates the implied position of the decimal point from the right of the amount, for each of the 1-4 "
                    + "reference currencies represented according to ISO 4217", DataSource.ICC,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(1), 0x70, new VariableByteLength(1, 4)),
    APPLICATION_TEMPLATE_0x61(
            0x61,
            "Application Template",
            "Contains one or more data objects relevant to an application directory entry according to ISO/IEC 7816-5",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 251), 0x70,
            new VariableByteLength(1, 252)),
    APPLICATION_TRANSACTION_COUNTER_0x9F36(
            0x9F36,
            "Application Transaction Counter (ATC)",
            "Counter maintained by the application in the ICC (incrementing the ATC is managed by the ICC)",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(2), 0x77, new FixedByteLength(2)),
    APPLICATION_USAGE_CONTROL_0x9F07(
            0x9F07,
            "Application Usage Control",
            "Indicates issuer’s specified restrictions on the geographic usage and services allowed for the application",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(2), 0x70, new FixedByteLength(2)),
    APPLICATION_VERSION_NUMBER_ICC_0x9F08(0x9F08, "Application Version Number (ICC)",
            "Version number assigned by the payment system for the application. From ICC",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(2), 0x70, new FixedByteLength(2)),
    APPLICATION_VERSION_NUMBER_TERMINAL_0x9F09(0x9F09, "Application Version Number (Terminal)",
            "Version number assigned by the payment system for the application. From Terminal",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new FixedDataLength(2), 0x00,
            new FixedByteLength(2)),

    AUTHORISATION_CODE_0x89(0x89, "Authorisation Code",
            "Value generated by the authorisation authority for an approved transaction",
            DataSource.ISSUER, TLVDataFormat.PROPRIETARY, new FixedDataLength(
            DataLength.DATA_LENGTH_PROPRIETARY), 0x00, new FixedByteLength(6)),
    AUTHORISATION_RESPONSE_CODE_0x8A(0x8A, "Authorisation Response Code",
            "Code that defines the disposition of a message", DataSource.ISSUER,
            TLVDataFormat.ASCII_ALPHA_NUMERIC, new FixedDataLength(2), 0x00, new FixedByteLength(2)),
    BANK_IDENTIFIER_CODE_0x5F54(0x5F54, "Bank Identifier Code (BIC)",
            "Uniquely identifies a bank as defined in ISO 9362.", DataSource.ICC,
            TLVDataFormat.PROPRIETARY, new VariableDiscreteDataLength(DataLength.DATA_LENGTH_PROPRIETARY,
            DataLength.DATA_LENGTH_PROPRIETARY), 0xBF0C, new VariableDiscreteByteLength(8, 11)),

    CARD_RISK_MANAGEMENT_DATA_OBJECT_LIST_1_0x8C(
            0x8C,
            "Card Risk Management Data Object List 1 (CDOL1)",
            "List of data objects (tag and length) to be passed to the ICC in the first GENERATE AC command",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 252), 0x70,
            new VariableByteLength(1, 252)),
    CARD_RISK_MANAGEMENT_DATA_OBJECT_LIST_2_0x8D(
            0x8D,
            "Card Risk Management Data Object List 2 (CDOL2)",
            "List of data objects (tag and length) to be passed to the ICC in the second GENERATE AC command",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 252), 0x70,
            new VariableByteLength(1, 252)),
    CARDHOLDER_NAME_0x5F20(0x5F20, "Cardholder Name",
            "Indicates cardholder name according to ISO 7813", DataSource.ICC,
            TLVDataFormat.ASCII_ALPHA_NUMERIC_SPECIAL, new VariableDataLength(2, 26), 0x70,
            new VariableByteLength(2, 26)),
    CARDHOLDER_NAME_EXTENDED_0x9F0B(
            0x9F0B,
            "Cardholder Name Extended",
            "Indicates the whole cardholder name when greater than 26 characters using the same coding convention as "
                    + "in ISO 7813", DataSource.ICC, TLVDataFormat.ASCII_ALPHA_NUMERIC_SPECIAL,
            new VariableDataLength(27, 45), 0x70, new VariableByteLength(27, 45)),
    CARDHOLDER_VERIFICATION_METHOD_LIST_0x8E(0x8E, "Cardholder Verification Method (CVM) List",
            "Identifies a method of verification of the cardholder supported by the application",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(10, 252), 0x70,
            new VariableByteLength(10, 252)),
    CARDHOLDER_VERIFICATION_METHOD_CVM_RESULTS_0x9F34(0x9F34,
            "Cardholder Verification Method (CVM) Results",
            "Indicates the results of the last CVM performed", DataSource.TERMINAL, TLVDataFormat.BINARY,
            new FixedDataLength(3), 0x00, new FixedByteLength(3)),
    CERTIFICATION_AUTHORITY_PUBLIC_KEY_INDEX_0x8F(0x8F, "Certification Authority Public Key Index",
            "Identifies the certification authority’s public key in conjunction with the RID",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(1), 0x70, new FixedByteLength(1)),
    CERTIFICATION_AUTHORITY_PUBLIC_KEY_INDEX_0x9F22(0x9F22,
            "Certification Authority Public Key Index",
            "Identifies the certification authority’s public key in conjunction with the RID",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new FixedDataLength(1), 0x00,
            new FixedByteLength(1)),
    COMMAND_TEMPLATE_0x83(0x83, "Command Template",
            "Identifies the data field of a command message", DataSource.TERMINAL, TLVDataFormat.BINARY,
            new VariableDataLength(DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0x00,
            new VariableByteLength(ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    CRYPTOGRAM_INFORMATION_DATA_0x9F27(0x9F27, "Cryptogram Information Data",
            "Indicates the type of cryptogram and the actions to be performed by the terminal",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(1), 0x77, new FixedByteLength(1)),
    DATA_AUTHENTICATION_CODE_0x9F45(0x9F45, "Data Authentication Code",
            "An issuer assigned value that is retained by the terminal during the verification process of the "
                    + "Signed Static Application Data", DataSource.ICC, TLVDataFormat.BINARY,
            new FixedDataLength(2), 0x00, new FixedByteLength(2)),
    DEDICATED_FILE_NAME_0x84(0x84, "Dedicated File (DF) Name",
            "Identifies the name of the DF as described in ISO/IEC 7816-4", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(5, 16), 0x6F, new VariableByteLength(5, 16)),
    DIRECTORY_DEFINITION_FILE_NAME_0x9D(0x9D, "Directory Definition File (DDF) Name",
            "Identifies the name of a DF associated with a directory", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(5, 16), 0x61, new VariableByteLength(5, 16)),
    DIRECTORY_DISCRETIONARY_TEMPLATE_0x73(0x73, "Directory Discretionary Template",
            "Issuer discretionary part of the directory according to ISO/IEC 7816-5", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(1, 252), 0x61, new VariableByteLength(1, 252)),
    DYNAMIC_DATA_AUTHENTICATION_DATA_OBJECT_LIST_0x9F49(
            0x9F49,
            "Dynamic Data Authentication Data Object List (DDOL)",
            "List of data objects (tag and length) to be passed to the ICC in the INTERNAL AUTHENTICATE command",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 252), 0x70,
            new VariableByteLength(1, 252)),
    FILE_CONTROL_INFORMATION_ISSUER_DISCRETIONARY_DATA_0xBF0C(0xBF0C,
            "File Control Information (FCI) Issuer Discretionary Data",
            "Issuer discretionary part of the FCI", DataSource.ICC, TLVDataFormat.BINARY,
            new VariableDataLength(1, 222), 0xA5, new VariableByteLength(1, 222)),
    FILE_CONTROL_INFORMATION_PROPRIETARY_TEMPLATE_0xA5(
            0xA5,
            "File Control Information (FCI) Proprietary Template",
            "Identifies the data object proprietary to this specification in the FCI template according to ISO/IEC 7816-4",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x6F, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    FILE_CONTROL_INFORMATION_TEMPLATE_0x6F(0x6F, "File Control Information (FCI) Template",
            "Identifies the FCI template according to ISO/IEC 7816-4", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(1, 252), 0x00, new VariableByteLength(1, 252)),
    ICC_DYNAMIC_NUMBER_0x9F4C(0x9F4C, "ICC Dynamic Number",
            "Time-variant number generated by the ICC, to be captured by the terminal", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(2, 8), 0x00, new VariableByteLength(2, 8)),
    INTEGRATED_CIRCUIT_CARD_PIN_ENCIPHERMENT_PUBLIC_KEY_CERTIFICATE_0x9F2D(0x9F2D,
            "Integrated Circuit Card (ICC) PIN Encipherment Public Key Certificate",
            "ICC PIN Encipherment Public Key certified by the issuer", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x70, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    INTEGRATED_CIRCUIT_CARD_PIN_ENCIPHERMENT_PUBLIC_KEY_EXPONENT_0x9F2E(0x9F2E,
            "Integrated Circuit Card (ICC) PIN Encipherment Public Key Exponent",
            "ICC PIN Encipherment Public Key Exponent used for PIN encipherment", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(1, 3), 0x70, new VariableDiscreteByteLength(1, 3)),
    INTEGRATED_CIRCUIT_CARD_PIN_ENCIPHERMENT_PUBLIC_KEY_REMAINDER_0x9F2F(0x9F2F,
            "Integrated Circuit Card (ICC) PIN Encipherment Public Key Remainder",
            "Remaining digits of the ICC PIN Encipherment Public Key Modulus", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x70, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    INTEGRATED_CIRCUIT_CARD_PUBLIC_KEY_CERTIFICATE_0x9F46(0x9F46,
            "Integrated Circuit Card (ICC) Public Key Certificate",
            "ICC Public Key certified by the issuer", DataSource.ICC, TLVDataFormat.BINARY,
            new VariableDataLength(DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0x70,
            new VariableByteLength(ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    INTEGRATED_CIRCUIT_CARD_PUBLIC_KEY_EXPONENT_0x9F47(0x9F47,
            "Integrated Circuit Card (ICC) Public Key Exponent",
            "ICC Public Key Exponent used for the verification of the Signed Dynamic Application Data",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 3), 0x70,
            new VariableByteLength(1, 3)),
    INTEGRATED_CIRCUIT_CARD_PUBLIC_KEY_REMAINDER_0x9F48(0x9F48,
            "Integrated Circuit Card (ICC) Public Key Remainder",
            "Remaining digits of the ICC Public Key Modulus", DataSource.ICC, TLVDataFormat.BINARY,
            new VariableDataLength(DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0x70,
            new VariableByteLength(ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    INTERFACE_DEVICE_IFD_SERIAL_NUMBER_0x9F1E(0x9F1E, "Interface Device (IFD) Serial Number",
            "Unique and permanent serial number assigned to the IFD by the manufacturer",
            DataSource.TERMINAL, TLVDataFormat.ASCII_ALPHA_NUMERIC, new FixedDataLength(8), 0x00,
            new FixedByteLength(8)),
    INTERNATIONAL_BANK_ACCOUNT_NUMBER_0x5F53(
            0x5F53,
            "International Bank Account Number (IBAN)",
            "Uniquely identifies the account of a customer at a financial institution as defined in ISO 13616.",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 34), 0xBF0C,
            new VariableByteLength(1, 34)),
    ISSUER_ACTION_CODE_DEFAULT_0x9F0D(0x9F0D, "Issuer Action Code - Default",
            "Specifies the issuer’s conditions that cause a transaction to be rejected if it might have been "
                    + "approved online, but the terminal is unable to process the transaction online",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(5), 0x70, new FixedByteLength(5)),
    ISSUER_ACTION_CODE_DENIAL_0x9F0E(
            0x9F0E,
            "Issuer Action Code - Denial",
            "Specifies the issuer’s conditions that cause the denial of a transaction without attempt to go online",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(5), 0x70, new FixedByteLength(5)),
    ISSUER_ACTION_CODE_ONLINE_0x9F0F(0x9F0F, "Issuer Action Code - Online",
            "Specifies the issuer’s conditions that cause a transaction to be transmitted online",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(5), 0x70, new FixedByteLength(5)),
    ISSUER_APPLICATION_DATA_0x9F10(
            0x9F10,
            "Issuer Application Data",
            "Contains proprietary application data for transmission to the issuer in an online transaction.",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 32), 0x77,
            new VariableByteLength(1, 32)),
    ISSUER_AUTHENTICATION_DATA_0x91(0x91, "Issuer Authentication Data",
            "Data sent to the ICC for online issuer authentication", DataSource.ISSUER,
            TLVDataFormat.BINARY, new VariableDataLength(8, 16), 0x00, new VariableByteLength(8, 16)),
    ISSUER_CODE_TABLE_INDEX_0x9F11(
            0x9F11,
            "Issuer Code Table Index",
            "Indicates the code table according to ISO/IEC 8859 for displaying the Application Preferred Name",
            DataSource.ICC, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(2), 0xA5,
            new FixedByteLength(1)),
    ISSUER_COUNTRY_CODE_0x5F28(0x5F28, "Issuer Country Code",
            "Indicates the country of the issuer according to ISO 3166", DataSource.ICC,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(3), 0x70, new FixedByteLength(2)),
    ISSUER_COUNTRY_CODE_ALPHA_2_0x5F55(
            0x5F55,
            "Issuer Country Code (alpha2 format)",
            "Indicates the country of the issuer as defined in ISO 3166 (using a 2 character alphabetic code)",
            DataSource.ICC, TLVDataFormat.ASCII_ALPHA, new FixedDataLength(2), 0xBF0C, new FixedByteLength(2)),
    ISSUER_COUNTRY_CODE_ALPHA_3_0x5F56(
            0x5F56,
            "Issuer Country Code (alpha3 format)",
            "Indicates the country of the issuer as defined in ISO 3166 (using a 3 character alphabetic code)",
            DataSource.ICC, TLVDataFormat.ASCII_ALPHA, new FixedDataLength(3), 0xBF0C, new FixedByteLength(3)),
    ISSUER_IDENTIFICATION_NUMBER_0x42(
            0x42,
            "Issuer Identification Number (IIN)",
            "The number that identifies the major industry and the card issuer and that forms the first part of "
                    + "the Primary Account Number (PAN)", DataSource.ICC,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(6), 0xBF0C, new FixedByteLength(3)),
    ISSUER_PUBLIC_KEY_CERTIFICATE_0x90(0x90, "Issuer Public Key Certificate",
            "Issuer public key certified by a certification authority", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x70, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    ISSUER_PUBLIC_KEY_EXPONENT_0x9F32(0x9F32, "Issuer Public Key Exponent",
            "Issuer public key exponent used for the verification of the Signed Static Application Data and "
                    + "the ICC Public Key Certificate", DataSource.ICC, TLVDataFormat.BINARY,
            new VariableDataLength(1, 3), 0x70, new VariableByteLength(1, 3)),
    ISSUER_PUBLIC_KEY_REMAINDER_0x92(0x92, "Issuer Public Key Remainder",
            "Remaining digits of the Issuer Public Key Modulus", DataSource.ICC, TLVDataFormat.BINARY,
            new VariableDataLength(DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0x70,
            new VariableByteLength(ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    ISSUER_SCRIPT_COMMAND_0x86(0x86, "Issuer Script Command",
            "Contains a command for transmission to the ICC", DataSource.ISSUER, TLVDataFormat.BINARY,
            new VariableDataLength(1, 261), 0x71, new VariableByteLength(1, 261)),
    ISSUER_SCRIPT_IDENTIFIER_0x9F18(0x9F18, "Issuer Script Identifier",
            "Identification of the Issuer Script", DataSource.ISSUER, TLVDataFormat.BINARY,
            new FixedDataLength(4), 0x71, new FixedByteLength(4)),
    ISSUER_SCRIPT_TEMPLATE_1_0x71(
            0x71,
            "Issuer Script Template 1",
            "Contains proprietary issuer data for transmission to the ICC before the second GENERATE AC command",
            DataSource.ISSUER, TLVDataFormat.CONSTRUCTED, new VariableDataLength(
            DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0x00, new VariableByteLength(
            ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    ISSUER_SCRIPT_TEMPLATE_2_0x72(
            0x72,
            "Issuer Script Template 2",
            "Contains proprietary issuer data for transmission to the ICC after the second GENERATE AC command",
            DataSource.ISSUER, TLVDataFormat.CONSTRUCTED, new VariableDataLength(
            DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0x00, new VariableByteLength(
            ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    ISSUER_URL_0x5F50(0x5F50, "Issuer URL",
            "The URL provides the location of the Issuer’s Library Server on the Internet.",
            DataSource.ICC, TLVDataFormat.ASCII_ALPHA_NUMERIC_SPECIAL, new VariableDataLength(
            DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0xBF0C,
            new VariableByteLength(ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    LANGUAGE_PREFERENCE_0x5F2D(0x5F2D, "Language Preference",
            "1-4 languages stored in order of preference, each represented by 2 alphabetical characters "
                    + "according to ISO 639", DataSource.ICC, TLVDataFormat.ASCII_ALPHA_NUMERIC,
            new FixedDataLength(2), 0xA5, new VariableByteLength(2, 8)),
    LAST_ONLINE_APPLICATION_TRANSACTION_COUNTER_REGISTER_0x9F13(0x9F13,
            "Last Online Application Transaction Counter (ATC) Register",
            "ATC value of the last transaction that went online", DataSource.ICC, TLVDataFormat.BINARY,
            new FixedDataLength(1), 0x00, new FixedByteLength(2)),
    LOG_ENTRY_0x9F4D(0x9F4D, "Log Entry",
            "Provides the SFI of the Transaction Log file and its number of records", DataSource.ICC,
            TLVDataFormat.BINARY, new FixedDataLength(1), 0xBF0C, new FixedByteLength(2)),
    LOG_FORMAT_0x9F4F(0x9F4F, "Log Format",
            "List (in tag and length format) of data objects representing the logged data elements that are "
                    + "passed to the terminal when a transaction log record is read", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x00, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    LOWER_CONSECUTIVE_OFFLINE_LIMIT_0x9F14(0x9F14, "Lower Consecutive Offline Limit",
            "Issuer-specified preference for the maximum number of consecutive offline transactions for this "
                    + "ICC application allowed in a terminal with online capability", DataSource.ICC,
            TLVDataFormat.BINARY, new FixedDataLength(1), 0x70, new FixedByteLength(1)),
    MERCHANT_CATEGORY_CODE_0x9F15(
            0x9F15,
            "Merchant Category Code",
            "Classifies the type of business being done by the merchant, represented according to ISO 8583:1993 "
                    + "for Card Acceptor Business Code", DataSource.TERMINAL,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(4), 0x00, new FixedByteLength(2)),
    MERCHANT_IDENTIFIER_0x9F16(0x9F16, "Merchant Identifier",
            "When concatenated with the Acquirer Identifier, uniquely identifies a given merchant",
            DataSource.TERMINAL, TLVDataFormat.ASCII_ALPHA_NUMERIC_SPECIAL, new FixedDataLength(15), 0x00,
            new FixedByteLength(15)),
    MERCHANT_NAME_AND_LOCATION_0x9F4E(0x9F4E, "Merchant Name and Location",
            "Indicates the name and location of the merchant", DataSource.TERMINAL,
            TLVDataFormat.ASCII_ALPHA_NUMERIC_SPECIAL, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x00, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    PERSONAL_IDENTIFICATION_NUMBER_TRY_COUNTER_0x9F17(0x9F17,
            "Personal Identification Number (PIN) Try Counter", "Number of PIN tries remaining",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(2), 0x00, new FixedByteLength(1)),
    POINT_OF_SERVICE_POS_ENTRY_MODE_0x9F39(0x9F39, "Point-of-Service (POS) Entry Mode",
            "Indicates the method by which the PAN was entered, according to the first two digits of the ISO "
                    + "8583:1987 POS Entry Mode", DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC,
            new FixedDataLength(2), 0x00, new FixedByteLength(1)),
    PROCESSING_OPTIONS_DATA_OBJECT_LIST_0x9F38(
            0x9F38,
            "Processing Options Data Object List (PDOL)",
            "Contains a list of terminal resident data objects (tags and lengths) needed by the ICC in processing "
                    + "the GET PROCESSING OPTIONS command", DataSource.ICC, TLVDataFormat.BINARY,
            new VariableDataLength(DataLength.DATA_LENGTH_VAR, DataLength.DATA_LENGTH_VAR), 0xA5,
            new VariableByteLength(ByteLength.BYTE_LENGTH_VAR, ByteLength.BYTE_LENGTH_VAR)),
    READ_RECORD_RESPONSE_MESSAGE_TEMPLATE_0x70(
            0x70,
            "READ RECORD Response Message Template",
            "Contains the contents of the record read. (Mandatory for SFIs 1-10. Response messages for SFIs 11-30 "
                    + "are outside the scope of EMV, but may use template '70')", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(1, 252), 0x00, new VariableByteLength(1, 252)),
    RESPONSE_MESSAGE_TEMPLATE_FORMAT_1_0x80(
            0x80,
            "Response Message Template Format 1",
            "Contains the data objects (without tags and lengths) returned by the ICC in response to a command",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x00, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    RESPONSE_MESSAGE_TEMPLATE_FORMAT_2_0x77(
            0x77,
            "Response Message Template Format 2",
            "Contains the data objects (with tags and lengths) returned by the ICC in response to a command",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x00, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    SERVICE_CODE_0x5F30(0x5F30, "Service Code",
            "Service code as defined in ISO/IEC 7813 for track 1 and track 2", DataSource.ICC,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(3), 0x70, new FixedByteLength(2)),
    SHORT_FILE_IDENTIFIER_0x88(
            0x88,
            "Short File Identifier (SFI)",
            "Identifies the AEF referenced in commands related to a given ADF or DDF. It is a binary data object "
                    + "having a value in the range 1 to 30 and with the three high order bits set to zero.",
            DataSource.ICC, TLVDataFormat.BINARY, new FixedDataLength(1), 0xA5, new FixedByteLength(1)),
    SIGNED_DYNAMIC_APPLICATION_DATA_0x9F4B(0x9F4B, "Signed Dynamic Application Data",
            "Digital signature on critical application parameters for DDA or CDA", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x77, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    SIGNED_STATIC_APPLICATION_DATA_0x93(0x93, "Signed Static Application Data",
            "Digital signature on critical application parameters for SDA", DataSource.ICC,
            TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x70, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    STATIC_DATA_AUTHENTICATION_TAG_LIST_0x9F4A(
            0x9F4A,
            "Static Data Authentication Tag List",
            "List of tags of primitive data objects defined in this specification whose value fields are to be "
                    + "included in the Signed Static or Dynamic Application Data", DataSource.ICC,
            TLVDataFormat.PROPRIETARY, ProprietaryVariableDataLength.INSTANCE, 0x70,
            ProprietaryVariableByteLength.INSTANCE),

    TERMINAL_CAPABILITIES_0x9F33(0x9F33, "Terminal Capabilities",
            "Indicates the card data input, CVM, and security capabilities of the terminal",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new FixedDataLength(3), 0x00,
            new FixedByteLength(3)),
    TERMINAL_COUNTRY_CODE_0x9F1A(0x9F1A, "Terminal Country Code",
            "Indicates the country of the terminal, represented according to ISO 3166",
            DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(3), 0x00,
            new FixedByteLength(2)),
    TERMINAL_FLOOR_LIMIT_0x9F1B(0x9F1B, "Terminal Floor Limit",
            "Indicates the floor limit in the terminal in conjunction with the AID",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new FixedDataLength(4), 0x00,
            new FixedByteLength(4)),
    TERMINAL_IDENTIFICATION_0x9F1C(0x9F1C, "Terminal Identification",
            "Designates the unique location of a terminal at a merchant", DataSource.TERMINAL,
            TLVDataFormat.ASCII_ALPHA_NUMERIC, new FixedDataLength(8), 0x00, new FixedByteLength(8)),
    TERMINAL_RISK_MANAGEMENT_DATA_0x9F1D(0x9F1D, "Terminal Risk Management Data",
            "Application-specific value used by the card for risk management purposes",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new VariableDataLength(1, 8), 0x00,
            new VariableByteLength(1, 8)),
    TERMINAL_TYPE_0x9F35(
            0x9F35,
            "Terminal Type",
            "Indicates the environment of the terminal, its communications capability, and its operational control",
            DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(2), 0x00,
            new FixedByteLength(1)),
    TERMINAL_VERIFICATION_RESULTS_0x95(0x95, "Terminal Verification Results",
            "Status of the different functions as seen from the terminal", DataSource.TERMINAL,
            TLVDataFormat.BINARY, new FixedDataLength(5), 0x00, new FixedByteLength(5)),
    TRACK_1_DISCRETIONARY_DATA_0x9F1F(0x9F1F, "Track 1 Discretionary Data",
            "Discretionary part of track 1 according to ISO/IEC 7813", DataSource.ICC,
            TLVDataFormat.ASCII_ALPHA_NUMERIC_SPECIAL, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x70, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    TRACK_2_DISCRETIONARY_DATA_0x9F20(0x9F20, "Track 2 Discretionary Data",
            "Discretionary part of track 2 according to ISO/IEC 7813", DataSource.ICC,
            TLVDataFormat.COMPRESSED_NUMERIC, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x70, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    TRACK_2_EQUIVALENT_DATA_0x57(0x57, "Track 2 Equivalent Data",
            "Contains the data elements of track 2 according to ISO/IEC 7813, excluding start sentinel, "
                    + "end sentinel, and Longitudinal Redundancy Check (LRC), as follows:",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 19), 0x70,
            new VariableByteLength(1, 19)),
    TRANSACTION_CERTIFICATE_HASH_VALUE_0x98(0x98, "Transaction Certificate (TC) Hash Value",
            "Result of a hash function specified in Book 2, Annex B3.1", DataSource.TERMINAL,
            TLVDataFormat.BINARY, new FixedDataLength(20), 0x00, new FixedByteLength(20)),
    TRANSACTION_CERTIFICATE_DATA_OBJECT_LIST_0x97(
            0x97,
            "Transaction Certificate Data Object List (TDOL)",
            "List of data objects (tag and length) to be used by the terminal in generating the TC Hash Value",
            DataSource.ICC, TLVDataFormat.BINARY, new VariableDataLength(1, 252), 0x70,
            new VariableByteLength(1, 252)),
    TRANSACTION_CURRENCY_CODE_0x5F2A(0x5F2A, "Transaction Currency Code",
            "Indicates the currency code of the transaction according to ISO 4217",
            DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(3), 0x00,
            new FixedByteLength(2)),
    TRANSACTION_CURRENCY_EXPONENT_0x5F36(0x5F36, "Transaction Currency Exponent",
            "Indicates the implied position of the decimal point from the right of the transaction amount "
                    + "represented according to ISO 4217", DataSource.TERMINAL,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(1), 0x00, new FixedByteLength(1)),
    TRANSACTION_DATE_0x9A(0x9A, "Transaction Date",
            "Local date that the transaction was authorised", DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC_DATE_YYMMDD,
            new FixedDataLength(6), 0x00, new FixedByteLength(3)),
    TRANSACTION_PERSONAL_IDENTIFICATION_NUMBER_PIN_DATA_0x99(0x99,
            "Transaction Personal Identification Number (PIN) Data",
            "Data entered by the cardholder for the purpose of the PIN verification",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new VariableDataLength(DataLength.DATA_LENGTH_VAR,
            DataLength.DATA_LENGTH_VAR), 0x00, new VariableByteLength(ByteLength.BYTE_LENGTH_VAR,
            ByteLength.BYTE_LENGTH_VAR)),
    TRANSACTION_REFERENCE_CURRENCY_CODE_0x9F3C(0x9F3C, "Transaction Reference Currency Code",
            "Code defining the common currency used by the terminal in case the Transaction Currency Code is "
                    + "different from the Application Currency Code", DataSource.TERMINAL,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(3), 0x00, new FixedByteLength(2)),
    TRANSACTION_REFERENCE_CURRENCY_EXPONENT_0x9F3D(0x9F3D,
            "Transaction Reference Currency Exponent",
            "Indicates the implied position of the decimal point from the right of the transaction amount, "
                    + "with the Transaction Reference Currency Code represented according to ISO 4217",
            DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(1), 0x00,
            new FixedByteLength(1)),
    TRANSACTION_SEQUENCE_COUNTER_0x9F41(0x9F41, "Transaction Sequence Counter",
            "Counter maintained by the terminal that is incremented by one for each transaction",
            DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC, new VariableDataLength(4, 8), 0x00,
            new VariableByteLength(2, 4)),
    TRANSACTION_STATUS_INFORMATION_0x9B(0x9B, "Transaction Status Information",
            "Indicates the functions performed in a transaction", DataSource.TERMINAL,
            TLVDataFormat.BINARY, new FixedDataLength(2), 0x00, new FixedByteLength(2)),
    TRANSACTION_TIME_0x9F21(0x9F21, "Transaction Time",
            "Local time that the transaction was authorised", DataSource.TERMINAL, TLVDataFormat.PACKED_NUMERIC_TIME_HHMMSS,
            new FixedDataLength(6), 0x00, new FixedByteLength(3)),
    TRANSACTION_TYPE_0x9C(
            0x9C,
            "Transaction Type",
            "Indicates the type of financial transaction, represented by the first two digits of the ISO 8583:1987 "
                    + "Processing Code. The actual values to be used for the Transaction Type data element are defined "
                    + "by the relevant payment system", DataSource.TERMINAL,
            TLVDataFormat.PACKED_NUMERIC, new FixedDataLength(2), 0x00, new FixedByteLength(1)),
    UNPREDICTABLE_NUMBER_0x9F37(0x9F37, "Unpredictable Number",
            "Value to provide variability and uniqueness to the generation of a cryptogram",
            DataSource.TERMINAL, TLVDataFormat.BINARY, new FixedDataLength(4), 0x00,
            new FixedByteLength(4)),
    UPPER_CONSECUTIVE_OFFLINE_LIMIT_0x9F23(
            0x9F23,
            "Upper Consecutive Offline Limit",
            "Issuer-specified preference for the maximum number of consecutive offline transactions for this ICC "
                    + "application allowed in a terminal without online capability", DataSource.ICC,
            TLVDataFormat.BINARY, new FixedDataLength(1), 0x70, new FixedByteLength(1));


    private final int tagNumber;
    private final String tagShortDescription;
    private final String tagDescription;
    private final DataSource source;
    private final TLVDataFormat format;
    private final Integer template;
    private final DataLength dataLength;
    private final ByteLength byteLength;

    EMVStandardTagType(final int tagNumber, final String tagName, final String tagDescription,
                       final DataSource source, final TLVDataFormat format, final DataLength dataLength,
                       final Integer template, final ByteLength byteLength) {
        this.tagNumber = tagNumber;
        this.tagShortDescription = tagName;
        this.tagDescription = tagDescription;
        this.source = source;
        this.format = format;
        this.template = template;
        this.dataLength = dataLength;
        this.byteLength = byteLength;
        if (!(0 == tagNumber)) {
            if (MapHolder.tagCodeMap.containsKey(tagNumber)) {
                throw new IllegalStateException(
                        "Illegal attempt to add duplicate EMVTagType with tagNumber: " + tagNumber +
                                ". Enum: " + this.name());
            }
            MapHolder.tagCodeMap.put(tagNumber, this);
        }
    }

    public static boolean isProprietaryTag(int code) {
        EMVTagType tagType = MapHolder.tagCodeMap.get(code);
        return tagType == null;
    }

    public static EMVStandardTagType forCode(int code) throws UnknownTagNumberException {
        EMVStandardTagType tagType = MapHolder.tagCodeMap.get(code);
        if (tagType == null) {
            throw new UnknownTagNumberException(String.valueOf(code));
        }
        return tagType;
    }

    public static EMVStandardTagType forHexCode(String hexString) throws UnknownTagNumberException {
        return forCode(Integer.parseInt(hexString, 16));
    }

    public static void main(String[] args) throws Exception {
        System.out.println(EMVStandardTagType.APPLICATION_CRYPTOGRAM_0x9F26.getTagNumberHex());
        System.out.println(EMVStandardTagType.APPLICATION_CRYPTOGRAM_0x9F26.getTagNumberLength());

        System.out.println(EMVStandardTagType.CARD_RISK_MANAGEMENT_DATA_OBJECT_LIST_1_0x8C.getTagNumberHex());
        System.out.println(EMVStandardTagType.CARD_RISK_MANAGEMENT_DATA_OBJECT_LIST_1_0x8C.getTagNumberLength());
    }

    @Override
    public int getTagNumber() {
        return tagNumber;
    }

    public boolean isProprietaryTag() {
        return false;
    }

    public int getTagNumberLength() {
        return tagNumber > 0xFF ? 2 : 1;
    }

    public String getTagNumberHex() {
        return Integer.toHexString(tagNumber).toUpperCase();
    }

    public byte[] getTagNumberBytes() {
        return ISOUtil.int2byte(tagNumber);
    }

    @Override
    public String getTagShortDescription() {
        return tagShortDescription;
    }

    @Override
    public String getTagDescription() {
        return tagDescription;
    }

    @Override
    public DataSource getSource() {
        return source;
    }

    @Override
    public TLVDataFormat getFormat() {
        return format;
    }

    public boolean isProprietaryFormat() {
        return TLVDataFormat.PROPRIETARY.equals(format);
    }

    /**
     * @return The template or null if no template
     */
    public EMVTagType getTemplate() {
        return MapHolder.tagCodeMap.get(this.template);
    }

    @Override
    public DataLength getDataLength() {
        return dataLength;
    }

    @Override
    public ByteLength getByteLength() {
        return byteLength;
    }

    public Class<?> getDataType() throws ProprietaryFormatException {
        switch (format) {
            case PROPRIETARY:
                throw new ProprietaryFormatException(tagShortDescription);
            case BINARY:
                return byte[].class;
            case CONSTRUCTED:
                return EMVTag[].class;
            default:
                return String.class;
        }
    }

    private static class MapHolder {

        private static Map<Integer, EMVStandardTagType> tagCodeMap =
                new HashMap<Integer, EMVStandardTagType>();
    }

    public static class ProprietaryFixedDataLength extends DataLength {

        public static final DataLength INSTANCE = new ProprietaryFixedDataLength(-1);


        public ProprietaryFixedDataLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryVariableDataLength extends DataLength {

        public static final DataLength INSTANCE = new ProprietaryVariableDataLength(-1, -1);


        public ProprietaryVariableDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }

    public static class ProprietaryVariableDiscreteDataLength extends DataLength {

        public static final DataLength INSTANCE = new ProprietaryVariableDiscreteDataLength(-1, -1);


        public ProprietaryVariableDiscreteDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryVariableDiscreteByteLength extends ByteLength {

        public static final ByteLength INSTANCE = new ProprietaryVariableDiscreteByteLength(-1, -1);


        public ProprietaryVariableDiscreteByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryFixedByteLength extends ByteLength {

        public static final ByteLength INSTANCE = new ProprietaryFixedByteLength(-1);


        public ProprietaryFixedByteLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    public static class ProprietaryVariableByteLength extends ByteLength {

        public static final ByteLength INSTANCE = new ProprietaryVariableByteLength(-1, -1);


        public ProprietaryVariableByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }

}
