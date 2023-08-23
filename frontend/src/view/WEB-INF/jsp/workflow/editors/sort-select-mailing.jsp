<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="decision-type-container">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label">
                <bean:message key="Mailing"/>
            </label>
        </div>
        <div class="col-sm-8">
            <c:if test="${param.containerId != 'mailing-editor'}">
                <label class="radio-inline">
                    <input type="radio" name="${param.statusName}" id="mailings_status_all" value="all" checked="checked"
                           data-action="${param.baseMailingEditor}-status-change">
                    <bean:message key="default.All"/>
                </label>
            </c:if>
            <label class="radio-inline">
                <input type="radio" name="${param.statusName}" id="mailings_status_${param.status1}" class="mailing-type-status-radio"
                ${param.containerId == 'mailing-editor' ? 'checked="checked"' : ''}
                       value="${param.status1}" data-action="${param.baseMailingEditor}-status-change">
                <bean:message key="${param.message1}"/>
            </label>
            <label class="radio-inline">
                <input type="radio" name="${param.statusName}" id="mailings_status_${param.status2}" class="mailing-type-status-radio"
               value="${param.status2}" data-action="${param.baseMailingEditor}-status-change">
                <bean:message key="${param.message2}"/>
            </label>
        </div>
    </div>
</div>
<div class="editor-main-content">
    <div class="settings_campaign_form_item campaign-mailing-sort">
        <c:if test="${param.disabledSelection == 'true'}">
            <c:set var="sortByNameOnClick" value=""/>
            <c:set var="sortByDateOnClick" value=""/>
        </c:if>
        <c:if test="${param.disabledSelection == 'false'}">
            <c:set var="sortByNameOnClick" value="data-action=\"${param.baseMailingEditor}-sort-shortname\""/>
            <c:set var="sortByDateOnClick"  value="data-action=\"${param.baseMailingEditor}-sort-shortname\" data-config=\"sortByDate:${param.sortByDate}\""/>
        </c:if>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="workflow.mailing.sortBy"/>
                </label>
            </div>
            <div class="col-sm-4">
                <div id="shortname_sort" class="sort sort-btn">
                    <div ${sortByNameOnClick} class="sort-icon unselectable" unselectable="on"
                         data-tooltip='<bean:message key="workflow.mailing.SortAlphabetically"/>'>
                        A-Z
                    </div>
                    <div class="arrows">
                        <div class="arrowUp" ${sortByNameOnClick}></div>
                        <div class="arrowDown" ${sortByNameOnClick}></div>
                    </div>
                </div>
            </div>
            <div class="col-sm-4">
                <div id="change_date_sort" class="sort">
                    <div ${sortByDateOnClick} class="sort-icon"
                     data-tooltip='<bean:message key="workflow.mailing.SortByDate"/>'>
                        <i class="icon icon-calendar-o"></i>
                    </div>
                    <div class="arrows">
                        <div class="arrowUp" ${sortByDateOnClick}></div>
                        <div class="arrowDown" ${sortByDateOnClick}></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="Mailing"/>
                </label>
            </div>
            <div class="col-sm-8">
                 <select name="${param.selectName}" class="form-control js-select"
                         data-action="${param.baseMailingEditor}-select-change"
                            ${param.disabledSelection == 'true' ? 'disabled' : ''}>

                 </select>
            </div>
        </div>

        <div id="${param.containerId}-security-dialog" style="display: none;">
            <div class="form-group">
                <div class="col-sm-12">
                    <div class="well">
                        <bean:message key="workflow.mailing.copyQuestion"/>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-xs-12">
                    <div class="btn-group">
                        <a href="#" class="btn btn-regular" data-action="${param.baseMailingEditor}-secure-cancel">
                            <bean:message key="button.Cancel"/>
                        </a>
                        <a href="#" class="btn btn-regular btn-primary" data-action="mailing-editor-copy">
                             <bean:message key="button.Copy"/> <bean:message key="and"/>  <bean:message key="button.Edit"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <c:if test="${param.showMailingLinks}">
        <div class="form-group">
            <div class="col-sm-push-4 col-sm-8">
                <div id="mailing_create_edit_link" >
                    <a href="#" class="btn btn-regular" ${param.disabledSelection == 'true' ? 'data-action=\"mailing-editor-new\"' : ''}>
                        <bean:message key="dashboard.mailing.new"/>
                    </a>
                </div>
            </div>
        </div>
    </c:if>
</div>
