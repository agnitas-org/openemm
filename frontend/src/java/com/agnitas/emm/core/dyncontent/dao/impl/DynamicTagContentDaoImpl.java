/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dyncontent.dao.impl;

import com.agnitas.beans.DynamicTag;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerException;
import com.agnitas.util.SpecialCharactersWorker;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.impl.DynamicTagContentImpl;
import com.agnitas.emm.core.dyncontent.dao.DynamicTagContentDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.util.AgnUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DynamicTagContentDaoImpl extends BaseDaoImpl implements DynamicTagContentDao {

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
		
		// Check for unallowed html tags
		try {
			HtmlChecker.checkForNoHtmlTags(dynamicTagContent.getDynName());
		} catch(HtmlCheckerException e) {
			throw new IllegalStateException("Mailing content name contains unallowed HTML tags");
		}
		try {
			HtmlChecker.checkForUnallowedHtmlTags(dynamicTagContent.getDynContent(), true);
		} catch(HtmlCheckerException e) {
			throw new IllegalStateException("Mailing content description contains unallowed HTML tags");
		}

		if (isExisting(dynamicTagContent.getCompanyID(), dynamicTagContent.getMailingID(), dynamicTagContent.getDynNameID(), dynamicTagContent.getId())) {
			final String updateSql = "UPDATE dyn_content_tbl SET dyn_content = ?, dyn_order = ?, target_id = ? WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ? AND dyn_content_id = ?";
			update(updateSql, dynamicTagContent.getDynContent(), dynamicTagContent.getDynOrder(), dynamicTagContent.getTargetID(), dynamicTagContent.getMailingID(), dynamicTagContent.getCompanyID(), dynamicTagContent.getDynNameID(), dynamicTagContent.getId());
		} else {
			int dynContentId;
			if (isOracleDB()) {
				dynContentId = selectInt("SELECT dyn_content_tbl_seq.NEXTVAL FROM DUAL");
				final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
				update(insertSql, dynamicTagContent.getMailingID(), dynamicTagContent.getCompanyID(), dynamicTagContent.getDynNameID(), dynContentId, dynamicTagContent.getDynContent(), dynamicTagContent.getDynOrder(), dynamicTagContent.getTargetID());
			} else {
				final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?)";
				dynContentId = insertIntoAutoincrementMysqlTable("dyn_content_id", insertSql, dynamicTagContent.getMailingID(), dynamicTagContent.getCompanyID(), dynamicTagContent.getDynNameID(), dynamicTagContent.getDynContent(), dynamicTagContent.getDynOrder(), dynamicTagContent.getTargetID());
			}
			dynamicTagContent.setId(dynContentId);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteContent(int companyID, int contentID) {
    	String deleteContentSQL = "DELETE from dyn_content_tbl WHERE dyn_content_id = ? AND company_id = ?";
    	return update(deleteContentSQL, contentID, companyID) > 0;
	}

	@Override
	public DynamicTagContent getContent(int companyId, int contentId) {
		String sql = "SELECT dyn_content_tbl.dyn_content_id, dyn_content_tbl.target_id, dyn_content_tbl.dyn_order, dyn_content_tbl.dyn_content, dyn_name_tbl.dyn_name, dyn_name_tbl.mailing_id, dyn_content_tbl.company_id, dyn_content_tbl.dyn_name_id " +
				"FROM dyn_content_tbl, dyn_name_tbl " +
				"WHERE dyn_content_tbl.dyn_content_id = ? " +
				"AND dyn_content_tbl.company_id = ? " +
				"AND dyn_content_tbl.dyn_name_id = dyn_name_tbl.dyn_name_id";

		return selectObjectDefaultNull(sql, dynContentRowMapper, contentId, companyId);
	}

	@Override
	public List<DynamicTagContent> getContentList(int companyId, int mailingId) {
		String sql = "SELECT dyn_content_tbl.dyn_content_id, dyn_content_tbl.target_id, dyn_content_tbl.dyn_order, dyn_content_tbl.dyn_content, dyn_name_tbl.dyn_name, dyn_name_tbl.mailing_id, dyn_content_tbl.company_id, dyn_content_tbl.dyn_name_id " +
				"FROM dyn_content_tbl, dyn_name_tbl " +
				"WHERE dyn_content_tbl.dyn_name_id IN (SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ?) " +
				"AND dyn_content_tbl.company_id = ? " +
				"AND dyn_content_tbl.dyn_name_id = dyn_name_tbl.dyn_name_id";

		return select(sql, dynContentRowMapper, mailingId, companyId, companyId);
	}

	@Override
	public boolean isExisting(int companyId, int mailingId, int dynNameId, int dynContentId) {
		String selectSql = "SELECT COUNT(dyn_content_id) FROM dyn_content_tbl WHERE company_id = ? AND mailing_id = ? " +
				"AND dyn_name_id = ? AND dyn_content_id = ?";
		return selectInt(selectSql, companyId, mailingId, dynNameId, dynContentId) > 0;
	}

	@Override
	public boolean isContentValueNotEmpty(int companyId, int mailingId, int dynNameId) {
		String selectSql = "SELECT COUNT(dyn_content_id) FROM dyn_content_tbl WHERE company_id = ? AND mailing_id = ?" +
				" AND dyn_name_id = ? AND dyn_content IS NOT NULL";
		return selectInt(selectSql, companyId, mailingId, dynNameId) > 0;
	}
	
	@Override
	public Map<Integer, List<Integer>> getExistingDynContentForDynName(int companyId, int mailingId, List<Integer> dynamicTags) {
		String selectSql = "SELECT dyn_name_id, dyn_content_id FROM dyn_content_tbl WHERE company_id = ? AND mailing_id = ? " +
				"AND " + makeBulkInClauseForInteger("dyn_name_id", dynamicTags);
		
		Map<Integer, List<Integer>> dynContentMap = new HashMap<>();
		query(selectSql, new DynTagContentMapCallback(dynContentMap), companyId, mailingId);
		
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
    public void saveDynamicTagContent(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags) {
		saveDynamicTagContent(companyID, mailingID, encodingCharset, dynamicTags, false);
	}
	
	@Override
    @DaoUpdateReturnValueCheck
    public void saveDynamicTagContent(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags, boolean removeUnusedContent) {
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

    private void saveDynContent(DynamicTag dynTag, Map<Integer, DynamicTagContent> contents, List<Integer> existingContentIds, boolean removeUnusedContent) {
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

    private void batchInsertDynContent(List<DynamicTagContent> dynamicTagContents) {
		for (DynamicTagContent dynamicTagContent : dynamicTagContents) {
			// Check for unallowed html tags
			try {
				HtmlChecker.checkForNoHtmlTags(dynamicTagContent.getDynName());
			} catch(HtmlCheckerException e) {
				throw new IllegalStateException("Mailing content name contains unallowed HTML tags");
			}
			try {
				HtmlChecker.checkForUnallowedHtmlTags(dynamicTagContent.getDynContent(), true);
			} catch(HtmlCheckerException e) {
				throw new IllegalStateException("Mailing content description contains unallowed HTML tags");
			}
		}
		
        if (isOracleDB()) {
            dynamicTagContents.forEach(entry -> {
                entry.setId(selectInt("SELECT dyn_content_tbl_seq.NEXTVAL FROM DUAL"));
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
            batchupdate(insertSql, paramList);
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
            int[] generatedKeys = batchInsertIntoAutoincrementMysqlTable("dyn_content_id", insertSql, paramList);

            for (int i = 0; i < generatedKeys.length && i < dynamicTagContents.size(); i++) {
                dynamicTagContents.get(i).setId(generatedKeys[i]);
            }
        }
    }

    private void batchUpdateDynContent(List<DynamicTagContent> dynamicTagContents) {
		for (DynamicTagContent dynamicTagContent : dynamicTagContents) {
			// Check for unallowed html tags
			try {
				HtmlChecker.checkForNoHtmlTags(dynamicTagContent.getDynName());
			} catch(HtmlCheckerException e) {
				throw new IllegalStateException("Mailing content name contains unallowed HTML tags");
			}
			try {
				HtmlChecker.checkForUnallowedHtmlTags(dynamicTagContent.getDynContent(), true);
			} catch(HtmlCheckerException e) {
				throw new IllegalStateException("Mailing content description contains unallowed HTML tags");
			}
		}
		
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
        batchupdate(updateSql, paramList);
    }

	@Override
    @DaoUpdateReturnValueCheck
    public boolean deleteContentFromMailing(final int companyId, final int mailingId, final int contentId) {
        final int affectedRows = update("DELETE FROM dyn_content_tbl WHERE dyn_content_id = ? AND mailing_id = ? AND company_id = ?", contentId, mailingId, companyId);
        return affectedRows > 0;
    }

	@Override
	public List<Integer> findTargetDependentMailingsContents(int targetGroupId, int companyId) {
		String query = "SELECT c.dyn_content_id " +
				"FROM mailing_tbl m INNER JOIN dyn_content_tbl c " +
				"    ON c.mailing_id = m.mailing_id " +
				"WHERE m.company_id = ? AND m.deleted = 0 AND c.target_id = ?";

		return select(query, IntegerRowMapper.INSTANCE, companyId, targetGroupId);
	}

	@Override
	public List<Integer> filterContentsOfNotSentMailings(List<Integer> contentsIds) {
		String contentsIdsInClause = makeBulkInClauseForInteger("c.dyn_content_id", contentsIds);

		String query = "SELECT c.dyn_content_id " +
				"FROM dyn_content_tbl c " +
				"         INNER JOIN mailing_tbl m ON c.mailing_id = m.mailing_id " +
				"WHERE m.work_status != ? " +
				"  AND " + contentsIdsInClause;

		return select(query, IntegerRowMapper.INSTANCE, MailingStatus.SENT.getDbKey());
	}
}
