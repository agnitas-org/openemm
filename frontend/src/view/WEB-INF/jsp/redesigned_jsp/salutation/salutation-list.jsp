<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.salutation.form.SalutationForm"--%>
<%--@elvariable id="salutations" type="org.agnitas.beans.impl.PaginatedListImpl<SalutationEntry>"--%>
<%--@elvariable id="salutation" type="org.agnitas.beans.SalutationEntry"--%>

<mvc:message var="salutationDeleteMessage" code="salutation.SalutationDelete"/>

<div class="tiles-container">
    <mvc:form servletRelativeAction="/salutation/list.action" modelAttribute="form" cssClass="tile">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "salutation-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>

        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table" pagesize="${form.numberOfRows}"
                                   id="salutation" name="salutations" sort="external" requestURI="/salutation/list.action"
                                   excludedParams="*" size="${salutations.fullListSize}">

                        <%@ include file="../displaytag/displaytag-properties.jspf" %>

                        <display:column property="titleId" titleKey="MailinglistID" headerClass="js-table-sort fit-content"
                                        sortProperty="title_id" sortable="true"/>
                        <display:column class="description" headerClass="js-table-sort" property="description"
                                        sortProperty="description" titleKey="settings.FormOfAddress" sortable="true"/>

                        <display:column headerClass="fit-content">
                            <a href='<c:url value="/salutation/${salutation.titleId}/view.action"/>' class="hidden" data-view-row></a>

                            <c:if test="${salutation.companyID ne 0}">
                                <emm:ShowByPermission token="salutation.delete">
                                    <a href='<c:url value="/salutation/${salutation.titleId}/delete.action"/>' class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${salutationDeleteMessage}">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </emm:ShowByPermission>
                            </c:if>
                        </display:column>

                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
