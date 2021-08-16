/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Map;
import java.util.Set;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface DynamicTag extends Cloneable {
	/**
	 * Adds a content.
	 *
	 * @param aContent
	 *            Added content
	 */
	boolean addContent(DynamicTagContent aContent);

	/**
	 * Changes the content order.
	 *
	 * @param aID
	 * @param direction
	 */
	boolean changeContentOrder(int aID, int direction);

	boolean changeContentOrder(int aID, int direction, boolean searchByOrderId);

	/**
	 * Move content down in the list.
	 *
	 * @param aID
	 * @param amount
	 *            (negative values will move up)
	 */
	boolean moveContentDown(int aID, int amount);

	boolean moveContentDown(int aID, int amount, boolean searchByOrderId);

	/**
	 * Removes a content.
	 *
	 * @param aID
	 *            ID of content which will be removed
	 */
	boolean removeContent(int aID);

	/**
	 * Getter for property companyId.
	 *
	 * @return Value of property companyID.
	 */
	int getCompanyID();

	/**
	 * Getter for property dynContent.
	 *
	 * @return Value of property dynContent.
	 */
	Map<Integer, DynamicTagContent> getDynContent();

	/**
	 * Getter for property dynContentCount.
	 *
	 * @return Value of property dynContentCount.
	 */
	int getDynContentCount();

	/**
	 * Getter for property dynContentID.
	 *
	 * @return Value of property dynContentID.
	 */
	DynamicTagContent getDynContentID(int id);

	/**
	 * Getter for property dynName.
	 *
	 * @return Value of property dynName.
	 */
	String getDynName();

	/**
	 * Getter for property id.
	 *
	 * @return Value of property id.
	 */
	int getId();

	/**
	 * Getter for property endTagEnd.
	 * 
	 * @return Value of property endTagEnd.
	 */
	int getEndTagEnd();

	/**
	 * Getter for property endTagStart.
	 * 
	 * @return Value of property endTagStart.
	 */
	int getEndTagStart();

	/**
	 * Getter for property mailingID.
	 * 
	 * @return Value of property mailingID.
	 */
	int getMailingID();

	/**
	 * Getter for property maxOrder.
	 * 
	 * @return Value of property maxOrder.
	 */
	int getMaxOrder();

	/**
	 * Getter for property endPos.
	 * 
	 * @return Value of property endPos.
	 */
	int getStartTagEnd();

	/**
	 * Getter for property startPos.
	 * 
	 * @return Value of property startPos.
	 */
	int getStartTagStart();

	/**
	 * Getter for property valueEnd.
	 * 
	 * @return Value of property valueEnd.
	 */
	int getValueTagEnd();

	/**
	 * Getter for property valueStart.
	 * 
	 * @return Value of property valueStart.
	 */
	int getValueTagStart();

	/**
	 * Getter for property standaloneTag.
	 * 
	 * @return Value of property standaloneTag.
	 */
	boolean isStandaloneTag();

	void setCompanyID(@VelocityCheck int id);

	/**
	 * Setter for property standaloneTag.
	 * 
	 * @param complex
	 *            New value of property standaloneTag.
	 */
	void setStandaloneTag(boolean standaloneTag);

	/**
	 * Setter for property dynName.
	 * 
	 * @param name
	 *            New value of property dynName.
	 */
	void setDynName(String name);

	/**
	 * Setter for property id.
	 * 
	 * @param id
	 *            New value of property id.
	 */
	void setId(int id);

	/**
	 * Setter for property endTagEnd.
	 * 
	 * @param endTagEnd
	 *            New value of property endTagEnd.
	 */
	void setEndTagEnd(int endTagEnd);

	/**
	 * Setter for property endTagStart.
	 * 
	 * @param endTagStart
	 *            New value of property endTagStart.
	 */
	void setEndTagStart(int endTagStart);

	/**
	 * Setter for property mailingId.
	 * 
	 * @param id
	 *            New value of property MailingId.
	 */
	void setMailingID(int id);

	/**
	 * Setter for property endPos.
	 * 
	 * @param startTagEnd
	 */
	void setStartTagEnd(int startTagEnd);

	/**
	 * Setter for property startPos.
	 * 
	 * @param startTagStart
	 */
	void setStartTagStart(int startTagStart);

	/**
	 * Setter for property valueEnd.
	 * 
	 * @param valueTagEnd
	 */
	void setValueTagEnd(int valueTagEnd);

	/**
	 * Setter for property valueStart.
	 * 
	 * @param valueTagStart
	 */
	void setValueTagStart(int valueTagStart);

	/**
	 * Setter for property dynContent.
	 *
	 * @param dynContent
	 *            New value of property dynContent.
	 */
	void setDynContent(Map<Integer, DynamicTagContent> dynContent);

	/**
	 * Getter for property mailing.
	 *
	 * @return Value of property mailing.
	 */
	Mailing getMailing();

	/**
	 * Setter for property mailing.
	 *
	 * @param mailing
	 *            New value of property mailing.
	 */
	void setMailing(Mailing mailing);

	/**
	 * Getter for property group. The group i used to group dynamic tags logicaly
	 * together.
	 * 
	 * @return Value of property group.
	 */
	int getGroup();

	/**
	 * Setter for property group.
	 * 
	 * @param group
	 */
	void setGroup(int group);

	String getDynInterestGroup();

	void setDynInterestGroup(String interestgroup);

	int getInterestValue();

	void setInterestValue(int interestValue);

	/**
	 * Set flag, if applying link extensions has to be disabled.
	 * 
	 * @param disable
	 *            flag, if applying link extensions is disabled
	 */
	void setDisableLinkExtension(final boolean disable);

	/**
	 * Checks, if applying link extensions has to be disabled.
	 * 
	 * @return <code>true</code>, if applying link extensions is disabled
	 */
	boolean isDisableLinkExtension();

	DynamicTag clone();

	public Set<Integer> getAllReferencedTargetGroups();
}
