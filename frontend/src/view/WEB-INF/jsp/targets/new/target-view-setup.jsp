<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>


<%--@elvariable id="targetEditForm" type="com.agnitas.emm.core.target.form.TargetEditForm"--%>
<%--@elvariable id="isLocked" type="java.lang.Boolean"--%>

<emm:CheckLogon />
<emm:Permission token="targets.show" />

<c:set var="isTabsMenuShown" value="false" scope="request" />
<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="target-id" value="${targetEditForm.targetId}"/>
</emm:instantiate>
<c:set var="agnTitleKey" value="Target" scope="request" />
<c:set var="agnSubtitleKey" value="Target" scope="request" />
<c:set var="sidemenu_active" value="Targetgroups" scope="request" />
<c:set var="isBreadcrumbsShown" value="true" scope="request" />
<c:set var="agnBreadcrumbsRootKey" value="Targetgroups" scope="request" />
<c:set var="agnHelpKey" value="targetGroupView" scope="request" />

<c:choose>
    <c:when test="${targetEditForm.targetId ne 0}">
        <c:set var="isTabsMenuShown" value="true" scope="request" />
        <c:set var="agnNavigationKey" value="TargetQBEdit" scope="request" />
        <c:set var="sidemenu_sub_active" value="none" scope="request" />
        <c:set var="agnHighlightKey" value="target.Edit" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" value="targets" scope="request" />
        <c:set var="sidemenu_sub_active" value="target.NewTarget" scope="request" />
        <c:set var="agnHighlightKey" value="target.NewTarget" scope="request" />
    </c:otherwise>
</c:choose>

