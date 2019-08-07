/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import org.agnitas.emm.core.commons.annotations.AnnotationIntrospector;
import org.agnitas.emm.core.commons.annotations.AnnotationIntrospector.ParameterAnnotationDescriptor;
import org.agnitas.emm.core.commons.packages.PackageInclusionChecker;
import org.agnitas.emm.core.velocity.checks.CompanyContextVelocityChecker;
import org.agnitas.emm.core.velocity.checks.VelocityChecker;
import org.agnitas.emm.core.velocity.checks.VelocityCheckerException;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.SecureUberspector;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertySet;

/**
 * Extension of Velocity's SecureUberspector.
 *  
 * This implementation prevents Velocity scripts to break out of its company context.
 * 
 * Due to the architecture of Velocity, this class does not support dependency injection!
 */
public class AgnVelocityUberspector extends SecureUberspector {
	
	/** List of restricted packages. */
	private static final String[] RESTRICTED_PACKAGES = { "java.lang.reflect" };
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( AgnVelocityUberspector.class);
	
	/** Decides which classes to check by this Uberspector. */
	private final PackageInclusionChecker packageChecker;
	
	/** Introspector for extracting specific annotations. */
	private final AnnotationIntrospector annotationIntrospector;

	/** Checker for company context. */
	private final VelocityChecker companyContextChecker;

	/** Company ID for working context. */
	private final int contextCompanyId;
	
	
	/**
	 * Creates a new AgnVelocityUberspector.
	 * 
	 * @param contextCompanyId company ID that executes the Velocity script
	 * @param companyContextChecker checker that verifies, that the script does not access company IDs out of its context
	 */
	public AgnVelocityUberspector( int contextCompanyId, CompanyContextVelocityChecker companyContextChecker) {
		this.packageChecker = createPackageInclusionChecker();
		this.annotationIntrospector = new AnnotationIntrospector();
		this.companyContextChecker = companyContextChecker;
		this.contextCompanyId = contextCompanyId;
	}
	
	/**
	 * Creates a new {@link PackageInclusionChecker}. This decides, which class accesses are
	 * to be checked by the Uberspector.
	 * 
	 * @return instance of {@link PackageInclusionChecker}
	 */
	protected PackageInclusionChecker createPackageInclusionChecker() {
		return new VelocityCheckPackageInclusionCheckerImpl();
	}

	@Override
	public VelMethod getMethod(Object obj, String methodName, Object[] args, Info info) throws Exception {
		
		if( isRuntimeCheckEnabled()) {
			checkRestrictedPackage( obj);

			if( this.packageChecker.includePackage( obj.getClass().getPackage())) {
				checkMethodAccess( obj, methodName, args, info);
			}
		}
		
		return super.getMethod(obj, methodName, args, info);
	}

