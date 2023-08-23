/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import org.agnitas.util.Tuple;

public interface MailingSizeCalculationService {

    /**
     * Evaluate mailing content structure and calculate the maximum possible size (in bytes) of a mail that mailing can
     * ever produce. Keep in mind that calculations could be a little bit inaccurate because an algorithm never assumes
     * any sort of connection between target groups. Although the same dyn-tag is replaced with the same content all over the mailing
     * (if used in multiple placed) because otherwise an inaccuracy could be that high so calculations get completely useless and untrustful.
     *
     * @param mailing the mailing entity to evaluate.
     * @return the tuple of maximum possible mail sizes in bytes depending on mailing format:
     *
     * Text - 1) content size + attachments size; 2) the same
     * Text & HTML - 1) content size + attachments size; 2) content size + attachments size + images size
     * Text & HTML & Offline-HTML - 1) content size + attachments size + images size; 2) the same
     */
    Tuple<Long, Long> calculateSize(Mailing mailing, Admin admin);
}
