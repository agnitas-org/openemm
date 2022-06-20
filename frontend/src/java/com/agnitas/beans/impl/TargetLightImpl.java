/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;
import java.util.regex.Matcher;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.ListSplit;
import com.agnitas.beans.TargetLight;

/**
 * Implementation of lightweight {@link TargetLight} interface.
 */
public class TargetLightImpl implements TargetLight {
	
	/** Company ID of target group. */
	protected int companyID;
	
	/** ID of target group. */
	protected int id;
	
	/** Name of target group. */
	protected String targetName;
	
	/** Descriptive text. */
	protected String targetDescription;
	
	/** Write-protection flag. */
	protected boolean locked;
	
	/** Creation date of target group. */
	protected Date creationDate;
	
	/** Date of last modification. */
	protected Date changeDate;
    
    /** Flag, if target group is deleted. */
    protected int deleted;
    
    /** Flag, if target group is valid. */
    private boolean valid;

	/** Indicator, if target group can be used with content blocks. */
    private boolean componentHide;

    /** A number that describes target structure complexity */
	private int complexityIndex;

	private boolean accessLimitation;

	private boolean favorite;
	
	@Override
	public int getCompanyID() {
		return companyID;
	}
	
	@Override
	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String getTargetName() {
		return targetName;
	}
	
	@Override
	public void setTargetName(String name) {
		this.targetName = name;
	}
	
	@Override
	public String getTargetDescription() {
		return targetDescription;
	}
	
	@Override
	public void setTargetDescription(String description) {
		this.targetDescription = description;
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	@Override
	public void setLocked(boolean locked) {
		this.locked = locked;
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
	public Date getChangeDate() {
		return changeDate;
	}

	@Override
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
    
    @Override
	public void setDeleted(int deleted) {
    	this.deleted = deleted;
    }
    
    @Override
	public int getDeleted() {
    	return this.deleted;
    }
    
    /**
     * Set flag, if target group is valid or not.
     * Invalid target groups can be caused by some EQL error (syntax error, invalid referenced items, ...)
     * 
     * @param valid <code>true</code> if target group is valid.
     */
    @Override
	public void setValid(boolean valid) {
    	this.valid = valid;
    }
    
    @Override
    public boolean isValid() {
    	return this.valid;
    }

	@Override
	public boolean isWorkflowManagerListSplit() {
		return StringUtils.isNotEmpty(targetName) && targetName.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX);
	}

	@Override
	public boolean isListSplit() {
		if (StringUtils.isEmpty(targetName)) {
			return false;
		}

		for (String prefix : TargetLight.LIST_SPLIT_PREFIXES) {
			if (targetName.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public ListSplit toListSplit() {
		if (StringUtils.isNotEmpty(targetName)) {
			String name = targetName;
			boolean isCampaignDriven;
			Matcher matcher;

			if (name.startsWith(TargetLight.LIST_SPLIT_PREFIX)) {
				isCampaignDriven = false;
				name = name.substring(TargetLight.LIST_SPLIT_PREFIX.length());
				matcher = LIST_SPLIT_PATTERN.matcher(name);
			} else if (name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX)) {
				isCampaignDriven = true;
				name = name.substring(TargetLight.LIST_SPLIT_CM_PREFIX.length());
				matcher = LIST_SPLIT_CM_PATTERN.matcher(name);
			} else {
				return null;
			}

			if (matcher.matches()) {
				int pos = name.lastIndexOf('_');
				String base = name.substring(0, pos);
				String part = name.substring(pos + 1);
				int partIndex = Integer.parseInt(part);

				String[] parts;

				if (isCampaignDriven) {
					parts = base.split(";");
				} else {
					int partsCount = base.length() / 2;
					parts = new String[partsCount];

					// Split into 2-digit groups (parts)
					for (int i = 0; i < partsCount; i++) {
						parts[i] = base.substring(i * 2, i * 2 + 2);
					}
				}

				if (partIndex > 0 && partIndex <= parts.length) {
					ListSplit split = new ListSplit();

					split.setTargetId(id);
					split.setParts(parts);
					split.setBase(base);
					split.setPartIndex(partIndex);
					split.setIsCampaignDriven(isCampaignDriven);

					return split;
				}
			}
		}

		return null;
	}

    @Override
    public void setComponentHide(boolean componentHide) {
        this.componentHide = componentHide;
    }

    @Override
    public boolean getComponentHide() {
        return componentHide;
    }

	@Override
	public int getComplexityIndex() {
		return complexityIndex;
	}

	@Override
	public void setComplexityIndex(int complexityIndex) {
		this.complexityIndex = complexityIndex;
	}

	@Override
	public boolean isAccessLimitation() {
		return accessLimitation;
	}

	@Override
	public void setAccessLimitation(boolean accessLimitation) {
		this.accessLimitation = accessLimitation;
	}
	
	@Override
	public final int hashCode() {
		return this.id;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if(obj instanceof TargetLight) {
			final TargetLight target = (TargetLight) obj;
			
			return target.getId() == this.id;
		} else {
			return false;
		}
	}

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
