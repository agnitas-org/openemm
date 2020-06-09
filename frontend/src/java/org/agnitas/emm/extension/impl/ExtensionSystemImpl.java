/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.agnitas.emm.extension.EmmFeatureExtension;
import org.agnitas.emm.extension.ExtensionManager;
import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.JspExtension;
import org.agnitas.emm.extension.PluginContext;
import org.agnitas.emm.extension.PluginFilenameFilter;
import org.agnitas.emm.extension.PluginInstaller;
import org.agnitas.emm.extension.ResourceBundleManager;
import org.agnitas.emm.extension.dao.PluginDao;
import org.agnitas.emm.extension.data.PluginData;
import org.agnitas.emm.extension.data.PluginDetail;
import org.agnitas.emm.extension.data.PluginStatus;
import org.agnitas.emm.extension.data.PluginStatusReport;
import org.agnitas.emm.extension.data.impl.PluginDataImpl;
import org.agnitas.emm.extension.data.impl.PluginDetailImpl;
import org.agnitas.emm.extension.data.impl.PluginStatusImpl;
import org.agnitas.emm.extension.data.impl.PluginStatusReportImpl;
import org.agnitas.emm.extension.exceptions.DatabaseScriptException;
import org.agnitas.emm.extension.exceptions.ExtensionException;
import org.agnitas.emm.extension.exceptions.JspExtensionException;
import org.agnitas.emm.extension.exceptions.MissingPluginManifestException;
import org.agnitas.emm.extension.exceptions.PluginInstantiationException;
import org.agnitas.emm.extension.exceptions.RemovingSystemPluginNotAllowedException;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;
import org.agnitas.emm.extension.util.ExtensionConstants;
import org.agnitas.emm.extension.util.ExtensionUtils;
import org.agnitas.emm.extension.util.I18NFactory;
import org.agnitas.emm.extension.util.I18NResourceBundle;
import org.apache.log4j.Logger;
import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.standard.StandardPluginLocation;
import org.springframework.context.ApplicationContext;

/**
 * ExtensionSystem. Encapsulates the whole logic to load, locate and invoke plugins.
 * 
 * This class is automatically instantiated once at start-up of the web application.
 * 
 * <b>Do not instantiate this class. To get the instance, use ExtensionUtils.getExtensionSystem(ServletContext).</b>
 * 
 * @see ExtensionUtils#getExtensionSystem(javax.servlet.ServletContext)
 */
public class ExtensionSystemImpl implements ExtensionSystem {

	/** Logger. */
	private static final transient Logger logger = Logger.getLogger(ExtensionSystemImpl.class);
	
	/** ID of core plugin */
	private static final String CORE_PLUGIN_ID = "core";
	
	/** JPFs ObjectFactory. */
	private final ObjectFactory objectFactory;
	
	/** JPFs PluginManager. */
	private final PluginManager pluginManager;
	
	/** Manager for JspExtensions. */
	private final ExtensionManager<JspExtension> jspExtensionManager;
	
	/** Manager for EmmFeatureExtensions. */
	private final ExtensionManager<EmmFeatureExtension> featureExtensionManager;
	
	/** Cache for ResourceBundles */
	private final ResourceBundleManager resourceBundleManager;

	private final LocationTracker locationTracker;
	
	private final PluginInstaller pluginInstaller;
	
	private final JspRestoreUtil jspRestoreUtil;
	
	private final ExtensionSystemConfiguration configuration;
	
	private final PluginDao pluginDao;
	
	/**
	 * Instantiates the ExtensionSystem.
	 * 
	 * Do not instantiate the class yourself. This is done at start-up.
	 * 
	 * @param configuration
	 * @param jspRestoreUtil
	 * @param pluginInstaller
	 * @param pluginDao
	 *
	 * @see ExtensionUtils#getExtensionSystem(javax.servlet.ServletContext)
	 */
	public ExtensionSystemImpl(ExtensionSystemConfiguration configuration, JspRestoreUtil jspRestoreUtil, PluginInstaller pluginInstaller, PluginDao pluginDao) {
		if(logger.isInfoEnabled()) {
			logger.info("creating extension system");
		}

		this.configuration = configuration;
				
		this.objectFactory = ObjectFactory.newInstance();
		this.pluginManager = objectFactory.createManager();
		
		this.jspExtensionManager = new ExtensionManager<>(this.pluginManager);
		this.featureExtensionManager = new ExtensionManager<>(this.pluginManager);
		this.resourceBundleManager = new ResourceBundleManager(this);
		this.locationTracker = new LocationTracker();
		this.jspRestoreUtil = jspRestoreUtil;
		
		this.pluginInstaller = pluginInstaller;
		this.pluginDao = pluginDao;
	}
	
