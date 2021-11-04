/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.importvalues;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * Values for importMode property of import profile
 */
public class ImportMode {
	private static final Map<String, ImportMode> SYSTEM_IMPORTMODES = new HashMap<>();
	
	/** Insert only new recipient data and subscribe them **/
	public static final ImportMode ADD = new ImportMode(0, "import.mode.add", "ImportModeAddHandler");
	
	/** Insert and update recipient data and subscribe them if without binding **/
	public static final ImportMode ADD_AND_UPDATE = new ImportMode(1, "import.mode.add_update", "ImportModeAddAndUpdateHandler");
	
	/** Update only existing recipient data and subscribe them if without binding **/
	public static final ImportMode UPDATE = new ImportMode(2, "import.mode.only_update", "ImportModeUpdateHandler");
	
	/** Unsubscribe existing recipients **/
	public static final ImportMode MARK_OPT_OUT = new ImportMode(3, "import.mode.unsubscribe", "ImportModeUnsubscribeHandler");

	/** Mark existing recipients as bounced **/
	public static final ImportMode MARK_BOUNCED = new ImportMode(4, "import.mode.bounce", "ImportModeMarkAsBounceHandler");

	/** Mark existing recipients as blacklisted **/
	public static final ImportMode TO_BLACKLIST = new ImportMode(5, "import.mode.blacklist", "ImportModeBlacklistHandler");

	/** Re-subscribe existing bounced recipients **/
	public static final ImportMode REACTIVATE_BOUNCED = new ImportMode(6, "import.mode.bouncereactivate", "ImportModeBounceReactivateHandler");

	/** Mark existing subscribed recipients as suspended **/
	public static final ImportMode MARK_SUSPENDED = new ImportMode(7, "import.mode.remove_status", "ImportModeMarkAsSuspendedHandler");
	
	/** Insert and update recipient data and subscribe them if without binding, but also re-subscribe admin-out and user-out recipients **/
	public static final ImportMode ADD_AND_UPDATE_FORCED = new ImportMode(8, "import.mode.add_update_forced", "ImportModeAddAndUpdateForcedHandler");
	
	/** Reactivate recipients from suspended to active**/
	public static final ImportMode REACTIVATE_SUSPENDED = new ImportMode(10, "import.mode.reactivateSuspended", "ImportModeReactivateSuspendedHandler");

	/** Mark existing recipients as blacklisted. Mark all blacklisted recipients as admin_out, which are not included in import data **/
	public static final ImportMode BLACKLIST_EXCLUSIVE = new ImportMode(12, "import.mode.blacklist_exclusive", "ImportModeBlacklistExclusiveHandler");

	/**
	 * int value for db storage
	 */
	private int storageInt;

	/**
	 * message key in resource bundle to display value on pages
	 * CAUTION: Text is message key and permission token
	 */
	private String messageKey;
	
	private String importModeHandlerName;

	public String getMessageKey() {
		return messageKey;
	}

	public int getIntValue() {
		return storageInt;
	}

	public String getImportModeHandlerName() {
		return importModeHandlerName;
	}

	public ImportMode(int storageInt, String messageKey, String importModeHandlerName) {
		if (storageInt < 0 || StringUtils.isBlank(messageKey) || StringUtils.isBlank(importModeHandlerName)) {
			throw new RuntimeException("Invalid new ImportMode: " + storageInt + ", " + messageKey + ", " + importModeHandlerName);
		}
		for (Entry<String, ImportMode> item : SYSTEM_IMPORTMODES.entrySet()) {
			if (item.getKey().equalsIgnoreCase(messageKey) || item.getValue().storageInt == storageInt || item.getValue().importModeHandlerName.equalsIgnoreCase(importModeHandlerName)) {
				throw new RuntimeException("Invalid new ImportMode, already exists: " + storageInt + ", " + messageKey + ", " + importModeHandlerName);
			}
		}
		
		this.storageInt = storageInt;
		this.messageKey = messageKey;
		this.importModeHandlerName = importModeHandlerName;
		
		SYSTEM_IMPORTMODES.put(messageKey, this);
	}
	
	public static List<ImportMode> values() {
		List<ImportMode> values = new ArrayList<>(SYSTEM_IMPORTMODES.values());
		values.sort(new Comparator<ImportMode>() {
			@Override
			public int compare(ImportMode o1, ImportMode o2) {
				return Integer.compare(o1.storageInt, o2.storageInt);
			}
		});
		return values;
	}

	public static ImportMode getFromString(String messageKey) throws Exception {
		ImportMode importMode = SYSTEM_IMPORTMODES.get(messageKey);
		if (importMode == null) {
			if (messageKey != null && !messageKey.startsWith("import.mode.")) {
				return getFromString("import.mode." + messageKey);
			} else {
				throw new Exception("Invalid key for ImportMode: " + messageKey);
			}
		} else {
			return importMode;
		}
	}

	public static ImportMode getFromInt(int intValue) throws Exception {
		for (ImportMode importMode : SYSTEM_IMPORTMODES.values()) {
			if (importMode.getIntValue() == intValue) {
				return importMode;
			}
		}
		throw new Exception("Invalid int value for ImportMode: " + intValue);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportMode that = (ImportMode) o;
        return storageInt == that.storageInt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageInt);
    }
}
