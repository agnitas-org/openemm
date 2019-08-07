/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class TagDefinition {
	public static Pattern COMPLEX_PARAMETER_PATTERN = Pattern.compile("\\{[A-Za-z0-9_]+\\}");
	
	public enum TagType {
		/**
		 * Selects in DB without replacements like {name}
		 */
		SIMPLE,
		
		/**
		 * Selects in DB with replacements like {name}
		 */
		COMPLEX,
		
		/**
		 * Executes LUA-Code from tag_function_tbl
		 */
		FUNCTION,
		
		/**
		 * Only agnDYN and agnDVALUE with special evaluation
		 */
		FLOW;
		
		public static TagType getTypeFromString(String value) throws Exception {
			if ("SIMPLE".equalsIgnoreCase(value)) {
				return SIMPLE;
			} else if ("COMPLEX".equalsIgnoreCase(value)) {
				return COMPLEX;
			} else if ("FUNCTION".equalsIgnoreCase(value)) {
				return FUNCTION;
			} else if ("FLOW".equalsIgnoreCase(value)) {
				return FLOW;
			} else {
				throw new Exception("Invalid TagDefinitionType");
			}
		}
	}
	
	/**
	 * Only names starting with "agn" are allowed
	 */
	private String name;
	
	/**
	 * Type of this tag
	 */
	private TagType type;
	
	/**
	 * selectvalue String of this tag.
	 * This is only effective for SIMPLE and COMPLEX tags which select data from DB
	 */
	private String selectValue;
	
	/**
	 * Mandatory Parameters which are included in the selectvalue like {name}.
	 * This is only effective for COMPLEX and FLOW tags
	 * 
	 * An agnTag in a text component my have other non mandatory parameters also.
	 * These do not have to be included in the selectvalue.
	 */
	private List<String> mandatoryParameters;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public TagType getType() {
		return type;
	}
	
	public void setType(TagType type) {
		this.type = type;
	}
	
	public void setTypeString(String typeString) throws Exception {
		this.type = TagType.getTypeFromString(typeString);
	}
	
	public String getSelectValue() {
		return selectValue;
	}
	
	public void setSelectValue(String selectValue) {
		this.selectValue = selectValue != null ? selectValue : "";
		
		mandatoryParameters = new ArrayList<>();
		if (StringUtils.isNotBlank(selectValue) && (type == TagType.COMPLEX || type == TagType.FLOW)) {
			Matcher complexParameterMatcher = COMPLEX_PARAMETER_PATTERN.matcher(selectValue);
			while (complexParameterMatcher.find()) {
				String parameterName = complexParameterMatcher.group();
				mandatoryParameters.add(parameterName.substring(1, parameterName.length() - 1));
			}
		}
	}

	/**
	 * Only COMPLEX and FLOW tags have effective mandatory parameters
	 * 
	 * @return
	 */
	public List<String> getMandatoryParameters() {
		if (mandatoryParameters == null) {
			mandatoryParameters = new ArrayList<>();
		}
		
		if (mandatoryParameters.size() > 0) {
			return mandatoryParameters;
		} else {
			// Add missing mandatory parameter names, since "selectvalue" in db table "tag_tbl" was cleared
			if ("agnDB".equals(name)) {
				mandatoryParameters.add("column");	
			} else if ("agnTITLE".equals(name)) {
				mandatoryParameters.add("type");	
			} else if ("agnTITLEFULL".equals(name)) {
				mandatoryParameters.add("type");
			} else if ("agnTITLEFIRST".equals(name)) {
				mandatoryParameters.add("type");
			} else if ("agnIMGLINK".equals(name)) {
				mandatoryParameters.add("name");	
			} else if ("agnIMAGE".equals(name)) {
				mandatoryParameters.add("name");	
			}

			return mandatoryParameters;
		}
	}

	/**
	 * Only COMPLEX and FLOW tags have effective mandatory parameters
	 * 
	 * @param mandatoryParameters
	 */
	public void setMandatoryParameters(List<String> mandatoryParameters) {
		this.mandatoryParameters = mandatoryParameters;
	}
}