	/**
	 * Starts up the extension system. Loads all plugins and activate them.
	 * 
	 * @throws JpfException on errors during start up
	 */
	public void startUp() throws JpfException {
		if(logger.isInfoEnabled()) {
			logger.info("startup of extension system");
		}
		
		initializeExtensionSystem();
		restorePluginJsps();
		
		if(logger.isInfoEnabled()) {
			logger.info("extension system is UP");
		}
	}
	
	private void restorePluginJsps() {
		Collection<PluginDescriptor> plugins = getPluginRegistry().getPluginDescriptors();
		
		for(PluginDescriptor descriptor : plugins) {
			if(logger.isInfoEnabled()) {
				logger.info("Restoring JSPs for plugin " + descriptor.getId());
			}
			
			try {
				this.jspRestoreUtil.createWorkingJspsFromBackupDirectory(descriptor.getId());
			} catch(Exception e) {
				logger.error("Error restoring plugin JSPs", e);
			}
		}
	}
		
	/**
	 * Initializes the ExtensionSystem instance. Loads and activates plugins.
	 * 
	 * @throws JpfException
	 */
	private void initializeExtensionSystem() throws JpfException {
		registerAvailablePlugins();
		
		activatePlugins();
	}
	
	/**
	 * Looks for plugins in the plugin directory and tries to load the plugins.
	 * Loading process is continued with the next plugin in the list, when loading of a plugin fails.
	 * 
	 * @throws JpfException on registering plugins
	 */
	private void registerAvailablePlugins() throws JpfException {
		if(logger.isInfoEnabled()) {
			logger.info("Registering system plugins");
		}
		registerAvailablePlugins(this.configuration.getSystemPluginsBaseDirectory(), true);
		
		if(logger.isInfoEnabled()) {
			logger.info("Registering additional plugins");
		}
		registerAvailablePlugins(this.configuration.getPluginsBaseDirectory(), false);
	}
	
	private void registerAvailablePlugins(String path, boolean isSystemPath) throws JpfException {
		File directory = new File(path);
		File[] files = directory.listFiles(new PluginFilenameFilter());
		
		Collection<PluginLocation> locations = new Vector<>();
		
		if(files != null) {
			for(File file : files) {
				try {
					if(file.isFile()) {
						if(logger.isInfoEnabled()) {
							logger.info("found archive file: " + file.getAbsolutePath());
						}
						
						PluginLocation location = StandardPluginLocation.create(file);
						if(logger.isDebugEnabled()) {
							logger.debug("plugin location is " + location);
						}
	
						locations.add(location);
					} else {
						PluginLocation location = processDirectory(file);
						
						if(location != null) {
							if(logger.isInfoEnabled()) {
								logger.info("found plugin location: " + file.getAbsolutePath());
							}
							
							locations.add(location);
						}
					}
				} catch (MalformedURLException e) {
					logger.warn("wrong plugin file: " + file.getAbsolutePath(), e);
				}
			}
		}
		
		publishPlugins(locations, isSystemPath);
	}
	
	private void registerLocations(Collection<PluginLocation> locations, boolean systemPlugins) {
		for(PluginLocation location : locations) {
			this.locationTracker.registerLocation(location, systemPlugins);
		}
	}
	
	private void publishPlugins(Collection<PluginLocation> locations, boolean systemPlugins) throws JpfException {
		PluginLocation[] pluginLocationsArray = locations.toArray(new PluginLocation[locations.size()]);
		
		if(logger.isInfoEnabled()) {
			logger.info("Publishing plugins");
		}
		
		@SuppressWarnings("unused")
		Map<String, Identity> map = pluginManager.publishPlugins(pluginLocationsArray);
		
		if(logger.isInfoEnabled()) {
			logger.info("Plugins published");
		}
		registerLocations(locations, systemPlugins);
	}
	
