/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.agnitas.emm.extension.annotation.DispatchTarget;
import org.agnitas.emm.extension.exceptions.ExtensionException;
import org.agnitas.emm.extension.util.ExtensionUtils;
import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.springframework.context.ApplicationContext;

/**
 * High sophisticated base class for plugins using annotations.
 * 
 * When the plugin extending this class is invoked, the request parameter &quot;method&quot; is read and depending of the
 * annotations, a method is chosed to be invoked.
 * 
 * The decision is done by these properties:
 * <ol>
 *   <li>The method must be annotated with <i>@DispatchTarget</i></li>
 *   <li>2. The annotated method must not be <i>invoke</i>, <i>setup</i>, <i>unknownTarget</i> or <i>unspecifiedTarget</i></li>
 *   <li>3. The method must match this signature:
 *   	<ul>
 *   		<li>Return type is <i>void</i></li>
 *   		<li>First argument type is <i>org.agnitas.emm.extension.PluginContext</i></li>
 *   		<li>Second argument type is <i>org.java.plugin.registry.Extension</i></li>
 *   		<li>Third argument type is <i>org.springframework.context.ApplicationContext</i></li>
 *   	</ul></li>
 *   <li>The value of &quot;name&quot; attribute of the DispatchTarget annotation must match the value &quot;method&quot; request parameter.</li>
 * </ol> 
 * 
 * If the request parameter &quot;method&quot; is not present, the method <i>unspecifiedTarget</i> is invoked.
 * When there is no matching annotation, the method <i>unknownTarget</i> is invoked.
 */
public class AnnotatedDispatchingEmmFeatureExtension implements EmmFeatureExtension {

	private static final transient Logger logger = Logger.getLogger( AnnotatedDispatchingEmmFeatureExtension.class);
	
	private final Map<String, Method> dispatchTargetMap;
	
	public AnnotatedDispatchingEmmFeatureExtension() {
		this.dispatchTargetMap = collectDispatchTargets();
	}
	
	private Map<String, Method> collectDispatchTargets() {
		Map<String, Method> map = new HashMap<>();
		
		Method[] methods = this.getClass().getMethods();
		if( logger.isInfoEnabled())
			logger.debug( "Searching dispatch targets in class " + this.getClass().getCanonicalName());
		
		for( Method method : methods) {
			DispatchTarget annotation = method.getAnnotation( DispatchTarget.class);
			
			if( annotation != null) {
				if( isRestrictedName( method.getName())) {
					throw new RuntimeException( "Cannot use method " + method.getName() + " as dispatch target");
				}
				
    			if( annotation.name() == null || annotation.name().equals( "")) {
    				throw new RuntimeException( "Attribute name not set for DispatchTarget for method " + this.getClass().getCanonicalName() + "." + method.getName());
    			}
    			
    			if( !isValidSignature( method)) {
    				throw new RuntimeException( "Signature of method " + method.getName() + " does not match dispatch target specification");
    			}
    			
    			if( map.containsKey( annotation.name())) {
    				throw new RuntimeException( "Duplicate target name: " + annotation.name());
    			}
    				
    			map.put( annotation.name(), method);
			}
		}
		
		return map;
	}
	
	private boolean isRestrictedName( String name) {
		return name.equals( "invoke") || name.equals( "setup") || name.equals( "unspecifiedTarget") || name.equals( "unknownTarget");
	}
	
	private boolean isValidSignature( Method method) {
		Class<?> expectedSignature[] = new Class<?>[] { PluginContext.class, Extension.class, ApplicationContext.class };
		
		// Check return type
		if( !method.getReturnType().equals( Void.TYPE))
			return false;

		// Check method parameters
		Class<?> parameterTypes[] = method.getParameterTypes();
		if( parameterTypes.length != expectedSignature.length)
			return false;
		for( int i = 0; i < parameterTypes.length; i++) {
			if( !parameterTypes[i].equals( expectedSignature[i]))
				return false;
		}
		return true;
	}
	
	@Override
	public void invoke( PluginContext pluginContext, Extension extension, ApplicationContext context) throws ExtensionException {
		String targetName = ExtensionUtils.getDispatchParameterValue( pluginContext);
		
		if( logger.isDebugEnabled())
			logger.debug( "Dispatch target to invoke: " + targetName);
		
		if( targetName == null) {
			try {
				unspecifiedTarget( pluginContext, extension, context);
			} catch( Throwable t) {
				throw new ExtensionException( t);
			}
		} else {
			Method method = dispatchTargetMap.get( targetName);
			
			if( method == null) {
				try {
					unknownTarget( pluginContext, extension, context, targetName);
				} catch( Throwable t) {
					throw new ExtensionException( t);
				}
			} else {
				try {
					method.invoke( this, pluginContext, extension, context);
				} catch( InvocationTargetException e) {
					logger.error( "Error calling method", e);
				} catch( IllegalArgumentException e) {
					logger.error( "Illegal argument calling method", e);
				} catch( IllegalAccessException e) {
					logger.error( "Access error calling method", e);
				}
			}
		}
	}

	@Override
	public void setup( PluginContext pluginContext, Extension extension, ApplicationContext context) throws ExtensionException {
		// Do nothing
	}

	/**
	 * Method to be called, when the request parameter &quot;method&quot; is not present.
	 * 
	 * @param pluginContext the plugin context
	 * @param extension the extension data
	 * @param context the Spring application context 
	 * 
	 * @throws Throwable
	 */
	public void unspecifiedTarget( PluginContext pluginContext, Extension extension, ApplicationContext context) throws Throwable {
		// Does nothing
	}

	/**
	 * Method to be called, when the value request parameter &quot;method&quot; does not match any annotation.
	 * 
	 * @param pluginContext the plugin context
	 * @param extension the extension data
	 * @param context the Spring application context 
	 * @param targetName of the dispatch target
	 * 
	 * @throws Throwable
	 */
	public void unknownTarget( PluginContext pluginContext, Extension extension, ApplicationContext context, String targetName) throws Throwable {
		// Does nothing		
	}
}
