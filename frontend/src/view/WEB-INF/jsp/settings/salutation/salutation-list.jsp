<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.salutation.form.SalutationForm"--%>
<%--@elvariable id="salutations" type="org.agnitas.beans.impl.PaginatedListImpl<SalutationEntry>"--%>
<%--@elvariable id="salutation" type="org.agnitas.beans.SalutationEntry"--%>

<mvc:form servletRelativeAction="/salutation/list.action" id="form" modelAttribute="form" data-form="resource">
    <script type="application/json" data-initializer="web-storage-persist">
        {
            "salutation-overview": {
                "rows-count": ${form.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="default.Overview"/>
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
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change
                                        data-form-submit>
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

                <display:table
                        class="table table-bordered table-striped table-hover js-table"
                        pagesize="${form.numberOfRows}"
                        id="salutation"
                        name="salutations"
                        sort="external"
                        requestURI="/salutation/list.action"
                        excludedParams="*"
                        size="${salutations.fullListSize}">

                    <display:column property="titleId" titleKey="MailinglistID" headerClass="js-table-sort"
                                    sortProperty="title_id" sortable="true"/>
                    <display:column class="description" headerClass="js-table-sort" property="description"
                                    sortProperty="description" titleKey="settings.FormOfAddress" sortable="true"/>

                    <display:column class="table-actions">
                        <c:url var="viewLink" value="/salutation/${salutation.titleId}/view.action" />
                        <a href="${viewLink}" class="hidden js-row-show" ></a>
                        
                        <c:if test="${salutation.companyID ne 0}">
                            <emm:ShowByPermission token="salutation.delete">
                                <c:set var="salutationDeleteMessage" scope="page">
                                    <mvc:message code="salutation.SalutationDelete"/>
                                </c:set>
                                <c:url var="deleteSalutationLink" value="/salutation/${salutation.titleId}/confirmDelete.action" />
                                <a href="${deleteSalutationLink}" class="btn btn-regular btn-alert js-row-delete" data-tooltip="${salutationDeleteMessage}">
                                    <i class="icon icon-trash-o"></i>
                                </a>
                            </emm:ShowByPermission>
                        </c:if>
                    </display:column>
                </display:table>
            </div>
        </div>
    </div>
</mvc:form>
