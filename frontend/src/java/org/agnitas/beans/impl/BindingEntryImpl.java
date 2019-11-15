/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.Date;
import java.util.Map;

import org.agnitas.beans.BindingEntry;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

/**
 * Class holds information about a Customers "Binding" to a Mailinglist
 */
public class BindingEntryImpl implements BindingEntry {

	/** Serial version UID. */
	private static final long serialVersionUID = -7149749237041195396L;

	/**
	 * Mailinglist ID for this BindingEntry
	 */
	protected int mailinglistID;
	protected int customerID;
	protected int exitMailingID;
	protected int entryMailingID;
	protected String userType;
	protected int userStatus;
	protected String userRemark;
	protected String referrer = null;

	protected Date changeDate;
	protected Date creationDate;

	private ComBindingEntryDao bindingEntryDao;

	/** Holds value of property mediaType. */
	protected int mediaType;

	/**
	 * Creates new, empty BindingEntry
	 */
	public BindingEntryImpl() {
		mailinglistID = 0;
		customerID = 0;
		userType = "W";
		userStatus = 0;
		userRemark = "";
		mediaType = MediaTypes.EMAIL.getMediaCode();
	}

	@Override
	public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

	@Override
	public void setMailinglistID(int mailinglistID) {
		this.mailinglistID = mailinglistID;
	}

	@Override
	public void setExitMailingID(int mailingID) {
		exitMailingID = mailingID;
	}

	@Override
	public int getExitMailingID() {
		return exitMailingID;
	}

	@Override
	public void setEntryMailingID(int mailingID) {
		entryMailingID = mailingID;
	}

	@Override
	public int getEntryMailingID() {
		return entryMailingID;
	}

	@Override
	public void setCustomerID(int customerID) {
		this.customerID = customerID;
	}

	@Override
	public void setUserType(String userTypeString) {
		try {
			userType = UserType.getUserTypeByString(userTypeString).getTypeCode();
		} catch (Exception e) {
			userType = UserType.World.getTypeCode();
		}
	}

	@Override
	public void setUserRemark(String remark) {
		if (remark == null) {
			remark = "";
		}
		userRemark = remark;
	}

	@Override
	public void setUserStatus(int us) {
		userStatus = us;
	}

	/*
	 * public void setUserRemark(String ur) { userRemark=ur; }
	 */

	@Override
	public void setChangeDate(Date ts) {
		changeDate = ts;
	}

	@Override
	public int getMailinglistID() {
		return mailinglistID;
	}

	@Override
	public int getCustomerID() {
		return customerID;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getUserStatus() {
		return userStatus;
	}

	@Override
	public String getUserRemark() {
		return userRemark;
	}

	@Override
	public Date getChangeDate() {
		return changeDate;
	}

	/**
	 * @Deprecated: Only used by Velocity scripts. To be replaced by some ScriptHelper method.
	 */
	@Deprecated
	@Override
	public boolean updateStatusInDB(@VelocityCheck int companyID) {
		return bindingEntryDao.updateStatus(this, companyID);
	}

	@Override
	public ComBindingEntryDao getBindingEntryDao() {
		return bindingEntryDao;
	}

	@Override
	public boolean saveBindingInDB(@VelocityCheck int companyID, Map<Integer, Map<Integer, BindingEntry>> allCustLists, ComAdmin admin) throws Exception {
		Map<Integer, BindingEntry> types = allCustLists.get(mailinglistID);
		if (types != null) {
			BindingEntry old = types.get(mediaType);
			if (old != null) {
				boolean changed = false;
				
				if (old.getExitMailingID() != exitMailingID) {
					changed = true;
				}
				
				if (!old.getUserType().equals(userType)) {
					changed = true;
				}
				
				if (old.getUserStatus() != userStatus) {
					changed = true;
					userRemark = getUserRemarkForStatusByAdmin(userStatus, admin);
				} else {
					userRemark = old.getUserRemark();
				}
				
				if (old.getMediaType() != mediaType) {
					changed = true;
				}
				
				if (changed) {
					return updateBindingInDB(companyID);
				} else {
					return true;
				}
			} else {
				userRemark = getUserRemarkForStatusByAdmin(userStatus, admin);
				return insertNewBindingInDB(companyID);
			}
		} else {
			userRemark = getUserRemarkForStatusByAdmin(userStatus, admin);
			return insertNewBindingInDB(companyID);
		}
	}

	private String getUserRemarkForStatusByAdmin(int userStatus, ComAdmin admin) throws Exception {
		switch (UserStatus.getUserStatusByID(userStatus)) {
			case Active:
				return "Opt-In by ADMIN (" + admin.getUsername() + ")";
			case Bounce:
				return "Bounced by ADMIN (" + admin.getUsername() + ")";
			case AdminOut:
				return "Opt-Out by ADMIN (" + admin.getUsername() + ")";
			case UserOut:
				return "User-Opt-Out by ADMIN (" + admin.getUsername() + ")";
			case WaitForConfirm:
				return "Wait-For-Confirm by ADMIN (" + admin.getUsername() + ")";
			case Blacklisted:
				return "Blacklist by ADMIN (" + admin.getUsername() + ")";
			case Suspend:
				return "Suspend by ADMIN (" + admin.getUsername() + ")";
			default:
				return "Unknown status " + userStatus + " by ADMIN (" + admin.getUsername() + ")";
		}
	}

	/**
	 * Updates this Binding in the Database
	 * 
	 * @return True: Sucess False: Failure
	 * @param companyID
	 *            The company ID of the Binding
	 */
	@Override
	public boolean updateBindingInDB(@VelocityCheck int companyID) {
		return bindingEntryDao.updateBinding(this, companyID);
	}

	@Override
	public boolean insertNewBindingInDB(@VelocityCheck int companyID) {
		return bindingEntryDao.insertNewBinding(this, companyID);
	}

	@Override
	public boolean optOutEmailAdr(String email, @VelocityCheck int companyID) {
		return bindingEntryDao.optOutEmailAdr(email, companyID);
	}

	/**
	 * Getter for property mediaType.
	 * 
	 * @return Value of property mediaType.
	 * 
	 */
	@Override
	public int getMediaType() {
		return this.mediaType;
	}

	/**
	 * Setter for property mediaType.
	 * 
	 * @param mediaType
	 *            New value of property mediaType.
	 * 
	 */
	@Override
	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}

	@Override
	public String toString() {
		return "List: " + mailinglistID + " Customer: " + customerID + " ExitID: " + exitMailingID + " Type: " + userType + " Status: " + userStatus + " Remark: " + userRemark
				+ " mediaType: " + mediaType;
	}

	@Override
	public boolean getUserBindingFromDB(@VelocityCheck int companyID) {
		return bindingEntryDao.getUserBindingFromDB(this, companyID);
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
	public String getReferrer() {
		return referrer;
	}

	@Override
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
}
