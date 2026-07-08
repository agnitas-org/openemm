<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"  %>

<%--@elvariable id="form" type="com.agnitas.web.forms.PaginationForm"--%>
<%--@elvariable id="profile" type="com.agnitas.beans.ImportProfile"--%>
<%--@elvariable id="defaultProfileId" type="java.lang.Integer"--%>

<c:url var="setDefaultUrl" value="/import-profile/setDefault.action"/>
<mvc:message var="deleteMsg" code="recipient.importprofile.delete" />

<c:set var="deleteAllowed" value="${emm:permissionAllowed('import.delete', pageContext.request)}" />
<c:url var="deleteUrl" value="/import-profile/delete.action" />

<div class="tiles-container">
    <mvc:form servletRelativeAction="/import-profile/list.action" modelAttribute="form" cssClass="tile" method="GET">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "import-profile-overview": {
                    "rows-count": ${form.numberOfRows}
                }
            }
        </script>

        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${deleteAllowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deleteMsg}" data-form-url='${deleteUrl}' data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>

                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${profiles.fullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="profile" modelAttribute="profiles" cssClass="table table-hover table--borderless js-table">

                        <c:if test="${deleteAllowed}">
                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="ids" value="${profile.id}" autocomplete="off" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column property="name" titleKey="Name" sortable="true" />

                        <emm:column titleKey="recipient.importprofile.defaultprofile" headerClass="fit-content">
                            <div class="form-check form-switch">
                                <c:choose>
                                    <c:when test="${defaultProfileId == profile.id}">
                                        <input type="checkbox" role="switch" class="form-check-input" disabled checked>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="checkbox" role="switch" class="form-check-input" data-form-url="${setDefaultUrl}" data-form-method="POST" data-form-set="id: ${profile.id}" data-form-submit>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <a href="<c:url value="/import-profile/${profile.id}/view.action"/>" class="hidden" data-view-row="page"></a>
                        </emm:column>

                        <c:if test="${deleteAllowed and profiles.fullListSize gt 0}">
                            <emm:column>
                                <a href="${deleteUrl}?ids=${profile.id}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMsg}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </emm:column>
                        </c:if>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
