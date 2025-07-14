<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="bounceFilterListForm" type="com.agnitas.emm.core.bounce.form.BounceFilterListForm"--%>
<%--@elvariable id="bounceFilterList" type="com.agnitas.beans.impl.PaginatedListImpl"--%>

<mvc:form servletRelativeAction="/administration/bounce/list.action" data-form="resource" modelAttribute="bounceFilterListForm">
    <input type="hidden" name="page" value="${bounceFilterList.pageNumber}"/>
    <input type="hidden" name="sort" value="${bounceFilterList.sortCriterion}"/>
    <input type="hidden" name="dir" value="${bounceFilterList.sortDirection}"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "bounce-filter-overview": {
                "rows-count": ${bounceFilterListForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>

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
                            <label class="label">
                                <mvc:radiobutton path="numberOfRows" value="200"/>
                                <span class="label-text">200</span>
                            </label>
                        </li>

                        <li class="divider"></li>

                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <span class="text"><mvc:message code="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="table-wrapper">
                <c:set var="allowedDeletion" value="false"/>
                <emm:ShowByPermission token="mailloop.delete">
                    <c:set var="allowedDeletion" value="true"/>
                </emm:ShowByPermission>

                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="bounceFilter"
                               requestURI="/administration/bounce/list.action"
                               name="bounceFilterList"
                               pagesize="${bounceFilterListForm.numberOfRows}"
                               excludedParams="*">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <display:column headerClass="js-table-sort mailloop_head_name header" class="mailing"
                                    property="shortName" titleKey="settings.Mailloop"
                                    sortable="true" sortProperty="shortname"/>

                    <display:column headerClass="js-table-sort mailloop_head_name header" class="mailing"
                                    property="description" titleKey="Description"
                                    sortable="true" sortProperty="description"/>

                    <display:column headerClass="js-table-sort mailloop_head_address header" class="mailing"
                                    property="filterEmailWithDefault" titleKey="mailloop.filter_adr"
                                    sortable="false"/>

                    <display:column class="table-actions ${allowedDeletion ? '' : 'hidden'}" headerClass="${allowedDeletion ? '' : 'hidden'}" sortable="false">
                        <c:url var="viewBounceFilterLink" value="/administration/bounce/${bounceFilter.id}/view.action"/>

                        <a href="${viewBounceFilterLink}" class="hidden js-row-show"></a>

                        <c:if test="${allowedDeletion}">
                            <c:url var="deleteBounceFilterLink" value="/administration/bounce/${bounceFilter.id}/confirmDelete.action"/>
                            <mvc:message var="deleteMessage" code="Delete"/>

                            <a href="${deleteBounceFilterLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </c:if>

                    </display:column>

                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
