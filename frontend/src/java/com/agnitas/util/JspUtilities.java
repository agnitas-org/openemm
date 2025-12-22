/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.enums.UiLayoutType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;

public class JspUtilities {

    private JspUtilities() {

    }

    private static final TimeoutLRUMap<Integer, String> CKEDITOR_PATH_CACHE = new TimeoutLRUMap<>(100, 5);
    private static final TimeoutLRUMap<Integer, String> JODIT_EDITOR_PATH_CACHE = new TimeoutLRUMap<>(100, 5);
    private static final TimeoutLRUMap<Integer, String> ACE_EDITOR_PATH_CACHE = new TimeoutLRUMap<>(100, 5);

    public static final String JS_TABLE_COLUMN_TYPE_COMMON = "";
    public static final String JS_TABLE_COLUMN_TYPE_NUMBER = "numberColumn";
    public static final String JS_TABLE_COLUMN_TYPE_DATE = "dateColumn";

    public static String getTimeZoneId(HttpServletRequest request) {
        Admin admin = AgnUtils.getAdmin(request);

        if (admin == null) {
            return null;
        }

        return admin.getAdminTimezone();
    }

    public static String getAdminId(HttpServletRequest request){
        Admin admin = AgnUtils.getAdmin(request);

        if (admin == null) {
            return null;
        }

        return String.valueOf(admin.getAdminID());
    }

    public static UiLayoutType getLayoutType(HttpServletRequest request) {
        return Optional.ofNullable(AgnUtils.getAdmin(request))
                .map(Admin::getLayoutType)
                .orElse(null);
    }

    public static String getCompanyId(HttpServletRequest request){
        Admin admin = AgnUtils.getAdmin(request);

        if (admin == null) {
            return null;
        }

        return String.valueOf(admin.getCompanyID());
    }

    public static boolean isJoditEditorUsageAllowed(HttpServletRequest request) {
        int companyId = Optional.ofNullable(AgnUtils.getAdmin(request))
                .map(Admin::getCompanyID)
                .orElse(0);

        return ConfigService.getInstance().getBooleanValue(ConfigValue.UseJoditEditor, companyId);
    }

    public static String asJsTableColumnType(DbColumnType type) {
        if (type == null) {
            return null;
        }

        switch (type.getSimpleDataType()) {
            case Date, DateTime:
                return JS_TABLE_COLUMN_TYPE_DATE;

            case Numeric, Float:
                return JS_TABLE_COLUMN_TYPE_NUMBER;

            default:
                // No custom handling.
                return JS_TABLE_COLUMN_TYPE_COMMON;
        }
    }

    public static String asText(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        return StringUtils.defaultString(value.toString());
    }

    public static boolean contains(Object container, Object object) throws JspException {
        if (container == null) {
            return false;
        }
        if (container instanceof Collection) {
            return ((Collection<?>) container).contains(object);
        }

        if (container instanceof String stringContainer) {
            if (object == null) {
                return false;
            }

            return stringContainer.contains(object.toString());
        }

        throw new JspException("emm:contains() accepts either Collection or String");
    }

    public static String absUrlPrefix(String url) {
        url = StringUtils.removeEnd(StringUtils.removeStart(url, "/"), "/");

        if (StringUtils.isEmpty(url)) {
            return "";
        }

        return "/" + url;
    }

    public static boolean permissionAllowed(String token, HttpServletRequest req) {
        return AgnUtils.allowed(req, Permission.getPermissionsByToken(token));
    }

    public static String getAceEditorPath(HttpServletRequest request) throws Exception {
        Admin admin = AgnUtils.getAdmin(request);
        int companyID = 0;

        if (admin != null) {
            companyID = admin.getCompanyID();
        }

        String cachedPath = ACE_EDITOR_PATH_CACHE.get(companyID);
        File aceEditorRootDir = getLibraryRootDir("/../../js/lib/ace");
        boolean shouldUseLatestVersion = ConfigService.getInstance().getBooleanValue(ConfigValue.UseLatestAceEditor, companyID);

        String editorPath = getLibraryPath("AceEditor", cachedPath, "ace_", aceEditorRootDir, shouldUseLatestVersion);

        if (cachedPath == null) {
            editorPath = "js/lib/ace/" + editorPath;
            ACE_EDITOR_PATH_CACHE.put(companyID, editorPath);
        }

        return editorPath;
    }

