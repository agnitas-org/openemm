/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

public enum AgnTag {

    DB("agnDB"),
    DB_MD5("agnDBMD5"),
    UID("agnUID"),
    REDIRECT("agnREDIRECT"),
    AGE("agnAGE"),
    DAYS_UNTIL("agnDAYS_UNTIL"),
    ITEM("agnITEM"),
    NULL("agnNULL"),
    NUM_FORMAT("agnNUMFORMAT"),
    MULTI_AFTER("agnMULTIAFTER"),
    MULTI_BEFORE("agnMULTIBEFORE"),
    PROFILE("agnPROFILE"),
    PREHEADER("agnPREHEADER"),
    PUBID("agnPUBID"),
    UNSUBSCRIBE("agnUNSUBSCRIBE"),
    VOUCHER("agnVOUCHER"),
    BARCODE("agnBARCODE"),
    CLEARANCE("agnCLEARANCE"),
    QRCODE("agnQRCODE"),
    ONEPIXEL("agnONEPIXEL"),
    IMAGE("agnIMAGE"),
    EMAIL("agnEMAIL"),
    SUBSCRIBER_COUNT("agnSUBSCRIBERCOUNT"),
    DATE("agnDATE"),
    SYS_INFO("agnSYSINFO"),
    DYN("agnDYN"),
    DVALUE("agnDVALUE"),
    PUBVIEW("agnPUBVIEW"),
    TITLE("agnTITLE"),
    TITLE_FULL("agnTITLEFULL"),
    TITLE_FIRST("agnTITLEFIRST"),
    IMG_LINK("agnIMGLINK"),
    TXT_IMG("agnTXTIMG"),
    DYN_IMG("agnDYNIMAGE"),
    SWYN("agnSWYN"),
    SEND_DATE("agnSENDDATE"),
    FORM("agnFORM"),
    FULLVIEW("agnFULLVIEW"),
    WEBVIEW("agnWEBVIEW");

    private final String name;

    AgnTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
