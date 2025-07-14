/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.preview.dto.PreviewResult;
import com.agnitas.emm.core.preview.form.PreviewForm;
import com.agnitas.preview.Page;

import java.util.List;

public interface MailingWebPreviewService {

    /**
     * Indicates, that the algorithm was not able to detect proper preview format.
     */
    int UNDEFINED_PREVIEW_FORMAT = -1;

    int INPUT_TYPE_TEXT = 0;
    int INPUT_TYPE_HTML = 1;

    String TEMPLATE = "__TEMPLATE__";
    String SUBJECT = "__SUBJECT__";
    String FROM = "__FROM__";

    void updateActiveMailingPreviewFormat(PreviewForm previewForm, int mailingId, int companyID);

    List<Integer> getAvailablePreviewFormats(Mailing mailing);

    PreviewResult getPreview(PreviewSettings settings, int companyId, Admin admin) throws Exception;

    @Deprecated
    Page generateBackEndPreview(PreviewForm previewForm);

    Page generateBackEndPreview(PreviewSettings settings);

    boolean isPostMailing(Mailing mailing);

}
