/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.ImportBaseFileForm;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;

/**
 * Base action that manages csv file uploading and storing. When user uploads
 * csv file it is stored to struts temporary directory and the location of
 * file is stored to session attribute "stored-csv-file-path", the name of
 * file is stored to "original-csv-file-name". Later the file can be used by
 * subclasses of ImportBaseFileAction.
 * User will also have possibility to remove current uploaded file and upload
 * another one.
 */
public abstract class ImportBaseFileAction extends StrutsActionBase {
	private static final transient Logger logger = Logger.getLogger(ImportBaseFileAction.class);

    public static final String CSV_FILE_PATH_KEY = "stored-csv-file-path";
    public static final String CSV_ORIGINAL_FILE_NAME_KEY = "original-csv-file-name";
    
	private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";

    protected boolean fileUploadPerformed;

    protected boolean fileRemovePerformed;
    
    protected ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
     * Stores uploaded csv file to temporary directory or removes existing csv file depending on request attributes ("remove_file" or "upload_file")
     * <br>
     * @param mapping The ActionMapping used to select this instance
     * @param form    The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing. Should contain parameters "remove_file" or "upload_file".
     * @param res     The HTTP response we are creating
     * @throws java.io.IOException            if an input/output error occurs
     * @throws javax.servlet.ServletException if a servlet exception occurs
     * @return destination to logon page if user is not logged in or NULL
     */
    @Override
	public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse res)
            throws IOException, ServletException {

        // Validate the request parameters specified by the user
        ImportBaseFileForm aForm;
        ActionMessages errors = new ActionMessages();

        fileUploadPerformed = false;
        fileRemovePerformed = false;

        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }
        if (form != null) {
            aForm = (ImportBaseFileForm) form;
        } else {
            aForm = new ImportBaseFileForm();
        }

        try {
            if (AgnUtils.parameterNotEmpty(request, "remove_file")) {
                removeStoredCsvFile(request);
                fileRemovePerformed = true;
            } else if (AgnUtils.parameterNotEmpty(request, "upload_file") &&
                    StringUtils.isEmpty(getCurrentFileName(request)) && (aForm.getCsvFile() != null)) {
                errors.add(storeCsvFile(request, aForm.getCsvFile()));
            }
            aForm.setCurrentFileName(getCurrentFileName(request));
        }
        catch (Exception e) {
            logger.error("execute: " + e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

        return null;
    }

    /**
     * Stores uploaded csv file to temporary directory and the location is
     * store to session. So the file can be used later.
     *
     * @param request request
     * @param csvFile uploaded csv file
     * @return errors that happened
     * @throws IOException
     * @throws FileNotFoundException
     */
    private ActionErrors storeCsvFile(HttpServletRequest request, FormFile csvFile) throws Exception {
        ActionErrors errors = new ActionErrors();
        HttpSession session = request.getSession();
        String savePath = generateSavePath(session);
        File file = new File(savePath);
        try (InputStream inputStream = csvFile.getInputStream()) {
	        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
	            IOUtils.copy(inputStream, outputStream);
	            removeStoredCsvFile(request);
	            session.setAttribute(CSV_FILE_PATH_KEY, savePath);
	            session.setAttribute(CSV_ORIGINAL_FILE_NAME_KEY, csvFile.getFileName());
	            fileUploadPerformed = true;
	        } catch (IOException e) {
	            errors.add("csvFile", new ActionMessage("error.import.cannotOpenFile", e.getMessage()));
	            return errors;
	        } finally {
	        	IOUtils.closeQuietly(inputStream);
	        }
        }
        return errors;
    }

    /**
     * Generated path in temporary directory for saving uploaded csv-file
     *
     * @param session current session
     * @return generated path
     */
    private String generateSavePath(HttpSession session) {
        try {
        	ComAdmin admin = ((ComAdmin) session.getAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN));
			int companyId = admin.getCompanyID();
			int adminId = admin.getAdminID();
			return File.createTempFile("upload_csv_file_" + companyId + "_" + adminId + "_", ".csv", AgnUtils.createDirectory(IMPORT_FILE_DIRECTORY + "/" + admin.getCompanyID())).getAbsolutePath();
		} catch (Exception e) {
			logger.error("Cannot create temp file path for upload file storage", e);
			return null;
		}
    }

    /**
     * Removes csv file that was uploaded earlier. Removes the file itself,
     * cleans session attribute that stores path to file
     *
     * @param request request
     */
    public void removeStoredCsvFile(HttpServletRequest request) {
        if (request.getSession().getAttribute(CSV_FILE_PATH_KEY) != null) {
        	request.getSession().setAttribute(CSV_FILE_PATH_KEY, null);
        	request.getSession().setAttribute(CSV_ORIGINAL_FILE_NAME_KEY, null);
        }
    }

    /**
     * Gets the file name for showing it in file panel
     *
     * @param request request
     * @return file name
     */
    protected String getCurrentFileName(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(CSV_ORIGINAL_FILE_NAME_KEY);
    }

    /**
     * Gets current uploaded csv file
     *
     * @param request request
     * @return csv file if it's present or null in other case
     */
    protected File getCurrentFile(HttpServletRequest request) {
        String filePath = (String) request.getSession().getAttribute(CSV_FILE_PATH_KEY);
        if (filePath == null) {
            return null;
        }
        File csvFile = new File(filePath);
        return csvFile;
    }


}
