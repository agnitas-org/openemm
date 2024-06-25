/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mediatypes.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.SpecialCharactersWorker;

import static java.text.MessageFormat.format;

public class MediatypesDaoImpl extends BaseDaoImpl implements MediatypesDao {

    private static final Logger logger = LogManager.getLogger(MediatypesDaoImpl.class);

    protected MediatypeFactory mediatypeFactory;
    private ConfigService configService;

    @Override
    @DaoUpdateReturnValueCheck
    public void saveMediatypes(int companyID, int mailingId, Map<Integer, Mediatype> mediatypes) throws Exception {
        for (Map.Entry<Integer, Mediatype> entry : mediatypes.entrySet()) {
            Integer type = entry.getKey();
            Mediatype mediatype = entry.getValue();

            if (mediatypeFactory.isTypeSupported(type)) {
                if (type == MediaTypes.EMAIL.getMediaCode() && mediatype instanceof MediatypeEmail) {
                    // process special characters for subject
                    MediatypeEmail mediatypeEmail = (MediatypeEmail) mediatype;
                    String subject = mediatypeEmail.getSubject();
                    String preHeader = mediatypeEmail.getPreHeader();

                    subject = SpecialCharactersWorker.processString(subject, mediatypeEmail.getCharset());
                    preHeader = SpecialCharactersWorker.processString(preHeader, mediatypeEmail.getCharset());

                    mediatypeEmail.setSubject(subject);
                    mediatypeEmail.setPreHeader(preHeader);

                	// Add default bcc email addresses if not already included
                	Set<String> currentBccEmailAddresses = AgnUtils.splitAndNormalizeEmails(mediatypeEmail.getBccRecipients());
                	Set<String> defaultBccEmailAddresses = AgnUtils.splitAndNormalizeEmails(configService.getValue(ConfigValue.DefaultBccEmail, companyID));
                	for (String defaultBccEmailAddress : defaultBccEmailAddresses) {
                		if (!currentBccEmailAddresses.contains(defaultBccEmailAddress)) {
                			mediatypeEmail.setBccRecipients(mediatypeEmail.getBccRecipients() + ", " + defaultBccEmailAddress);
                		}
                	}
                }
                
                String param = mediatype.getParam();
				validateParam(param);

                String sql = "SELECT COUNT(mediatype) FROM mailing_mt_tbl WHERE mailing_id = ? AND mediatype = ?";
                int total = selectInt(logger, sql, mailingId, type);

                if (total > 0) {
                    sql = "UPDATE mailing_mt_tbl SET priority = ?, status = ?, param = ? WHERE mailing_id = ? AND mediatype = ?";
                    update(logger, sql, mediatype.getPriority(), mediatype.getStatus(), param, mailingId, type);
                } else {
                    sql = "INSERT INTO mailing_mt_tbl (mailing_id, mediatype, priority, status, param) VALUES (?, ?, ?, ?, ?)";
                    update(logger, sql, mailingId, type, mediatype.getPriority(), mediatype.getStatus(), param);
                }
            }
        }
    }

    @Override
    public Map<Integer, Mediatype> loadMediatypes(int mailingId, int companyId) throws MediatypesDaoException {
        Map<Integer, Mediatype> mediatypes = new HashMap<>();

        String sql = "SELECT mediatype, priority, status, param FROM mailing_mt_tbl WHERE mailing_id = ?";
        try {
            query(logger, sql, new MediatypeMapRowCallbackHandler(mediatypes, companyId), mailingId);
        } catch (Exception e) {
            logger.error(format("Error reading media types for mailing {0}, company ID {1}", mailingId, companyId), e);
            throw new MediatypesDaoException("Error reading media types for mailing " + mailingId + ", company ID " + companyId, e);
        }

        return mediatypes;
    }

    @Override
    public Map<Integer, Mediatype> loadMediatypesByStatus(MediaTypeStatus status, int mailingId, int companyId) throws MediatypesDaoException {
        Map<Integer, Mediatype> mediatypes = new HashMap<>();

        String sql = "SELECT mediatype, priority, status, param FROM mailing_mt_tbl WHERE mailing_id = ? AND status = ?";
        try {
            query(logger, sql, new MediatypeMapRowCallbackHandler(mediatypes, companyId), mailingId, status.getCode());
        } catch (Exception e) {
            logger.error(format("Error reading media types for mailing {0}, company ID {1}", mailingId, companyId), e);
            throw new MediatypesDaoException("Error reading media types for mailing " + mailingId + ", company ID " + companyId, e);
        }

        return mediatypes;
    }

    @Required
    public void setMediatypeFactory(MediatypeFactory mediatypeFactory) {
        this.mediatypeFactory = mediatypeFactory;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    private class MediatypeMapRowCallbackHandler implements RowCallbackHandler {
        private final Map<Integer, Mediatype> map;
        private final int companyId;

        public MediatypeMapRowCallbackHandler(Map<Integer, Mediatype> map, int companyId) {
            this.map = Objects.requireNonNull(map);
            this.companyId = companyId;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            int type = rs.getInt("mediatype");

            if (mediatypeFactory.isTypeSupported(type)) {
                try {
                	Mediatype mediatype = mediatypeFactory.create(type);

                    mediatype.setCompanyID(companyId);
                    mediatype.setPriority(rs.getInt("priority"));
                    mediatype.setStatus(rs.getInt("status"));
                    mediatype.setParam(rs.getString("param"));

                    map.put(type, mediatype);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    private void validateParam(String param) {
		if (param != null && param.length() > 1000) {
			throw new RuntimeException("Value for mailing_mt_tbl.param is to long (Maximum: 1000, Current: " + param.length() + ")");
		}
	}
}
