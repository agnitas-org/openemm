/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.agnitas.beans.Admin;
import com.agnitas.emm.premium.service.PremiumFeaturesService;
import com.agnitas.emm.premium.web.SpecialPremiumFeature;

import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Shows JSP content, when a specific feature is available to current user.
 * 
 * <p>
 * Usage:
 * 
 * For <i>feature</i> attribute, use name of enum constant of {@link SpecialPremiumFeature} enum.
 * </p>
 * 
 * <p>
 * Example:
 * 
 * To show content, when the <i>Automation</i> package is available, surround
 * the JSP content with this code:
 * <pre>
 * 	&lt;ShowWhenFeatureEnabled feature="AUTOMATION"> 
 * 		... 
 * 	&lt;/ShowWhenFeatureEnabled>
 * </pre>
 * </p>
 */

public class ShowWhenFeatureEnabledTag extends PermissionExceptionTagSupport {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(ShowWhenFeatureEnabledTag.class);
	
	/** Serial version UID. */
    private static final long serialVersionUID = 2088220971349294443L;

    /** Name of required feature. */
    protected String feature;

    /**
     * Setter for <i>feature</i> attribute.
     * 
     * @param feature name of feature
     */
    public void setFeature(String feature) {
    	this.feature = feature != null ? feature : "";
    }
    
    @Override
	public int doStartTag() throws JspException {
    	final SpecialPremiumFeature premiumFeature = this.feature != null ? SpecialPremiumFeature.valueOf(this.feature) : null;
    	
    	// was this a valid feature name?
    	if(premiumFeature != null) {
    		if(LOGGER.isDebugEnabled()) {
    			LOGGER.debug(String.format("Identified feature '%s' as '%s'", this.feature, premiumFeature));
    		}
    		
	    	final WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
	    	
	    	try {
		    	final PremiumFeaturesService featureService = applicationContext.getBean("PremiumFeaturesService", PremiumFeaturesService.class);
		    	
		    	if(featureService != null) {
			    	final Admin aAdmin = AgnUtils.getAdmin(pageContext);
					if (aAdmin != null) {
			            try {
			            	final boolean enabled = featureService.isFeatureRightsAvailable(premiumFeature.getName(), aAdmin.getCompanyID());
			            	
			            	if(LOGGER.isDebugEnabled()) {
			            		LOGGER.debug(String.format("Feature '%s' enabled: %b", premiumFeature, enabled));
			            	}
			            	
			            	if(enabled) {
			                    return TagSupport.EVAL_BODY_INCLUDE;
			                }
			            } catch (Exception e) {
			                releaseException(e, feature);
			            }
			        }
		    	}
	    	} catch(final NoSuchBeanDefinitionException e) {
	    		return SKIP_BODY;
	    	}
    	}
    	
		return SKIP_BODY;
	}
}
