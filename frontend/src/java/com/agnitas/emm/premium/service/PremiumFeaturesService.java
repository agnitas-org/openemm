/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.premium.service;

import java.util.List;


import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.premium.bean.FeaturePackage;
import com.agnitas.emm.premium.web.SpecialPremiumFeature;

public interface PremiumFeaturesService {
    
    List<FeaturePackage> getPremiumFeaturesPackages(int companyID);
    
    boolean isFeatureRightsAvailable(final SpecialPremiumFeature feature, final int companyId);
    
    /**
     * @see #isFeatureRightsAvailable(SpecialPremiumFeature, int)
     */
    @Deprecated
    boolean isFeatureRightsAvailable(String featureName, int companyId);
    
    List<Permission> getPremiumFeaturesPermissions(String featureName);
    
    void changeFeatureStatus(Admin admin, int companyId, String featurePackageName, String newStatus) throws Exception;

    List<FeaturePackage> getAvailableFeatureList();
}
