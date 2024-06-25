<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="dependentsForm" type="com.agnitas.emm.core.target.form.TargetDependentsListForm"--%>
<%--@elvariable id="dependents" type="java.util.List<com.agnitas.emm.core.beans.Dependent<com.agnitas.emm.core.target.beans.TargetGroupDependentType>>"--%>
<%--@elvariable id="mailingGridTemplateMap" type="java.util.Map<java.lang.Integer, java.lang.Integer>"--%>

<mvc:form cssClass="table-box" servletRelativeAction="/target/${dependentsForm.targetId}/dependents.action" modelAttribute="dependentsForm" data-load-stop="">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "target-dependents-overview": {
                "rows-count": ${dependentsForm.numberOfRows}
            }
        }
    </script>

    <div class="table-scrollable">
        <display:table id="item" class="table table-rounded table-hover js-table" list="${dependents}"
                pagesize="${dependentsForm.numberOfRows}" sort="external" excludedParams="*"
                requestURI="/target/${dependentsForm.targetId}/dependents.action"
                partialList="true" size="${dependents.fullListSize}">

            <%@ include file="../displaytag/displaytag-properties.jspf" %>

            <display:column headerClass="js-table-sort js-filter-type" sortProperty="type" sortable="true" titleKey="default.Type">
                <c:choose>
                    <c:when test="${item.type == 'MAILING'}">
                        <emm:ShowByPermission token="mailing.show">
                            <a href="<c:url value="/mailing/${item.id}/settings.action"/>" class="hidden" data-view-row="page"></a>
                        </emm:ShowByPermission>
                        <mvc:message code="Mailings"/>
                    </c:when>
                    <c:when test="${item.type == 'REPORT'}">
                        <emm:ShowByPermission token="report.birt.show">
                            <a href="<c:url value='/statistics/report/${item.id}/view.action'/>" class="hidden" data-view-row="page"></a>
                        </emm:ShowByPermission>
                        <mvc:message code="Reports"/>
                    </c:when>
                    <c:when test="${item.type == 'EXPORT_PROFILE'}">
                        <emm:ShowByPermission token="wizard.export">
                            <a href="<c:url value='/export/${item.id}/view.action'/>" class="hidden" data-view-row="page"></a>
                        </emm:ShowByPermission>
                        <mvc:message code="export.ExportProfile"/>
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
                        <mvc:message code="mailing.searchContent"/>
                    </c:when>
                </c:choose>
            </display:column>
            <display:column headerClass="js-table-sort fit-content" property="id" sortProperty="id" sortable="true" titleKey="MailinglistID"/>
            <display:column headerClass="js-table-sort" property="shortname" sortProperty="name" sortable="true" titleKey="Name" escapeXml="true"/>
        </display:table>
    </div>
</mvc:form>
