<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="bounceFilterForm" type="com.agnitas.emm.core.bounce.form.BounceFilterForm"--%>
<%--@elvariable id="userFormList" type="java.util.List<com.agnitas.userform.bean.UserForm>"--%>
<%--@elvariable id="mailingLists" type="java.util.List<org.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="allowedActionBasedResponder" type="java.lang.Boolean"--%>
<%--@elvariable id="actionBasedMailings" type="java.util.List<org.agnitas.emm.core.mailing.beans.LightweightMailing>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp"/>

<mvc:form servletRelativeAction="/administration/bounce/save.action" id="bounceFilterForm" modelAttribute="bounceFilterForm" data-form="resource">

    <mvc:hidden path="id"/>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="settings.EditMailloop"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="shortName"><bean:message key="Name"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortName" id="shortName" maxlength="99" size="32" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="description"><bean:message key="Description"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" id="description" rows="5" cols="32" cssClass="form-control"/>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="report.mailing.filter" /></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="filterEmail">
                    	<bean:message key="mailloop.filter_adr"/>
                    	<button class="icon icon-help" data-help="help_${helplanguage}/settings/BounceFilterAddress.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="filterEmail" id="filterEmail" maxlength="99" size="42" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="doForward"><bean:message key="settings.mailloop.forward"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="doForward" id="doForward" cssClass="tag-spring"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="forwardEmail"><bean:message key="settings.mailloop.forward.address"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="forwardEmail" id="forwardEmail" maxlength="99" size="42" cssClass="form-control"/>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="mailloop.Subscriptions"/></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="doSubscribe"><bean:message key="mailloop.subscribe"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="doSubscribe" id="doSubscribe" cssClass="tag-spring"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailingListId"><bean:message key="mailinglist"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="mailingListId" id="mailingListId" cssClass="form-control js-select" size="1">
                        <c:forEach items="${mailingLists}" var="item">
                            <mvc:option value="${item.id}">${item.shortname}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="userFormId"><bean:message key="settings.mailloop.userform"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="userFormId" id="userFormId" cssClass="form-control js-select" size="1">
                        <c:forEach var="userForm" items="${userFormList}">
                            <mvc:option value="${userForm.id}">${userForm.formName}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><bean:message key="mailSettings" /></h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="doAutoRespond"> <bean:message key="mailloop.autoresponder"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="toggle">
                        <mvc:checkbox path="doAutoRespond" id="doAutoRespond" cssClass="tag-spring"/>
                        <div class="toggle-control"></div>
                    </label>
                </div>
            </div>

            <%-- TODO: (EMM2693) After transition phase, remove "otherwise"-path and keep only "when"-path. --%>
            <c:choose>
                <c:when test="${allowedActionBasedResponder}">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="mailingListId"><bean:message key="mailloop.autoresponder.mailing"/></label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:select path="arMailingId" id="arMailingId" cssClass="form-control js-select" size="1">
                                <c:forEach items="${actionBasedMailings}" var="mailing">
                                    <mvc:option value="${mailing.mailingID}">${mailing.shortname}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <mvc:hidden path="arMailingId" value="0"/>
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="arSenderAddress"><bean:message key="mailing.Sender_Adress"/></label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:text path="arSenderAddress" id="arSenderAddress" maxlength="99" size="42" cssClass="form-control"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="arSubject"><bean:message key="mailing.Subject"/></label>
                        </div>
                        <div class="col-sm-8">
                            <mvc:text path="arSubject" id="arSubject" maxlength="99" size="42" cssClass="form-control"/>
                        </div>
                    </div>
                    <c:set var="editorMessage">
                        <bean:message key='editor.enlargeEditor'/>
                    </c:set>
                    <div class="inline-tile">
                        <div class="inline-tile-header">
                            <c:set var="testVersionTitle">
                                <bean:message key="Text_Version"/>
                            </c:set>
                            <h2 class="headline">${testVersionTitle}</h2>
                            <ul class="inline-tile-header-actions">
                                <li>
                                    <a href="#" data-modal="modal-editor-text" data-modal-set="title: '${testVersionTitle}', target: 'arText', id: 0" data-tooltip="${editorMessage}">
                                        <i class="icon icon-arrows-alt"></i>
                                    </a>
                                </li>
                            </ul>
                        </div>
                        <div class="inline-tile-content">
                            <mvc:textarea path="arText" id="arText" rows="14" cols="75" cssClass="form-control js-editor-text js-editor-wrap"/>
                        </div>
                    </div>
                    <div class="inline-tile" id="content">
                        <div class="inline-tile-header">
                            <c:set var="htmlVersionTitle">
                                <bean:message key="mailing.HTML_Version"/>
                            </c:set>
                            <c:set var="htmlEditorTitle">
                                <bean:message key="mailingContentHTMLEditor"/>
                            </c:set>
                            <h2 class="headline-static">${htmlVersionTitle}</h2>
                            <ul class="inline-tile-header-nav">
                                <li>
                                    <a href="#" data-toggle-tab="#tab-bounceFilterContentHtmlView">${htmlEditorTitle}</a>
                                </li>
                                <li class="active">
                                    <a href="#" data-toggle-tab="#tab-bounceFilterContentViewCode">${htmlVersionTitle}</a>
                                </li>
                            </ul>
                            <ul class="inline-tile-header-actions">
                                <li>
                                    <a href="#" data-modal="modal-editor" data-modal-set="title: '${htmlVersionTitle}', target: 'newContentText'" data-tooltip="${editorMessage}">
                                        <i class="icon icon-arrows-alt"></i>
                                    </a>
                                </li>
                            </ul>
                        </div>
                        <div class="inline-tile-content">
                            <div id="tab-bounceFilterContentHtmlView" class="hidden">
                                <div class="row">
                                    <div class="col-sm-12">
                                        <mvc:textarea path="arHtml" id="newContentText" rows="14" cols="20"
                                                      cssClass="form-control js-editor js-wysiwyg"
                                                      data-browse-mailing-id="${bounceFilterForm.arMailingId}"/>
                                    </div>
                                </div>
                            </div>
                            <div id="tab-bounceFilterContentViewCode">
                                <div class="row">
                                    <div class="col-sm-12">
                                        <div id="newContentTextEditor" class="form-control"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</mvc:form>

