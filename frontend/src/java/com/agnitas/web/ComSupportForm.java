/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.struts.action.ActionForm;

public class ComSupportForm extends ActionForm {
	private static final long serialVersionUID = -493768728053196649L;
	
	protected Map<Integer,String> parameterNames;
	protected Map<Integer,String> parameterValues;
	
	private String url;

    public ComSupportForm() {
		this.parameterNames = new HashMap<>();
		this.parameterValues = new HashMap<>();
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public Set<Integer> getIndices() {
		return parameterNames.keySet();  // parameterNames and parameterValues should contain the same indices!!!
	}
	
	public void setParameterName(int index, String name) {
		this.parameterNames.put(index, name);
	}
	
	public String getParameterName(int index) {
		return this.parameterNames.get(index);
	}
	
	public void setParameterValue(int index, String value) {
		this.parameterValues.put(index, value);
	}
	
	public String getParameterValue(int index) {
		return this.parameterValues.get(index);
	}
	
	public void addParameter(String name, String value) {
		int index = Math.max(parameterNames.size(), parameterValues.size());
		
		setParameterName(index, name);
		setParameterValue(index, value);
	}
	
	public int numParameter() {
		return this.parameterNames.size();
	}
}
