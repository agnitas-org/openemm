/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.form;

import java.util.HashSet;
import java.util.Set;

import org.agnitas.util.AgnUtils;

import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class MailinglistForm {
	
	private int id;
	
	private String shortname;
	
	private String description;
	
	private int targetId;

	private boolean frequencyCounterEnabled;
	
	private RecipientProgressStatisticDto statistic;
	
	private Set<Integer> mediatypes = new HashSet<>();
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getShortname() {
		return shortname;
	}
	
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}
	
	public RecipientProgressStatisticDto getStatistic() {
        return statistic;
    }
    
    public void setStatistic(RecipientProgressStatisticDto statistic) {
        this.statistic = statistic;
    }

	public boolean getFrequencyCounterEnabled() {
		return frequencyCounterEnabled;
	}

	public void setFrequencyCounterEnabled(boolean markedForFrequencyCounter) {
		this.frequencyCounterEnabled = markedForFrequencyCounter;
	}
	
	public void setMediatype(int mediatypeId, String value) {
		if (AgnUtils.interpretAsBoolean(value)) {
			mediatypes.add(mediatypeId);
		} else {
			mediatypes.remove(mediatypeId);
		}
	}

	public String getMediatype(int mediatypeId) {
		return mediatypes.contains(mediatypeId) ? "on" : "";
	}

	public Set<MediaTypes> getMediatypes() {
		Set<MediaTypes> returnSet = new HashSet<>();
		for (int mediatypeCode : mediatypes) {
			returnSet.add(MediaTypes.getMediaTypeForCode(mediatypeCode));
		}
		return returnSet;
	}

	public void setMediatypes(Set<MediaTypes> mediatypes) {
		this.mediatypes.clear();
		for (MediaTypes mediatype : mediatypes) {
			this.mediatypes.add(mediatype.getMediaCode());
		}
	}
}
