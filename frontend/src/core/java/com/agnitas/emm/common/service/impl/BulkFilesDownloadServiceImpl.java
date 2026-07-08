/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common.service.impl;

import com.agnitas.emm.common.exceptions.ZipArchiveException;
import com.agnitas.emm.common.exceptions.ZipDownloadException;
import com.agnitas.emm.common.service.BulkFilesDownloadService;
import com.agnitas.emm.common.service.ZipArchiveService;
import com.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;
import java.util.function.Function;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

@Service
public class BulkFilesDownloadServiceImpl implements BulkFilesDownloadService {

    private final ZipArchiveService<Integer> zipArchiveService;

    public BulkFilesDownloadServiceImpl(ZipArchiveService zipArchiveService) {
        this.zipArchiveService = zipArchiveService;
    }

    @Override
    public File getZipToDownload(Set<Integer> ids, String fileNameSuffix, Function<Integer, Tuple<String, byte[]>> dataFunction) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ZipDownloadException(NOTHING_SELECTED_MSG);
        }

        try {
            return zipArchiveService.createZipArchive(ids, fileNameSuffix, dataFunction);
        } catch (ZipArchiveException e) {
            throw new ZipDownloadException(ERROR_MSG);
        }
    }
}
