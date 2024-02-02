/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.upload.bean;

import java.util.Date;
import java.util.List;
import java.util.Locale;


public class UploadData {
	private int uploadID;
	private int adminID; // remove after EMMGUI-714 has been tested and it's not used anymore. new field - owners
	private List<Integer> owners;
	private int fromAdminID;
	private String adminName;
	private Date creationDate;
	private Date changeDate;
	private String contactName;
	private String contactFirstname;
	private String contactPhone;
	private String contactMail;
	private String sendtoMail;
	private String description;	 
	private String filename;
	private int filesize;	
	private byte[] data;
	private int companyID = -1;	// just to be sure...

	// the following variables are more "virtual" which means, they are not written into the database but used
	// for shown tables.
	private String linkToFile;	// contains the link to the file. This is a dynamic field. It comes NOT from the DB.
	private Date deleteDate;	// will be calculated, default is creation-Date + 14 Days.
	private String fileType = null;	// get the last part of the filename ...
	
	private Locale locale = null;
	
	// Default constructor...
	public UploadData() {
	}
	
	/**
	 * constructor with all possible values of this Data Objects.
	 * @param uploadID
	 * @param adminID
	 * @param adminName
	 * @param creation_date
	 * @param change_date
	 * @param contactName
	 * @param contactFirstname
	 * @param contactPhone
	 * @param contactMail
	 * @param sendtoMail
	 * @param description
	 */
	public UploadData(
			int uploadID,
			int adminID,
			int fromAdminID,
			String adminName,
			Date creation_date,
			Date change_date,
			String contactName,
			String contactFirstname,
			String contactPhone,
			String contactMail,
			String sendtoMail,
			String description) 
	{
		this.uploadID=uploadID;
		this.adminID=adminID;
		this.fromAdminID=fromAdminID;
        this.adminName = adminName;
		this.creationDate=creation_date;
		this.changeDate=change_date;
		this.contactName=contactName;
		this.contactFirstname=contactFirstname;
		this.contactPhone=contactPhone;
		this.contactMail=contactMail;
		this.sendtoMail=sendtoMail;
		this.description=description;
	}
	
	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}
	
	public String getLinkToFile() {
		return linkToFile;
	}

	public void setLinkToFile(String linkToFile) {
		this.linkToFile = linkToFile;
	}

	public void setUploadID(int uploadID) {
		this.uploadID = uploadID;		
	}
	
	public int getUploadID() {
		return uploadID;
	}
	
	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}
	
	public int getAdminID() {
		return adminID;
	}

    public List<Integer> getOwners() {
        return owners;
    }

    public void setOwners(List<Integer> owners) {
        this.owners = owners;
    }

	public int getFromAdminID() {
		return fromAdminID;
	}

	public void setFromAdminID(int fromAdminID) {
		this.fromAdminID = fromAdminID;
	}

	public void setCreation_date(Date creation_date) {
		this.creationDate = creation_date;
		/*// Also set delete-date.
		Date calculateDeleteDate = calculateDeleteDate(creation_date);
		setDeleteDate(calculateDeleteDate);*/
	}
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setChangeDate(Date change_date) {
		this.changeDate = change_date;
	}
	
	public Date getChangeDate() {
		return changeDate;
	}
	
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	
	public String getContact_name() {
		return contactName;
	}
	
	public void setContactFirstname(String contactFirstname) {
		this.contactFirstname = contactFirstname;
	}
	
	public String getContactFirstname() {
		return contactFirstname;
	}
	
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	
	public String getContactPhone() {
		return contactPhone;
	}
	
	public void setContactMail(String contactMail) {
		this.contactMail = contactMail;
	}
	
	public String getContactMail() {
		return contactMail;
	}
	
	public void setSendtoMail(String sendtoMail) {
		this.sendtoMail = sendtoMail;
	}
	
	public String getSendtoMail() {
		return sendtoMail;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilesize(int filesize) {
		this.filesize = filesize;
	}

	public int getFilesize() {
		return filesize;
	}

	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public Date getDeleteDate() {		
		return deleteDate;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileType() {
		if (fileType == null) {
			String tmpString = getFilename();
			if (tmpString.contains(".")) {
				fileType = tmpString.substring(tmpString.lastIndexOf("."),
						tmpString.length());
			}
		}
		return fileType;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * the get method checks, if the locale is null. If it is null, a default will be set!
	 * @return
	 */
	public Locale getLocale() {
		if (locale == null) {
			// set default
			locale = Locale.GERMAN;
		}
		return locale; 
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
}
