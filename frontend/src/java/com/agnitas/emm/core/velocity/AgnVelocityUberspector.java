/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import org.agnitas.emm.core.commons.annotations.AnnotationIntrospector;
import org.agnitas.emm.core.commons.annotations.AnnotationIntrospector.ParameterAnnotationDescriptor;
import org.agnitas.emm.core.commons.packages.PackageInclusionChecker;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.CheckType;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.emm.core.velocity.checks.CompanyContextVelocityChecker;
import org.agnitas.emm.core.velocity.checks.VelocityChecker;
import org.agnitas.emm.core.velocity.checks.VelocityCheckerException;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.SecureUberspector;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertySet;

import com.agnitas.emm.core.JavaMailService;

/**
 * Implementation of {@link AgnVelocityUberspector} checking OpenEMM and EMM
 * classes.
 */
@Deprecated // After completion of EMM-8360, this class can be removed without replacement
public class AgnVelocityUberspector extends SecureUberspector {
	/** List of restricted packages. */
	private static final String[] RESTRICTED_PACKAGES = { "java.lang.reflect" };

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(AgnVelocityUberspector.class);

	/** Decides which classes to check by this Uberspector. */
	private final PackageInclusionChecker packageChecker;

	/** Introspector for extracting specific annotations. */
	private final AnnotationIntrospector annotationIntrospector;

	/** Checker for company context. */
	private final VelocityChecker companyContextChecker;

	/** Company ID for working context. */
	private final int contextCompanyId;

	/** Config service. */
	private final ConfigService configService;

	private final JavaMailService javaMailService;

	/*
	 * Instantiates a new Velocity uberspector.
	 *
	 * @param contextCompanyId ID of company executing the script
	 * 
	 * @param checker checker for company ID
	 * 
	 * @param configService config service
	 */
	public AgnVelocityUberspector(int contextCompanyId, CompanyContextVelocityChecker companyContextChecker, ConfigService configService, JavaMailService javaMailService) {
		this.packageChecker = createPackageInclusionChecker();
		this.annotationIntrospector = new AnnotationIntrospector();
		this.companyContextChecker = companyContextChecker;
		this.contextCompanyId = contextCompanyId;
		this.configService = configService;
		this.javaMailService = javaMailService;
	}

	/**
	 * Creates a new {@link PackageInclusionChecker}. This decides, which class
	 * accesses are to be checked by the Uberspector.
	 * 
	 * @return instance of {@link PackageInclusionChecker}
	 */
	protected PackageInclusionChecker createPackageInclusionChecker() {
		return new ComVelocityCheckPackageInclusionCheckerImpl();
	}

	protected boolean isRuntimeCheckEnabled() {
		return configService.isVelocityRuntimeCheckEnabled(getContextCompanyId());
	}

	protected boolean isAbortScriptsEnabled() {
		return configService.isVelocityScriptAbortEnabled(getContextCompanyId());
	}

	@Override
	public VelMethod getMethod(Object obj, String methodName, Object[] args, Info info) {
		if (isRuntimeCheckEnabled()) {
			checkRestrictedPackage(obj);

			if (this.packageChecker.includePackage(obj.getClass().getPackage())) {
				checkMethodAccess(obj, methodName, args, info);
			}
		}

		return super.getMethod(obj, methodName, args, info);
	}

