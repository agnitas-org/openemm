/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.dao.impl.ComMailingDaoImpl.DynamicTagContentRowMapper;
import com.agnitas.dao.impl.ComMailingDaoImpl.DynamicTagRowMapper;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.dao.DynamicTagContentDao;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicTagDaoImpl extends BaseDaoImpl implements DynamicTagDao {

    private static final transient Logger logger = LogManager.getLogger(DynamicTagDaoImpl.class);

    private DynamicTagContentDao dynamicTagContentDao;

    @Required
    public void setDynamicTagContentDao(DynamicTagContentDao dynamicTagContentDao) {
        this.dynamicTagContentDao = dynamicTagContentDao;
    }

    @Override
    public List<DynamicTag> getNameList(int companyId, int mailingId) {
        return select(logger, "SELECT company_id, dyn_name_id, dyn_name FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ? AND deleted = 0", new DynamicTag_RowMapper(), mailingId,
                companyId);
    }

    private class DynamicTag_RowMapper implements RowMapper<DynamicTag> {
        @Override
        public DynamicTag mapRow(ResultSet resultSet, int row) throws SQLException {
            DynamicTag dynamicTag = new DynamicTagImpl();

            dynamicTag.setCompanyID(resultSet.getInt("company_id"));
            dynamicTag.setId(resultSet.getInt("dyn_name_id"));
            dynamicTag.setDynName(resultSet.getString("dyn_name"));

            return dynamicTag;
        }
    }

    @Override
    public void markNameAsDeleted(int mailingID, String name) {
        setDynNameDeletionMark(mailingID, true, Collections.singletonList(name));
    }

    @Override
    public void markNamesAsDeleted(int mailingID, List<String> names) {
        setDynNameDeletionMark(mailingID, true, names);
    }

    @Override
    public void markNameAsUsed(int mailingID, String name) {
        setDynNameDeletionMark(mailingID, false, Collections.singletonList(name));
    }

    @Override
    public void markNamesAsUsed(int mailingID, List<String> names) {
        setDynNameDeletionMark(mailingID, false, names);
    }

    @DaoUpdateReturnValueCheck
    protected void setDynNameDeletionMark(int mailingID, boolean setDeleted, List<String> nameList) {
        if (nameList.isEmpty()) {
            return;
        }

        String updateSql;
        if (setDeleted) {
            updateSql = "UPDATE dyn_name_tbl SET change_date = current_timestamp, deleted = 1, deletion_date = CURRENT_TIMESTAMP";
        } else {
            updateSql = "UPDATE dyn_name_tbl SET change_date = current_timestamp, deleted = 0, deletion_date = null";
        }

        updateSql += " WHERE mailing_id = ? AND " + makeBulkInClauseForString("dyn_name", nameList);

        update(logger, updateSql, mailingID);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteDynamicTagsMarkAsDeleted(int retentionTime) {
        // Determine threshold for date
        GregorianCalendar threshold = new GregorianCalendar();
        threshold.add(Calendar.DAY_OF_MONTH, -retentionTime);

        // Deleted marked and outdated records
        update(logger, "DELETE FROM dyn_content_tbl WHERE dyn_name_id IN (SELECT dyn_name_id FROM dyn_name_tbl WHERE deleted = 1 AND deletion_date IS NOT NULL AND deletion_date < ?)", threshold);
        update(logger, "DELETE FROM dyn_name_tbl WHERE deleted = 1 AND deletion_date IS NOT NULL AND deletion_date < ?", threshold);
    }

    @Override
    public boolean deleteDynamicTagsByCompany(int companyID) {
        try {
            update(logger, "DELETE FROM dyn_content_tbl WHERE company_id = ?", companyID);
            update(logger, "DELETE FROM dyn_name_tbl WHERE company_id = ?", companyID);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting content data (company ID: " + companyID + ")", e);
            return false;
        }
    }

    @Override
    public String getDynamicTagInterestGroup(int companyId, int mailingId, int dynTagId) {
        return select(logger, "SELECT interest_group FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ?", String.class, mailingId, companyId, dynTagId);
    }

    @Override
    public int getId(int companyId, int mailingId, String dynTagName) {
        String sqlGetId = "SELECT dyn_name_id FROM dyn_name_tbl WHERE company_id = ? AND mailing_id = ? AND dyn_name = ?";
        return selectInt(logger, sqlGetId, companyId, mailingId, dynTagName);
    }

    @Override
    public String getDynamicTagName(int companyId, int mailingId, int dynTagId) {
        String sqlGetDynName = "SELECT dyn_name FROM dyn_name_tbl WHERE company_id = ? AND mailing_id = ? AND dyn_name_id = ?";
        if (isOracleDB()) {
            sqlGetDynName += " AND ROWNUM = 1";
        } else {
            sqlGetDynName += " LIMIT 1";
        }

        List<String> nameResults = select(logger, sqlGetDynName, StringRowMapper.INSTANCE, companyId, mailingId, dynTagId);

        if (nameResults.size() > 0) {
            return nameResults.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Integer> getDynTagIdsByName(int companyId, int mailingId, List<String> dynNames) {
        Map<String, Integer> dynNameIds = new HashMap<>();
        if (dynNames.isEmpty()) {
            return dynNameIds;
        }

        String sql = "SELECT dyn_name, dyn_name_id FROM dyn_name_tbl WHERE company_id = ? AND mailing_id = ? " +
                "AND " + makeBulkInClauseForString("dyn_name", dynNames);
        query(logger, sql, new DynNamesMapCallback(dynNameIds), companyId, mailingId);
        return dynNameIds;
    }

    private static class DynNamesMapCallback implements RowCallbackHandler {
        private Map<String, Integer> dynNamesIdsMap;

        public DynNamesMapCallback(Map<String, Integer> dynNamesIdsMap) {
            this.dynNamesIdsMap = Objects.requireNonNull(dynNamesIdsMap);
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            dynNamesIdsMap.put(rs.getString("dyn_name"), rs.getInt("dyn_name_id"));
        }
    }

    @Override
    public DynamicTag getDynamicTag(int dynNameId, int companyId) {
        final String sqlGetTags = "SELECT company_id, mailing_id, dyn_name_id, dyn_name, dyn_group, interest_group, no_link_extension " +
                "FROM dyn_name_tbl " +
                "WHERE company_id = ? AND dyn_name_id = ? AND deleted = 0";

        final String sqlGetContents = "SELECT company_id, mailing_id, dyn_content_id, dyn_name_id, target_id, dyn_order, dyn_content " +
                "FROM dyn_content_tbl " +
                "WHERE company_id = ? AND dyn_name_id = ? " +
                "ORDER BY dyn_order, dyn_content_id ASC";

        final DynamicTag tag = selectObjectDefaultNull(logger, sqlGetTags, new DynamicTagRowMapper(), companyId, dynNameId);

        if (tag != null) {
            // Retrieve content entries for the tag.
            select(logger, sqlGetContents, new DynamicTagContentRowMapper(), companyId, dynNameId)
                    .forEach(tag::addContent);
        }

        return tag;
    }

    @Override
    public List<DynamicTag> getDynamicTags(final int mailingId, final int companyId, final boolean includeDeletedDynTags) {
        final String sqlGetTags = "SELECT company_id, mailing_id, dyn_name_id, dyn_name, dyn_group, interest_group, no_link_extension " +
                "FROM dyn_name_tbl " +
                "WHERE company_id = ? AND mailing_id = ? AND (deleted = 0 OR 1 = ?) " +
                "ORDER BY dyn_group, dyn_name ASC";

        final List<DynamicTag> tags = select(logger, sqlGetTags, new DynamicTagRowMapper(), companyId, mailingId, includeDeletedDynTags ? 1 : 0);

        if (!tags.isEmpty()) {
            final Map<Integer, Map<Integer, DynamicTagContent>> contentMapsMap = new HashMap<>();

            for (final DynamicTag tag : tags) {
                final Map<Integer, DynamicTagContent> contentMap = new LinkedHashMap<>();
                tag.setDynContent(contentMap);
                contentMapsMap.put(tag.getId(), contentMap);
            }

            String sqlGetContents = "SELECT company_id, mailing_id, dyn_content_id, dyn_name_id, target_id, dyn_order, dyn_content" +
                    " FROM dyn_content_tbl WHERE " + makeBulkInClauseForInteger("dyn_name_id", contentMapsMap.keySet())  + " ORDER BY dyn_order, dyn_content_id ASC";
            // Retrieve contents for found tags
            List<DynamicTagContent> contents = select(logger, sqlGetContents, new DynamicTagContentRowMapper());

            for (final DynamicTagContent dynamicTagContent : contents) {
                contentMapsMap.get(dynamicTagContent.getDynNameID()).put(dynamicTagContent.getDynOrder(), dynamicTagContent);
            }
        }

        return tags;
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteAllDynTags(final int mailingId) {
        update(logger, "DELETE FROM dyn_content_tbl WHERE dyn_name_id IN (SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = ?)", mailingId);
        update(logger, "DELETE FROM dyn_name_tbl WHERE mailing_id = ?", mailingId);
    }

    /**
     * Deletes all dyn content by dyn tag name
     *
     * @return true if at least con row was affected otherwise return false
     */
    @Override
    @DaoUpdateReturnValueCheck
    public boolean cleanupContentForDynName(int mailingId, int companyId, String dynName) {
        return cleanupContentForDynNames(mailingId, companyId, Collections.singletonList(dynName));
    }

    /**
     * Deletes all dyn content by dyn tag name
     *
     * @return true if at least con row was affected otherwise return false
     */
    @Override
    @DaoUpdateReturnValueCheck
    public boolean cleanupContentForDynNames(int mailingId, int companyId, List<String> dynNames) {
        if (dynNames.isEmpty()) {
            return true;
        }

        final String deleteContentSQL = "DELETE from dyn_content_tbl" +
                " WHERE mailing_id = ? AND company_id = ? AND dyn_name_id IN " +
                " (SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ? " +
                " AND " + makeBulkInClauseForString("dyn_name", dynNames) + ")";

        return update(logger, deleteContentSQL, mailingId, companyId, mailingId, companyId) > 0;
    }

    @Override
    public void updateDynamicTags(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags) throws Exception {
        updateDynamicTags(companyID, mailingID, encodingCharset, dynamicTags, false);
    }

    private void updateDynamicTags(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags, final boolean removeUnusedContent) throws Exception {
        if (CollectionUtils.isEmpty(dynamicTags)) {
            return;
        }

        for (DynamicTag tag : dynamicTags) {
            validateDynName(tag.getDynName());
        }

        List<Object[]> parameterList = dynamicTags.stream().map(tag -> new Object[]{
                tag.getDynName(),
                tag.getGroup(),
                tag.getDynInterestGroup(),
                tag.isDisableLinkExtension() ? 1 : 0,
                mailingID,
                companyID,
                tag.getId()
        }).collect(Collectors.toList());

        final String updateSql = "UPDATE dyn_name_tbl SET change_date = current_timestamp, dyn_name = ?, dyn_group = ?, interest_group = ?, deleted = 0, no_link_extension = ? WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ?";
        batchupdate(logger, updateSql, parameterList);

        dynamicTagContentDao.saveDynamicTagContent(companyID, mailingID, encodingCharset, dynamicTags, removeUnusedContent);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void saveDynamicTags(final Mailing mailing, final Map<String, DynamicTag> dynTags) throws Exception {
        saveDynamicTags(mailing, dynTags, false);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void saveDynamicTags(final Mailing mailing, final Map<String, DynamicTag> dynTags, final boolean removeUnusedContent) throws Exception {
        int companyId = mailing.getCompanyID();
        int mailingId = mailing.getId();

        List<String> dynNames = dynTags.values().stream()
                .map(DynamicTag::getDynName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        Map<String, Integer> dynNamesToIdsMap = getDynTagIdsByName(companyId, mailingId, dynNames);

        List<DynamicTag> dynTagsForUpdate = new ArrayList<>();
        List<DynamicTag> dynTagsForCreation = new ArrayList<>();

        for (final DynamicTag tag : dynTags.values()) {
            if (StringUtils.isBlank(tag.getDynName())) {
                if (tag.getId() > 0) {
                    logger.warn("Could not update dynName ID " + tag.getId());
                    dynNamesToIdsMap.entrySet().stream().filter(entry -> entry.getValue() == tag.getId())
                            .map(Entry::getKey).findAny()
                            .ifPresent(tag::setDynName);
                } else {
                    logger.warn("Could not create a dynamic tag without an assigned name for mailingID: " + mailing.getId());
                }
            }

            if (StringUtils.isNotBlank(tag.getDynName())) {
                int tagId = tag.getId();
                if (tagId <= 0) {
                    tagId = dynNamesToIdsMap.getOrDefault(tag.getDynName(), 0);
                    tag.setId(tagId);
                }

                tag.setCompanyID(companyId);
                tag.setMailingID(mailingId);
                if (tagId > 0) {
                    dynTagsForUpdate.add(tag);
                } else {
                    dynTagsForCreation.add(tag);
                }
            }
        }

        String mailingCharset = "UTF-8";
        if (mailing.getEmailParam() != null) {
            mailingCharset = mailing.getEmailParam().getCharset();
        }

        createDynamicTags(companyId, mailingId, mailingCharset, dynTagsForCreation);
        updateDynamicTags(companyId, mailingId, mailingCharset, dynTagsForUpdate, removeUnusedContent);
    }

    @Override
    public void createDynamicTags(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags) throws Exception {
        if (CollectionUtils.isEmpty(dynamicTags)) {
            return;
        } else {
            for (DynamicTag tag : dynamicTags) {
                validateDynName(tag.getDynName());
            }

            if (isOracleDB()) {
                dynamicTags.forEach(tag -> tag.setId(selectInt(logger, "SELECT dyn_name_tbl_seq.NEXTVAL FROM DUAL")));

                List<Object[]> parameterList = dynamicTags.stream().map(tag -> new Object[]{
                        mailingID,
                        companyID,
                        tag.getId(),
                        tag.getGroup(),
                        tag.getDynName(),
                        tag.getDynInterestGroup(),
                        tag.isDisableLinkExtension() ? 1 : 0
                }).collect(Collectors.toList());
                batchupdate(logger,
                        "INSERT INTO dyn_name_tbl (mailing_id, company_id, dyn_name_id, dyn_group, dyn_name, interest_group, no_link_extension) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        parameterList);
            } else {
                List<Object[]> parameterList = dynamicTags.stream().map(tag -> new Object[]{
                        mailingID,
                        companyID,
                        tag.getGroup(),
                        tag.getDynName(),
                        tag.getDynInterestGroup(),
                        tag.isDisableLinkExtension() ? 1 : 0
                }).collect(Collectors.toList());

                int[] generatedKeys = batchInsertIntoAutoincrementMysqlTable(logger, "dyn_name_id",
                        "INSERT INTO dyn_name_tbl (mailing_id, company_id, dyn_group, dyn_name, interest_group, no_link_extension, creation_date) VALUES (?, ?, ?, ?, ?, ?, current_timestamp)",
                        parameterList);

                for (int i = 0; i < generatedKeys.length && i < dynamicTags.size(); i++) {
                    dynamicTags.get(i).setId(generatedKeys[i]);
                }
            }

            dynamicTagContentDao.saveDynamicTagContent(companyID, mailingID, encodingCharset, dynamicTags);
        }
    }

    @Override
    public void removeAbsentDynContent(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        List<Integer> idForRemoving = getIdForRemoving(oldDynamicTag, newDynamicTag);
        idForRemoving.forEach(contentId -> {
            dynamicTagContentDao.deleteContentFromMailing(oldDynamicTag.getCompanyID(), oldDynamicTag.getMailingID(), contentId);
        });
    }

    private List<Integer> getIdForRemoving(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        Set<Integer> oldIds = oldDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        Set<Integer> newIds = newDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        return oldIds.stream().filter(oldId -> !newIds.contains(oldId)).collect(Collectors.toList());
    }

    private void validateDynName(String dynName) {
        if (dynName != null && dynName.length() > 100) {
            throw new RuntimeException("Value for dyn_name_tbl.dyn_name is to long (Maximum: 100, Current: " + dynName.length() + ")");
        }
    }
}
