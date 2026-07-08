/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Map;
import java.util.Set;

public interface DynamicTag extends Cloneable {

	boolean addContent(DynamicTagContent aContent);

	boolean removeContent(int aID);

	int getCompanyID();

	Map<Integer, DynamicTagContent> getDynContent();

	DynamicTagContent getDynContentID(int id);

	String getDynName();

	int getId();

	int getEndTagEnd();

	int getEndTagStart();

	int getMailingID();

	int getMaxOrder();

	int getStartTagEnd();

	int getStartTagStart();

	int getValueTagEnd();

	int getValueTagStart();

	Integer getDivChildId();

	boolean isStandaloneTag();

	void setCompanyID(int id);

	void setStandaloneTag(boolean standaloneTag);

	void setDynName(String name);

	void setId(int id);

	void setEndTagEnd(int endTagEnd);

	void setEndTagStart(int endTagStart);

	void setMailingID(int id);

	void setStartTagEnd(int startTagEnd);

	void setStartTagStart(int startTagStart);

	void setValueTagEnd(int valueTagEnd);

	void setValueTagStart(int valueTagStart);

	void setDynContent(Map<Integer, DynamicTagContent> dynContent);

	Mailing getMailing();

	void setMailing(Mailing mailing);

	int getGroup();

	void setGroup(int group);

	String getDynInterestGroup();

	void setDynInterestGroup(String dynInterestGroup);

	int getInterestValue();

	void setInterestValue(int interestValue);

	void setDisableLinkExtension(boolean disable);

	void setDivChildId(Integer divChildId);

	boolean isDisableLinkExtension();

	DynamicTag clone();

	Set<Integer> getAllReferencedTargetGroups();

}
