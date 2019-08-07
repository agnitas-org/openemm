/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.util.HttpUtils;

public class TextFileDownload extends HttpServlet {

    private static final long serialVersionUID = 5844323149267914354L;

    private static final String CHARSET = "UTF-8";
             
	/**
     * Gets file parameters from request. <br>
     * reads file from session. <br>
     * build filepath (timestamp + .csv) and set it to response header  <br>
     * write parameters and file to response
     * <br><br>
     */
    @Override
	public void doGet(HttpServletRequest req, HttpServletResponse response)
                      throws IOException, ServletException {

        response.setContentType("text/plain");
        @SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) (req.getSession().getAttribute("map"));       
      
        String outFileName = "";	// contains the Filename, build from the timestamp
        String outFile = "";		// contains the actual data.
      	
        outFileName = req.getParameter("key");       
        outFile = map.get(req.getParameter("key"));	// get the key from the Hashmap.

        // build filepath (timestamp + .csv) and return it.
        HttpUtils.setDownloadFilenameHeader(response, outFileName + ".csv");
        response.setCharacterEncoding(CHARSET);

        PrintWriter writer = response.getWriter();
        writer.print(outFile);
    }
}
