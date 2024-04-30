/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.form;

import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;

import java.util.ArrayList;
import java.util.List;

public class TrackableLinkForm {

    private int id;
    private int usage;
    private int action;
    private int deepTracking;
    private String url;
    private String shortname;
    private boolean admin;
    private boolean staticLink;
    private boolean createSubstituteForAgnDynMulti;
    private List<ExtensionProperty> extensions = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public List<ExtensionProperty> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionProperty> extensions) {
        this.extensions = extensions;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isCreateSubstituteForAgnDynMulti() {
        return createSubstituteForAgnDynMulti;
    }

    public void setCreateSubstituteForAgnDynMulti(boolean createSubstituteForAgnDynMulti) {
        this.createSubstituteForAgnDynMulti = createSubstituteForAgnDynMulti;
    }

    public int getDeepTracking() {
        return deepTracking;
    }

    public void setDeepTracking(int deepTracking) {
        this.deepTracking = deepTracking;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public boolean isStaticLink() {
        return staticLink;
    }

    public void setStaticLink(boolean staticLink) {
        this.staticLink = staticLink;
    }
}
