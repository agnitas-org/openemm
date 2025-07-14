/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class JSchUserInfo implements UserInfo, UIKeyboardInteractive {
	private static final transient Logger logger = LogManager.getLogger(JSchUserInfo.class);
	
	private String password = null;

	public JSchUserInfo(String pass) {
		this.password = pass;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return true;
	}

	@Override
	public boolean promptPassword(String message) {
		return true;
	}

	@Override
	public boolean promptYesNo(String str) {
		return true;
	}

	@Override
	public void showMessage(String message) {
		if (logger.isDebugEnabled()) {
			logger.debug("JshUserInfo: " + message);
		}
	}

	@Override
	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
		String[] response = new String[prompt.length];
		for (int i = 0; i < prompt.length; i++) {
			String promptString = prompt[i];
			if (promptString.toLowerCase().contains("password")) {
				response[i] = password;
			} else {
				response[i] = "";
			}
		}
		return response;
	}
}
