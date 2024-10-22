/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.service.impl;

import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.beans.MailingArchiveEntry;
import org.agnitas.emm.core.mailing.service.MailingArchiveService;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MailingArchiveServiceImpl implements MailingArchiveService {

    /** The logger. */
    private static final transient Logger LOGGER = LogManager.getLogger(MailingArchiveServiceImpl.class);

    private final MailingDao mailingDao;
    private final MediatypesDao mediatypesDao;

    public MailingArchiveServiceImpl(final MailingDao mailingDao, final MediatypesDao mediatypesDao) {
        this.mailingDao = Objects.requireNonNull(mailingDao, "mailing DAO");
        this.mediatypesDao = Objects.requireNonNull(mediatypesDao, "mediatypes DAO");
    }

    @Override
    public List<MailingArchiveEntry> listMailingArchive(int companyId, int archiveId) {
        final List<LightweightMailing> mailings = mailingDao.getLightweightMailingsForActionOperationGetArchiveList(companyId, archiveId);
        final List<MailingArchiveEntry> result = new ArrayList<>();

        for(final LightweightMailing mailing : mailings) {
            try {
                final Map<Integer, Mediatype> mediatypes = this.mediatypesDao.loadMediatypes(mailing.getMailingID(), mailing.getCompanyID());
                final MediatypeEmail mediatype = (MediatypeEmail) mediatypes.get(MediaTypes.EMAIL.getMediaCode());

                if(mediatype != null) {
                    final MailingArchiveEntry entry = new MailingArchiveEntry(mailing.getMailingID(), mailing.getShortname(), mediatype.getSubject());

                    result.add(entry);
                }
            } catch(final MediatypesDaoException e) {
                LOGGER.error(String.format("Error loading mediatypes for mailing %d", mailing.getMailingID()), e);
            }
        }

        return result;
    }
}
