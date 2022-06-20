/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.account.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridCopyInfoBean {
    private Map<Integer, Integer> oldNewGridIdsMapping = new HashMap<>();
    private Map<Integer, Integer> oldNewGridContainerIdsMapping = new HashMap<>();
    private Map<Integer, Integer> oldNewGridPlaceholdersIdsMapping = new HashMap<>();
    private Map<Integer, Integer> oldNewGridTemplateIdsMapping = new HashMap<>();
    private Map<Integer, Integer> oldNewGridChildIdsMapping = new HashMap<>();
    private Map<Integer, Integer> oldNewCategoryIdsMapping = new HashMap<>();
    private Map<Integer, Integer> oldNewMediapoolIds = new HashMap<>();

    private List<String> errors = new ArrayList<>();

    public Map<Integer, Integer> getOldNewGridIdsMapping() {
        return oldNewGridIdsMapping;
    }

    public void setOldNewGridIdsMapping(Map<Integer, Integer> oldNewGridIdsMapping) {
        this.oldNewGridIdsMapping = oldNewGridIdsMapping;
    }

    public Map<Integer, Integer> getOldNewGridContainerIdsMapping() {
        return oldNewGridContainerIdsMapping;
    }

    public void setOldNewGridContainerIdsMapping(Map<Integer, Integer> oldNewGridContainerIdsMapping) {
        this.oldNewGridContainerIdsMapping = oldNewGridContainerIdsMapping;
    }

    public Map<Integer, Integer> getOldNewGridPlaceholdersIdsMapping() {
        return oldNewGridPlaceholdersIdsMapping;
    }

    public void setOldNewGridPlaceholdersIdsMapping(Map<Integer, Integer> oldNewGridPlaceholdersIdsMapping) {
        this.oldNewGridPlaceholdersIdsMapping = oldNewGridPlaceholdersIdsMapping;
    }

    public Map<Integer, Integer> getOldNewGridTemplateIdsMapping() {
        return oldNewGridTemplateIdsMapping;
    }

    public void setOldNewGridTemplateIdsMapping(Map<Integer, Integer> oldNewGridTemplateIdsMapping) {
        this.oldNewGridTemplateIdsMapping = oldNewGridTemplateIdsMapping;
    }

    public Map<Integer, Integer> getOldNewGridChildIdsMapping() {
        return oldNewGridChildIdsMapping;
    }

    public void setOldNewGridChildIdsMapping(Map<Integer, Integer> oldNewGridChildIdsMapping) {
        this.oldNewGridChildIdsMapping = oldNewGridChildIdsMapping;
    }

    public Map<Integer, Integer> getOldNewCategoryIdsMapping() {
        return oldNewCategoryIdsMapping;
    }

    public void setOldNewCategoryIdsMapping(Map<Integer, Integer> oldNewCategoryIdsMapping) {
        this.oldNewCategoryIdsMapping = oldNewCategoryIdsMapping;
    }

    public Map<Integer, Integer> getOldNewMediapoolIds() {
        return oldNewMediapoolIds;
    }

    public void setOldNewMediapoolIds(Map<Integer, Integer> oldNewMediapoolIds) {
        this.oldNewMediapoolIds = oldNewMediapoolIds;
    }

    public List<String> getErrors() {
        return errors;
    }
}
