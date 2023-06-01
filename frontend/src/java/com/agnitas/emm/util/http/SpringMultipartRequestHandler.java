/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.http;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.struts.upload.CommonsMultipartRequestHandler;
import org.apache.struts.upload.FormFile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpringMultipartRequestHandler extends CommonsMultipartRequestHandler {

    @Override
    public void handleRequest(HttpServletRequest request) throws ServletException {
        super.handleRequest(request);

        MultipartRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartRequest.class);

        if (multipartRequest != null && shouldAddMultipartFiles(multipartRequest)) {
            Map<String, FormFile> filesMap = convertToStrutsFiles(multipartRequest.getMultiFileMap());

            this.getFileElements().putAll(filesMap);
            this.getAllElements().putAll(filesMap);
        }
    }

    private boolean shouldAddMultipartFiles(MultipartRequest req) {
        return this.getFileElements().isEmpty() && !req.getMultiFileMap().isEmpty();
    }

    private Map<String, FormFile> convertToStrutsFiles(MultiValueMap<String, MultipartFile> fileMap) {
        Map<String, FormFile> resultMap = new HashMap<>();

        for (Map.Entry<String, List<MultipartFile>> entry : fileMap.entrySet()) {
            String parameterName = entry.getKey();
            List<MultipartFile> files = entry.getValue();

            for (MultipartFile file : files) {
                FormFile formFile = convertToStrutsFile(file);
                resultMap.put(parameterName, formFile);
            }
        }

        return resultMap;
    }

    private FormFile convertToStrutsFile(MultipartFile file) {
        return new FormFile() {
            @Override
            public String getContentType() {
                return file.getContentType();
            }

            @Override
            public void setContentType(String s) {
            	// do nothing
            }

            @Override
            public int getFileSize() {
                return ((int) file.getSize());
            }

            @Override
            public void setFileSize(int i) {
            	// do nothing
            }

            @Override
            public String getFileName() {
                return file.getOriginalFilename();
            }

            @Override
            public void setFileName(String s) {
            	// do nothing
            }

            @Override
            public byte[] getFileData() throws IOException {
                return file.getBytes();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return file.getInputStream();
            }

            @Override
            public void destroy() {
            	// do nothing
            }
        };
    }
}
