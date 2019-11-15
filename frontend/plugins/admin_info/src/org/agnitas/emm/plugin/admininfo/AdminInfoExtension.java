package org.agnitas.emm.plugin.admininfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.JspExtension;
import org.agnitas.emm.extension.exceptions.JspExtensionException;
import org.agnitas.emm.extension.util.ExtensionUtils;
import org.agnitas.emm.extension.util.I18NResourceBundle;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;

import com.agnitas.beans.ComAdmin;

/**
 * Simple example of a JSP extension.
 */
public class AdminInfoExtension implements JspExtension {

	@Override
	public void invoke( Extension extension, PageContext context) throws JspExtensionException {
		try {
			ComAdmin admin = AgnUtils.getAdmin((HttpServletRequest) context.getRequest());
			ExtensionSystem extensionSystem = ExtensionUtils.getExtensionSystem(context.getSession());
			I18NResourceBundle bundle = extensionSystem.getPluginI18NResourceBundle("admin_info");

			String message = bundle.getMessage("admin.info", admin.getLocale());

			context.getOut().println("<div class=\"grey_box_container\"> \n" +
					                      "<div class=\"grey_box_top\"></div> \n" +
					                      "<div class=\"grey_box_content\"> \n" +
					                           message +
					                      "</div> \n" +
					                      "<div class=\"grey_box_bottom\"></div> \n" +
					                  "</div>");
		} catch (Exception e) {
			Logger.getLogger(getClass()).error(e);

			throw new JspExtensionException(e);
		}
	}

}
