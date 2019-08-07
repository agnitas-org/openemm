<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.web.CampaignAction"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="campaign.delete"/>

<c:set var="ACTION_LIST" value="<%= CampaignAction.ACTION_LIST %>" scope="page"/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><bean:message key="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <logic:equal name="campaignForm" property="mailingID" value="0">
                        <bean:message key="mailing.archive"/>:&nbsp;${campaignForm.shortname}
                    </logic:equal>
                    <logic:notEqual name="campaignForm" property="mailingID" value="0">
                        <c:if test="${isTemplate eq 0}">
                            <bean:message key="Mailing"/>:&nbsp;${tmpShortname}
                        </c:if>
                        <c:if test="${isTemplate ne 0}">
                            <bean:message key="Template"/>:&nbsp;${tmpShortname}
                        </c:if>
                    </logic:notEqual>
                </h4>
            </div>
            <html:form action="/campaign.do">
                <html:hidden property="campaignID"/>
                <html:hidden property="mailingID"/>
                <html:hidden property="action"/>
                <input type="hidden" id="kill" name="kill" value="true"/>

                <div class="modal-body">
                    <logic:equal name="campaignForm" property="mailingID" value="0">
                        <bean:message key="campaign.DeleteCampaignQuestion"/>
                    </logic:equal>
                    <logic:notEqual name="campaignForm" property="mailingID" value="0">
                        <c:if test="${isTemplate eq 0}">
                            <bean:message key="mailing.MailingDeleteQuestion"/>
                        </c:if>
                        <c:if test="${isTemplate ne 0}">
                            <bean:message key="mailing.Delete_Template_Question"/>
                        </c:if>
                    </logic:notEqual>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Delete"/></span>
                        </button>
                    </div>
                </div>

            </html:form>
        </div>
    </div>
</div>
