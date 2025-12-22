/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.emm.core.mailing.service.ListMailingCondition.MailingPropertyCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.SendDateCondition;

public final class ListMailingFilter {

	private final List<ListMailingCondition> conditions;
	
	public ListMailingFilter(final List<ListMailingCondition> conditions) {
		this.conditions = conditions != null ? conditions : new ArrayList<>();
	}
	
	public List<MailingPropertyCondition> listMailingPropertyConditions() {
		return conditions
				.stream()
				.filter(c -> c instanceof MailingPropertyCondition)
				.map(c -> (MailingPropertyCondition) c)
				.collect(Collectors.toList());
	}
	
	public boolean containsSendDateConditions() {
		return filterSendDateConditions()
				.findFirst()
				.isPresent();
	}
	
	public List<SendDateCondition> listSendDateConditions() {
		return filterSendDateConditions()
				.collect(Collectors.toList());
	}
	
	private Stream<SendDateCondition> filterSendDateConditions() {
		return conditions
				.stream()
				.filter(c -> c instanceof SendDateCondition)
				.map(c -> (SendDateCondition) c);
	}
}