	@Override
	public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info info) throws Exception {
		
		if( isRuntimeCheckEnabled()) {
			checkRestrictedPackage( obj);
	
			if( this.packageChecker.includePackage( obj.getClass().getPackage())) {
				checkPropertyWriteAccess( obj, identifier, arg, info);
			}
		}
		
		return super.getPropertySet(obj, identifier, arg, info);
	}

	/*
	@Override
	public VelPropertyGet getPropertyGet(Object obj, String identifier, Info info) throws Exception {
		checkRestrictedPackage( obj);
		
		return super.getPropertyGet(obj, identifier, info);
	}
	*/

	/**
	 * Checks the method call from Velocity script.
	 * 
	 * @param callee objects thats method is called 
	 * @param methodName name of the called method
	 * @param args arguments
	 * @param info information on template
	 * 
	 * @throws VelocityCheckerException 
	 */
	private void checkMethodAccess( Object callee, String methodName, Object[] args, Info info) {
		if( callee != null) {
			Method method = introspector.getMethod( callee.getClass(), methodName, args);
			
			if( method != null) {
				checkMethodAccess( method, args, info);
			} else {
				if( logger.isInfoEnabled())
					logger.info( "No matching method name " + methodName + " found for given argument types - skipping checks");
			}
		} else {
			try {
				throw new RuntimeException( "Cannot check null reference");
			} catch( RuntimeException e) {
				logger.warn( "Performing access check on null reference", e);
			}
		}
	}
	
	/**
	 * Checks the write access to a property from Velocity script.
	 * 
	 * @param callee objects thats property is set 
	 * @param propertyName name of the accessed property
	 * @param arg value to be set
	 * @param info information on template
	 * 
	 * @throws IntrospectionException on error introspecting the callee 
	 * @throws VelocityCheckerException 
	 */
	private void checkPropertyWriteAccess( Object callee, String propertyName, Object arg, Info info) throws IntrospectionException {
		PropertyDescriptor descriptor = new PropertyDescriptor( propertyName, callee.getClass());
		Method setterMethod = descriptor.getWriteMethod();
		
		checkMethodAccess( setterMethod, new Object[]{ arg }, info);
	}
	
	/**
	 * Checks method call or property accesses (which are performed by calling getter or setter).
	 * 
	 * @param method called method
	 * @param args arguments
	 * @param info information on template
	 * 
	 * @throws VelocityCheckerException 
	 */
	private void checkMethodAccess( Method method, Object[] args, Info info) {
		List<ParameterAnnotationDescriptor> list = annotationIntrospector.getParameterAnnotation( method, VelocityCheck.class);
		
		if( logger.isInfoEnabled()) {
			logger.info( "Check access for method " + method.getName());
			for( ParameterAnnotationDescriptor pad : list) {
				logger.info( "Annotation for parameter " + pad.getParameterIndex() + " is " + pad.getAnnotation().annotationType().getName());
			}
			
		}

		try {
			performChecksOnParameters( method, args, list);
		} catch( VelocityCheckerException e) {
			logger.fatal( "Runtime check of Velocity script failed! (" + info + ")", e);
			
			if( isAbortScriptsEnabled()) {
				logger.info( "Aborting script by exception");
				throw new MethodInvocationException( "Aborting Velocity script", e, method.getName(), info.getTemplateName(), info.getLine(), info.getColumn());
			}
		} 
	}
	
	/**
	 * Perform different checks on method parameters.
	 * 
	 * @param method called method
	 * @param args arguments to method call
	 * @param parameterDescriptorList descriptors for method parameters
	 * 
	 * @throws VelocityCheckerException on failed checks
	 */
	private void performChecksOnParameters( Method method, Object[] args, List<ParameterAnnotationDescriptor> parameterDescriptorList) throws VelocityCheckerException {
		
		for( ParameterAnnotationDescriptor pad : parameterDescriptorList) {
			if( pad.getAnnotation() instanceof VelocityCheck)
				performChecksOnParameter( method, args[pad.getParameterIndex()], (VelocityCheck) pad.getAnnotation());
			else {
				// That should never occur, due to the previously used AnnotationIntrospector
				logger.error( "Don't known how to handle annotation for Velocity check: " + pad.getAnnotation().getClass().getCanonicalName());
			}
		}
	}
	
	/**
	 * Perform checks on a specific parameter of a method call.
	 * 
	 * @param method called method
	 * @param arg argument
	 * @param annotation VelocityCheck annotation containing information about checks to perform
	 * 
	 * @throws VelocityCheckerException on failed checks
	 */
	private void performChecksOnParameter( Method method, Object arg, VelocityCheck annotation) throws VelocityCheckerException {
		for( CheckType checkType : annotation.value()) {
			performCheckOnParameter( method, arg, checkType);
		}
	}
	
	/**
	 * Perform a single check on a specific method parameter.
	 * 
	 * @param method called method
	 * @param arg argument to check
	 * @param checkType check to perform
	 * 
	 * @throws VelocityCheckerException on failed check
	 */
	private void performCheckOnParameter( Method method, Object arg, CheckType checkType) throws VelocityCheckerException {
		switch( checkType) {
		case COMPANY_CONTEXT:
			this.companyContextChecker.performCheck( method, arg, checkType, this.contextCompanyId);
			break;
			
		default:
			logger.warn( "Unhandled Velocity check: " + checkType);
			break;
		}
	}
	
	/**
	 * Checks, if type of given objects resides in a restricted package.
	 * 
	 * @param object object to check
	 */
	protected void checkRestrictedPackage( Object object) {
		checkRestrictedPackage( object.getClass());
	}
	
	/**
	 * Checks, if given class resides in a restricted package.
	 * 
	 * @param clazz class to check
	 */
	protected void checkRestrictedPackage( Class<?> clazz) {
		checkRestrictedPackage( clazz.getPackage());
	}
	
	/**
	 * Checks, if given package has restricted access.
	 * 
	 * @param pack package to check
	 */
	protected void checkRestrictedPackage( Package pack) {
		String name = pack.getName();
		
		for( String packageName : RESTRICTED_PACKAGES)
			if( packageName.equals( name) || name.startsWith( packageName + ".")) {
				logger.warn( "Access denied to restricted package: " + name);
				throw new RuntimeException( "Access denied to restricted package " + name);
			}
	}
	
	/**
	 * Checks, if runtime checks of Velocity scripts are enabled.
	 * 
	 * @return true, if runtime checks are enabled
	 */
	protected boolean isRuntimeCheckEnabled() {
		return true;
	}
	
	/**
	 * Checks, if scripts that violated company context should be aborted.
	 * 
	 * @return true, if scipts should be aborted
	 */
	protected boolean isAbortScriptsEnabled() {
		return true;
	}
	
	/**
	 * Returns the company ID that runs the Velocity script.
	 * 
	 * @return company ID running Velocity script
	 */
	public int getContextCompanyId() {
		return this.contextCompanyId;
	}
}


