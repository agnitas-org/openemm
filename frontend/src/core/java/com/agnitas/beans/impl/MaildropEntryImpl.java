/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import com.agnitas.beans.MaildropEntry;

public class MaildropEntryImpl implements MaildropEntry {

	private int companyID;
	private int id;
	private int mailingID;
	private char status;
	protected Date sendDate;
	protected Date genDate;
	protected int genStatus;
	protected Date genChangeDate;
	protected int stepping;
	protected int blocksize;
	private int maxRecipients;
	private int adminTestTargetID;
	private int overwriteTestRecipient; // GWUA-5664
	private String mailGenerationOptimizationMode;
	private Set<Integer> altgIds = new HashSet<>();

	@Override
	public int getAdminTestTargetID() {
		return adminTestTargetID;
	}

	@Override
	public void setAdminTestTargetID(int targetID) {
		this.adminTestTargetID = targetID;
	}

	@Override
	public String getMailGenerationOptimization() {
		return mailGenerationOptimizationMode;
	}

	@Override
	public void setMailGenerationOptimization(String mode) {
		mailGenerationOptimizationMode = mode;
	}

	/**
	 * Getter for property companyID.
	 * 
	 * @return Value of property companyID.
	 */
	@Override
	public int getCompanyID() {
		return companyID;
	}

	/**
	 * Setter for property companyID.
	 * 
	 * @param companyID
	 *            New value of property companyID.
	 */
	@Override
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	/**
	 * Getter for property id.
	 * 
	 * @return Value of property id.
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * Setter for property id.
	 * 
	 * @param id
	 *            New value of property id.
	 */
	@Override
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Getter for property mailingID.
	 * 
	 * @return Value of property mailingID.
	 */
	@Override
	public int getMailingID() {
		return mailingID;
	}

	/**
	 * Setter for property mailingID.
	 * 
	 * @param mailingID
	 *            New value of property mailingID.
	 */
	@Override
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	/**
	 * Getter for property status.
	 * 
	 * @return Value of property status.
	 */
	@Override
	public char getStatus() {
		return status;
	}

	/**
	 * Setter for property status.
	 * 
	 * @param status
	 *            New value of property status.
	 */
	@Override
	public void setStatus(char status) {
		this.status = status;
	}

	/**
	 * Getter for property senddate.
	 * 
	 * @return Value of property senddate.
	 */
	@Override
	public Date getSendDate() {
		return sendDate;
	}

	/**
	 * Setter for property senddate.
	 * 
	 * @param sendDate
	 */
	@Override
	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	/**
	 * Getter for property genDate.
	 * 
	 * @return Value of property genDate.
	 */
	@Override
	public Date getGenDate() {
		return genDate;
	}

	/**
	 * Setter for property genDate.
	 * 
	 * @param genDate
	 *            New value of property genDate.
	 */
	@Override
	public void setGenDate(Date genDate) {
		this.genDate = genDate;
	}

	/**
	 * Getter for property genChangeDate.
	 * 
	 * @return Value of property genChangeDate.
	 */
	@Override
	public Date getGenChangeDate() {
		return genChangeDate;
	}

	/**
	 * Setter for property genChangeDate.
	 * 
	 * @param genChangeDate
	 *            New value of property genChangeDate.
	 */
	@Override
	public void setGenChangeDate(Date genChangeDate) {
		this.genChangeDate = genChangeDate;
	}

	/**
	 * Getter for property genStatus.
	 * 
	 * @return Value of property genStatus.
	 */
	@Override
	public int getGenStatus() {
		return genStatus;
	}

	/**
	 * Setter for property genStatus.
	 * 
	 * @param genStatus
	 *            New value of property genStatus.
	 */
	@Override
	public void setGenStatus(int genStatus) {
		this.genStatus = genStatus;
	}

	/**
	 * Getter for property stepping.
	 * 
	 * @return Value of property stepping.
	 */
	@Override
	public int getStepping() {
		return stepping;
	}

	/**
	 * Setter for property stepping.
	 * 
	 * @param stepping
	 *            New value of property stepping.
	 */
	@Override
	public void setStepping(int stepping) {
		this.stepping = stepping;
	}

	/**
	 * Getter for property blocksize.
	 * 
	 * @return Value of property blocksize.
	 */
	@Override
	public int getBlocksize() {
		return blocksize;
	}

	/**
	 * Setter for property blocksize.
	 * 
	 * @param blocksize
	 *            New value of property blocksize.
	 */
	@Override
	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}

	@Override
	public int getMaxRecipients() {
		return maxRecipients;
	}

	@Override
	public void setMaxRecipients(int maxRecipients) {
		this.maxRecipients = maxRecipients;
	}

    @Override
    public int getOverwriteTestRecipient() {
        return overwriteTestRecipient;
    }

    @Override
    public void setOverwriteTestRecipient(int overwriteTestRecipient) {
        this.overwriteTestRecipient = overwriteTestRecipient;
    }

	@Override
	public void setAltgIds(Set<Integer> altgIds) {
		this.altgIds = altgIds;
	}

	@Override
	public Set<Integer> getAltgIds() {
		return altgIds;
	}

	@Override
	public String toString() {
		String ret = "{";
		ret += "companyID=" + companyID;
		ret += ";id=" + id;
		ret += ";mailingID=" + mailingID;
		ret += ";status=" + status;
		ret += ";sendDate=" + sendDate;
		ret += ";genDate=" + genDate;
		ret += ";genChangeDate=" + genChangeDate;
		ret += ";genStatus=" + genStatus;
		ret += ";stepping=" + stepping;
		ret += ";blocksize=" + blocksize;
		ret += ";overwriteTestRecipient=" + overwriteTestRecipient;
		ret += "}";
		return ret;
	}
}
