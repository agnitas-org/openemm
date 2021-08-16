/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.bean.impl;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.emm.core.velocity.VelocitySpringUtils;
import org.agnitas.emm.core.velocity.VelocityWrapper;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.springframework.context.ApplicationContext;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

/**
 * Bean extension of EMM (needed for trackable links)
 */
public class UserFormImpl implements UserForm {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UserFormImpl.class);
	
	/**
     * Holds value of property companyID.
     */
    protected int companyID;
    
    /**
     * Holds value of property formName.
     */
    protected String formName;
    
    /**
     * Holds value of property id.
     */
    protected int id;
    
    /**
     * Holds value of property startActionID.
     */
    protected int startActionID;
    
    /**
     * Holds value of property endActionID.
     */
    protected int endActionID;
    
    /**
     * Holds value of property successTemplate.
     */
    protected String successTemplate;
    
    /**
     * Holds value of property errorTemplate.
     */
    protected String errorTemplate;
    
    /**
     * Holds value of property description.
     */
    protected String description;

    /**
     * Holds value of property startAction.
     */
    protected org.agnitas.actions.EmmAction startAction;
    
    /**
     * Holds value of property endAction.
     */
    protected org.agnitas.actions.EmmAction endAction;

    protected String successUrl;
    protected String errorUrl;
    protected boolean successUseUrl;
    protected boolean errorUseUrl;

    protected String actionNames;

	/**
	 * holds all user form trackable links
	 */
	protected Map<String, ComTrackableUserFormLink> trackableLinks;
	protected Date creationDate;
	protected Date changeDate;
	protected String successMimetype = "text/html"; // alternative: "application/json"
	protected String errorMimetype = "text/html"; // alternative: "application/json"

    private boolean isActive;

    private String successFormBuilderJson;
    private String errorFormBuilderJson;

    /**
     * Getter for property companyID.
     * @return Value of property companyID.
     */
    @Override
	public int getCompanyID() {
        return companyID;
    }
    
    /**
     * Setter for property companyID.
     * @param companyID New value of property companyID.
     */
    @Override
	public void setCompanyID( @VelocityCheck int companyID) {
        this.companyID = companyID;
    }
    
    /**
     * Getter for property formName.
     * @return Value of property formName.
     */
    @Override
	public String getFormName() {
        return formName;
    }
    
    /**
     * Setter for property formName.
     * @param formName New value of property formName.
     */
    @Override
	public void setFormName(String formName) {
        this.formName = formName;
    }
    
    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    @Override
	public int getId() {
        return id;
    }
    
    /**
     * Setter for property id.
     * 
     * @param formID 
     */
    @Override
	public void setId(int formID) {
        this.id = formID;
    }
    
    /**
     * Getter for property startActionID.
     * @return Value of property startActionID.
     */
    @Override
	public int getStartActionID() {
        return startActionID;
    }
    
    /**
     * Setter for property startActionID.
     * @param startActionID New value of property startActionID.
     */
    @Override
	public void setStartActionID(int startActionID) {
        this.startActionID = startActionID;
    }
    
    /**
     * Getter for property endActionID.
     * @return Value of property endActionID.
     */
    @Override
	public int getEndActionID() {
        return endActionID;
    }
    
    /**
     * Setter for property endActionID.
     * @param endActionID New value of property endActionID.
     */
    @Override
	public void setEndActionID(int endActionID) {
        this.endActionID = endActionID;
    }
    
    /**
     * Getter for property successTemplate.
     * @return Value of property successTemplate.
     */
    @Override
	public String getSuccessTemplate() {
        return successTemplate;
    }
    
    /**
     * Setter for property successTemplate.
     * @param successTemplate
     */
    @Override
	public void setSuccessTemplate(String successTemplate) {
        this.successTemplate = successTemplate;
    }
    
    /**
     * Getter for property errorTemplate.
     * @return Value of property errorTemplate.
     */
    @Override
	public String getErrorTemplate() {
        return errorTemplate;
    }
    
    /**
     * Setter for property errorTemplate.
     * @param errorTemplate New value of property errorTemplate.
     */
    @Override
	public void setErrorTemplate(String errorTemplate) {
        this.errorTemplate = errorTemplate;
    }
    
    /**
     * Getter for property description.
     * @return Value of property description.
     */
    @Override
	public String getDescription() {
        return description;
    }
    
    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    @Override
	public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Getter for property startAction.
     * @return Value of property startAction.
     */
    @Override
	public org.agnitas.actions.EmmAction getStartAction() {
        return startAction;
    }
    
    /**
     * Setter for property startAction.
     * @param startAction New value of property startAction.
     */
    @Override
	public void setStartAction(org.agnitas.actions.EmmAction startAction) {
        this.startAction = startAction;
    }
    
    /**
     * Getter for property endAction.
     * @return Value of property endAction.
     */
    @Override
	public org.agnitas.actions.EmmAction getEndAction() {
        return endAction;
    }
    
    /**
     * Setter for property endAction.
     * @param endAction New value of property endAction.
     */
    @Override
	public void setEndAction(org.agnitas.actions.EmmAction endAction) {
        this.endAction = endAction;
    }

    @Override
	public String getSuccessUrl() {
        return successUrl;
    }

    @Override
	public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    @Override
	public String getErrorUrl() {
        return errorUrl;
    }

    @Override
	public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    @Override
	public boolean isSuccessUseUrl() {
        return successUseUrl;
    }

    @Override
	public void setSuccessUseUrl(boolean successUseUrl) {
        this.successUseUrl = successUseUrl;
    }

    @Override
	public boolean isErrorUseUrl() {
        return errorUseUrl;
    }

    @Override
	public void setErrorUseUrl(boolean errorUseUrl) {
        this.errorUseUrl = errorUseUrl;
    }

    protected boolean evaluateAction(ApplicationContext con, org.agnitas.actions.EmmAction aAction, Map<String, Object> params, final EmmActionOperationErrors errors) {
        if (aAction == null) {
            return true;
        }

        boolean result = false;
        try {
        	EmmActionService emmActionService = (EmmActionService) con.getBean("EmmActionService");
            result = emmActionService.executeActions(aAction.getId(), aAction.getCompanyID(), params, errors);
        } catch (ViciousFormDataException e) {
			throw e;
        } catch (Exception e) {
            logFormParameters(params);
            logger.error("evaluateAction: "+e, e);
        }
        
        return result;
    }

    @Override
	public boolean evaluateStartAction(ApplicationContext con, Map<String, Object> params, final EmmActionOperationErrors errors) {
		if (startActionID != 0 && startAction == null) {
			EmmActionDao dao = (EmmActionDao) con.getBean("EmmActionDao");

			startAction = dao.getEmmAction(startActionID, companyID);
			if (startAction == null) {
				logger.error("Action not found: CompanyID=" + companyID + " ActionID=" + startActionID);
				return false;
			}
		}

		boolean actionResult = evaluateAction(con, startAction, params, errors);

		if (!actionResult) {
			logger.error(String.format("Action Error: CompanyID=%d, ActionID=%d, error codes=%s", companyID, startActionID, errors));
		} else if (logger.isDebugEnabled()) {
			logger.debug("Action Result: CompanyID=" + companyID + " ActionID=" + startActionID + " " + actionResult);
		}

		return actionResult;
	}
    
    @Override
	public boolean evaluateEndAction(ApplicationContext con, Map<String, Object> params, final EmmActionOperationErrors errors) {
		if (endActionID != 0 && endAction == null) {
			EmmActionDao dao = (EmmActionDao) con.getBean("EmmActionDao");

			endAction = dao.getEmmAction(endActionID, companyID);

			if (endAction == null) {
				return false;
			}
		}

		return evaluateAction(con, endAction, params, errors);
	}
    
	@Override
	public String evaluateForm(ApplicationContext con, Map<String, Object> params, final EmmActionOperationErrors errors) {
		boolean actionResult = evaluateStartAction(con, params, errors);
		if (!actionResult) {
			params.put("_error", "1");
		}

		return evaluateFormResult(params, actionResult, con);
	}
    
	private void evaluteTemplate(final Map<String, Object> params, final ApplicationContext applicationContext, final String template, final Writer writer) throws Exception {
		final VelocityWrapperFactory factory = VelocitySpringUtils.getVelocityWrapperFactory(applicationContext);
		final VelocityWrapper velocity = factory.getWrapper(companyID);

		final CaseInsensitiveMap<String, Object> paramsEscaped = escapeRequestParameters(params);
		
		velocity.evaluate(paramsEscaped, template, writer, id, 0);	// This script is from the form, not from a action, so action ID is 0
	}
	
	private String evaluateResultUrl(final Map<String, Object> params, final ApplicationContext applicationContext, final String url) throws Exception {
		try(final StringWriter writer = new StringWriter()) {
			evaluteTemplate(params, applicationContext, url, writer);
			
			writer.flush();
			return writer.toString();
		}
	}
	
	private final CaseInsensitiveMap<String, Object> escapeRequestParameters(final Map<String, Object> params) {
		final CaseInsensitiveMap<String, Object> paramsEscaped = new CaseInsensitiveMap<>(params);
		
		@SuppressWarnings("unchecked")
		final Map<String, Object> parameters = (Map<String, Object>) paramsEscaped.get("requestParameters");
        paramsEscaped.put("requestParameters", AgnUtils.escapeHtmlInValues(parameters));
        
        return paramsEscaped;
	}
	
	protected String evaluateFormResult(Map<String, Object> params, boolean actionResult, ApplicationContext context) {
        if(actionResult && successUseUrl) {
        	try {
	        	final String url = evaluateResultUrl(params, context, successUrl);
	            // return success URL and set flag for redirect
	            params.put(TEMP_REDIRECT_PARAM, Boolean.TRUE);
	            return url;
        	} catch(final Exception e) {
        		logger.error("Error evaluating success URL", e);
    			logFormParameters(params);

        		return null;
        	}
        }
        if(!actionResult && errorUseUrl) {
        	try {
	        	final String url = evaluateResultUrl(params, context, errorUrl);
	            // return success URL and set flag for redirect
	            params.put(TEMP_REDIRECT_PARAM, Boolean.TRUE);
	            return url;
        	} catch(final Exception e) {
        		logger.error("Error evaluating error URL", e);
    			logFormParameters(params);

        		return null;
        	}
        }
        
        final StringWriter aWriter = new StringWriter();
        final CaseInsensitiveMap<String, Object> paramsEscaped = escapeRequestParameters(params);
		try {
			VelocityWrapperFactory factory = VelocitySpringUtils.getVelocityWrapperFactory( context);
			VelocityWrapper velocity = factory.getWrapper(companyID);
			
			if (actionResult) {
				velocity.evaluate( paramsEscaped, successTemplate, aWriter, id, 0);	// This script is from the form, not from a action, so action ID is 0
			} else {
				velocity.evaluate( paramsEscaped, errorTemplate, aWriter, id, 0);	// This script is from the form, not from a action, so action ID is 0
			}
		} catch (Exception e) {
			logger.error("evaluateForm: " + e.getMessage(), e);
			logFormParameters(params);
		}

        String result=aWriter.toString();
        if(params.get("velocity_error") != null) {
            result += "<br/><br/>" + params.get("velocity_error");
            params.remove("velocity_error");
        }
        if(params.get("errors") != null) {
            result += "<br/>";
            ActionErrors velocityErrors = (ActionErrors) params.get("errors");
            @SuppressWarnings("unchecked")
			Iterator<Object> it = velocityErrors.get();
            while(it.hasNext()) {
                result += "<br/>" + it.next();
            }
        }
        return result;
    }

    private void logFormParameters(Map<String, Object> params) {
    	for (Entry<String, Object> entry : params.entrySet()) {
            logger.error(entry.getKey() + ": " + (entry.getValue() != null ? entry.getValue() : "[value is null]") + "\n");	// md: Removed call of "toString()" on key and value. See AGNEMM-2002 for more information
        }
    }

    @Override
    public List<Integer> getUsedActionIds() {
        ArrayList<Integer> actions = new ArrayList<>();
        if (startActionID > 0) {
            actions.add(startActionID);
        }

        if (endActionID > 0) {
            actions.add(endActionID);
        }

        return actions;
    }

    @Override
    public boolean isUsesActions(){
        return getStartActionID() > 0 || getEndActionID() > 0;
    }

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getChangeDate() {
		return changeDate;
	}

	@Override
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	@Override
	public Map<String, ComTrackableUserFormLink> getTrackableLinks() {
		return trackableLinks;
	}

	@Override
	public void setTrackableLinks(Map<String, ComTrackableUserFormLink> trackableLinks) {
		this.trackableLinks = trackableLinks;
	}

	@Override
	public String getSuccessMimetype() {
		return successMimetype;
	}

	@Override
	public void setSuccessMimetype(String successMimetype) {
		this.successMimetype = StringUtils.defaultIfBlank(successMimetype, "text/html");
	}

	@Override
	public String getErrorMimetype() {
		return errorMimetype;
	}

	@Override
	public void setErrorMimetype(String errorMimetype) {
		this.errorMimetype = StringUtils.defaultIfBlank(errorMimetype, "text/html");
	}

    @Override
	public boolean isActive() {
        return isActive;
    }

    @Override
	public void setActive(boolean active) {
        isActive = active;
    }

    @Override
	public boolean getIsActive() {
        return isActive;
    }

    @Override
	public void setIsActive(boolean active) {
        isActive = active;
    }

    @Override
    public String getSuccessFormBuilderJson() {
        return successFormBuilderJson;
    }

    @Override
    public void setSuccessFormBuilderJson(String successFormBuilderJson) {
        this.successFormBuilderJson = successFormBuilderJson;
    }

    @Override
    public String getErrorFormBuilderJson() {
        return errorFormBuilderJson;
    }

    @Override
    public void setErrorFormBuilderJson(String errorFormBuilderJson) {
        this.errorFormBuilderJson = errorFormBuilderJson;
    }
}
