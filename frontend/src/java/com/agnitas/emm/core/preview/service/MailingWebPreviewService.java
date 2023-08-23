/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.preview.form.PreviewForm;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;

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

    String getPreview(PreviewForm previewForm, int companyId, Admin admin) throws Exception;

    String getMailingHtml(int mailingId, boolean mobile, boolean noImages);

    Page generateBackEndPreview(PreviewForm previewForm);

    boolean isPostMailing(Mailing mailing);

    String getMediaQuery(Preview.Size size);

    int getPreviewWidth(Preview.Size size);
}