    public static String getJoditEditorPath(HttpServletRequest request) throws Exception {
        Admin admin = AgnUtils.getAdmin(request);
        int companyID = 0;

        if (admin != null) {
            companyID = admin.getCompanyID();
        }

        String cachedPath = JODIT_EDITOR_PATH_CACHE.get(companyID);
        File joditEditorRootDir = getLibraryRootDir("/../../js/lib/jodit");
        boolean shouldUseLatestVersion = ConfigService.getInstance().getBooleanValue(ConfigValue.UseLatestCkEditor, companyID);

        String editorPath = getLibraryPath("Jodit", cachedPath, "jodit_", joditEditorRootDir, shouldUseLatestVersion);

        if (cachedPath == null) {
            editorPath = "js/lib/jodit/" + editorPath;
            JODIT_EDITOR_PATH_CACHE.put(companyID, editorPath);
        }

        return editorPath;
    }

    private static File getLibraryRootDir(String libRelativePath) throws Exception {
        String applicationInstallPath = JspUtilities.class.getClassLoader().getResource("/").getFile();
        if (applicationInstallPath != null && applicationInstallPath.endsWith("/")) {
            applicationInstallPath = applicationInstallPath.substring(0, applicationInstallPath.length() - 1);
        }

        if (StringUtils.isBlank(applicationInstallPath)) {
            throw new IllegalStateException("Cannot find application install directory");
        } else {
            applicationInstallPath += libRelativePath;
            try {
                return new File(applicationInstallPath).getCanonicalFile();
            } catch (Exception e) {
                throw new Exception("Cannot find application install directory: " + e.getMessage(), e);
            }
        }
    }

    private static String getLibraryPath(String libName, String cachedPath, String dirNamePrefix, File rootDir, boolean useLastVersion) throws Exception {
        String libraryPath = cachedPath;
        if (libraryPath == null) {
            try {
                Version libraryVersion = new Version("0.0.0");
                for (File subFile : rootDir.listFiles()){
                    if (subFile.isDirectory() && subFile.getName().startsWith(dirNamePrefix)) {
                        String versionString = subFile.getName().substring(dirNamePrefix.length());
                        Version nextVersion = new Version(versionString);
                        if (libraryPath == null) {
                            libraryVersion = nextVersion;
                            libraryPath = subFile.getName();
                        } else {
                            if (useLastVersion) {
                                if (nextVersion.compareTo(libraryVersion) > 0) {
                                    libraryVersion = nextVersion;
                                    libraryPath = subFile.getName();
                                }
                            } else {
                                if (nextVersion.compareTo(libraryVersion) < 0) {
                                    libraryVersion = nextVersion;
                                    libraryPath = subFile.getName();
                                }
                            }
                        }
                    }
                }

                if (libraryPath != null) {
                    return libraryPath;
                }

                throw new Exception("Cannot find " + libName + " directory");
            } catch (Exception e) {
                throw new Exception("Cannot find " + libName + " directory: " + e.getMessage(), e);
            }
        }

        return libraryPath;
    }

    /**
     * Searches for the current ckEditor installation with the highest version number.
     * The result is cached.
     */
    public static String getCkEditorPath(HttpServletRequest request) throws Exception {
        Admin admin = AgnUtils.getAdmin(request);
        int companyID = 0;

        if (admin != null) {
            companyID = admin.getCompanyID();
        }

        String cachedPath = CKEDITOR_PATH_CACHE.get(companyID);
        File ckEditorRootDir = getLibraryRootDir("/../../js/lib/ckeditor");
        boolean shouldUseLatestVersion = ConfigService.getInstance().getBooleanValue(ConfigValue.UseLatestCkEditor, companyID);

        String editorPath = getLibraryPath("CkEditor", cachedPath, "ckeditor-", ckEditorRootDir, shouldUseLatestVersion);

        if (cachedPath == null) {
            editorPath = "js/lib/ckeditor/" + editorPath;
            CKEDITOR_PATH_CACHE.put(companyID, editorPath);
        }

        return editorPath;
    }

    public static String getWysiwygToolbarType(HttpServletRequest request, String defaultType) {
        if (AgnUtils.allowed(request, Permission.CKEDITOR_EXTENDED)) {
            return "Full";
        }

        if (AgnUtils.allowed(request, Permission.CKEDITOR_TRIMMED)) {
            return "Trimmed";
        }

        return defaultType;
    }
}