	private void publishPlugin(PluginLocation location) throws JpfException {
		if(logger.isInfoEnabled()) {
			logger.info("Publishing single plugin");
		}
		
		this.pluginManager.publishPlugins(new PluginLocation[] { location });
		this.locationTracker.registerLocation(location, false);
		
		if(logger.isInfoEnabled()) {
			logger.info("Plugin published");
		}
	}
	
	/**
	 * Post-order traversal of directory tree to find a plugin not archived in a single file.
	 * For each directory, the methods tries to create a PluginLocation and stops, if the attempt
	 * is successful. Otherwise the subdirectories are processed.
	 * If no plugin was found, null is returned.
	 * 
	 * @param directory directory to search in
	 * 
	 * @return PluginLocation location of the plugin or null, if no plugin was found.
	 */
	private PluginLocation processDirectory(File directory) {
		PluginLocation pluginLocation = null;
		
        try {
            pluginLocation = StandardPluginLocation.create(directory);
            
            // No plugin found?
            if (pluginLocation == null) {
            	// Get directory content...
                File[] files = directory.listFiles();
                for(File file : files) {
                	// ... and traverse sub-directories.
                    if(file.isDirectory()) {
                        pluginLocation = processDirectory(file);
                        
                        if(pluginLocation != null) {
							break;
						}
                    }
                }
            }

        } catch(MalformedURLException e) {
            return null;
        }
        
        return pluginLocation;
	}
	
	/**
	 * Tries to active all loaded plugins.
	 */
	private void activatePlugins() {
		Collection<PluginDescriptor> descriptors = getPluginRegistry().getPluginDescriptors();
		
		for(PluginDescriptor descriptor : descriptors) {
			try {
				// Check DB if plugin is to be enabled at startup
				if(checkActivationOnStartup(descriptor.getId())) {
					if(logger.isInfoEnabled()) {
						logger.info("Activating plugin " + descriptor.getId());
					}
					
					activatePlugin(descriptor);
				} else {
					if(logger.isInfoEnabled()) {
						logger.info("Do not activate plugin " + descriptor.getId() + " due to startup-configuration");
					}
				}
			} catch (PluginLifecycleException | PluginInstantiationException e) {
				logger.error("Error activating plugin: " + descriptor.getPluginClassName(), e);
			}
		}
	}
	
	/**
	 * Active a single plugin.
	 * 
	 * @param pluginDescriptor plugin to active
	 * 
	 * @throws PluginLifecycleException on errors activating the plugin
	 * @throws PluginInstantiationException on errors creating a plugin instance
	 */
	private void activatePlugin(PluginDescriptor pluginDescriptor) throws PluginLifecycleException, PluginInstantiationException {
		if(logger.isInfoEnabled()) {
			logger.info("Activating plugin: " + pluginDescriptor.getId());
		}
		
		pluginManager.activatePlugin(pluginDescriptor.getId());
	}
	
	@Override
	public void invokeJspExtension(String pluginName, String extensionPointName, PageContext pageContext) {
		Collection<Extension> extensions = getActiveExtensions(pluginName, extensionPointName);
		
		JspExtension jspExtension;
		
		for(Extension extension : extensions) {
			try {
				jspExtension = this.jspExtensionManager.getOrCreateExtensionInstance(extension);
				jspExtension.invoke(extension, pageContext);
			} catch (JspExtensionException | PluginInstantiationException e) {
				logger.error("Error occurred: " + e.getMessage(), e);
			}
		}
	}

	// TODO: Uses ApplicationContext in parameter list. PoC only!
	@Override
	public void invokeFeatureExtension(String pluginId, ApplicationContext context, HttpServletRequest request, HttpServletResponse response) throws PluginInstantiationException, ExtensionException, UnknownPluginException {
		Collection<Extension> extensions = getActiveExtensions(getCorePluginId(), "featurePlugin");
		
		EmmFeatureExtension featureExtension = null;
		Extension extension = null;
		
		for(Extension extension0 : extensions) {
			if(extension0.getId().equals(pluginId)) {
				extension = extension0;
				break;
			}
		}

		if(extension != null) {
			featureExtension = this.featureExtensionManager.getOrCreateExtensionInstance(extension);
			PluginContext pluginContext = new PluginContextImpl(extension.getDeclaringPluginDescriptor().getId(), request, response);
			featureExtension.invoke(pluginContext, extension, context);
		} else {
			logger.warn("Unknown plugin to invoke: " + pluginId);
			
			throw new UnknownPluginException(pluginId);
		}
	}
	
