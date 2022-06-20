/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.dao.DynamicTagContentDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.DynamicTag;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.util.SpecialCharactersWorker;

public class DynamicTagContentDaoImpl extends BaseDaoImpl implements DynamicTagContentDao {
	private static final transient Logger logger = LogManager.getLogger(DynamicTagContentDaoImpl.class);

	private final RowMapper<DynamicTagContent> dynContentRowMapper = (resultSet, rowNum) -> {
		DynamicTagContent content = new DynamicTagContentImpl();

		content.setId(resultSet.getInt("dyn_content_id"));
		content.setTargetID(resultSet.getInt("target_id"));
		content.setDynOrder(resultSet.getInt("dyn_order"));
		content.setDynContent(resultSet.getString("dyn_content"));
		content.setDynName(resultSet.getString("dyn_name"));
		content.setMailingID(resultSet.getInt("mailing_id"));
		content.setDynNameID(resultSet.getInt("dyn_name_id"));
		content.setCompanyID(resultSet.getInt("company_id"));

		return content;
	};

	@Override
	@DaoUpdateReturnValueCheck
	public void saveDynamicContent(DynamicTagContent dynamicTagContent, String mailingCharset) {
		if (AgnUtils.DEFAULT_MAILING_TEXT_DYNNAME.equals(dynamicTagContent.getDynName())) {
			dynamicTagContent.setDynContent(SpecialCharactersWorker.processString(dynamicTagContent.getDynContent(), mailingCharset));
		}

		if (isExisting(dynamicTagContent.getCompanyID(), dynamicTagContent.getMailingID(), dynamicTagContent.getDynNameID(), dynamicTagContent.getId())) {
			final String updateSql = "UPDATE dyn_content_tbl SET dyn_content = ?, dyn_order = ?, target_id = ? WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ? AND dyn_content_id = ?";
			update(logger, updateSql, dynamicTagContent.getDynContent(), dynamicTagContent.getDynOrder(), dynamicTagContent.getTargetID(), dynamicTagContent.getMailingID(), dynamicTagContent.getCompanyID(), dynamicTagContent.getDynNameID(), dynamicTagContent.getId());
		} else {
			int dynContentId;
			if (isOracleDB()) {
				dynContentId = selectInt(logger, "SELECT dyn_content_tbl_seq.NEXTVAL FROM DUAL");
				final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
				update(logger, insertSql, dynamicTagContent.getMailingID(), dynamicTagContent.getCompanyID(), dynamicTagContent.getDynNameID(), dynContentId, dynamicTagContent.getDynContent(), dynamicTagContent.getDynOrder(), dynamicTagContent.getTargetID());
			} else {
				final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?)";
				dynContentId = insertIntoAutoincrementMysqlTable(logger, "dyn_content_id", insertSql, dynamicTagContent.getMailingID(), dynamicTagContent.getCompanyID(), dynamicTagContent.getDynNameID(), dynamicTagContent.getDynContent(), dynamicTagContent.getDynOrder(), dynamicTagContent.getTargetID());
			}
			dynamicTagContent.setId(dynContentId);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteContent(@VelocityCheck int companyID, int contentID) {
    	String deleteContentSQL = "DELETE from dyn_content_tbl WHERE dyn_content_id = ? AND company_id = ?";
    	return update(logger, deleteContentSQL, contentID, companyID) > 0;
	}

	@Override
	public DynamicTagContent getContent(@VelocityCheck int companyId, int contentId) {
		String sql = "SELECT dyn_content_tbl.dyn_content_id, dyn_content_tbl.target_id, dyn_content_tbl.dyn_order, dyn_content_tbl.dyn_content, dyn_name_tbl.dyn_name, dyn_name_tbl.mailing_id, dyn_content_tbl.company_id, dyn_content_tbl.dyn_name_id " +
				"FROM dyn_content_tbl, dyn_name_tbl " +
				"WHERE dyn_content_tbl.dyn_content_id = ? " +
				"AND dyn_content_tbl.company_id = ? " +
				"AND dyn_content_tbl.dyn_name_id = dyn_name_tbl.dyn_name_id";

		return selectObjectDefaultNull(logger, sql, dynContentRowMapper, contentId, companyId);
	}

	@Override
	public List<DynamicTagContent> getContentList(@VelocityCheck int companyId, int mailingId) {
		String sql = "SELECT dyn_content_tbl.dyn_content_id, dyn_content_tbl.target_id, dyn_content_tbl.dyn_order, dyn_content_tbl.dyn_content, dyn_name_tbl.dyn_name, dyn_name_tbl.mailing_id, dyn_content_tbl.company_id, dyn_content_tbl.dyn_name_id " +
				"FROM dyn_content_tbl, dyn_name_tbl " +
				"WHERE dyn_content_tbl.dyn_name_id IN (SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ?) " +
				"AND dyn_content_tbl.company_id = ? " +
				"AND dyn_content_tbl.dyn_name_id = dyn_name_tbl.dyn_name_id";

		return select(logger, sql, dynContentRowMapper, mailingId, companyId, companyId);
	}

	@Override
	public boolean isExisting(@VelocityCheck int companyId, int mailingId, int dynNameId, int dynContentId) {
		String selectSql = "SELECT COUNT(dyn_content_id) FROM dyn_content_tbl WHERE company_id = ? AND mailing_id = ? " +
				"AND dyn_name_id = ? AND dyn_content_id = ?";
		return selectInt(logger, selectSql, companyId, mailingId, dynNameId, dynContentId) > 0;
	}

	@Override
	public boolean isContentValueNotEmpty(@VelocityCheck int companyId, int mailingId, int dynNameId) {
		String selectSql = "SELECT COUNT(dyn_content_id) FROM dyn_content_tbl WHERE company_id = ? AND mailing_id = ?" +
				" AND dyn_name_id = ? AND dyn_content IS NOT NULL";
		return selectInt(logger, selectSql, companyId, mailingId, dynNameId) > 0;
	}
	
	@Override
	public Map<Integer, List<Integer>> getExistingDynContentForDynName(int companyId, int mailingId, List<Integer> dynamicTags) {
		String selectSql = "SELECT dyn_name_id, dyn_content_id FROM dyn_content_tbl WHERE company_id = ? AND mailing_id = ? " +
				"AND " + makeBulkInClauseForInteger("dyn_name_id", dynamicTags);
		
		Map<Integer, List<Integer>> dynContentMap = new HashMap<>();
		query(logger, selectSql, new DynTagContentMapCallback(dynContentMap), companyId, mailingId);
		
		return dynContentMap;
	}
	
	private static class DynTagContentMapCallback implements RowCallbackHandler {
		private Map<Integer, List<Integer>> dynContentMap;

		public DynTagContentMapCallback(Map<Integer, List<Integer>> dynContentMap) {
			this.dynContentMap = Objects.requireNonNull(dynContentMap);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			int dynNameId = rs.getInt("dyn_name_id");
			List<Integer> contentIds = dynContentMap.getOrDefault(dynNameId, new ArrayList<>());
			contentIds.add(rs.getInt("dyn_content_id"));
			dynContentMap.put(dynNameId, contentIds);
		}
	}

	@Override
    @DaoUpdateReturnValueCheck
    public void saveDynamicTagContent(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags) throws Exception {
		saveDynamicTagContent(companyID, mailingID, encodingCharset, dynamicTags, false);
	}
	
	@Override
    @DaoUpdateReturnValueCheck
    public void saveDynamicTagContent(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags, final boolean removeUnusedContent) throws Exception {
        Map<Integer, List<Integer>> existingDynContentForDynName = getExistingDynContentForDynName(companyID, mailingID, dynamicTags.stream().map(DynamicTag::getId).collect(Collectors.toList()));

        for (DynamicTag dynamicTag : dynamicTags) {
            String dynamicName = dynamicTag.getDynName();
            if (AgnUtils.DEFAULT_MAILING_TEXT_DYNNAME.equals(dynamicName)) {
                dynamicTag.getDynContent().values().forEach(content -> {
                    //update dyn content according to mailing charset
                    content.setDynContent(SpecialCharactersWorker.processString(content.getDynContent(), encodingCharset));
                });
            }

            saveDynContent(dynamicTag, dynamicTag.getDynContent(), existingDynContentForDynName.getOrDefault(dynamicTag.getId(), Collections.emptyList()), removeUnusedContent);
        }
    }

    private void saveDynContent(DynamicTag dynTag, Map<Integer, DynamicTagContent> contents, final List<Integer> existingContentIds, final boolean removeUnusedContent) throws Exception {
        List<DynamicTagContent> tagContentForUpdate = new ArrayList<>();
        List<DynamicTagContent> tagContentForCreation = new ArrayList<>();
        
        List<Integer> contentIdsToRemove = new ArrayList<>(existingContentIds);

        contents.values().forEach(entry -> {
            entry.setCompanyID(dynTag.getCompanyID());
            entry.setMailingID(dynTag.getMailingID());
            entry.setDynNameID(dynTag.getId());
            if (existingContentIds.contains(entry.getId())) {
                tagContentForUpdate.add(entry);
            } else {
                tagContentForCreation.add(entry);
            }
            
            contentIdsToRemove.remove(Integer.valueOf(entry.getId()));
        });

        batchUpdateDynContent(tagContentForUpdate);
        batchInsertDynContent(tagContentForCreation);
        
        if(removeUnusedContent) {
	        // Remove all unused contents
	        for(final int contentId : contentIdsToRemove) {
	        	this.deleteContent(dynTag.getCompanyID(), contentId);
	        }
        }
    }

    private void batchInsertDynContent(List<DynamicTagContent> dynamicTagContents) throws Exception {
        if (isOracleDB()) {
            dynamicTagContents.forEach(entry -> {
                entry.setId(selectInt(logger, "SELECT dyn_content_tbl_seq.NEXTVAL FROM DUAL"));
            });

            List<Object[]> paramList = dynamicTagContents.stream().map(entry -> new Object[]{
                    entry.getMailingID(),
                    entry.getCompanyID(),
                    entry.getDynNameID(),
                    entry.getId(),
                    entry.getDynContent(),
                    entry.getDynOrder(),
                    entry.getTargetID()
            }).collect(Collectors.toList());


            final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            batchupdate(logger, insertSql, paramList);
        } else {
            List<Object[]> paramList = dynamicTagContents.stream().map(entry -> new Object[]{
                    entry.getMailingID(),
                    entry.getCompanyID(),
                    entry.getDynNameID(),
                    entry.getDynContent(),
                    entry.getDynOrder(),
                    entry.getTargetID()
            }).collect(Collectors.toList());


            final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?)";
            int[] generatedKeys = batchInsertIntoAutoincrementMysqlTable(logger, "dyn_content_id", insertSql, paramList);

            for (int i = 0; i < generatedKeys.length && i < dynamicTagContents.size(); i++) {
                dynamicTagContents.get(i).setId(generatedKeys[i]);
            }
        }
    }

    private void batchUpdateDynContent(List<DynamicTagContent> dynamicTagContents) {
        List<Object[]> paramList = dynamicTagContents.stream().map(entry -> new Object[]{
                entry.getDynContent(),
                entry.getDynOrder(),
                entry.getTargetID(),
                entry.getMailingID(),
                entry.getCompanyID(),
                entry.getDynNameID(),
                entry.getId()
        }).collect(Collectors.toList());


        final String updateSql = "UPDATE dyn_content_tbl SET dyn_content = ?, dyn_order = ?, target_id = ? WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ? AND dyn_content_id = ?";
        batchupdate(logger, updateSql, paramList);
    }

	@Override
    @DaoUpdateReturnValueCheck
    public boolean deleteContentFromMailing(@VelocityCheck final int companyId, final int mailingId, final int contentId) {
        final int affectedRows = update(logger, "DELETE FROM dyn_content_tbl WHERE dyn_content_id = ? AND mailing_id = ? AND company_id = ?", contentId, mailingId, companyId);
        return affectedRows > 0;
    }
}
