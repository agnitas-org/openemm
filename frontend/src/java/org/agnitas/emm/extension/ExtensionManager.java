/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.agnitas.emm.extension.exceptions.PluginInstantiationException;
import org.apache.log4j.Logger;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

/**
 * Utility class to encapsulate all logic for managing EMM extension instances to ensure
 * that there is only one instance per Extension instance.
 * 
 * JspExtension and EmmFeatureExtension are two different types of EMM extensions, so each
 * of these needs one ExtensionManager.
 *
 * @param <T> type of plugin
 */
public class ExtensionManager<T> {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ExtensionManager.class);
	
	/** JPFs PluginManager */
	private final PluginManager pluginManager;
	
	/** Instances of plugins for Extension instances */
	private final Map<Extension, T> extensionInstances;
	
	/**
	 * Create a new ExtensionManager.
	 * 
	 * @param pluginManager JPFs PluginManager 
	 */
	public ExtensionManager( PluginManager pluginManager) {
		this.extensionInstances = new HashMap<>();
		this.pluginManager = pluginManager;
	}
	
	/**
	 * Returns an instance of the EMM feature for the given Extension. 
	 * If an EMM feature was previously created for the Extension, this instance is returned. Otherwise, a
	 * new instance is created.
	 * 
	 * @param extension JPF Extension
	 * 
	 * @return instance of EMM extension
	 * 
	 * @throws PluginInstantiationException on errors creating the instance
	 */
	public T getOrCreateExtensionInstance( Extension extension) throws PluginInstantiationException {
		T extensionInstance = this.extensionInstances.get( extension);
		
		if( extensionInstance == null) {
			extensionInstance = createAndRegisterExtensionInstance( extension);
		}
		
		return extensionInstance;
	}
	
	/**
	 * Creates a new instance of the EMM extension for the given JPS extension and registeres this
	 * instance in the internal storage.
	 * 
	 * @param extension JPF Extension
	 * 
	 * @return new EMM extension instance
	 * 
	 * @throws PluginInstantiationException on errors createing the instance
	 */
	private T createAndRegisterExtensionInstance( Extension extension) throws PluginInstantiationException {
		PluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();

		// Do not close retrieved class loader - plugin manager holds it and manages its lifecycle
		// If you close it then neither dependency for extension class will be loaded
		PluginClassLoader pluginClassLoader = pluginManager.getPluginClassLoader(pluginDescriptor);

		String className = extension.getParameter( "class").valueAsString();

		try {
			Class<?> extensionClass = pluginClassLoader.loadClass( className);

			@SuppressWarnings("unchecked")
			T extensionInstance = (T) extensionClass.newInstance();

			this.extensionInstances.put( extension, extensionInstance);

			return extensionInstance;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
			logger.error("Unable to instantiate an extension class", e);
			throw new PluginInstantiationException( e);
		}
	}

	/**
	 * De-register given extensions.
	 * 
	 * @param extensions list of extensions to de-register
	 */
	public void deregisterExtensions(Collection<Extension> extensions) {
		for( Extension extension : extensions)
			this.extensionInstances.remove( extension);
	}

}
