<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="birtReportsForm" type="org.agnitas.web.forms.PaginationForm"--%>
<%--@elvariable id="reports" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.birtreport.bean.ReportEntry>"--%>
<%--@elvariable id="dateFormat" type="java.text.SimpleDateFormat"--%>

<mvc:form servletRelativeAction="/statistics/reports.action" modelAttribute="birtReportsForm" method="post">
    <input type="hidden" name="page" value="${reports.pageNumber}"/>
    <input type="hidden" name="sort" value="${reports.sortCriterion}"/>
    <input type="hidden" name="dir" value="${reports.sortDirection}"/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "birt-report-overview": {
                "rows-count": ${birtReportsForm.numberOfRows}
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
                <c:set var="allowedDeletion" value="false"/>
                <emm:ShowByPermission token="report.birt.delete">
                    <c:set var="allowedDeletion" value="true"/>
                </emm:ShowByPermission>
                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="report"
                               name="reports"
                               requestURI="/statistics/reports.action"
                               pagesize="${birtReportsForm.numberOfRows}"
                               excludedParams="*">

                    <!-- Prevent table controls/headers collapsing when the table is empty -->
                    <display:setProperty name="basic.empty.showtable" value="true"/>

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <%-- Shortname --%>
                    <display:column headerClass="forms_head_name header" class="name"
                                    titleKey="Report" property="shortname"
                                    maxLength="150"
                                    sortable="true" sortProperty="shortname"/>

                    <%-- Description --%>
                    <display:column headerClass="forms_head_desc header" class="description"
                                    titleKey="Description" property="description"
                                    maxLength="150" maxWords="5"
                                    sortable="true" sortProperty="description"/>

                    <%-- Changed at --%>
                    <display:column headerClass="header" class="changedat"
                                    titleKey="default.changeDate"
                                    sortable="true" sortProperty="change_date">
                        <emm:formatDate value="${report.changeDate}" format="${dateFormat}"/>
                    </display:column>


                    <%-- Last delivery --%>
                    <display:column headerClass="header" class="mailing.LastDelivery"
                                    titleKey="mailing.LastDelivery"
                                    sortable="true" sortProperty="delivery_date">
                        <emm:formatDate value="${report.deliveryDate}" format="${dateFormat}"/>
                    </display:column>

                    <%-- Actions--%>
                    <display:column class="table-actions ${allowedDeletion ? '' : 'hidden'}" headerClass="${allowedDeletion ? '' : 'hidden'}" sortable="false">
                        <c:url var="viewReportLink" value="/statistics/report/${report.id}/view.action"/>
                        <a href="${viewReportLink}" class="hidden js-row-show"></a>

                        <c:if test="${allowedDeletion}">
                            <c:url var="deleteReportLink" value="/statistics/report/${report.id}/confirmDelete.action"/>

                            <mvc:message var="deleteMessage" code="Delete"/>

                            <a href="${deleteReportLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${deleteMessage}">
                                <i class="icon icon-trash-o"></i>
                            </a>
                        </c:if>

                    </display:column>
                </display:table>
            </div>
        </div>

    </div>

</mvc:form>
