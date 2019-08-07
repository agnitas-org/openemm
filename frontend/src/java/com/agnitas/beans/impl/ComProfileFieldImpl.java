/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.impl.ProfileFieldImpl;

import com.agnitas.beans.ComProfileField;

public class ComProfileFieldImpl extends ProfileFieldImpl implements ComProfileField {
    private static final long serialVersionUID = -6125451198749198856L;
    
	protected int line = 0;
	protected int sort = 1000;
	protected boolean interest = false;
	
	protected Date creationDate;
	protected Date changeDate;
	
	private boolean historize;
	private String[] allowedValues;
	private boolean isHiddenField;
	
	private int numericPrecision;
	private int numericScale;
	
	@Override
	public int getLine() {
		return line;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public int getSort() {
		return sort;
	}

	@Override
	public void setSort(int sort) {
		this.sort = sort;
	}

	@Override
	public boolean isInterest() {
		return interest;
	}

	@Override
	public void setInterest(boolean interest) {
		this.interest = interest;
	}

	@Override
	public int getInterest() {
		return interest ? 1:0;
	}

	@Override
	public void setInterest(int interest) {
		this.interest = ( interest == 0 ? false :true ); 		
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
	public boolean getHistorize() {
		return historize;
	}

	@Override
	public void setHistorize(boolean historize) {
		this.historize = historize;
	}

	@Override
	public String[] getAllowedValues() {
		return allowedValues;
	}

	@Override
	public void setAllowedValues(String[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	@Override
	public boolean isHiddenField() {
		return isHiddenField;
	}

	@Override
	public void setHiddenField(boolean hiddenField) {
		isHiddenField = hiddenField;
	}

	public boolean getIsHiddenField() {
		return isHiddenField;
	}
	
	@Override
	public int getNumericPrecision() {
		return numericPrecision;
	}
	
	@Override
	public void setNumericPrecision(int numericPrecision) {
		this.numericPrecision = numericPrecision;
	}
	
	@Override
	public int getNumericScale() {
		return numericScale;
	}
	
	@Override
	public void setNumericScale(int numericScale) {
		this.numericScale = numericScale;
	}
}
