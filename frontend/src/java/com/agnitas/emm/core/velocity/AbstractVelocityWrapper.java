/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity;

import java.io.Writer;
import java.util.Map;

import com.agnitas.emm.core.velocity.emmapi.CompanyAccessCheck;
import com.agnitas.emm.core.velocity.event.MessageEventHandlerImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Wrapper for Velocity hiding all the boilerplate code.
 */
public class AbstractVelocityWrapper implements VelocityWrapper {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(AbstractVelocityWrapper.class);

	/** Velocity engine. */
	private final VelocityEngine engine;
	
	/** Company ID in which the script is executed. */
	private final int contextCompanyId;
	
	/**
	 * Creates a new Velocity instance with given Uberspector.
	 * 
	 * @param companyId ID of company, for that the script is executed
	 * @param factory factory to create the Uberspect delegate target
	 */
	protected AbstractVelocityWrapper(int companyId, CompanyAccessCheck companyAccessCheck) {
		this.contextCompanyId = companyId;
		this.engine = createEngine(companyId, companyAccessCheck);
	}
	
	/**
	 * Creates a new Velocity engine. If an Uberspector class is defined, this class will
	 * be used for tracking the company context.
	 * 
	 * @param companyId ID of the company for that the scripts are executed
	 * @param factory factory to create the Uberspect delegate target
	 * 
	 * @return Velocity engine
	 * 
	 * @throws Exception on errors initializing Velocity
	 */
	private VelocityEngine createEngine(int companyId, CompanyAccessCheck companyAccessCheck) {
		VelocityEngine ve = new VelocityEngine();
		
		ve.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		ve.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, "com.agnitas.emm.core.velocity.IncludeParsePreventionHandler");
		ve.setProperty(RuntimeConstants.EVENTHANDLER_METHODEXCEPTION, "com.agnitas.emm.core.velocity.ThrowingMethodExceptionEventHandler");
		
        companyAccessCheck.checkCompanyAccess(companyId, getCompanyId());
		
		try {
			ve.init();
	
			return ve;
		} catch( Exception e) {
			logger.error( "Error initializing Velocity engine", e);
			
			throw e;
		}
		
	}

	@Override
	public VelocityResult evaluate(Map<String, Object> params, String template, Writer writer, int formId, int actionId) {
		String logTag = createLogTag( getCompanyId(), formId, actionId);
		
		return evaluate( params, template, writer, logTag);
	}
	
	@Override
	public VelocityResult evaluate( Map<String, Object> params, String template, Writer writer, String logTag) {
        VelocityContext context = new VelocityContext(params);
        MessageEventHandlerImpl velocityEH = new MessageEventHandlerImpl(context);
        
        String nonNullTemplate = StringUtils.defaultString( template);
        
		boolean successful = this.engine.evaluate( context, writer, logTag, nonNullTemplate);
		
		return new VelocityResultImpl( successful, velocityEH);
	}
	
	/**
	 * Create log tag depending on given company ID, form ID and action ID.
	 * The log tag is used to identify scripts violating the company context.
	 * 
	 * @param companyId company ID
	 * @param formId form ID
	 * @param actionId action ID
	 * 
	 * @return log tag
	 */
	private String createLogTag( int companyId, int formId, int actionId) {
		StringBuffer buffer = new StringBuffer();
		
		if( companyId != 0) {
			buffer.append( "Company ID ");
			buffer.append( companyId);
		}
		
		if( formId != 0) {
			if( companyId != 0) {
				buffer.append( ", ");
			}
			buffer.append( "form ID ");
			buffer.append( formId);
		}
		
		if( actionId != 0) {
			if( companyId != 0 || formId != 0) {
				buffer.append( ", ");
			}
			buffer.append( "action ID ");
			buffer.append( actionId);
		}
		
		return buffer.toString();
	}
	
	@Override
	public int getCompanyId() {
		return this.contextCompanyId;
	}
}
