<%@ page import="com.agnitas.emm.core.imports.beans.ImportResultFileType" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnTitleKey" 			       value="import.UploadSubscribers"	    scope="request" />
<c:set var="sidemenu_active" 		       value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	       value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		       value="import.Wizard" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	       value="import" 				        scope="request" />
<c:set var="agnHelpKey" 			       value="importStep4" 				    scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.standard"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <%-- Items for dropdown --%>

        <c:if test="${resultFile ne null}">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="url">
                    <c:url value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${ImportResultFileType.RESULT}" />
                    </c:url>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="import.download.result" />
                </c:set>
            </emm:instantiate>
        </c:if>

        <c:if test="${validRecipientsFile ne null}">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="1" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="url">
                    <c:url value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${ImportResultFileType.VALID_RECIPIENTS}" />
                    </c:url>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="import.download.result.invalid" />
                </c:set>
            </emm:instantiate>
        </c:if>

        <c:if test="${invalidRecipientsFile ne null}">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="2" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="url">
                    <c:url value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${ImportResultFileType.INVALID_RECIPIENTS}" />
                    </c:url>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="GWUA.import.downloadInvalidRecipients" />
                </c:set>
            </emm:instantiate>
        </c:if>

        <c:if test="${duplicateRecipientsFile ne null}">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="3" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="url">
                    <c:url value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${ImportResultFileType.DUPLICATED_RECIPIENTS}" />
                    </c:url>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="import.download.result.duplicate" />
                </c:set>
            </emm:instantiate>
        </c:if>

        <c:if test="${fixedRecipientsFile ne null}">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="4" value="${option}"/>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="url">
                    <c:url value="/recipient/import/download.action">
                        <c:param name="importResultFileType" value="${ImportResultFileType.FIXED_BY_HAND_RECIPIENTS}" />
                    </c:url>
                </c:set>
                <c:set target="${option}" property="name">
                    <mvc:message code="import.download.result.changed" />
                </c:set>
            </emm:instantiate>
        </c:if>
    </emm:instantiate>

    <emm:instantiate var="element1" type="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
        <c:set target="${element1}" property="iconBefore" value="icon-check"/>
        <c:set target="${element1}" property="type" value="href"/>
        <c:set target="${element1}" property="url">
            <c:url value="/recipient/import/finish.action" />
        </c:set>
        <c:set target="${element1}" property="name">
            <mvc:message code="button.Finish"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
