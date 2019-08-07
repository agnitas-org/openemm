/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.agnitas.util.ServerCommand;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.dao.ComServerMessageDao;

/**
 * Watchdog which checks interserver communication table for new messages and processes acquired commands
 */
public class ServerMessagesProcessor implements ServletContextListener {

    private static final int INTERVAL = 60000;
    private static final int SECOND = 1000;
    private Timer timer;
    private Date previousRun = new Date();
    private ComServerMessageDao serverMessageDao;
    private RedirectServlet redirectServlet;
    private ShowImageServlet showImageServlet;
    private WebApplicationContext applicationContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        timer = new Timer();
        CheckServerCommand checkServerCommandTask = new CheckServerCommand();
        timer.scheduleAtFixedRate(checkServerCommandTask, INTERVAL, INTERVAL);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        timer.cancel();
    }

    @Required
    public void setServerMessageDao(ComServerMessageDao serverMessageDao) {
        this.serverMessageDao = serverMessageDao;
    }

    @Required
    public void setRedirectServlet(RedirectServlet redirectServlet) {
        this.redirectServlet = redirectServlet;
    }

    @Required
    public void setShowImageServlet(ShowImageServlet showImageServlet) {
        this.showImageServlet = showImageServlet;
    }

    private void ensureParameters() {
        if (serverMessageDao == null) {
            setServerMessageDao((ComServerMessageDao) applicationContext.getBean("ServerMessageDao"));
        }
        if (redirectServlet == null) {
            setRedirectServlet((RedirectServlet) applicationContext.getBean("RedirectServlet"));
        }
        if (showImageServlet == null) {
        	setShowImageServlet((ShowImageServlet) applicationContext.getBean("ShowImageServlet"));
        }
    }

    /**
     * Timer task which eventually checks for commands and executes them.
     */
    private class CheckServerCommand extends TimerTask {

        @Override
        public void run() {
            Date thisRun = new Date();
            ensureParameters();
            previousRun = new Date(thisRun.getTime() - SECOND);
        }
    }
}
