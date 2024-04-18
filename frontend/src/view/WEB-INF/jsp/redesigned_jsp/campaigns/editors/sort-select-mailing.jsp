<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<div class="grid gap-3" style="--bs-columns: 1">
    <div>
    <%--        todo check radio -> option in js--%>
        <label class="form-label" for="mailing-status-select"><mvc:message code="Status"/></label>
        <select name="${param.statusName}" id="mailing-status-select" class="form-control js-select">
    <%--            todo check option id and data-action--%>
            <c:if test="${param.containerId != 'mailing-editor'}">
                <option value="all" id="mailings_status_all" data-action="${param.baseMailingEditor}-status-change"><mvc:message code="default.All"/></option>
            </c:if>
            <option value="${param.status1}" id="mailings_status_${param.status1}" data-action="${param.baseMailingEditor}-status-change"><mvc:message code="${param.message1}"/></option>
            <option value="${param.status2}" id="mailings_status_${param.status2}" data-action="${param.baseMailingEditor}-status-change"><mvc:message code="${param.message2}"/></option>
        </select>
    </div>
<%--<div>--%>
<%--    <c:if test="${param.disabledSelection == 'true'}">--%>
<%--        <c:set var="sortByNameOnClick" value=""/>--%>
<%--        <c:set var="sortByDateOnClick" value=""/>--%>
<%--    </c:if>--%>
<%--    <c:if test="${param.disabledSelection == 'false'}">--%>
<%--        <c:set var="sortByNameOnClick" value="data-action=\"${param.baseMailingEditor}-sort-shortname\""/>--%>
<%--        <c:set var="sortByDateOnClick"  value="data-action=\"${param.baseMailingEditor}-sort-shortname\" data-config=\"sortByDate:${param.sortByDate}\""/>--%>
<%--    </c:if>--%>
<%--    <div class="form-group">--%>
<%--        <div class="col-sm-4">--%>
<%--            <label class="form-label">--%>
<%--                <mvc:message code="workflow.mailing.sortBy"/>--%>
<%--            </label>--%>
<%--        </div>--%>
<%--        <div class="col-sm-4">--%>
<%--            <div id="shortname_sort" class="sort sort-btn">--%>
<%--                <div ${sortByNameOnClick} class="sort-icon unselectable" unselectable="on"--%>
<%--                     data-tooltip='<mvc:message code="workflow.mailing.SortAlphabetically"/>'>--%>
<%--                    A-Z--%>
<%--                </div>--%>
<%--                <div class="arrows">--%>
<%--                    <div class="arrowUp" ${sortByNameOnClick}></div>--%>
<%--                    <div class="arrowDown" ${sortByNameOnClick}></div>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--        <div class="col-sm-4">--%>
<%--            <div id="change_date_sort" class="sort">--%>
<%--                <div ${sortByDateOnClick} class="sort-icon"--%>
<%--                 data-tooltip='<mvc:message code="workflow.mailing.SortByDate"/>'>--%>
<%--                    <i class="icon icon-calendar-o"></i>--%>
<%--                </div>--%>
<%--                <div class="arrows">--%>
<%--                    <div class="arrowUp" ${sortByDateOnClick}></div>--%>
<%--                    <div class="arrowDown" ${sortByDateOnClick}></div>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </div>--%>
<%--</div>--%>

    <div>
        <label class="form-label" for="mailing-select"><mvc:message code="Mailing"/></label>
        <div class="d-flex gap-1">
            <select name="${param.selectName}" id="mailing-select" class="form-control js-select"
                    data-action="${param.baseMailingEditor}-select-change"
                    ${param.disabledSelection == 'true' ? 'disabled' : ''}>
            </select>
            <c:if test="${param.showMailingLinks and not emm:permissionAllowed('mailing.content.readonly', pageContext.request)}">
                <div id="mailing_create_edit_link">
                    <a href="#" class="btn btn-icon-sm btn-primary" ${param.disabledSelection == 'true' ? 'data-action=\"mailing-editor-new\"' : ''}>
                        <i class="icon icon-plus"></i>
                    </a>
                </div>
            </c:if>
        </div>
    </div>

<%--    <div id="${param.containerId}-security-dialog" style="display: none;">--%>
<%--        <div class="form-group">--%>
<%--            <div class="col-sm-12">--%>
<%--                <div class="well">--%>
<%--                    <mvc:message code="workflow.mailing.copyQuestion"/>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--        <div class="form-group">--%>
<%--            <div class="col-xs-12">--%>
<%--                <div class="btn-group">--%>
<%--                    <a href="#" class="btn btn-regular" data-action="${param.baseMailingEditor}-secure-cancel">--%>
<%--                        <mvc:message code="button.Cancel"/>--%>
<%--                    </a>--%>
<%--                    <a href="#" class="btn btn-regular btn-primary" data-action="mailing-editor-copy">--%>
<%--                         <mvc:message code="button.Copy"/> <mvc:message code="and"/>  <mvc:message code="button.Edit"/>--%>
<%--                    </a>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </div>--%>
</div>
