<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="FORWARD_ARCHIVE_CREATE" value="<%= WorkflowController.FORWARD_ARCHIVE_CREATE%>" scope="page"/>


<div id="archive-editor" data-initializer="archive-editor-initializer">
    <mvc:form action="" id="archiveForm" name="archiveForm">

        <emm:ShowByPermission token="campaign.show">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="settings_general_campaign" class="control-label">
                        <bean:message key="mailing.archive"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <select id="settings_general_campaign" class="form-control js-select" name="campaignId">
                                <logic:iterate id="campaign" collection="${campaigns}" length="500">
                                    <option value="${campaign.id}">${campaign.shortname}</option>
                                </logic:iterate>
                            </select>
                        </div>
                        <div class="input-group-btn">
                            <a class="btn btn-regular settings_campaign_in_archive disable-for-active" href="#" data-action="archive-editor-new">
                                <bean:message key="archive.new"/>
                            </a>
                        </div>
                    </div>

                </div>
            </div>
        </emm:ShowByPermission>

        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <label class="checkbox-inline">
                    <input type="checkbox" id="settings_general_in_archiv" name="archived" value="true"/>
                    <bean:message key="mailing.archived"/>
                </label>
            </div>
        </div>

        <hr>
        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <span><bean:message key="button.Cancel"/></span>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="editor-save-current">
                        <span><bean:message key="button.Apply"/></span>
                    </a>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
