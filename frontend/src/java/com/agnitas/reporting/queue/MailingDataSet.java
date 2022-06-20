/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.queue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MailingDataSet {
	public enum Field {
		Unknown(false, ""),

		MailingId(false, "mailingid"),
		TargetGroupId(false, "targetgroupid"),
		
		Name(false, "mailing"),
		Country(false, "country"),
		Campaign(false, "campaign", "campaign-name"),
		Inmarket(false, "inmarket", "inmarket-date"),
		
		Contacts(true, "contacts", "number of contacts"),
		Openers(true, "openers"),
		Clicks_brutto(true, "bruttoclicks", "clicks (brt)"),
		Clicks_netto(true, "nettoclicks", "clicks (nt)"),
		Clickers(true, "clickers"),
		Bounces_hard(true, "hardbounces"),
		Bounces_soft(true, "softbounces"),
		Optouts(true, "optouts");
		
		private Field(boolean isNumeric, List<String> fieldnames) {
			this.isNumeric = isNumeric;
			this.fieldnames = fieldnames;
		}
		
		private Field(boolean isNumeric, String... fieldnames) {
			this.isNumeric = isNumeric;
			this.fieldnames = Arrays.asList(fieldnames);
		}
		
		private final List<String> fieldnames;
		private final boolean isNumeric;
		
		public boolean isNumeric() {
			return isNumeric;
		}
		
		public List<String> toNameList() {
			return fieldnames;
		}

		public static Field getFieldForName(String name) {
			String searchString = name.toLowerCase();
			for (Field field : Field.values()) {
				if (field.toNameList().contains(searchString))
					return field;
			}
			
			return Field.Unknown;
		}
	}

	private int mailingId = -1;
	private int targetGroupId = -1;
	private String name;
	private String country;
	private String campaign;
	private Calendar inmarket;
	private int contacts;
	private int openers;
	private int clickers;
	private int clicks_netto;
	private int clicks_brutto;
	private int bounces_hard;
	private int bounces_soft;
	private int optouts;

	public MailingDataSet() {
	}

	public MailingDataSet(
		String name,
		String country,
		String campaign,
		Calendar inmarket,
		int contacts,
		int openers,
		int clickers,
		int clicks_netto,
		int clicks_brutto,
		int bounces_hard,
		int bounces_soft,
		int optouts) {
		
		this.name = name;
		this.country = country;
		this.campaign = campaign;
		this.inmarket = inmarket;
		this.contacts = contacts;
		this.openers = openers;
		this.clickers = clickers;
		this.clicks_netto = clicks_netto;
		this.clicks_brutto = clicks_brutto;
		this.bounces_hard = bounces_hard;
		this.bounces_soft = bounces_soft;
		this.optouts = optouts;
	}
	
	public void setFieldValue(Field field, String value) throws Exception {
		if (StringUtils.isNotEmpty(value)) {
			switch(field) {
				case MailingId:
					setMailingId(Integer.parseInt(value));
					break;
				case TargetGroupId:
					setTargetGroupId(Integer.parseInt(value));
					break;
				case Name:
					setName(value);
					break;
				case Country:
					setCountry(value);
					break;
				case Campaign:
					setCampaign(value);
					break;
				case Inmarket:
					throw new Exception("Invalid non numeric datatype for date field");
				default:
					throw new Exception("Invalid non numeric datatype for numeric field");
			}
		}
	}
	
	public void setFieldValue(Field field, int value) throws Exception {
		switch(field) {
			case MailingId:
				setMailingId(value);
				break;
			case TargetGroupId:
				setTargetGroupId(value);
				break;
			case Contacts:
				setContacts(value);
				break;
			case Openers:
				setOpeners(value);
				break;
			case Clickers:
				setClickers(value);
				break;
			case Clicks_netto:
				setClicks_netto(value);
				break;
			case Clicks_brutto:
				setClicks_brutto(value);
				break;
			case Bounces_hard:
				setBounces_hard(value);
				break;
			case Bounces_soft:
				setBounces_soft(value);
				break;
			case Optouts:
				setOptouts(value);
				break;
			case Inmarket:
				throw new Exception("Invalid numeric datatype for date field");
			default:
				throw new Exception("Invalid numeric datatype for non numeric field");
		}
	}

	public void setFieldValue(Field field, Date value) throws Exception {
		if (value != null) {
			switch(field) {
				case Inmarket:
					GregorianCalendar gregCal = new GregorianCalendar();
					gregCal.setTime(value);
					setInmarket(gregCal);
					break;
				default:
					throw new Exception("Invalid numeric datatype for non numeric field");
			}
		}
	}
	
	public String getFieldValue(Field field) throws Exception {
		switch(field) {
			case MailingId:
				return Integer.toString(getMailingId());
			case TargetGroupId:
				return Integer.toString(getTargetGroupId());
			case Name:
				return getName();
			case Country:
				return getCountry();
			case Campaign:
				return getCampaign();
			case Inmarket:
				return formatInmarketDate(getInmarket());
			case Contacts:
				return java.text.NumberFormat.getNumberInstance().format(getContacts());
			case Openers:
				return java.text.NumberFormat.getNumberInstance().format(getOpeners());
			case Clickers:
				return java.text.NumberFormat.getNumberInstance().format(getClickers());
			case Clicks_netto:
				return java.text.NumberFormat.getNumberInstance().format(getClicks_netto());
			case Clicks_brutto:
				return java.text.NumberFormat.getNumberInstance().format(getClicks_brutto());
			case Bounces_hard:
				return java.text.NumberFormat.getNumberInstance().format(getBounces_hard());
			case Bounces_soft:
				return java.text.NumberFormat.getNumberInstance().format(getBounces_soft());
			case Optouts:
				return java.text.NumberFormat.getNumberInstance().format(getOptouts());
			default:
				throw new Exception("Unknown field");
		}
	}
	
	public int getNumericFieldValue(Field field) throws Exception {
		switch(field) {
			case MailingId:
				return getMailingId();
			case TargetGroupId:
				return getTargetGroupId();
			case Contacts:
				return getContacts();
			case Openers:
				return getOpeners();
			case Clickers:
				return getClickers();
			case Clicks_netto:
				return getClicks_netto();
			case Clicks_brutto:
				return getClicks_brutto();
			case Bounces_hard:
				return getBounces_hard();
			case Bounces_soft:
				return getBounces_soft();
			case Optouts:
				return getOptouts();
			default:
				throw new Exception("Not a numeric field");
		}
	}
		
	public static String formatInmarketDate(Calendar value) {
		if (value == null) {
			return "";
		} else {
			return new SimpleDateFormat("dd.MM.yyyy").format(value.getTime());
		}
	}
	
	public boolean hasContent() {
		return 
			mailingId != 0
			|| targetGroupId != 0
			|| name != null
			|| country != null
			|| campaign != null
			|| inmarket != null
			|| contacts != 0
			|| openers != 0
			|| clickers != 0
			|| clicks_netto != 0
			|| clicks_brutto != 0
			|| bounces_hard != 0
			|| bounces_soft != 0
			|| optouts != 0;
	}

	/**
	 * @return the mailingId
	 */
	public int getMailingId() {
		return mailingId;
	}

	/**
	 * @param mailingId the mailingId to set
	 */
	public void setMailingId(int mailingId) {
		this.mailingId = mailingId;
	}

	/**
	 * @return the targetGroupId
	 */
	public int getTargetGroupId() {
		return targetGroupId;
	}

	/**
	 * @param targetGroupId the targetGroupId to set
	 */
	public void setTargetGroupId(int targetGroupId) {
		this.targetGroupId = targetGroupId;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * @return the campaign
	 */
	public String getCampaign() {
		return campaign;
	}
	
	/**
	 * @param campaign the campaign to set
	 */
	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}
	
	/**
	 * @return the inmarket
	 */
	public Calendar getInmarket() {
		return inmarket;
	}
	
	/**
	 * @param inmarket the inmarket to set
	 */
	public void setInmarket(Calendar inmarket) {
		this.inmarket = inmarket;
	}
	
	/**
	 * @return the contacts
	 */
	public int getContacts() {
		return contacts;
	}
	
	/**
	 * @param contacts the contacts to set
	 */
	public void setContacts(int contacts) {
		this.contacts = contacts;
	}
	
	/**
	 * @return the openers
	 */
	public int getOpeners() {
		return openers;
	}
	
	/**
	 * @param openers the openers to set
	 */
	public void setOpeners(int openers) {
		this.openers = openers;
	}
	
	/**
	 * @return the clickers
	 */
	public int getClickers() {
		return clickers;
	}
	
	/**
	 * @param clickers the clickers to set
	 */
	public void setClickers(int clickers) {
		this.clickers = clickers;
	}
	
	/**
	 * @return the clicks_netto
	 */
	public int getClicks_netto() {
		return clicks_netto;
	}
	/**
	 * @param clicks_netto the clicks_netto to set
	 */
	public void setClicks_netto(int clicks_netto) {
		this.clicks_netto = clicks_netto;
	}
	/**
	 * @return the clicks_brutto
	 */
	public int getClicks_brutto() {
		return clicks_brutto;
	}
	
	/**
	 * @param clicks_brutto the clicks_brutto to set
	 */
	public void setClicks_brutto(int clicks_brutto) {
		this.clicks_brutto = clicks_brutto;
	}
	
	/**
	 * @return the bounces_hard
	 */
	public int getBounces_hard() {
		return bounces_hard;
	}
	
	/**
	 * @param bounces_hard the bounces_hard to set
	 */
	public void setBounces_hard(int bounces_hard) {
		this.bounces_hard = bounces_hard;
	}
	
	/**
	 * @return the bounces_soft
	 */
	public int getBounces_soft() {
		return bounces_soft;
	}
	
	/**
	 * @param bounces_soft the bounces_soft to set
	 */
	public void setBounces_soft(int bounces_soft) {
		this.bounces_soft = bounces_soft;
	}
	
	/**
	 * @return the optouts
	 */
	public int getOptouts() {
		return optouts;
	}
	
	/**
	 * @param optouts the optouts to set
	 */
	public void setOptouts(int optouts) {
		this.optouts = optouts;
	}
}