	@Override
	public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info info) {
		if (isRuntimeCheckEnabled()) {
			checkRestrictedPackage(obj);

			if (this.packageChecker.includePackage(obj.getClass().getPackage())) {
				try {
					checkPropertyReadAccess(obj, identifier, arg, info);
				} catch (final IntrospectionException e) {
					final String msg = String.format("Property '%s' of '%s' is not readable" , obj.getClass().getCanonicalName(), identifier);
					logger.warn(msg);

					throw new RuntimeException(msg, e);
				}
			}
		}

		return super.getPropertySet(obj, identifier, arg, info);
	}

	/**
	 * Checks the method call from Velocity script.
	 * 
	 * @param callee
	 *            objects thats method is called
	 * @param methodName
	 *            name of the called method
	 * @param args
	 *            arguments
	 * @param info
	 *            information on template
	 * 
	 * @throws VelocityCheckerException
	 */
	private void checkMethodAccess(Object callee, String methodName, Object[] args, Info info) {
		if (callee != null) {
			Method method = introspector.getMethod(callee.getClass(), methodName, args);

			if (method != null) {
				checkMethodAccess(method, args, info);
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("No matching method name " + methodName + " found for given argument types - skipping checks");
				}
			}
		} else {
			try {
				throw new RuntimeException("Cannot check null reference");
			} catch (RuntimeException e) {
				logger.warn("Performing access check on null reference", e);
			}
		}
	}

	/**
	 * Checks the read access to a property from Velocity script.
	 * 
	 * @param callee
	 *            objects thats property is set
	 * @param propertyName
	 *            name of the accessed property
	 * @param arg
	 *            value to be set
	 * @param info
	 *            information on template
	 * 
	 * @throws IntrospectionException
	 *             on error introspecting the callee
	 * @throws VelocityCheckerException
	 */
	private void checkPropertyReadAccess(Object callee, String propertyName, Object arg, Info info) throws IntrospectionException {
		PropertyDescriptor descriptor = new PropertyDescriptor(propertyName, callee.getClass());
		Method getterMethod = descriptor.getReadMethod();

		checkMethodAccess(getterMethod, new Object[] { arg }, info);
	}

	/**
	 * Checks method call or property accesses (which are performed by calling
	 * getter or setter).
	 * 
	 * @param method
	 *            called method
	 * @param args
	 *            arguments
	 * @param info
	 *            information on template
	 * 
	 * @throws VelocityCheckerException
	 */
	private void checkMethodAccess(Method method, Object[] args, Info info) {
		List<ParameterAnnotationDescriptor> list = annotationIntrospector.getParameterAnnotation(method, VelocityCheck.class);

		if (logger.isInfoEnabled()) {
			logger.info("Check access for method " + method.getName());
			for (ParameterAnnotationDescriptor pad : list) {
				logger.info("Annotation for parameter " + pad.getParameterIndex() + " is " + pad.getAnnotation().annotationType().getName());
			}

		}

		try {
			performChecksOnParameters(method, args, list);
		} catch (VelocityCheckerException e) {
			logger.fatal("Runtime check of Velocity script failed! (" + info + ")", e);

			if (isAbortScriptsEnabled()) {
				logger.info("Aborting script by exception");
				throw new MethodInvocationException("Aborting Velocity script", e, method.getName(), info.getTemplateName(), info.getLine(), info.getColumn());
			}
		}
	}

	/**
	 * Perform different checks on method parameters.
	 * 
	 * @param method
	 *            called method
	 * @param args
	 *            arguments to method call
	 * @param parameterDescriptorList
	 *            descriptors for method parameters
	 * 
	 * @throws VelocityCheckerException
	 *             on failed checks
	 */
	private void performChecksOnParameters(Method method, Object[] args, List<ParameterAnnotationDescriptor> parameterDescriptorList) throws VelocityCheckerException {
		for (ParameterAnnotationDescriptor pad : parameterDescriptorList) {
			if (pad.getAnnotation() instanceof VelocityCheck) {
				performChecksOnParameter(method, args[pad.getParameterIndex()], (VelocityCheck) pad.getAnnotation());
			} else {
				// That should never occur, due to the previously used
				// AnnotationIntrospector
				logger.error("Don't known how to handle annotation for Velocity check: " + pad.getAnnotation().getClass().getCanonicalName());
			}
		}
	}

	/**
	 * Perform checks on a specific parameter of a method call.
	 * 
	 * @param method
	 *            called method
	 * @param arg
	 *            argument
	 * @param annotation
	 *            VelocityCheck annotation containing information about checks
	 *            to perform
	 * 
	 * @throws VelocityCheckerException
	 *             on failed checks
	 */
	private void performChecksOnParameter(Method method, Object arg, VelocityCheck annotation) throws VelocityCheckerException {
		try {
			for (CheckType checkType : annotation.value()) {
				performCheckOnParameter(method, arg, checkType);
			}
		} catch (VelocityCheckerException e) {
			javaMailService.sendEmail(0, configService.getValue(ConfigValue.Mailaddress_Frontend), "Error in Velocity Script", "Error in Velocity Script: " + e.getMessage(), "Error in Velocity Script: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Perform a single check on a specific method parameter.
	 * 
	 * @param method
	 *            called method
	 * @param arg
	 *            argument to check
	 * @param checkType
	 *            check to perform
	 * 
	 * @throws VelocityCheckerException
	 *             on failed check
	 */
	private void performCheckOnParameter(Method method, Object arg, CheckType checkType) throws VelocityCheckerException {
		switch (checkType) {
			case COMPANY_CONTEXT:
				companyContextChecker.performCheck(method, arg, checkType, contextCompanyId);
				break;
	
			default:
				logger.warn("Unhandled Velocity check: " + checkType);
				break;
		}
	}

	/**
	 * Checks, if type of given objects resides in a restricted package.
	 * 
	 * @param object
	 *            object to check
	 */
	protected void checkRestrictedPackage(Object object) {
		checkRestrictedPackage(object.getClass());
	}

	/**
	 * Checks, if given class resides in a restricted package.
	 * 
	 * @param clazz
	 *            class to check
	 */
	protected void checkRestrictedPackage(Class<?> clazz) {
		if(clazz != null) {
			checkRestrictedPackage(clazz.getPackage());
		}
	}

	/**
	 * Checks, if given package has restricted access.
	 * 
	 * @param pack
	 *            package to check
	 */
	protected void checkRestrictedPackage(Package pack) {
		if(pack == null) {
			return;
		}
		
		String name = pack.getName();

		for (String packageName : RESTRICTED_PACKAGES) {
			if (packageName.equals(name) || name.startsWith(packageName + ".")) {
				logger.warn("Access denied to restricted package: " + name);
				throw new RuntimeException("Access denied to restricted package " + name);
			}
		}
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
