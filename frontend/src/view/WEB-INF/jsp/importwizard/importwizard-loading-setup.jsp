<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="recipient.show"/>

<c:set var="ACTION_START" value="<%= ProfileImportAction.ACTION_START %>" scope="request"/>
<c:set var="ACTION_IGNORE_ERRORS" value="<%= ProfileImportAction.ACTION_IGNORE_ERRORS %>" scope="request"/>
<c:set var="ACTION_CANCEL" value="<%= ProfileImportAction.ACTION_CANCEL %>" scope="request"/>

<c:url var="importWizardLink" value="/newimportwizard.do">
    <c:param name="action" value="${ACTION_START}"/>
</c:url>

<c:set var="agnNavigationKey" 		value="none"		 				scope="request" />
<c:set var="agnTitleKey" 			value="import.UploadSubscribers"	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.UploadSubscribers" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		value="import.Wizard" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 				scope="request"	/>
<c:set var="agnHelpKey" 			value="importStep4" 				scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.Wizard"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${importWizardLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.edit.data"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
        <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-warning"/>
        <c:set target="${element0}" property="extraAttributes" value=""/>
        <c:set target="${element0}" property="iconBefore" value="icon-warning"/>
        <c:set target="${element0}" property="type" value="href"/>
        <c:set target="${element0}" property="url">
            <html:rewrite page="/newimportwizard.do">
                <html:param name="action" value="${ACTION_IGNORE_ERRORS}"/>
            </html:rewrite>
        </c:set>
        <c:set target="${element0}" property="name">
            <bean:message key="Ignore"/>
        </c:set>
    </jsp:useBean>
    
    <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
        <c:set target="${element1}" property="btnCls" value="btn btn-secondary btn-regular"/>
        <c:set target="${element1}" property="iconBefore" value="icon-times"/>
        <c:set target="${element1}" property="extraAttributes" value=""/>
        <c:set target="${element1}" property="type" value="href"/>
        <c:set target="${element1}" property="url">
            <html:rewrite page="/newimportwizard.do?action=${ACTION_CANCEL}"/>
        </c:set>
        <c:set target="${element1}" property="name">
            <bean:message key="button.Cancel"/>
        </c:set>
    </jsp:useBean>

    <jsp:useBean id="element2" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="2" value="${element2}"/>
        <c:set target="${element2}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element2}" property="extraAttributes" value="data-form-set='edit_page_save: save' data-form-target='#newimportwizard' data-form-submit"/>
        <c:set target="${element2}" property="iconBefore" value="icon-save"/>
        <c:set target="${element2}" property="name">
            <bean:message key="button.Save"/>
        </c:set>
    </jsp:useBean>
</jsp:useBean>
