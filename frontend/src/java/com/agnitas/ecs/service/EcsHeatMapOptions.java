/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.service;

public class EcsHeatMapOptions {
    public static Builder builder() {
        return new Builder();
    }
    
    private int mailingId;
    private int recipientId;
    private int viewMode;
    private int previewSize;
    private int deviceType;
    
    public int getMailingId() {
        return mailingId;
    }
    
    public int getRecipientId() {
        return recipientId;
    }
    
    public int getViewMode() {
        return viewMode;
    }
    
    public int getPreviewSize() {
        return previewSize;
    }
    
    public int getDeviceType() {
        return deviceType;
    }

    
    public static class Builder {
        private EcsHeatMapOptions options = new EcsHeatMapOptions();

        private Builder() {}

        public EcsHeatMapOptions.Builder setMailingId(int mailingId) {
            options.mailingId = mailingId;
            return this;
        }

        public EcsHeatMapOptions.Builder setRecipientId(int recipientId) {
            options.recipientId = recipientId;
            return this;
        }
        
        public EcsHeatMapOptions.Builder setViewMode(int viewMode) {
            options.viewMode = viewMode;
            return this;
        }
        
        public EcsHeatMapOptions.Builder setDeviceType(int deviceType) {
            options.deviceType = deviceType;
            return this;
        }
        
        public EcsHeatMapOptions.Builder setPreviewSize(int previewSize) {
            options.previewSize = previewSize;
            return this;
        }

        public EcsHeatMapOptions build() {
            EcsHeatMapOptions result = options;
            options = null;
            return result;
        }
    }
}