	// TODO: Uses ApplicationContext in parameter list. PoC only!
	@Override
	public void invokeFeatureSetupExtension(String pluginId, ApplicationContext context, HttpServletRequest request, HttpServletResponse response) throws PluginInstantiationException, ExtensionException, UnknownPluginException {
		Collection<Extension> extensions = getActiveExtensions(getCorePluginId(), "featurePlugin");
		
		EmmFeatureExtension featureExtension = null;
		Extension extension = null;
		
		for(Extension extension0 : extensions) {
			if(extension0.getId().equals(pluginId)) {
				extension = extension0;
				break;
			}
		}

		if(extension != null) {
			featureExtension = this.featureExtensionManager.getOrCreateExtensionInstance(extension);
			PluginContext pluginContext = new PluginContextImpl(extension.getDeclaringPluginDescriptor().getId(), request, response);
			
			request.setAttribute("agnPluginId", extension.getDeclaringPluginDescriptor().getId());
			request.setAttribute("agnExtensionId", extension.getId());
			
			featureExtension.setup(pluginContext, extension, context);
		} else {
			logger.warn("Unknown plugin to setup: " + pluginId);
			
			throw new UnknownPluginException(pluginId);
		}
	}
	
	@Override
	public Collection<Extension> getActiveExtensions(String plugin, String extensionPoint) {
		ExtensionPoint point = getPluginRegistry().getExtensionPoint(plugin, extensionPoint);
		Collection<Extension> connectedExtensions = point.getConnectedExtensions();
		Collection<Extension> activeExtensions = new Vector<>();
		
		for(Extension extension : connectedExtensions) {
			if(this.pluginManager.isPluginActivated(extension.getDeclaringPluginDescriptor())) {
				activeExtensions.add(extension);
			}
		}
		
		return activeExtensions;
	}
	
	@Override
	public InputStream getPluginResource(String plugin, String resource) {
		PluginDescriptor pluginDescriptor = getPluginRegistry().getPluginDescriptor(plugin);
		PluginClassLoader classLoader = this.pluginManager.getPluginClassLoader(pluginDescriptor);
		return classLoader.getResourceAsStream(resource);
	}
	
	@Override
	public I18NResourceBundle getPluginI18NResourceBundle(String plugin) {
		PluginDescriptor pluginDescriptor = getPluginRegistry().getPluginDescriptor(plugin);
		PluginAttribute attribute = pluginDescriptor.getAttribute("i18n-bundle");
		I18NResourceBundle bundle = null;
		
		if(attribute != null) {
			String bundleName = attribute.getValue();

			I18NFactory factory = new I18NFactory(pluginManager.getPluginClassLoader(pluginDescriptor), bundleName);
			bundle = new I18NResourceBundle(factory);
		}
		
		return bundle;
	}
	
	@Override
	public ResourceBundle getPluginResourceBundle(String plugin, String bundleName) throws Exception {
		return this.resourceBundleManager.getResourceBundle(plugin, bundleName);
	}

	@Override
	public Extension getExtension(String plugin, String extension) {
		return getPluginRegistry().getPluginDescriptor(plugin).getExtension(extension);
	}
	
	public void shutdown() {
		this.pluginManager.shutdown();
	}
	
	@Override
	public PluginStatusReport getPluginStatusReport() {
		Map<URL, PluginStatus> pluginStatusItemMap = createStatusMapFromPluginLocations();
		Collection<PluginDescriptor> descriptors = getPluginRegistry().getPluginDescriptors();
		
		setPluginStatusInformationsInMap(descriptors, pluginStatusItemMap);
		
		return convertMapToPluginStatusReport(pluginStatusItemMap);
	}
	
