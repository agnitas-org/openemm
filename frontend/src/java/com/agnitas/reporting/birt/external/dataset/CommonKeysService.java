/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

public class CommonKeysService implements CommonKeys {
    
    public static String tokenByIndex(int index) {
    	switch(index) {
	    	case RECIPIENTS_NUMBER_INDEX:
	    		return RECIPIENTS_NUMBER;
	    	case DELIVERED_EMAILS_INDEX:
	    		return DELIVERED_EMAILS;
	    	case DELIVERED_EMAILS_DELIVERED_INDEX:
	    		return DELIVERED_EMAILS_DELIVERED;
	//    	case OPENERS_INDEX:
	//    		return OPENERS;
	    	case OPENERS_MEASURED_INDEX:
	    		return OPENERS_MEASURED;
	    	case OPENERS_INVISIBLE_INDEX:
	    		return OPENERS_INVISIBLE;
	    	case OPENERS_TOTAL_INDEX:
	    		return OPENERS_TOTAL;
	    	case CLICKER_INDEX:
	    		return CLICKER;
	    	case OPT_OUTS_INDEX:
	    		return OPT_OUTS;
	    	case SOFT_BOUNCES_INDEX:
	    		return SOFT_BOUNCES;
	    	case HARD_BOUNCES_INDEX:
	    		return HARD_BOUNCES;
	    	case SOFT_BOUNCES_UNDELIVERABLE_INDEX:
	    		return SOFT_BOUNCES_UNDELIVERABLE;
	    	case REVENUE_INDEX:
	    		return REVENUE;
	    	case BOUNCES_INDEX:
	    		return BOUNCES;
	    	case OPENERS_TRACKED_INDEX:
	    		return OPENERS_TRACKED;
	    	case OPENERS_PC_INDEX:
	    		return OPENERS_PC;
	    	case OPENERS_TABLET_INDEX:
	    		return OPENERS_TABLET;
	    	case OPENERS_MOBILE_INDEX:
	    		return OPENERS_MOBILE;
	    	case OPENERS_SMARTTV_INDEX:
	    		return OPENERS_SMARTTV;
	    	case OPENERS_PC_AND_MOBILE_INDEX:
	    		return OPENERS_PC_AND_MOBILE;
	    	case CLICKER_TRACKED_INDEX:
	    		return CLICKER_TRACKED;
	    	case CLICKER_PC_INDEX:
	    		return CLICKER_PC;
	    	case CLICKER_TABLET_INDEX:
	    		return CLICKER_TABLET;
	    	case CLICKER_MOBILE_INDEX:
	    		return CLICKER_MOBILE;
	    	case CLICKER_SMARTTV_INDEX:
	    		return CLICKER_SMARTTV;
	    	case CLICKER_PC_AND_MOBILE_INDEX:
	    		return CLICKER_PC_AND_MOBILE;
	    	case SENT_HTML_INDEX:
	    		return SENT_HTML;
	    	case SENT_TEXT_INDEX:
	    		return SENT_TEXT;
	    	case SENT_OFFLINE_HTML_INDEX:
	    		return SENT_OFFILE_HTML;
	    		
    		default:
    			return "unknown";
    	}
    }

}
