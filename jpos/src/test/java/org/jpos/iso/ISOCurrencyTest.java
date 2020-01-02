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

package org.jpos.iso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ISOCurrencyTest {

    @Test
    public void testAllISOCurrenciesIncluded() {
        Set<java.util.Currency> currencies = java.util.Currency.getAvailableCurrencies();
        StringBuilder msg = new StringBuilder();
        for (java.util.Currency sc : currencies) {
            try {
                if (sc.getDefaultFractionDigits() < 0) continue; // Skip pseudo currencies
                int currencyCode = sc.getNumericCode();
                
                Currency currencyByCode = ISOCurrency.getCurrency(currencyCode);
                                
                assertEquals(sc.getDefaultFractionDigits(), currencyByCode.getDecimals(), "jPOS currency does not match decimals");
            } catch (Throwable ignored) {
                msg.append(sc.getCurrencyCode().toUpperCase() + "=" + 
                        ISOUtil.zeropad(sc.getNumericCode(), 3) + " " +  sc.getDefaultFractionDigits() + 
                        " //" + sc.getDisplayName() + ":" + ignored.getMessage() + "\n");
            }
        }
        assertEquals(msg.length(), 0, msg.toString());
    }
    
    @Test
    public void testIsoStandardCurrencies() {
        // Pulled from ISO web site on Aug 3, 2016
        Map<String, String> c = new HashMap<String, String>();
        Map<String, Integer> cD = new HashMap<String, Integer>();
        c.put("971", "AFN"); cD.put("971", 2); // Afghani AFGHANISTAN 
        c.put("978", "EUR"); cD.put("978", 2); // Euro ÅLAND ISLANDS 
        c.put("008", "ALL"); cD.put("008", 2); // Lek ALBANIA 
        c.put("012", "DZD"); cD.put("012", 2); // Algerian Dinar ALGERIA 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar AMERICAN SAMOA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro ANDORRA 
        c.put("973", "AOA"); cD.put("973", 2); // Kwanza ANGOLA 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar ANGUILLA 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar ANTIGUA AND BARBUDA 
        c.put("032", "ARS"); cD.put("032", 2); // Argentine Peso ARGENTINA 
        c.put("051", "AMD"); cD.put("051", 2); // Armenian Dram ARMENIA 
        c.put("533", "AWG"); cD.put("533", 2); // Aruban Florin ARUBA 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar AUSTRALIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro AUSTRIA 
        c.put("944", "AZN"); cD.put("944", 2); // Azerbaijanian Manat AZERBAIJAN 
        c.put("044", "BSD"); cD.put("044", 2); // Bahamian Dollar BAHAMAS (THE) 
        c.put("048", "BHD"); cD.put("048", 3); // Bahraini Dinar BAHRAIN 
        c.put("050", "BDT"); cD.put("050", 2); // Taka BANGLADESH 
        c.put("052", "BBD"); cD.put("052", 2); // Barbados Dollar BARBADOS 
        c.put("933", "BYN"); cD.put("933", 2); // Belarusian Ruble BELARUS 
        c.put("974", "BYR"); cD.put("974", 0); // Belarusian Ruble BELARUS 
        c.put("978", "EUR"); cD.put("978", 2); // Euro BELGIUM 
        c.put("084", "BZD"); cD.put("084", 2); // Belize Dollar BELIZE 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO BENIN 
        c.put("060", "BMD"); cD.put("060", 2); // Bermudian Dollar BERMUDA 
        c.put("356", "INR"); cD.put("356", 2); // Indian Rupee BHUTAN 
        c.put("064", "BTN"); cD.put("064", 2); // Ngultrum BHUTAN 
        c.put("068", "BOB"); cD.put("068", 2); // Boliviano BOLIVIA (PLURINATIONAL STATE OF) 
        c.put("984", "BOV"); cD.put("984", 2); // Mvdol BOLIVIA (PLURINATIONAL STATE OF) TRUE
        c.put("840", "USD"); cD.put("840", 2); // US Dollar "BONAIRE, SINT EUSTATIUS AND SABA" 
        c.put("977", "BAM"); cD.put("977", 2); // Convertible Mark BOSNIA AND HERZEGOVINA 
        c.put("072", "BWP"); cD.put("072", 2); // Pula BOTSWANA 
        c.put("578", "NOK"); cD.put("578", 2); // Norwegian Krone BOUVET ISLAND 
        c.put("986", "BRL"); cD.put("986", 2); // Brazilian Real BRAZIL 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar BRITISH INDIAN OCEAN TERRITORY (THE) 
        c.put("096", "BND"); cD.put("096", 2); // Brunei Dollar BRUNEI DARUSSALAM 
        c.put("975", "BGN"); cD.put("975", 2); // Bulgarian Lev BULGARIA 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO BURKINA FASO 
        c.put("108", "BIF"); cD.put("108", 0); // Burundi Franc BURUNDI 
        c.put("132", "CVE"); cD.put("132", 2); // Cabo Verde Escudo CABO VERDE 
        c.put("116", "KHR"); cD.put("116", 2); // Riel CAMBODIA 
        c.put("950", "XAF"); cD.put("950", 0); // CFA Franc BEAC CAMEROON 
        c.put("124", "CAD"); cD.put("124", 2); // Canadian Dollar CANADA 
        c.put("136", "KYD"); cD.put("136", 2); // Cayman Islands Dollar CAYMAN ISLANDS (THE) 
        c.put("950", "XAF"); cD.put("950", 0); // CFA Franc BEAC CENTRAL AFRICAN REPUBLIC (THE) 
        c.put("950", "XAF"); cD.put("950", 0); // CFA Franc BEAC CHAD 
        c.put("152", "CLP"); cD.put("152", 0); // Chilean Peso CHILE 
        c.put("990", "CLF"); cD.put("990", 4); // Unidad de Fomento CHILE TRUE
        c.put("156", "CNY"); cD.put("156", 2); // Yuan Renminbi CHINA 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar CHRISTMAS ISLAND 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar COCOS (KEELING) ISLANDS (THE) 
        c.put("170", "COP"); cD.put("170", 2); // Colombian Peso COLOMBIA 
        c.put("970", "COU"); cD.put("970", 2); // Unidad de Valor Real COLOMBIA TRUE
        c.put("174", "KMF"); cD.put("174", 0); // Comoro Franc COMOROS (THE) 
        c.put("976", "CDF"); cD.put("976", 2); // Congolese Franc CONGO (THE DEMOCRATIC REPUBLIC OF THE) 
        c.put("950", "XAF"); cD.put("950", 0); // CFA Franc BEAC CONGO (THE) 
        c.put("554", "NZD"); cD.put("554", 2); // New Zealand Dollar COOK ISLANDS (THE) 
        c.put("188", "CRC"); cD.put("188", 2); // Costa Rican Colon COSTA RICA 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO CÔTE D'IVOIRE 
        c.put("191", "HRK"); cD.put("191", 2); // Kuna CROATIA 
        c.put("192", "CUP"); cD.put("192", 2); // Cuban Peso CUBA 
        c.put("931", "CUC"); cD.put("931", 2); // Peso Convertible CUBA 
        c.put("532", "ANG"); cD.put("532", 2); // Netherlands Antillean Guilder CURAÇAO 
        c.put("978", "EUR"); cD.put("978", 2); // Euro CYPRUS 
        c.put("203", "CZK"); cD.put("203", 2); // Czech Koruna CZECH REPUBLIC (THE) 
        c.put("208", "DKK"); cD.put("208", 2); // Danish Krone DENMARK 
        c.put("262", "DJF"); cD.put("262", 0); // Djibouti Franc DJIBOUTI 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar DOMINICA 
        c.put("214", "DOP"); cD.put("214", 2); // Dominican Peso DOMINICAN REPUBLIC (THE) 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar ECUADOR 
        c.put("818", "EGP"); cD.put("818", 2); // Egyptian Pound EGYPT 
        c.put("222", "SVC"); cD.put("222", 2); // El Salvador Colon EL SALVADOR 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar EL SALVADOR 
        c.put("950", "XAF"); cD.put("950", 0); // CFA Franc BEAC EQUATORIAL GUINEA 
        c.put("232", "ERN"); cD.put("232", 2); // Nakfa ERITREA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro ESTONIA 
        c.put("230", "ETB"); cD.put("230", 2); // Ethiopian Birr ETHIOPIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro EUROPEAN UNION 
        c.put("238", "FKP"); cD.put("238", 2); // Falkland Islands Pound FALKLAND ISLANDS (THE) [MALVINAS] 
        c.put("208", "DKK"); cD.put("208", 2); // Danish Krone FAROE ISLANDS (THE) 
        c.put("242", "FJD"); cD.put("242", 2); // Fiji Dollar FIJI 
        c.put("978", "EUR"); cD.put("978", 2); // Euro FINLAND 
        c.put("978", "EUR"); cD.put("978", 2); // Euro FRANCE 
        c.put("978", "EUR"); cD.put("978", 2); // Euro FRENCH GUIANA 
        c.put("953", "XPF"); cD.put("953", 0); // CFP Franc FRENCH POLYNESIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro FRENCH SOUTHERN TERRITORIES (THE) 
        c.put("950", "XAF"); cD.put("950", 0); // CFA Franc BEAC GABON 
        c.put("270", "GMD"); cD.put("270", 2); // Dalasi GAMBIA (THE) 
        c.put("981", "GEL"); cD.put("981", 2); // Lari GEORGIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro GERMANY 
        c.put("936", "GHS"); cD.put("936", 2); // Ghana Cedi GHANA 
        c.put("292", "GIP"); cD.put("292", 2); // Gibraltar Pound GIBRALTAR 
        c.put("978", "EUR"); cD.put("978", 2); // Euro GREECE 
        c.put("208", "DKK"); cD.put("208", 2); // Danish Krone GREENLAND 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar GRENADA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro GUADELOUPE 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar GUAM 
        c.put("320", "GTQ"); cD.put("320", 2); // Quetzal GUATEMALA 
        c.put("826", "GBP"); cD.put("826", 2); // Pound Sterling GUERNSEY 
        c.put("324", "GNF"); cD.put("324", 0); // Guinea Franc GUINEA 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO GUINEA-BISSAU 
        c.put("328", "GYD"); cD.put("328", 2); // Guyana Dollar GUYANA 
        c.put("332", "HTG"); cD.put("332", 2); // Gourde HAITI 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar HAITI 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar HEARD ISLAND AND McDONALD ISLANDS 
        c.put("978", "EUR"); cD.put("978", 2); // Euro HOLY SEE (THE) 
        c.put("340", "HNL"); cD.put("340", 2); // Lempira HONDURAS 
        c.put("344", "HKD"); cD.put("344", 2); // Hong Kong Dollar HONG KONG 
        c.put("348", "HUF"); cD.put("348", 2); // Forint HUNGARY 
        c.put("352", "ISK"); cD.put("352", 0); // Iceland Krona ICELAND 
        c.put("356", "INR"); cD.put("356", 2); // Indian Rupee INDIA 
        c.put("360", "IDR"); cD.put("360", 2); // Rupiah INDONESIA 
        c.put("364", "IRR"); cD.put("364", 2); // Iranian Rial IRAN (ISLAMIC REPUBLIC OF) 
        c.put("368", "IQD"); cD.put("368", 3); // Iraqi Dinar IRAQ 
        c.put("978", "EUR"); cD.put("978", 2); // Euro IRELAND 
        c.put("826", "GBP"); cD.put("826", 2); // Pound Sterling ISLE OF MAN 
        c.put("376", "ILS"); cD.put("376", 2); // New Israeli Sheqel ISRAEL 
        c.put("978", "EUR"); cD.put("978", 2); // Euro ITALY 
        c.put("388", "JMD"); cD.put("388", 2); // Jamaican Dollar JAMAICA 
        c.put("392", "JPY"); cD.put("392", 0); // Yen JAPAN 
        c.put("826", "GBP"); cD.put("826", 2); // Pound Sterling JERSEY 
        c.put("400", "JOD"); cD.put("400", 3); // Jordanian Dinar JORDAN 
        c.put("398", "KZT"); cD.put("398", 2); // Tenge KAZAKHSTAN 
        c.put("404", "KES"); cD.put("404", 2); // Kenyan Shilling KENYA 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar KIRIBATI 
        c.put("408", "KPW"); cD.put("408", 2); // North Korean Won KOREA (THE DEMOCRATIC PEOPLE’S REPUBLIC OF) 
        c.put("410", "KRW"); cD.put("410", 0); // Won KOREA (THE REPUBLIC OF) 
        c.put("414", "KWD"); cD.put("414", 3); // Kuwaiti Dinar KUWAIT 
        c.put("417", "KGS"); cD.put("417", 2); // Som KYRGYZSTAN 
        c.put("418", "LAK"); cD.put("418", 2); // Kip LAO PEOPLE’S DEMOCRATIC REPUBLIC (THE) 
        c.put("978", "EUR"); cD.put("978", 2); // Euro LATVIA 
        c.put("422", "LBP"); cD.put("422", 2); // Lebanese Pound LEBANON 
        c.put("426", "LSL"); cD.put("426", 2); // Loti LESOTHO 
        c.put("710", "ZAR"); cD.put("710", 2); // Rand LESOTHO 
        c.put("430", "LRD"); cD.put("430", 2); // Liberian Dollar LIBERIA 
        c.put("434", "LYD"); cD.put("434", 3); // Libyan Dinar LIBYA 
        c.put("756", "CHF"); cD.put("756", 2); // Swiss Franc LIECHTENSTEIN 
        c.put("978", "EUR"); cD.put("978", 2); // Euro LITHUANIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro LUXEMBOURG 
        c.put("446", "MOP"); cD.put("446", 2); // Pataca MACAO 
        c.put("807", "MKD"); cD.put("807", 2); // Denar MACEDONIA (THE FORMER YUGOSLAV REPUBLIC OF) 
        c.put("969", "MGA"); cD.put("969", 2); // Malagasy Ariary MADAGASCAR 
        c.put("454", "MWK"); cD.put("454", 2); // Malawi Kwacha MALAWI 
        c.put("458", "MYR"); cD.put("458", 2); // Malaysian Ringgit MALAYSIA 
        c.put("462", "MVR"); cD.put("462", 2); // Rufiyaa MALDIVES 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO MALI 
        c.put("978", "EUR"); cD.put("978", 2); // Euro MALTA 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar MARSHALL ISLANDS (THE) 
        c.put("978", "EUR"); cD.put("978", 2); // Euro MARTINIQUE 
        c.put("478", "MRO"); cD.put("478", 2); // Ouguiya MAURITANIA 
        c.put("480", "MUR"); cD.put("480", 2); // Mauritius Rupee MAURITIUS 
        c.put("978", "EUR"); cD.put("978", 2); // Euro MAYOTTE 
        c.put("484", "MXN"); cD.put("484", 2); // Mexican Peso MEXICO 
        c.put("979", "MXV"); cD.put("979", 2); // Mexican Unidad de Inversion (UDI) MEXICO TRUE
        c.put("840", "USD"); cD.put("840", 2); // US Dollar MICRONESIA (FEDERATED STATES OF) 
        c.put("498", "MDL"); cD.put("498", 2); // Moldovan Leu MOLDOVA (THE REPUBLIC OF) 
        c.put("978", "EUR"); cD.put("978", 2); // Euro MONACO 
        c.put("496", "MNT"); cD.put("496", 2); // Tugrik MONGOLIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro MONTENEGRO 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar MONTSERRAT 
        c.put("504", "MAD"); cD.put("504", 2); // Moroccan Dirham MOROCCO 
        c.put("943", "MZN"); cD.put("943", 2); // Mozambique Metical MOZAMBIQUE 
        c.put("104", "MMK"); cD.put("104", 2); // Kyat MYANMAR 
        c.put("516", "NAD"); cD.put("516", 2); // Namibia Dollar NAMIBIA 
        c.put("710", "ZAR"); cD.put("710", 2); // Rand NAMIBIA 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar NAURU 
        c.put("524", "NPR"); cD.put("524", 2); // Nepalese Rupee NEPAL 
        c.put("978", "EUR"); cD.put("978", 2); // Euro NETHERLANDS (THE) 
        c.put("953", "XPF"); cD.put("953", 0); // CFP Franc NEW CALEDONIA 
        c.put("554", "NZD"); cD.put("554", 2); // New Zealand Dollar NEW ZEALAND 
        c.put("558", "NIO"); cD.put("558", 2); // Cordoba Oro NICARAGUA 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO NIGER (THE) 
        c.put("566", "NGN"); cD.put("566", 2); // Naira NIGERIA 
        c.put("554", "NZD"); cD.put("554", 2); // New Zealand Dollar NIUE 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar NORFOLK ISLAND 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar NORTHERN MARIANA ISLANDS (THE) 
        c.put("578", "NOK"); cD.put("578", 2); // Norwegian Krone NORWAY 
        c.put("512", "OMR"); cD.put("512", 3); // Rial Omani OMAN 
        c.put("586", "PKR"); cD.put("586", 2); // Pakistan Rupee PAKISTAN 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar PALAU 
        c.put("590", "PAB"); cD.put("590", 2); // Balboa PANAMA 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar PANAMA 
        c.put("598", "PGK"); cD.put("598", 2); // Kina PAPUA NEW GUINEA 
        c.put("600", "PYG"); cD.put("600", 0); // Guarani PARAGUAY 
        c.put("604", "PEN"); cD.put("604", 2); // Sol PERU 
        c.put("608", "PHP"); cD.put("608", 2); // Philippine Peso PHILIPPINES (THE) 
        c.put("554", "NZD"); cD.put("554", 2); // New Zealand Dollar PITCAIRN 
        c.put("985", "PLN"); cD.put("985", 2); // Zloty POLAND 
        c.put("978", "EUR"); cD.put("978", 2); // Euro PORTUGAL 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar PUERTO RICO 
        c.put("634", "QAR"); cD.put("634", 2); // Qatari Rial QATAR 
        c.put("978", "EUR"); cD.put("978", 2); // Euro RÉUNION 
        c.put("946", "RON"); cD.put("946", 2); // Romanian Leu ROMANIA 
        c.put("643", "RUB"); cD.put("643", 2); // Russian Ruble RUSSIAN FEDERATION (THE) 
        c.put("646", "RWF"); cD.put("646", 0); // Rwanda Franc RWANDA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro SAINT BARTHÉLEMY 
        c.put("654", "SHP"); cD.put("654", 2); // Saint Helena Pound "SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA" 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar SAINT KITTS AND NEVIS 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar SAINT LUCIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro SAINT MARTIN (FRENCH PART) 
        c.put("978", "EUR"); cD.put("978", 2); // Euro SAINT PIERRE AND MIQUELON 
        c.put("951", "XCD"); cD.put("951", 2); // East Caribbean Dollar SAINT VINCENT AND THE GRENADINES 
        c.put("882", "WST"); cD.put("882", 2); // Tala SAMOA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro SAN MARINO 
        c.put("678", "STD"); cD.put("678", 2); // Dobra SAO TOME AND PRINCIPE 
        c.put("682", "SAR"); cD.put("682", 2); // Saudi Riyal SAUDI ARABIA 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO SENEGAL 
        c.put("941", "RSD"); cD.put("941", 2); // Serbian Dinar SERBIA 
        c.put("690", "SCR"); cD.put("690", 2); // Seychelles Rupee SEYCHELLES 
        c.put("694", "SLL"); cD.put("694", 2); // Leone SIERRA LEONE 
        c.put("702", "SGD"); cD.put("702", 2); // Singapore Dollar SINGAPORE 
        c.put("532", "ANG"); cD.put("532", 2); // Netherlands Antillean Guilder SINT MAARTEN (DUTCH PART) 
        c.put("978", "EUR"); cD.put("978", 2); // Euro SLOVAKIA 
        c.put("978", "EUR"); cD.put("978", 2); // Euro SLOVENIA 
        c.put("090", "SBD"); cD.put("090", 2); // Solomon Islands Dollar SOLOMON ISLANDS 
        c.put("706", "SOS"); cD.put("706", 2); // Somali Shilling SOMALIA 
        c.put("710", "ZAR"); cD.put("710", 2); // Rand SOUTH AFRICA 
        c.put("728", "SSP"); cD.put("728", 2); // South Sudanese Pound SOUTH SUDAN 
        c.put("978", "EUR"); cD.put("978", 2); // Euro SPAIN 
        c.put("144", "LKR"); cD.put("144", 2); // Sri Lanka Rupee SRI LANKA 
        c.put("938", "SDG"); cD.put("938", 2); // Sudanese Pound SUDAN (THE) 
        c.put("968", "SRD"); cD.put("968", 2); // Surinam Dollar SURINAME 
        c.put("578", "NOK"); cD.put("578", 2); // Norwegian Krone SVALBARD AND JAN MAYEN 
        c.put("748", "SZL"); cD.put("748", 2); // Lilangeni SWAZILAND 
        c.put("752", "SEK"); cD.put("752", 2); // Swedish Krona SWEDEN 
        c.put("756", "CHF"); cD.put("756", 2); // Swiss Franc SWITZERLAND 
        c.put("947", "CHE"); cD.put("947", 2); // WIR Euro SWITZERLAND TRUE
        c.put("948", "CHW"); cD.put("948", 2); // WIR Franc SWITZERLAND TRUE
        c.put("760", "SYP"); cD.put("760", 2); // Syrian Pound SYRIAN ARAB REPUBLIC 
        c.put("901", "TWD"); cD.put("901", 2); // New Taiwan Dollar TAIWAN (PROVINCE OF CHINA) 
        c.put("972", "TJS"); cD.put("972", 2); // Somoni TAJIKISTAN 
        c.put("834", "TZS"); cD.put("834", 2); // Tanzanian Shilling "TANZANIA, UNITED REPUBLIC OF" 
        c.put("764", "THB"); cD.put("764", 2); // Baht THAILAND 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar TIMOR-LESTE 
        c.put("952", "XOF"); cD.put("952", 0); // CFA Franc BCEAO TOGO 
        c.put("554", "NZD"); cD.put("554", 2); // New Zealand Dollar TOKELAU 
        c.put("776", "TOP"); cD.put("776", 2); // Pa’anga TONGA 
        c.put("780", "TTD"); cD.put("780", 2); // Trinidad and Tobago Dollar TRINIDAD AND TOBAGO 
        c.put("788", "TND"); cD.put("788", 3); // Tunisian Dinar TUNISIA 
        c.put("949", "TRY"); cD.put("949", 2); // Turkish Lira TURKEY 
        c.put("934", "TMT"); cD.put("934", 2); // Turkmenistan New Manat TURKMENISTAN 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar TURKS AND CAICOS ISLANDS (THE) 
        c.put("036", "AUD"); cD.put("036", 2); // Australian Dollar TUVALU 
        c.put("800", "UGX"); cD.put("800", 0); // Uganda Shilling UGANDA 
        c.put("980", "UAH"); cD.put("980", 2); // Hryvnia UKRAINE 
        c.put("784", "AED"); cD.put("784", 2); // UAE Dirham UNITED ARAB EMIRATES (THE) 
        c.put("826", "GBP"); cD.put("826", 2); // Pound Sterling UNITED KINGDOM OF GREAT BRITAIN AND NORTHERN IRELAND (THE) 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar UNITED STATES MINOR OUTLYING ISLANDS (THE) 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar UNITED STATES OF AMERICA (THE) 
        c.put("997", "USN"); cD.put("997", 2); // US Dollar (Next day) UNITED STATES OF AMERICA (THE) TRUE
        c.put("858", "UYU"); cD.put("858", 2); // Peso Uruguayo URUGUAY 
        c.put("940", "UYI"); cD.put("940", 0); // Uruguay Peso en Unidades Indexadas (URUIURUI) URUGUAY TRUE
        c.put("860", "UZS"); cD.put("860", 2); // Uzbekistan Sum UZBEKISTAN 
        c.put("548", "VUV"); cD.put("548", 0); // Vatu VANUATU 
        c.put("937", "VEF"); cD.put("937", 2); // Bolívar VENEZUELA (BOLIVARIAN REPUBLIC OF) 
        c.put("704", "VND"); cD.put("704", 0); // Dong VIET NAM 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar VIRGIN ISLANDS (BRITISH) 
        c.put("840", "USD"); cD.put("840", 2); // US Dollar VIRGIN ISLANDS (U.S.) 
        c.put("953", "XPF"); cD.put("953", 0); // CFP Franc WALLIS AND FUTUNA 
        c.put("504", "MAD"); cD.put("504", 2); // Moroccan Dirham WESTERN SAHARA 
        c.put("886", "YER"); cD.put("886", 2); // Yemeni Rial YEMEN 
        c.put("967", "ZMW"); cD.put("967", 2); // Zambian Kwacha ZAMBIA 
        c.put("932", "ZWL"); cD.put("932", 2); // Zimbabwe Dollar ZIMBABWE 
        
        StringBuilder msg = new StringBuilder();
        
        for (String code: c.keySet()) {
            try {
                Currency currencyByCode = ISOCurrency.getCurrency(code);
                assertEquals(cD.get(code).intValue(), currencyByCode.getDecimals(), "Decimal digits do not match for " + currencyByCode);
                assertEquals(c.get(code), currencyByCode.getAlphaCode(), "Name does not match for " + currencyByCode);
            } catch (Throwable ignored) {
                msg.append(c.get(code) + "=" +
                        code + " " +  cD.get(code) +
                        " //"  + ignored.getMessage() + "\n");
            }
        }
        assertEquals(msg.length(), 0, msg.toString());
    }

    @Test
    public void testparseFromISO87String () {
        assertEquals(new BigDecimal("12.34"), ISOCurrency.parseFromISO87String("000000001234", "840"), "2 decimals");
        assertEquals(new BigDecimal("1.234"), ISOCurrency.parseFromISO87String("000000001234", "048"), "3 decimals");
        assertEquals(new BigDecimal("1234"), ISOCurrency.parseFromISO87String("000000001234", "020"), "no decimals");
    }
    @Test
    public void testtoISO87String () {
        assertEquals("000000001234", ISOCurrency.toISO87String(new BigDecimal("12.34"), "840"), "2 decimals");
        assertEquals("000000001234", ISOCurrency.toISO87String(new BigDecimal("1.234"), "048"), "3 decimals");
        assertEquals("000000001234", ISOCurrency.toISO87String(new BigDecimal("1234"), "020"), "no decimals");
    }
}
