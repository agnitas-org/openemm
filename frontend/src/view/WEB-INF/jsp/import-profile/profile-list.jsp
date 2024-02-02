<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="org.agnitas.web.forms.PaginationForm"--%>
<%--@elvariable id="profile" type="org.agnitas.beans.ImportProfile"--%>
<%--@elvariable id="defaultProfileId" type="java.lang.Integer"--%>

<mvc:form servletRelativeAction="/import-profile/list.action" modelAttribute="form">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "import-profile-overview": {
                "rows-count": ${form.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="import.ProfileAdministration" />
            </h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="listSize"/></li>
                        <li>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table" id="profile" name="profiles"
                        pagesize="${form.numberOfRows}" requestURI="/import-profile/list.action" excludedParams="*" defaultsort="1">

                    <display:column headerClass="js-table-sort" sortProperty="name" property="name" titleKey="import.ImportProfile" sortable="true" />
                    <display:column titleKey="recipient.importprofile.defaultprofile">
                        <c:choose>
                            <c:when test="${defaultProfileId == profile.id}">
                                <button type="button" class="btn btn-regular profile_checkbox_btn--checked" disabled="disabled">
                                    <i class="icon icon-check-square-o"></i>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <c:url var="setDefaultUrl" value="/import-profile/setDefault.action"/>
                                <button type="button" class="btn btn-regular profile_checkbox_btn" data-form-url="${setDefaultUrl}" data-form-set="id: ${profile.id}" data-form-submit>
                                    <i class="icon icon-square-o"></i>
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                    <display:column class="table-actions">
                        <c:url var="viewLink" value="/import-profile/${profile.id}/view.action"/>
                        <a href="${viewLink}" class="hidden js-row-show"></a>

                        <emm:ShowByPermission token="import.delete">
                            <c:url var="deletionLink" value="/import-profile/${profile.id}/confirmDelete.action"/>
                            <mvc:message var="deletionTooltip" code="recipient.importprofile.delete"/>

                            <a href="${deletionLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deletionTooltip}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
