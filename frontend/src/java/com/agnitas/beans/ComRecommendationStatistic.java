/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

public class ComRecommendationStatistic {
	public enum RecommendationStatisticStatus{
		RECOMMENDATION(0),
		PARTICIPATION(1);
		
		private final int dbValue;

		private RecommendationStatisticStatus(int dbValue) {
	        this.dbValue = dbValue;
	    }
		
		public int getDbValue() {
			return dbValue;
		}
		
		public static RecommendationStatisticStatus getFromInt(int statusDbValue) throws Exception {
			switch (statusDbValue) {
				case 0:
					return RECOMMENDATION;
				case 1:
					return PARTICIPATION;
				default:
					throw new Exception("Invalid RecommendationStatisticStatus");
			}
		}
	}
	
	private int recommendationId;
	private Date creationDate;
	private int senderCustomerId;
	private String senderEmail;
	private int receiverCustomerId;
	private String receiverEmail;
	private boolean isNew;
	private RecommendationStatisticStatus status;
	
	public int getRecommendationId() {
		return recommendationId;
	}
	
	public void setRecommendationId(int recommendationId) {
		this.recommendationId = recommendationId;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public int getSenderCustomerId() {
		return senderCustomerId;
	}
	
	public void setSenderCustomerId(int senderCustomerId) {
		this.senderCustomerId = senderCustomerId;
	}
	
	public String getSenderEmail() {
		return senderEmail;
	}
	
	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}
	
	public int getReceiverCustomerId() {
		return receiverCustomerId;
	}
	
	public void setReceiverCustomerId(int receiverCustomerId) {
		this.receiverCustomerId = receiverCustomerId;
	}
	
	public String getReceiverEmail() {
		return receiverEmail;
	}
	
	public void setReceiverEmail(String receiverEmail) {
		this.receiverEmail = receiverEmail;
	}
	
	public boolean getIsNew() {
		return isNew;
	}
	
	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	public void setIsNew(int isNew) {
		if (isNew <= 0) {
			this.isNew = false;
		} else {
			this.isNew = true;
		}
	}
	
	public RecommendationStatisticStatus getStatus() {
		return status;
	}
	
	public void setStatus(RecommendationStatisticStatus status) {
		this.status = status;
	}
}
