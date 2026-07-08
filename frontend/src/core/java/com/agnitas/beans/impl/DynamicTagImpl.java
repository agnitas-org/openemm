/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.Mailing;
import org.apache.commons.lang3.Strings;

public final class DynamicTagImpl implements DynamicTag {

	protected String dynName;
	protected int companyID;
	protected int mailingID;
	protected int id;
	protected Map<Integer, DynamicTagContent> dynContent = new HashMap<>();
	protected Mailing mailing;
	protected int startTagStart;
	protected int startTagEnd;
	protected int valueTagStart;
	protected int valueTagEnd;
	protected boolean standaloneTag;
	protected int endTagStart;
	protected int endTagEnd;

	private Integer divChildId;
	
	private int group = 0;
	
	private String dynInterestGroup;
	private int interestValue;
	private boolean disableLinkExtensions;

	@Override
	public void setDynName(String name) {
		dynName = name;
	}

	@Override
	public void setCompanyID( int id) {
		companyID = id;
	}

	@Override
	public void setMailingID(int id) {
		mailingID = id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean addContent(DynamicTagContent aContent) {
		dynContent.put(aContent.getDynOrder(), aContent);
		return true;
	}

	@Override
	public String getDynName() {
		return dynName;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Map<Integer, DynamicTagContent> getDynContent() {
		return dynContent;
	}

	@Override
	public int getMaxOrder() {
		int maxOrder = 0;

		if (dynContent != null) {
			for (DynamicTagContent aContent : dynContent.values()) {
				if (aContent.getDynOrder() > maxOrder) {
					maxOrder = aContent.getDynOrder();
				}
			}
		}

		return maxOrder;
	}

	@Override
	public DynamicTagContent getDynContentID(int contentId) {
		if (dynContent != null) {
			for (DynamicTagContent aContent : dynContent.values()) {
				if (aContent.getId() == contentId) {
					return aContent;
				}
			}
		}

		return null;
	}

	@Override
	public boolean removeContent(int aID) {
		if (dynContent == null) {
			return false;
		}

		Iterator<DynamicTagContent> aIt = dynContent.values().iterator();
		while (aIt.hasNext()) {
			DynamicTagContent aContent = aIt.next();
			if (aContent.getId() == aID) {
				aIt.remove();
				break;
			}
		}

		return true;
	}

	@Override
	public int getCompanyID() {
		return companyID;
	}

	@Override
	public int getMailingID() {
		return mailingID;
	}

	@Override
	public int getStartTagStart() {
		return startTagStart;
	}

	@Override
	public void setStartTagStart(int startTagStart) {
		this.startTagStart = startTagStart;
	}

	@Override
	public int getStartTagEnd() {
		return startTagEnd;
	}

	@Override
	public void setStartTagEnd(int startTagEnd) {
		this.startTagEnd = startTagEnd;
	}

	@Override
	public int getValueTagStart() {
		return valueTagStart;
	}

	@Override
	public void setValueTagStart(int valueTagStart) {
		this.valueTagStart = valueTagStart;
	}

	@Override
	public int getValueTagEnd() {
		return valueTagEnd;
	}

	@Override
	public void setValueTagEnd(int valueTagEnd) {
		this.valueTagEnd = valueTagEnd;
	}

	@Override
	public boolean isStandaloneTag() {
		return standaloneTag;
	}

	@Override
	public void setStandaloneTag(boolean standaloneTag) {
		this.standaloneTag = standaloneTag;
	}

	@Override
	public int getEndTagStart() {
		return endTagStart;
	}

	@Override
	public void setEndTagStart(int endTagStart) {
		this.endTagStart = endTagStart;
	}

	@Override
	public int getEndTagEnd() {
		return endTagEnd;
	}

	@Override
	public void setEndTagEnd(int endTagEnd) {
		this.endTagEnd = endTagEnd;
	}

	@Override
	public void setDynContent(Map<Integer, DynamicTagContent> dynContent) {
		this.dynContent = dynContent;
	}

	@Override
	public Mailing getMailing() {
		return mailing;
	}

	@Override
	public void setMailing(Mailing mailing) {
		this.mailing = mailing;
	}

	@Override
	public int getGroup() {
		return group;
	}

	@Override
	public void setGroup(int group) {
		this.group = group;
	}

	@Override
	public boolean equals(Object object) {
		// According to Object.equals(Object), equals(null) returns false
		if (object instanceof DynamicTag dynTag) {
			return Strings.CS.equals(dynName, dynTag.getDynName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return dynName.hashCode();
	}

	@Override
	public String getDynInterestGroup() {
		return dynInterestGroup;
	}

	@Override
	public void setDynInterestGroup(String dynInterestGroup) {
		this.dynInterestGroup = dynInterestGroup;
	}

	@Override
	public int getInterestValue() {
		return interestValue;
	}

	@Override
	public void setInterestValue(int interestValue) {
		this.interestValue = interestValue;
	}

	@Override
	public void setDisableLinkExtension(boolean disable) {
		this.disableLinkExtensions = disable;
	}

	@Override
	public boolean isDisableLinkExtension() {
		return this.disableLinkExtensions;
	}

	@Override
	public Integer getDivChildId() {
		return this.divChildId;
	}

	@Override
	public void setDivChildId(Integer divChildId) {
		this.divChildId = divChildId;
	}

	@Override
	public DynamicTag clone() {
		DynamicTagImpl dynamicTag = new DynamicTagImpl();

		dynamicTag.setMailingID(mailingID);
		dynamicTag.setMailing(mailing);
		dynamicTag.setDynName(dynName);
		dynamicTag.setCompanyID(companyID);
		dynamicTag.setDisableLinkExtension(disableLinkExtensions);
		dynamicTag.setDynInterestGroup(dynInterestGroup);
		dynamicTag.setEndTagEnd(endTagEnd);
		dynamicTag.setEndTagStart(endTagStart);
		dynamicTag.setGroup(group);
		dynamicTag.setId(id);
		dynamicTag.setInterestValue(interestValue);
		dynamicTag.setStandaloneTag(standaloneTag);
		dynamicTag.setStartTagEnd(startTagEnd);
		dynamicTag.setStartTagStart(startTagStart);
		dynamicTag.setValueTagEnd(valueTagEnd);
		dynamicTag.setValueTagStart(valueTagStart);

		return dynamicTag;
	}

	@Override
	public Set<Integer> getAllReferencedTargetGroups() {
		final Set<Integer> targetGroups = new HashSet<>();
		
		for(final DynamicTagContent content : this.dynContent.values()) {
			targetGroups.add(content.getTargetID());
		}

		targetGroups.remove(0);
		
		return targetGroups;
	}
}
