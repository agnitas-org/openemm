package com.agnitas.emm.premium.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.premium.bean.FeaturePackage;
import com.agnitas.emm.premium.service.PremiumFeaturesService;
import com.agnitas.emm.premium.web.SpecialPremiumFeature;

/**
 * Dummy implementation for OpenEMM.
 */
@Service("PremiumFeaturesService")
public class PremiumFeaturesServiceImpl implements PremiumFeaturesService {

	@Override
	public List<FeaturePackage> getPremiumFeaturesPackages(int companyID) {
		return List.of();
	}

	@Override
	public boolean isFeatureRightsAvailable(SpecialPremiumFeature feature, int companyId) {
		return false;
	}

	@Override
	public boolean isFeatureRightsAvailable(String featureName, int companyId) {
		return false;
	}

	@Override
	public List<Permission> getPremiumFeaturesPermissions(String featureName) {
		return List.of();
	}

	@Override
	public void changeFeatureStatus(Admin admin, int companyId, String featurePackageName, String newStatus) throws Exception {
		// Do nothing
	}

	@Override
	public List<FeaturePackage> getAvailableFeatureList() {
		return List.of();
	}

}
