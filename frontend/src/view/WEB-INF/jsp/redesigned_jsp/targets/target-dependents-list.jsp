<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="dependentsForm" type="com.agnitas.emm.core.target.form.TargetDependentsListForm"--%>
<%--@elvariable id="dependents" type="java.util.List<com.agnitas.emm.core.beans.Dependent<com.agnitas.emm.core.target.beans.TargetGroupDependentType>>"--%>
<%--@elvariable id="mailingGridTemplateMap" type="java.util.Map<java.lang.Integer, java.lang.Integer>"--%>

<mvc:form cssClass="table-wrapper" servletRelativeAction="/target/${dependentsForm.targetId}/dependents.action" modelAttribute="dependentsForm">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "target-dependents-overview": {
                "rows-count": ${dependentsForm.numberOfRows}
            }
        }
    </script>

    <div class="table-wrapper__header">
        <h1 class="table-wrapper__title"><mvc:message code="default.usedIn" /></h1>
        <div class="table-wrapper__controls">
            <%@include file="../common/table/toggle-truncation-btn.jspf" %>
            <jsp:include page="../common/table/entries-label.jsp">
                <jsp:param name="totalEntries" value="${dependents.fullListSize}"/>
            </jsp:include>
        </div>
    </div>

    <div class="table-wrapper__body">
        <agnDisplay:table id="item" class="table table--borderless table-hover js-table" list="${dependents}"
                pagesize="${dependentsForm.numberOfRows}" sort="external" excludedParams="*"
                requestURI="/target/${dependentsForm.targetId}/dependents.action"
                partialList="true" size="${dependents.fullListSize}">

            <%@ include file="../common/displaytag/displaytag-properties.jspf" %>

            <agnDisplay:column headerClass="js-table-sort fit-content" sortProperty="type" sortable="true" titleKey="default.Type">
                <c:choose>
                    <c:when test="${item.type == 'MAILING'}">
                        <emm:ShowByPermission token="mailing.show">
                            <a href="<c:url value="/mailing/${item.id}/settings.action"/>" class="hidden" data-view-row="page"></a>
                        </emm:ShowByPermission>
                        <span><mvc:message code="Mailings"/></span>
                    </c:when>
                    <c:when test="${item.type == 'REPORT'}">
                        <emm:ShowByPermission token="report.birt.show">
                            <a href="<c:url value='/statistics/report/${item.id}/view.action'/>" class="hidden" data-view-row="page"></a>
                        </emm:ShowByPermission>
                        <span><mvc:message code="Reports"/></span>
                    </c:when>
                    <c:when test="${item.type == 'EXPORT_PROFILE'}">
                        <emm:ShowByPermission token="wizard.export">
                            <a href="<c:url value='/export/${item.id}/view.action'/>" class="hidden" data-view-row="page"></a>
                        </emm:ShowByPermission>
                        <span><mvc:message code="export.ExportProfile"/></span>
                    </c:when>
                    <c:when test="${item.type == 'MAILING_CONTENT'}">
                        <emm:ShowByPermission token="mailing.content.show">
                            <c:set var="templateId" value="${mailingGridTemplateMap[item.id]}"/>
                            <c:choose>
                                <c:when test="${templateId > 0}">
                                    <a href="<c:url value='/layoutbuilder/template/${templateId}/view.action'/>" class="hidden" data-view-row="page"></a>
                                </c:when>
                                <c:otherwise>
                                    <c:url var="contentViewLink" value='/mailing/content/${item.id}/view.action'/>
                                    <a href="${contentViewLink}" class="hidden" data-view-row="page"></a>
                                </c:otherwise>
                            </c:choose>
                        </emm:ShowByPermission>
                        <span><mvc:message code="mailing.searchContent"/></span>
                    </c:when>
                </c:choose>
            </agnDisplay:column>
            <agnDisplay:column headerClass="js-table-sort fit-content" sortProperty="id" sortable="true" titleKey="MailinglistID">
                <span>${item.id}</span>
            </agnDisplay:column>
            <agnDisplay:column headerClass="js-table-sort" sortProperty="name" sortable="true" titleKey="Name">
                <span>${fn:escapeXml(item.shortname)}</span>
            </agnDisplay:column>
        </agnDisplay:table>
    </div>
</mvc:form>