<c:set var="submitType" value="data-form-submit" />
<c:if test="${not empty workflowForwardParams}">
    <c:set var="submitType" value="data-form-submit-static" />
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap"
    scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0"
            value="${agnBreadcrumb}" />
        <c:set target="${agnBreadcrumb}" property="textKey"
            value="default.Overview" />
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/target/list.action" />
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1"
            value="${agnBreadcrumb}" />
        <c:choose>
            <c:when test="${targetEditForm.targetId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey"
                    value="target.NewTarget" />
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text"
                    value="${targetEditForm.shortname}" />
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty workflowForwardParams}">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="4" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular" />
            <c:set target="${element}" property="iconBefore" value="icon-angle-left" />
            <c:set target="${element}" property="type" value="href" />
            <c:set target="${element}" property="url">
                <c:url value="/workflow/${workflowId}/view.action">
                    <c:param name="forwardParams" value="${workflowForwardParams};elementValue=${targetEditForm.targetId}"/>
                </c:url>
            </c:set>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Back" />
            </c:set>
        </emm:instantiate>
    </c:if>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}" />

        <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle" />
        <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'" />
        <c:set target="${element}" property="iconBefore" value="icon-wrench" />
        <c:set target="${element}" property="name">
            <mvc:message code="action.Action" />
        </c:set>
        <c:set target="${element}" property="iconAfter" value="icon-caret-down" />

        <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
        </emm:instantiate>

        <%--options for dropdown--%>
        <c:if test="${not isLocked}">
            <c:if test="${not empty targetEditForm.targetId and targetEditForm.targetId != 0}">
                <emm:ShowByPermission token="targets.change">
                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="2" value="${dropDownItem}" />
                        <c:set target="${dropDownItem}" property="url">
                            <c:url value="/target/${targetEditForm.targetId}/copy.action"/>
                        </c:set>
                        <c:set target="${dropDownItem}" property="icon" value="icon-copy" />
                        <c:set target="${dropDownItem}" property="name">
                            <mvc:message code="button.Copy" />
                        </c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>

                <c:if test="${mailingId gt 0}">
                    <emm:ShowByPermission token="targets.change">
                        <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                            <c:set target="${dropDownItems}" property="1" value="${dropDownItem}" />
                            <c:set target="${dropDownItem}" property="url">
                                <c:url value="/mailingbase.do">
                                    <c:param name="action" value="2" />
                                    <c:param name="mailingID" value="${mailingId}" />
                                </c:url>
                            </c:set>
                            <c:set target="${dropDownItem}" property="icon" value="icon-reply" />
                            <c:set target="${dropDownItem}" property="name">
                                <mvc:message code="button.tomailing" />
                            </c:set>
                        </emm:instantiate>
                    </emm:ShowByPermission>
                </c:if>

                <c:if test="${not empty targetEditForm.targetId and targetEditForm.targetId != 0}">
                    <emm:ShowByPermission token="targets.createml">
                        <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                            <c:set target="${dropDownItems}" property="5" value="${dropDownItem}" />
                            <c:set target="${dropDownItem}" property="extraAttributes" value="data-confirm=''" />
                            <c:set target="${dropDownItem}" property="url">
                                <c:url value="/target/${targetEditForm.targetId}/confirm/create/mailinglist.action" />
                            </c:set>
                            <c:set target="${dropDownItem}" property="icon" value="icon-file-o" />
                            <c:set target="${dropDownItem}" property="name">
                                <mvc:message code="createMList" />
                            </c:set>
                        </emm:instantiate>
                    </emm:ShowByPermission>
                </c:if>

                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="7" value="${dropDownItem}" />
                    <c:set target="${dropDownItem}" property="btnCls" value="btn btn-regular btn-secondary" />
                    <c:set target="${dropDownItem}" property="type" value="href" />
                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/statistics/recipient/view.action">
                            <c:param name="mailinglistId" value="0"/>
                            <c:param name="targetId" value="${targetEditForm.targetId}"/>
                        </c:url>
                    </c:set>
                    <c:set target="${dropDownItem}" property="icon" value="icon-bar-chart-o" />
                    <c:set target="${dropDownItem}" property="name">
                        <mvc:message code="Statistics" />
                    </c:set>
                </emm:instantiate>

                <emm:ShowByPermission token="targets.lock">
                    <c:url var="lockUrl" value="/target/${targetEditForm.targetId}/lock.action" />
                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="3" value="${dropDownItem}" />
                        <c:set target="${dropDownItem}" property="btnCls" value="btn btn-regular btn-inverse"/>
                        <c:set target="${dropDownItem}" property="url" value="${lockUrl}"/>
                        <c:set target="${dropDownItem}" property="icon" value="icon-lock" />
                        <c:set target="${dropDownItem}" property="name">
                            <mvc:message code="target.lock" />
                        </c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </c:if>
        </c:if>

        <c:if test="${isLocked}">
            <emm:ShowByPermission token="targets.lock">
                <c:url var="unlockUrl" value="/target/${targetEditForm.targetId}/unlock.action" />
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="4" value="${dropDownItem}" />
                    <c:set target="${dropDownItem}" property="btnCls" value="btn btn-regular btn-inverse"/>
                    <c:set target="${dropDownItem}" property="url" value="${unlockUrl}" />
                    <c:set target="${dropDownItem}" property="icon" value="icon-unlock" />
                    <c:set target="${dropDownItem}" property="name">
                        <mvc:message code="button.Unlock" />
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:if>

        <c:if test="${not empty targetEditForm.targetId and targetEditForm.targetId != 0}">
            <emm:ShowByPermission token="recipient.delete">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="6" value="${dropDownItem}" />
                    <c:set target="${dropDownItem}" property="extraAttributes" value="data-confirm" />
                    <c:set target="${dropDownItem}" property="type" value="href" />
                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/target/${targetEditForm.targetId}/confirm/delete/recipients.action" />
                    </c:set>
                    <c:set target="${dropDownItem}" property="icon" value="icon-trash-o" />
                    <c:set target="${dropDownItem}" property="name">
                        <mvc:message code="target.delete.recipients" />
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:if>

        <c:if test="${not isLocked}">
            <emm:ShowByPermission token="targets.delete">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="0" value="${dropDownItem}" />
                    <c:set target="${dropDownItem}" property="icon" value="icon-trash-o" />
                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/target/${targetEditForm.targetId}/confirm/delete.action" />
                    </c:set>
                    <c:set target="${dropDownItem}" property="name">
                        <mvc:message code="button.Delete" />
                    </c:set>
                    <c:set target="${dropDownItem}" property="extraAttributes" value=" data-confirm=''" />
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:if>

    </emm:instantiate>

    <c:if test="${not isLocked}">
        <emm:ShowByPermission token="targets.change">
                <c:set var="SHOW_SAVE_BUTTON" value="true" scope="page" />
        </emm:ShowByPermission>
        <emm:HideByPermission token="targets.change">
            <c:set var="SHOW_SAVE_BUTTON" value="false" scope="page" />
        </emm:HideByPermission>

        <c:if test="${SHOW_SAVE_BUTTON}">
            <c:url var="saveUrl" value="/target/${targetEditForm.targetId}/save.action"/>
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="3" value="${element}" />
                <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse" />
                <c:set target="${element}" property="extraAttributes" value="data-form-url='${saveUrl}' data-form-target='#targetViewForm' ${submitType}" />
                <c:set target="${element}" property="iconBefore" value="icon-save" />
                <c:set target="${element}" property="name">
                    <mvc:message code="button.Save" />
                </c:set>
                <c:set target="${element}" property="url" value="${saveUrl}" />
            </emm:instantiate>
        </c:if>
    </c:if>

</emm:instantiate>
