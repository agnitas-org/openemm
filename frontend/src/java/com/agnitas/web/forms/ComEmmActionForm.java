/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.Tuple;
import org.agnitas.web.forms.EmmActionForm;
import org.apache.struts.action.ActionMapping;


public class ComEmmActionForm extends EmmActionForm {
	private static final long serialVersionUID = 8382313452743675738L;

	private Set<Integer> bulkIDs = new HashSet<>();

    private List<Tuple<Integer, String>> usedByFormsNames;
    
    private List<Tuple<Integer, String>> usedByImportNames;

    private Map<Integer, Boolean> activenessMap = new HashMap<>();
    private String activenessFilter;

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);

        clearBulkIds();
        usedByFormsNames = null;
        usedByImportNames = null;
        activenessMap.clear();
        activenessFilter = null;
    }

    public void setBulkID(int id, String value) {
        if (value != null && (value.equals("on") || value.equals("yes") || value.equals("true"))) {
			this.bulkIDs.add(id);
		}
    }

    public String getBulkID(int id) {
        return this.bulkIDs.contains(id) ? "on" : "";
    }

    public Set<Integer> getBulkIds() {
        return this.bulkIDs;
    }

    public void clearBulkIds() {
        this.bulkIDs.clear();
    }

    public List<Tuple<Integer, String>> getUsedByFormsNames() {
        return usedByFormsNames;
    }

    public void setUsedByFormsNames(List<Tuple<Integer, String>> usedByFormsNames) {
        this.usedByFormsNames = usedByFormsNames;
    }

    public List<Tuple<Integer, String>> getUsedByImportNames() {
        return usedByImportNames;
    }

    public void setUsedByImportNames(List<Tuple<Integer, String>> usedByImportNames) {
        this.usedByImportNames = usedByImportNames;
    }

    public void setActiveness(int id, String value) {
        activenessMap.put(id, Boolean.parseBoolean(value));
    }

    public Map<Integer, Boolean> getActivenessMap() {
        return activenessMap;
    }

    public String getActivenessFilter() {
        return activenessFilter;
    }

    public void setActivenessFilter(String activenessFilter) {
        this.activenessFilter = activenessFilter;
    }
}
