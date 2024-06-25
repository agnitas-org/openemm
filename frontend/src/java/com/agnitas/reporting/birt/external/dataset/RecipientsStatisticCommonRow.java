/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

public class RecipientsStatisticCommonRow extends RecipientsStatisticRowBase {

	protected int countTypeText;
	protected int countTypeHtml;
	protected int countTypeOfflineHtml;
	protected int countActive;
	protected int countActiveForPeriod;
	protected int countWaitingForConfirm;
	protected int countBlacklisted;
	protected int countOptout;
	protected int countBounced;
	protected int countGenderMale;
	protected int countGenderFemale;
	protected int countGenderUnknown;
	protected int countRecipient;
	protected int countTargetGroup;
	protected int countActiveAsOf;
	protected int countBlacklistedAsOf;
	protected int countOptoutAsOf;
	protected int countBouncedAsOf;
	protected int countWaitingForConfirmAsOf;
	protected int countRecipientAsOf;
	protected int notConfirmedDoiCount;
	protected int notConfirmedAndDeletedDoiCount;
	protected int confirmedDoiCount;
	protected int confirmedAndNotActiveDoiCount;
	protected int totalDoiCount;

	public int getCountTypeText() {
		return countTypeText;
	}

	public void setCountTypeText(int countTypeText) {
		this.countTypeText = countTypeText;
	}

	public int getCountTypeHtml() {
		return countTypeHtml;
	}

	public void setCountTypeHtml(int countTypeHtml) {
		this.countTypeHtml = countTypeHtml;
	}

	public int getCountTypeOfflineHtml() {
		return countTypeOfflineHtml;
	}

	public void setCountTypeOfflineHtml(int countTypeOfflineHtml) {
		this.countTypeOfflineHtml = countTypeOfflineHtml;
	}

	public int getCountActive() {
		return countActive;
	}

	public void setCountActive(int countActive) {
		this.countActive = countActive;
	}

	public int getCountActiveForPeriod() {
		return countActiveForPeriod;
	}

	public void setCountActiveForPeriod(int countActiveForPeriod) {
		this.countActiveForPeriod = countActiveForPeriod;
	}

	public int getCountWaitingForConfirm() {
		return countWaitingForConfirm;
	}

	public void setCountWaitingForConfirm(int countWaitingForConfirm) {
		this.countWaitingForConfirm = countWaitingForConfirm;
	}

	public int getCountBlacklisted() {
		return countBlacklisted;
	}

	public void setCountBlacklisted(int countBlacklisted) {
		this.countBlacklisted = countBlacklisted;
	}

	public int getCountOptout() {
		return countOptout;
	}

	public void setCountOptout(int countOptout) {
		this.countOptout = countOptout;
	}

	public int getCountBounced() {
		return countBounced;
	}

	public void setCountBounced(int countBounced) {
		this.countBounced = countBounced;
	}

	public int getCountGenderMale() {
		return countGenderMale;
	}

	public void setCountGenderMale(int countGenderMale) {
		this.countGenderMale = countGenderMale;
	}

	public int getCountGenderFemale() {
		return countGenderFemale;
	}

	public void setCountGenderFemale(int countGenderFemale) {
		this.countGenderFemale = countGenderFemale;
	}

	public int getCountGenderUnknown() {
		return countGenderUnknown;
	}

	public void setCountGenderUnknown(int countGenderUnknown) {
		this.countGenderUnknown = countGenderUnknown;
	}

	public int getCountRecipient() {
		return countRecipient;
	}

	public void setCountRecipient(int countRecipient) {
		this.countRecipient = countRecipient;
	}

	public int getCountTargetGroup() {
		return countTargetGroup;
	}

	public void setCountTargetGroup(int countTargetGroup) {
		this.countTargetGroup = countTargetGroup;
	}

	public int getCountActiveAsOf() {
		return countActiveAsOf;
	}

	public void setCountActiveAsOf(int countActiveAsOf) {
		this.countActiveAsOf = countActiveAsOf;
	}

	public int getCountBlacklistedAsOf() {
		return countBlacklistedAsOf;
	}

	public void setCountBlacklistedAsOf(int countBlacklistedAsOf) {
		this.countBlacklistedAsOf = countBlacklistedAsOf;
	}

	public int getCountOptoutAsOf() {
		return countOptoutAsOf;
	}

	public void setCountOptoutAsOf(int countOptoutAsOf) {
		this.countOptoutAsOf = countOptoutAsOf;
	}

	public int getCountBouncedAsOf() {
		return countBouncedAsOf;
	}

	public void setCountBouncedAsOf(int countBouncedAsOf) {
		this.countBouncedAsOf = countBouncedAsOf;
	}

	public int getCountWaitingForConfirmAsOf() {
		return countWaitingForConfirmAsOf;
	}

	public void setCountWaitingForConfirmAsOf(int countWaitingForConfirmAsOf) {
		this.countWaitingForConfirmAsOf = countWaitingForConfirmAsOf;
	}

	public int getCountRecipientAsOf() {
		return countRecipientAsOf;
	}

	public void setCountRecipientAsOf(int countRecipientAsOf) {
		this.countRecipientAsOf = countRecipientAsOf;
	}

	public int getNotConfirmedDoiCount() {
		return notConfirmedDoiCount;
	}

	public void setNotConfirmedDoiCount(int notConfirmedDoiCount) {
		this.notConfirmedDoiCount = notConfirmedDoiCount;
	}

	public int getNotConfirmedAndDeletedDoiCount() {
		return notConfirmedAndDeletedDoiCount;
	}

	public void setNotConfirmedAndDeletedDoiCount(int notConfirmedAndDeletedDoiCount) {
		this.notConfirmedAndDeletedDoiCount = notConfirmedAndDeletedDoiCount;
	}

	public int getConfirmedDoiCount() {
		return confirmedDoiCount;
	}

	public void setConfirmedDoiCount(int confirmedDoiCount) {
		this.confirmedDoiCount = confirmedDoiCount;
	}

	public int getConfirmedAndNotActiveDoiCount() {
		return confirmedAndNotActiveDoiCount;
	}

	public void setConfirmedAndNotActiveDoiCount(int confirmedAndNotActiveDoiCount) {
		this.confirmedAndNotActiveDoiCount = confirmedAndNotActiveDoiCount;
	}

	public int getTotalDoiCount() {
		return totalDoiCount;
	}

	public void setTotalDoiCount(int totalDoiCount) {
		this.totalDoiCount = totalDoiCount;
	}
}
