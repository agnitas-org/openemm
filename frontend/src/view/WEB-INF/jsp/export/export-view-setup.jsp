<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="id" type="java.lang.Integer"--%>
<%--@elvariable id="isManageAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="isOwnColumnsExportAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="exportForm" type="com.agnitas.emm.core.export.form.ExportForm"--%>

<c:url var="evaluateUrl" value="/export/${id}/evaluate.action"/>

<c:set  var="agnTitleKey" 			  value="export" 			                    scope="request" />
<c:set  var="sidemenu_active" 		  value="ImportExport" 		                    scope="request" />
<c:set  var="sidemenu_sub_active" 	  value="export" 			                    scope="request" />
<c:set  var="agnHighlightKey" 		  value="export" 			                    scope="request" />
<c:set  var="agnBreadcrumbsRootKey"  value="manage.tables.exportProfiles" 		    scope="request" />
<c:url  var="agnBreadcrumbsRootUrl"  value="/export/list.action?restoreSort=true"   scope="request" />
<c:set  var="agnHelpKey" 			  value="export" 				                scope="request" />
<c:set  var="agnEditViewKey" 	      value="export-view" 	                        scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${id > 0}">
                <c:set target="${agnBreadcrumb}" property="text" value="${exportForm.shortname}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="export.new_export_profile"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<c:if test="${isManageAllowed}">
    <emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>
            <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
            </emm:instantiate>

            <%-- Items for dropdown --%>
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:choose>
                    <c:when test="${isOwnColumnsExportAllowed}">
                        <c:set target="${option}" property="extraAttributes" value="data-form-target='#exportForm' data-form-url='${evaluateUrl}' data-action='evaluate'"/>
                    </c:when>
                    <c:otherwise>
                        <c:set target="${option}" property="extraAttributes" value="data-form-target='#exportForm' data-form-url='${evaluateUrl}' data-form-submit"/>
                    </c:otherwise>
                </c:choose>
                <c:set target="${option}" property="name"><mvc:message code="Evaluate"/></c:set>
            </emm:instantiate>
        </emm:instantiate>
        
        <emm:ShowByPermission token="export.change">
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
                <c:choose>
                    <c:when test="${isOwnColumnsExportAllowed}">
                        <c:set target="${element}" property="extraAttributes" value="data-form-target='#exportForm' data-action='save'"/>
                    </c:when>
                    <c:otherwise>
                        <c:set target="${element}" property="extraAttributes" value="data-form-target='#exportForm' data-form-submit"/>
                    </c:otherwise>
                </c:choose>
                <c:set target="${element}" property="iconBefore" value="icon-save"/>
                <c:set target="${element}" property="name"><mvc:message code="button.Save"/></c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </emm:instantiate>
</c:if>
