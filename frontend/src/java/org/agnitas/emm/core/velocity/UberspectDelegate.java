/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.ContextAware;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;

/**
 * Auxiliary class to delegate method calls to {@link Uberspect} to wrapped instance.
 * This class is required to allow dependency injection by Spring.
 */
public class UberspectDelegate implements Uberspect, RuntimeServicesAware, ContextAware {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( UberspectDelegate.class);
	
	/** Name of property holding delegate target. */
	public static final String DELEGATE_TARGET_PROPERTY_NAME = "org.agnitas.emm.core.velocity.UberspectDelegate.DELEGATE_TARGET";
	
	/** {@link Uberspect} defined as delegate target. */
	private Uberspect uberspector;
	
	/** Runtime services. */
	private RuntimeServices runtimeServices;
	
	/** Context. */
	private Context context;
	
	@Override
	public Iterator<?> getIterator(Object obj, Info info) throws Exception {
		return uberspector.getIterator(obj, info);
	}

	@Override
	public VelMethod getMethod(Object obj, String method, Object[] args, Info info) throws Exception {
		return uberspector.getMethod( obj, method, args, info);
	}

	@Override
	public VelPropertyGet getPropertyGet(Object obj, String identifier, Info info) throws Exception {
		return uberspector.getPropertyGet( obj, identifier, info);
	}

	@Override
	public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info info) throws Exception {
		return uberspector.getPropertySet( obj, identifier, arg, info);
	}

	@Override
	public void init() throws Exception {
		try {
			this.uberspector = (Uberspect) runtimeServices.getProperty( DELEGATE_TARGET_PROPERTY_NAME);
		} catch( ClassCastException e) {
			logger.error( "Cannot cast delegate target to " + Uberspect.class.getCanonicalName(), e);
			
			throw e;
		}
			
		if( this.uberspector == null) {
			logger.warn( "No delegate target defined");
			
			throw new RuntimeException( "No delegate target defined");
		} else {
			if( logger.isDebugEnabled()) {
				logger.debug( "Delegate target defined. Class is " + this.uberspector.getClass().getCanonicalName());
			}
			
			initUberspectTarget( this.uberspector);
		}
	}
	
	/**
	 * Initialize the delegate target.
	 * 
	 * @param uberspectorTarget target to initialize
	 * 
	 * @throws Exception on error initializing the target
	 */
	private void initUberspectTarget( Uberspect uberspectorTarget) throws Exception {
		// Call methods of Aware-interfaces
		if( uberspectorTarget instanceof RuntimeServicesAware) {
			((RuntimeServicesAware) uberspectorTarget).setRuntimeServices( runtimeServices);
		}
		if( uberspectorTarget instanceof ContextAware) {
			((ContextAware) uberspectorTarget).setContext( context);
		}

		uberspectorTarget.init();
	}

	@Override
	public void setRuntimeServices(RuntimeServices runtimeServices) {
		this.runtimeServices = runtimeServices;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}
}
