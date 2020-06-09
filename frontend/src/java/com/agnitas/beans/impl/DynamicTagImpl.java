/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.DynamicTag;

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
	
	private int group = 0;
	
	private String dynInterestGroup;
	private int interestValue;
	private boolean disableLinkExtensions;

	/**
	 * Creates new DynamicTag
	 */
	public DynamicTagImpl() {
	}

	@Override
	public void setDynName(String name) {
		dynName = name;
	}

	@Override
	public void setCompanyID( @VelocityCheck int id) {
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
	public int getDynContentCount() {
		if (dynContent == null) {
			return 0;
		} else {
			return dynContent.size();
		}
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
	public boolean changeContentOrder(int aID, int direction) {
		return changeContentOrder(aID, direction, false);
	}

	@Override
	public boolean changeContentOrder(int aID, int direction, boolean searchByOrderId) {
		int otherID = 0;

		if (dynContent == null) {
			return false;
		} else {
			DynamicTagContent firstContent = searchByOrderId ? getDynContentByOrderId(aID) : getDynContentID(aID);
	
			if (firstContent != null) {
				if (direction == 1) {
					// ascending
					otherID = -1;
					for (DynamicTagContent swapContent : dynContent.values()) {
						if (swapContent.getDynOrder() < firstContent.getDynOrder() && swapContent.getDynOrder() > otherID) {
							otherID = swapContent.getDynOrder();
						}
					}
				} else {
					// descending
					otherID = Integer.MAX_VALUE;
					for (DynamicTagContent swapContent : dynContent.values()) {
						if (swapContent.getDynOrder() > firstContent.getDynOrder() && swapContent.getDynOrder() < otherID) {
							otherID = swapContent.getDynOrder();
						}
					}
	
				}
			}
	
			if (otherID == -1 || otherID == Integer.MAX_VALUE) {
				return false;
			} else {
				DynamicTagContent swapContent = dynContent.get(otherID);
		
				if (firstContent == null) {
					throw new RuntimeException("firstContent was null");
				}
				int tmp = firstContent.getDynOrder();
				firstContent.setDynOrder(swapContent.getDynOrder());
				swapContent.setDynOrder(tmp);
		
				dynContent.put(swapContent.getDynOrder(), swapContent);
				dynContent.put(firstContent.getDynOrder(), firstContent);
		
				return true;
			}
		}
	}

	@Override
	public boolean moveContentDown(int aID, int amount) {
		return moveContentDown(aID, amount, false);
	}

	@Override
	public boolean moveContentDown(int aID, int amount, boolean searchByOrderId) {
		DynamicTagContent swapContent = null;
		int otherID = 0;
		int tmp = 0;

		if (dynContent == null)
			return false;

		DynamicTagContent firstContent = searchByOrderId ? getDynContentByOrderId(aID) : getDynContentID(aID);

		if (firstContent != null) {
			Iterator<DynamicTagContent> aIt = dynContent.values().iterator();
			if (amount < 0) {
				// rauf
				otherID = -1;
				for (; amount < 0; amount++) {
					while (aIt.hasNext()) {
						swapContent = aIt.next();
						if (swapContent.getDynOrder() < firstContent.getDynOrder() && swapContent.getDynOrder() > otherID) {
							otherID = swapContent.getDynOrder();
						}
					}
				}
			} else {
				// runter
				otherID = Integer.MAX_VALUE;
				for (; amount > 0; amount--) {
					while (aIt.hasNext()) {
						swapContent = aIt.next();
						if (swapContent.getDynOrder() > firstContent.getDynOrder() && swapContent.getDynOrder() < otherID) {
							otherID = swapContent.getDynOrder();
						}
					}
				}
			}
		}

		if (otherID == -1 || otherID == Integer.MAX_VALUE) {
			return false;
		}

		swapContent = dynContent.get(otherID);

		if (firstContent == null) {
			throw new RuntimeException("firstContent was null");
		}
		tmp = firstContent.getDynOrder();
		firstContent.setDynOrder(swapContent.getDynOrder());
		swapContent.setDynOrder(tmp);

		dynContent.put(swapContent.getDynOrder(), swapContent);
		dynContent.put(firstContent.getDynOrder(), firstContent);

		return true;
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

	public DynamicTagContent getDynContentByOrderId(int orderId) {
		if (dynContent != null) {
			for (DynamicTagContent aContent : dynContent.values()) {
				if (aContent.getDynOrder() == orderId) {
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

	/**
	 * Getter for property startPos.
	 * 
	 * @return Value of property startPos.
	 * 
	 */
	@Override
	public int getStartTagStart() {
		return startTagStart;
	}

	/**
	 * Setter for property startPos.
	 * 
	 * @param startTagStart
	 */
	@Override
	public void setStartTagStart(int startTagStart) {
		this.startTagStart = startTagStart;
	}

	/**
	 * Getter for property endPos.
	 * 
	 * @return Value of property endPos.
	 * 
	 */
	@Override
	public int getStartTagEnd() {
		return startTagEnd;
	}

	/**
	 * Setter for property endPos.
	 * 
	 * @param startTagEnd
	 */
	@Override
	public void setStartTagEnd(int startTagEnd) {
		this.startTagEnd = startTagEnd;
	}

	/**
	 * Getter for property valueStart.
	 * 
	 * @return Value of property valueStart.
	 * 
	 */
	@Override
	public int getValueTagStart() {
		return valueTagStart;
	}

	/**
	 * Setter for property valueStart.
	 * 
	 * @param valueTagStart
	 */
	@Override
	public void setValueTagStart(int valueTagStart) {
		this.valueTagStart = valueTagStart;
	}

	/**
	 * Getter for property valueEnd.
	 * 
	 * @return Value of property valueEnd.
	 * 
	 */
	@Override
	public int getValueTagEnd() {
		return valueTagEnd;
	}

	/**
	 * Setter for property valueEnd.
	 * 
	 * @param valueTagEnd
	 */
	@Override
	public void setValueTagEnd(int valueTagEnd) {
		this.valueTagEnd = valueTagEnd;
	}

	/**
	 * Getter for property standaloneTag.
	 * 
	 * @return Value of property standaloneTag.
	 * 
	 */
	@Override
	public boolean isStandaloneTag() {
		return standaloneTag;
	}

	/**
	 * Setter for property standaloneTag.
	 * 
	 * @param standaloneTag
	 *    New value of property standaloneTag.
	 * 
	 */
	@Override
	public void setStandaloneTag(boolean standaloneTag) {
		this.standaloneTag = standaloneTag;
	}

	/**
	 * Getter for property endTagStart.
	 * 
	 * @return Value of property endTagStart.
	 * 
	 */
	@Override
	public int getEndTagStart() {
		return endTagStart;
	}

	/**
	 * Setter for property endTagStart.
	 * 
	 * @param endTagStart
	 *            New value of property endTagStart.
	 * 
	 */
	@Override
	public void setEndTagStart(int endTagStart) {
		this.endTagStart = endTagStart;
	}

	/**
	 * Getter for property endTagEnd.
	 * 
	 * @return Value of property endTagEnd.
	 * 
	 */
	@Override
	public int getEndTagEnd() {
		return endTagEnd;
	}

	/**
	 * Setter for property endTagEnd.
	 * 
	 * @param endTagEnd
	 *            New value of property endTagEnd.
	 * 
	 */
	@Override
	public void setEndTagEnd(int endTagEnd) {
		this.endTagEnd = endTagEnd;
	}

	/**
	 * Setter for property dynContent.
	 * 
	 * @param dynContent
	 *            New value of property dynContent.
	 */
	@Override
	public void setDynContent(Map<Integer, DynamicTagContent> dynContent) {
		this.dynContent = dynContent;
	}

	/**
	 * Getter for property mailing.
	 * 
	 * @return Value of property mailing.
	 */
	@Override
	public Mailing getMailing() {
		return mailing;
	}

	/**
	 * Setter for property mailing.
	 * 
	 * @param mailing
	 *            New value of property mailing.
	 */
	@Override
	public void setMailing(org.agnitas.beans.Mailing mailing) {
		this.mailing = mailing;
	}

	/**
	 * Getter for the group of this tag. Groups are a new feature of dynamic
	 * content,which allows the contents to be grouped together when displaying
	 * them in the content list.
	 * 
	 * @return Value of property group.
	 * 
	 */
	@Override
	public int getGroup() {
		return group;
	}

	/**
	 * Setter for property group.
	 * 
	 * @param group
	 *            New value of property group.
	 * 
	 */
	@Override
	public void setGroup(int group) {
		this.group = group;
	}

	@Override
	public boolean equals(Object object) {
		// According to Object.equals(Object), equals(null) returns false
		if (object instanceof DynamicTag) {
			return StringUtils.equals(dynName, ((DynamicTag) object).getDynName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return dynName.hashCode();
	}

	@Override
	public final String getDynInterestGroup() {
		return dynInterestGroup;
	}

	@Override
	public final void setDynInterestGroup(final String interestgroup) {
		this.dynInterestGroup = interestgroup;		
	}

	@Override
	public final int getInterestValue() {
		return interestValue;
	}

	@Override
	public final void setInterestValue(final int interestValue) {
		this.interestValue = interestValue;		
	}

	@Override
	public final void setDisableLinkExtension(final boolean disable) {
		this.disableLinkExtensions = disable;
	}

	@Override
	public final boolean isDisableLinkExtension() {
		return this.disableLinkExtensions;
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
	public final Set<Integer> getAllReferencedTargetGroups() {
		final Set<Integer> targetGroups = new HashSet<>();
		
		for(final DynamicTagContent content : this.dynContent.values()) {
			targetGroups.add(content.getTargetID());
		}

		targetGroups.remove(0);
		
		return targetGroups;
	}
}
