/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import com.agnitas.emm.core.maildrop.MaildropStatus;

import java.util.HashMap;
import java.util.Map;

public class MonthCounterStat {

	private final Map<String, Integer> mailings = new HashMap<>(MaildropStatus.values().length); // status -> count
	private final Map<String, Double> kilobytes = new HashMap<>(MaildropStatus.values().length); // status -> kilobytes
	private final Map<String, Integer> emails = new HashMap<>(MaildropStatus.values().length);   // status -> kilobytes

	public Map<String, Integer> getMailings() {
		return mailings;
	}

	public Map<String, Double> getKilobytes() {
		return kilobytes;
	}

	public Map<String, Integer> getEmails() {
		return emails;
	}

	// region getters used in Monthly.rptdesign

	public int getWorldMailingsCount() {
		return mailings.getOrDefault(MaildropStatus.WORLD.getCodeString(), 0);
	}

	public int getActionBasedMailingsCount() {
		return mailings.getOrDefault(MaildropStatus.ACTION_BASED.getCodeString(), 0);
	}

	public int getDateBasedMailingsCount() {
		return mailings.getOrDefault(MaildropStatus.DATE_BASED.getCodeString(), 0);
	}

	public int getIntervalMailingsCount() {
		return mailings.getOrDefault(MaildropStatus.ON_DEMAND.getCodeString(), 0);
	}

	public int getTotalMailingsCount() {
		return mailings.values().stream().mapToInt(Integer::intValue).sum();
	}

	public double getEmailKilobytes() {
		return kilobytes.values().stream().mapToDouble(Double::doubleValue).sum();
	}

	public int getWorldEmailsCount() {
		return emails.getOrDefault(MaildropStatus.WORLD.getCodeString(), 0);
	}

	public int getActionBasedEmailsCount() {
		return emails.getOrDefault(MaildropStatus.ACTION_BASED.getCodeString(), 0);
	}

	public int getDateBasedEmailsCount() {
		return emails.getOrDefault(MaildropStatus.DATE_BASED.getCodeString(), 0);
	}

	public int getIntervalEmailsCount() {
		return emails.getOrDefault(MaildropStatus.ON_DEMAND.getCodeString(), 0);
	}

	public int getTotalEmailsCount() {
		return emails.values().stream().mapToInt(Integer::intValue).sum();
	}

	// endregion
}
