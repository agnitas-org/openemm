/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.dao.impl;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.report.dao.RecipientHistoryReportDao;
import com.agnitas.emm.core.report.dto.RecipientIpAddressesOfClicksHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientMailingLinkClicksHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientMailingOpeningsHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientSoftBounceHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientUserFormLinkClicksHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientWorkflowReactionsHistoryDto;
import com.agnitas.emm.core.report.dto.impl.RecipientIpAddressesOfClicksHistoryDtoImpl;
import com.agnitas.emm.core.report.dto.impl.RecipientMailingLinkClicksHistoryDtoImpl;
import com.agnitas.emm.core.report.dto.impl.RecipientMailingOpeningsHistoryDtoImpl;
import com.agnitas.emm.core.report.dto.impl.RecipientSoftBounceHistoryDtoImpl;
import com.agnitas.emm.core.report.dto.impl.RecipientUserFormLinkClicksHistoryDtoImpl;
import com.agnitas.emm.core.report.dto.impl.RecipientWorkflowReactionsHistoryDtoImpl;
import com.agnitas.dao.impl.BaseDaoImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

@Component
public class RecipientHistoryReportDaoImpl extends BaseDaoImpl implements RecipientHistoryReportDao {

    private static final String MAILING_ID_COL = "mailing_id";
    private static final String SHORTNAME_COL = "shortname";
    private static final String TIMESTAMP_COL = "timestamp";
    private static final String URL_ID_COL = "url_id";
    private static final String URL_COL = "full_url";
    private static final String IP_ADR_COL = "ip_adr";
    private static final String FORM_NAME_COL = "formname";

    public RecipientHistoryReportDaoImpl(@Qualifier("dataSource") DataSource dataSource, JavaMailService javaMailService) {
        super(dataSource, javaMailService);
    }

    @Override
    public List<RecipientSoftBounceHistoryDto> getSoftBouncesHistory(int recipientId, int companyId) {
        return select("SELECT bounce_tbl.timestamp, mailing_tbl.mailing_id, mailing_tbl.shortname" +
                        " FROM bounce_tbl JOIN mailing_tbl ON mailing_tbl.mailing_id = bounce_tbl.mailing_id" +
                        " WHERE bounce_tbl.company_id = ? AND bounce_tbl.detail < 500 AND bounce_tbl.customer_id = ?",
                (rs, i) -> new RecipientSoftBounceHistoryDtoImpl(
                        rs.getInt(MAILING_ID_COL),
                        rs.getString(SHORTNAME_COL),
                        rs.getDate(TIMESTAMP_COL)
                ), companyId, recipientId);
    }

    @Override
    public List<RecipientMailingOpeningsHistoryDto> getOpeningsHistory(int recipientId, int companyId) {
        return select(
                " SELECT opl.creation, opl.mailing_id, mailing_tbl.shortname" +
                " FROM onepixellog_device_" + companyId + "_tbl opl" +
                " JOIN mailing_tbl ON mailing_tbl.mailing_id = opl.mailing_id WHERE customer_id = ?",
                (rs, i) -> new RecipientMailingOpeningsHistoryDtoImpl(
                        rs.getInt(MAILING_ID_COL),
                        rs.getString(SHORTNAME_COL),
                        rs.getDate("creation")
                ), recipientId);
    }

    @Override
    public List<RecipientMailingLinkClicksHistoryDto> getMailingClicksHistory(int recipientId, int companyId) {
        return select(" SELECT rlog.timestamp, rlog.mailing_id, m.shortname, rlog.url_id, rdir.full_url" +
                " FROM rdirlog_" + companyId + "_tbl rlog" +
                " JOIN mailing_tbl m ON m.mailing_id = rlog.mailing_id" +
                " JOIN rdir_url_tbl rdir ON rdir.url_id = rlog.url_id WHERE customer_id = ?", (rs, i) -> {
            RecipientMailingLinkClicksHistoryDto dto = new RecipientMailingLinkClicksHistoryDtoImpl();
            dto.setEntityId(rs.getInt(MAILING_ID_COL));
            dto.setEntityName(rs.getString(SHORTNAME_COL));
            dto.setDate(rs.getDate(TIMESTAMP_COL));
            dto.setLinkId(rs.getInt(URL_ID_COL));
            dto.setLinkUrl(rs.getString(URL_COL));
            return dto;
        }, recipientId);
    }

    @Override
    public List<RecipientUserFormLinkClicksHistoryDto> getUserFormClicksHistory(int recipientId, int companyId) {
        return select(
                " SELECT rlog.timestamp, rlog.ip_adr, rlog.form_id, userform_tbl.formname, rlog.url_id, rurl.full_url" +
                " FROM rdirlog_userform_" + companyId + "_tbl rlog" +
                " JOIN userform_tbl ON userform_tbl.form_id = rlog.form_id" +
                " JOIN rdir_url_userform_tbl rurl ON rurl.url_id = rlog.url_id" +
                " WHERE customer_id = ?", (rs, i) -> {
                    RecipientUserFormLinkClicksHistoryDto dto = new RecipientUserFormLinkClicksHistoryDtoImpl();
                    dto.setEntityId(rs.getInt(URL_ID_COL));
                    dto.setEntityName(rs.getString(FORM_NAME_COL));
                    dto.setDate(rs.getDate(TIMESTAMP_COL));
                    dto.setLinkId(rs.getInt(URL_ID_COL));
                    dto.setLinkUrl(rs.getString(URL_COL));
                    dto.setIpAddress(rs.getString(IP_ADR_COL));
                    return dto;
                }, recipientId);
    }

    @Override
    public List<RecipientWorkflowReactionsHistoryDto> getWorkflowReactionsHistory(int recipientId, int companyId) {
        return select("SELECT step_date, case_id, reaction_id FROM workflow_reaction_out_tbl" +
                " WHERE company_id = ? AND customer_id = ?", (rs, i) -> new RecipientWorkflowReactionsHistoryDtoImpl(
                rs.getInt("reaction_id"),
                rs.getInt("case_id"),
                rs.getDate("step_date")
        ), companyId, recipientId);
    }

    @Override
    public List<RecipientIpAddressesOfClicksHistoryDto> getIpAddressesOfClicksHistory(int recipientId, int companyId) {
        return select(
                " SELECT rlog.timestamp, rlog.ip_adr, rlog.mailing_id, m.shortname, rlog.url_id, rurl.full_url" +
                " FROM rdirlog_" + companyId + "_tbl rlog" +
                " JOIN mailing_tbl m ON m.mailing_id = rlog.mailing_id" +
                " JOIN rdir_url_tbl rurl ON rurl.url_id = rlog.url_id" +
                " WHERE customer_id = ?", (rs, i) -> {
                    RecipientIpAddressesOfClicksHistoryDto dto = new RecipientIpAddressesOfClicksHistoryDtoImpl();
                    dto.setEntityId(rs.getInt(URL_ID_COL));
                    dto.setEntityName(rs.getString(SHORTNAME_COL));
                    dto.setDate(rs.getDate(TIMESTAMP_COL));
                    dto.setLinkId(rs.getInt(URL_ID_COL));
                    dto.setLinkUrl(rs.getString(URL_COL));
                    dto.setIpAddress(rs.getString(IP_ADR_COL));
                    return dto;
                }, recipientId);
    }
}
