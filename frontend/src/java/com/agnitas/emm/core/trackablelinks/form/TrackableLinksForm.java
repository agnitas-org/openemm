/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.form;

import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;
import org.agnitas.web.forms.PaginationForm;

import java.util.ArrayList;
import java.util.List;

public class TrackableLinksForm extends PaginationForm {

    private int openActionId;
    private int clickActionId;
    private String intelliAdIdString;
    private boolean intelliAdEnabled;
    private boolean trackOnEveryPosition;
    private boolean modifyAllLinksExtensions;
    private boolean numberOfRowsChanged;
    private List<ExtensionProperty> extensions = new ArrayList<>();
    private int bulkUsage;
    private int bulkAction;
    private int bulkStatic;
    private int bulkDeepTracking;
    private String bulkDescription;
    private boolean bulkModifyDescription;
    private boolean modifyBulkLinksExtensions;
    private List<Integer> bulkIds = new ArrayList<>();
    private List<TrackableLinkForm> links = new ArrayList<>();
    private Boolean includeDeleted; // keep it as object to detect if it change from UI form if not null

    public List<TrackableLinkForm> getLinks() {
        return links;
    }

    public void setLinks(List<TrackableLinkForm> links) {
        this.links = links;
    }

    public int getBulkAction() {
        return bulkAction;
    }

    public void setBulkAction(int bulkAction) {
        this.bulkAction = bulkAction;
    }

    public int getBulkStatic() {
        return bulkStatic;
    }

    public void setBulkStatic(int bulkStatic) {
        this.bulkStatic = bulkStatic;
    }

    public boolean isIntelliAdEnabled() {
        return intelliAdEnabled;
    }

    public void setIntelliAdEnabled(boolean intelliAdEnabled) {
        this.intelliAdEnabled = intelliAdEnabled;
    }

    public boolean isTrackOnEveryPosition() {
        return trackOnEveryPosition;
    }

    public void setTrackOnEveryPosition(boolean trackOnEveryPosition) {
        this.trackOnEveryPosition = trackOnEveryPosition;
    }

    public boolean isModifyAllLinksExtensions() {
        return modifyAllLinksExtensions;
    }

    public void setModifyAllLinksExtensions(boolean modifyAllLinksExtensions) {
        this.modifyAllLinksExtensions = modifyAllLinksExtensions;
    }

    public boolean isModifyBulkLinksExtensions() {
        return modifyBulkLinksExtensions;
    }

    public void setModifyBulkLinksExtensions(boolean modifyBulkLinksExtensions) {
        this.modifyBulkLinksExtensions = modifyBulkLinksExtensions;
    }

    public boolean isBulkModifyDescription() {
        return bulkModifyDescription;
    }

    public void setBulkModifyDescription(boolean bulkModifyDescription) {
        this.bulkModifyDescription = bulkModifyDescription;
    }

    public boolean isNumberOfRowsChanged() {
        return numberOfRowsChanged;
    }

    public void setNumberOfRowsChanged(boolean numberOfRowsChanged) {
        this.numberOfRowsChanged = numberOfRowsChanged;
    }

    public int getOpenActionId() {
        return openActionId;
    }

    public void setOpenActionId(int openActionId) {
        this.openActionId = openActionId;
    }

    public int getClickActionId() {
        return clickActionId;
    }

    public void setClickActionId(int clickActionId) {
        this.clickActionId = clickActionId;
    }

    public String getIntelliAdIdString() {
        return intelliAdIdString;
    }

    public void setIntelliAdIdString(String intelliAdIdString) {
        this.intelliAdIdString = intelliAdIdString;
    }

    public String getBulkDescription() {
        return bulkDescription;
    }

    public void setBulkDescription(String bulkDescription) {
        this.bulkDescription = bulkDescription;
    }

    public int getBulkUsage() {
        return bulkUsage;
    }

    public void setBulkUsage(int bulkUsage) {
        this.bulkUsage = bulkUsage;
    }

    public int getBulkDeepTracking() {
        return bulkDeepTracking;
    }

    public void setBulkDeepTracking(int bulkDeepTracking) {
        this.bulkDeepTracking = bulkDeepTracking;
    }

    public List<Integer> getBulkIds() {
        return bulkIds;
    }

    public void setBulkIds(List<Integer> bulkIds) {
        this.bulkIds = bulkIds;
    }

    public List<ExtensionProperty> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionProperty> extensions) {
        this.extensions = extensions;
    }

    public Boolean getIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }
}