	@Override
	public PluginDetail getPluginDetails(String pluginID) throws UnknownPluginException {
		PluginDescriptor pluginDescriptor = getPluginRegistry().getPluginDescriptor(pluginID);
		
		if(pluginDescriptor == null) {
			logger.warn("No plugin with ID " + pluginID + " found");
			throw new UnknownPluginException(pluginID);
		}
		
		PluginDetailImpl pluginDetail = new PluginDetailImpl();
		setPluginStatusInformation(pluginDetail, pluginDescriptor);
		setPluginDetailInformation(pluginDetail, pluginDescriptor);
		
		return pluginDetail;
	}
	
	private Map<URL, PluginStatus> createStatusMapFromPluginLocations() {
		Collection<PluginLocation> pluginLocations = locationTracker.getPluginLocations();
		Map<URL, PluginStatus> map = new HashMap<>();
		
		for(PluginLocation location : pluginLocations) {
			if(logger.isDebugEnabled()) {
				logger.debug("Tracked plugin location: " + location);
			}
			
			PluginStatusImpl item = new PluginStatusImpl();
			item.setUrl(location.getContextLocation());
			
			map.put(location.getManifestLocation(), item);
		}
		
		return map;
	}
	
	private void setPluginStatusInformationsInMap(Collection<PluginDescriptor> descriptors, Map<URL, PluginStatus> pluginStatusItemMap) {
		for(PluginDescriptor descriptor : descriptors) {
			PluginStatus item = pluginStatusItemMap.get(descriptor.getLocation());
			
			setPluginStatusInformation(item, descriptor);
		}
	}
	
	private void setPluginStatusInformation(PluginStatus pluginStatus, PluginDescriptor pluginDescriptor) {
		if(logger.isDebugEnabled()) {
			logger.debug("Descriptor location: " + pluginDescriptor.getLocation());
		}

		PluginAttribute nameAttribute = pluginDescriptor.getAttribute(ExtensionConstants.PLUGIN_NAME_MANIFEST_ATTRIBUTE);
		PluginAttribute descriptionAttribute = pluginDescriptor.getAttribute(ExtensionConstants.PLUGIN_DESCRIPTION_MANIFEST_ATTRIBUTE);

		PluginStatusImpl pluginStatusImp = (PluginStatusImpl) pluginStatus;
		
		pluginStatusImp.setPluginId(pluginDescriptor.getId());
		pluginStatusImp.setVersion(pluginDescriptor.getVersion().toString());
		pluginStatusImp.setVendor(pluginDescriptor.getVendor());
		pluginStatusImp.setPluginName(nameAttribute != null ? nameAttribute.getValue() : null);
		pluginStatusImp.setDescription(descriptionAttribute != null ? descriptionAttribute.getValue() : null);
		pluginStatusImp.setActivated(this.pluginManager.isPluginActivated(pluginDescriptor));
	}
	
	private void setPluginDetailInformation(PluginDetailImpl pluginDetail, PluginDescriptor pluginDescriptor) {
		Collection<PluginDescriptor> dependingPlugins = getPluginRegistry().getDependingPlugins(pluginDescriptor);
		Collection<String> dependingPluginIds = new Vector<>();
		for(PluginDescriptor dependingPlugin : dependingPlugins) {
			dependingPluginIds.add(dependingPlugin.getId());
		}
		
		pluginDetail.setDependingPluginIds(dependingPluginIds);
		pluginDetail.setSystemPlugin(isSystemPlugin(pluginDescriptor));
	}
	
	private PluginStatusReport convertMapToPluginStatusReport(Map<URL, PluginStatus> map) {
		return new PluginStatusReportImpl(map.values());
	}

	/**
	 * Utility method to return assigned plugin registry.
	 * 
	 * @return PluginRegistry
	 */
	private PluginRegistry getPluginRegistry() {
		return this.pluginManager.getRegistry();
	}
	
	@Override
	public void activatePluginForStartup(String pluginId) throws PluginLifecycleException {
		activatePlugin(pluginId);
		
		PluginData pluginData;
		
		try {
			pluginData = this.pluginDao.getPluginData(pluginId);
		} catch(UnknownPluginException e) {
			pluginData = createPluginData(pluginId);
		}

		pluginData.setActivatedOnStartup(true);
		
		pluginDao.savePluginData(pluginData);
	}
	
