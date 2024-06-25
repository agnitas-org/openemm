<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="org.agnitas.web.forms.PaginationForm"--%>
<%--@elvariable id="profile" type="org.agnitas.beans.ImportProfile"--%>
<%--@elvariable id="defaultProfileId" type="java.lang.Integer"--%>

<c:url var="setDefaultUrl" value="/import-profile/setDefault.action"/>
<mvc:message var="deletionTooltip" code="recipient.importprofile.delete"/>

<div class="tiles-container">
    <mvc:form servletRelativeAction="/import-profile/list.action" modelAttribute="form" cssClass="tile">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "import-profile-overview": {
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
                    <display:table class="table table-hover table-rounded js-table" id="profile" name="profiles" pagesize="${form.numberOfRows}"
                                   requestURI="/import-profile/list.action" excludedParams="*" defaultsort="1">

                        <%@ include file="../displaytag/displaytag-properties.jspf" %>

                        <display:column headerClass="js-table-sort" sortProperty="name" property="name" titleKey="Name" sortable="true" />
                        <display:column titleKey="recipient.importprofile.defaultprofile" headerClass="fit-content" class="js-checkable">
                            <div class="form-check form-switch">
                                <c:choose>
                                    <c:when test="${defaultProfileId == profile.id}">
                                        <input type="checkbox" role="switch" class="form-check-input" disabled checked>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="checkbox" role="switch" class="form-check-input" data-form-url="${setDefaultUrl}" data-form-set="id: ${profile.id}" data-form-submit>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </display:column>

                        <display:column headerClass="fit-content">
                            <c:url var="viewLink" value="/import-profile/${profile.id}/view.action"/>
                            <a href="${viewLink}" class="hidden" data-view-row="page"></a>

                            <emm:ShowByPermission token="import.delete">
                                <c:url var="deletionLink" value="/import-profile/${profile.id}/delete.action"/>

                                <a href="${deletionLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${deletionTooltip}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </emm:ShowByPermission>
                        </display:column>

                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