<script id="modal-editor-text" type="text/x-mustache-template">
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                </div>
                <div class="modal-body">
                    <textarea id="modalTextArea" data-sync="\#{{= target }}" class="form-control js-editor-text" data-form-target="#bounceFilterForm"></textarea>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><bean:message key="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large" data-sync-from="#modalTextArea" data-sync-to="\#{{= target }}" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><bean:message key="button.Apply"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
<script id="modal-editor" type="text/x-mustache-template">
    {{ showHTMLEditor = $('\#tab-bounceFilterContentViewHTML').is(':visible') }}
    <div class="modal modal-editor">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
                    <ul class="modal-header-nav">
                        <li>
                            <a href="#" data-toggle-tab="#tab-bounceFilterContentViewHTMLModal">
                                <bean:message key="mailingContentHTMLEditor"/>
                            </a>
                        </li>
                        <li class="active">
                            <a href="#" data-toggle-tab="#tab-bounceFilterContentViewCodeModal">
                                <bean:message key="mailing.HTML_Version"/>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="modal-body">
                    <div id="tab-bounceFilterContentViewHTMLModal" {{ (showHTMLEditor) ? print('data-tab-show') : print('data-tab-hide') }}>
                    <textarea id="modalTextArea" name="modalTextArea" data-sync="\#{{= target }}" class="form-control js-editor js-wysiwyg"
                              data-form-target="#bounceFilterForm"
                              data-browse-mailing-id="${bounceFilterForm.arMailingId}"></textarea>
                </div>

                <div id="tab-bounceFilterContentViewCodeModal" {{ (showHTMLEditor) ? print('data-tab-hide') : print('data-tab-show') }}>
                <div id="modalTextAreaEditor" class="form-control"></div>
            </div>

        </div>
        <div class="modal-footer">
            <div class="btn-group">
                <button type="button" class="btn btn-default btn-large" data-dismiss="modal">
                    <i class="icon icon-times"></i>
                    <span class="text"><bean:message key="button.Cancel"/></span>
                </button>
                <button type="button" class="btn btn-primary btn-large" data-sync-from="#modalTextArea" data-sync-to="\#{{= target }}" data-dismiss="modal">
                    <i class="icon icon-check"></i>
                    <span class="text"><bean:message key="button.Apply"/></span>
                </button>
            </div>
        </div>
    </div>
</script>