	private void activatePlugin(String pluginId) throws PluginLifecycleException {
		this.pluginManager.activatePlugin(pluginId);
	}
	
	@Override
	public void deactivatePluginForStartup(String pluginId) {
		deactivatePlugin(pluginId);
		
		PluginData pluginData;
		
		try {
			pluginData = this.pluginDao.getPluginData(pluginId);
		} catch(UnknownPluginException e) {
			pluginData = createPluginData(pluginId);
		}

		pluginData.setActivatedOnStartup(false);
		
		pluginDao.savePluginData(pluginData);
	}
	
	private void deactivatePlugin(String pluginId) {
		PluginDescriptor descriptor = getPluginRegistry().getPluginDescriptor(pluginId);
		
		this.pluginManager.deactivatePlugin(pluginId);

		if(descriptor != null) {
			this.jspExtensionManager.deregisterExtensions(descriptor.getExtensions());
			this.featureExtensionManager.deregisterExtensions(descriptor.getExtensions());
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("No descriptor found for plugin '" + pluginId + "'???");
			}
		}
	}
	
	private boolean checkActivationOnStartup(String pluginId) {
		try {
			PluginData pluginData = this.pluginDao.getPluginData(pluginId);
			
			return pluginData.isActivatedOnStartup();
		} catch(UnknownPluginException e) {
			if(logger.isInfoEnabled()) {
				logger.info("No informations on plugin '" + pluginId + "' found. No activation on startup");
			}
			
			return false;
		}
	}
	
	private PluginData createPluginData(String pluginId) {
		PluginData pluginData = new PluginDataImpl();
		
		pluginData.setPluginId(pluginId);
		
		return pluginData;
	}

	@Override
	public void installPlugin(String pluginFilename) throws MissingPluginManifestException, IOException, JpfException, DatabaseScriptException {
		String pluginId = this.pluginInstaller.installPlugin(pluginFilename);
		
		File directory = new File(this.configuration.getPluginDirectory(pluginId));
		logger.debug("Activating installed plugin at " + directory.getAbsolutePath());
		
		PluginLocation location = StandardPluginLocation.create(directory);

		try {
			this.deactivatePlugin(pluginId); // Try to deactive active plugin
			
			logger.info("plugin '" + pluginId + "' successfully deactivated before re-installing plugin");
		} catch(IllegalArgumentException e) {
			logger.info("error deactivating plugin '" + pluginId + "' - plugin may not be active or installed");
		}
		
		this.getPluginRegistry().unregister(new String[]{pluginId}); // Try to remove previously installed plugin
		this.publishPlugin(location);
	}
	
	@Override
	public void uninstallPlugin(String pluginId) throws RemovingSystemPluginNotAllowedException {
		// TODO: Prevent system plugins from removal
		PluginDescriptor descriptor = this.getPluginRegistry().getPluginDescriptor(pluginId);
		
		if(isSystemPlugin(descriptor)) {
			logger.warn("Attempt to uninstall system plugin: " + pluginId);
			
			throw new RemovingSystemPluginNotAllowedException(pluginId);
		}
		
		this.locationTracker.unregisterLocation(descriptor);

		this.getPluginRegistry().unregister(new String[] { pluginId });
		this.pluginInstaller.uninstallPlugin(pluginId);
		
		try {
			this.pluginDao.removePluginData(pluginId);
		} catch(Exception e) {
			logger.warn("Error removing plugin data", e);
		}
	}
	
	protected String getCorePluginId() {
		return CORE_PLUGIN_ID;
	}
	
	@Override
	public boolean isSystemPlugin(String pluginId) {
		PluginDescriptor descriptor = this.getPluginRegistry().getPluginDescriptor(pluginId);
		
		return isSystemPlugin(descriptor);
	}
	
	public boolean isSystemPlugin(PluginDescriptor descriptor) {
		return this.locationTracker.isSystemPlugin(descriptor);
	}
	
	@Override
	public boolean isPluginExist(String pluginId) {
		try {
			return Objects.nonNull(getPluginRegistry().getPluginDescriptor(pluginId));
		} catch (Exception e) {
			return false;
		}
	}
}
