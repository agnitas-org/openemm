<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.mailing.web.MailingPreviewHelper" %>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="com.agnitas.preview.ModeType" %>
<%@ page import="com.agnitas.preview.Preview" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingListExist" type="java.lang.Boolean"--%>
<%--@elvariable id="isTagFailureInSubject" type="java.lang.Boolean"--%>
<%--@elvariable id="isTagFailureInPreHeader" type="java.lang.Boolean"--%>
<%--@elvariable id="isTagFailureInFromAddress" type="java.lang.Boolean"--%>
<%--@elvariable id="availablePreviewFormats" type="java.util.List"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.preview.form.PreviewForm"--%>
<%--@elvariable id="components" type="java.util.List<com.agnitas.beans.MailingComponent>"--%>
<%--@elvariable id="previewSizes" type="java.util.List<com.agnitas.preview.Preview.Size>"--%>
<%--@elvariable id="availableTargetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="previewRecipients" type="java.util.List<com.agnitas.beans.impl.RecipientLiteImpl>"--%>

<c:set var="EMAIL_TYPE_CODE" value="<%= MediaTypes.EMAIL.getMediaCode() %>" scope="page"/>
<c:set var="MOBILE_PORTRAIT" value="<%= Preview.Size.MOBILE_PORTRAIT.getValue() %>" scope="request" />
<c:set var="MOBILE_LANDSCAPE" value="<%= Preview.Size.MOBILE_LANDSCAPE.getValue() %>" scope="request" />

<c:set var="storedFieldsScope" value="${form.mailingId}"/>

<c:url var="previewLink" value="/mailing/preview/view-content.action">
    <c:param name="mailingId" value="${form.mailingId}"/>
    <c:param name="format" value="${form.format}"/>
    <c:param name="modeType" value="${form.modeType}"/>
    <c:param name="size" value="${form.size}"/>
    <c:param name="customerID" value="${form.customerID}"/>
    <c:param name="targetGroupId" value="${form.targetGroupId}"/>
    <c:param name="noImages" value="${form.noImages}"/>
</c:url>

<div class="tiles-container">
    <mvc:form cssClass="tile" id="preview-form" servletRelativeAction="/mailing/preview/${form.mailingId}/view.action" modelAttribute="form"
              data-controller="mailing-preview"
              data-initializer="mailing-preview"
              data-editable-tile="">
        <mvc:hidden path="mailingId"/>
        <mvc:hidden path="pure"/>
        <mvc:hidden path="reload" value="true"/> <%-- detect if this is initial page load or user form submit --%>

        <script id="config:mailing-preview" type="application/json">
            {
                "MAILING_ID": "${form.mailingId}",
                "MOBILE_SIZES": ${[MOBILE_PORTRAIT, MOBILE_LANDSCAPE]}
            }
        </script>

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "mailing-preview": {
                    "settings": {
                        "${form.mailingId}" : {
                            "format": "${form.format}",
                            "size": "${form.size}",
                            "noImages": "${form.noImages}",
                            "customerEmail": "${form.customerEmail}",
                            "modeType": "${form.modeType}",
                            "targetId": "${form.targetGroupId}"
                        }
                    }
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Preview" /></h1>

            <c:if test="${empty mailingListExist or mailingListExist}">
                <div class="tile-controls">

                    <c:if test="${form.containsHeader}">
                        <input type="checkbox" class="btn-check" id="sender-info-toggle" autocomplete="off" data-stored-field="${storedFieldsScope}">
                        <label class="btn btn-icon btn-primary" for="sender-info-toggle"><i class="icon icon-info"></i></label>
                    </c:if>

                    <c:if test="${emm:contains(availablePreviewFormats, EMAIL_TYPE_CODE)}">
                        <c:url var="downloadHtmlLink" value="/mailing/preview/html.action">
                            <c:param name="mailingId" value="${form.mailingId}"/>
                            <c:param name="format" value="${form.format}"/>
                            <c:param name="modeType" value="${form.modeType}"/>
                            <c:param name="size" value="${form.size}"/>
                            <c:param name="customerID" value="${form.customerID}"/>
                            <c:param name="targetGroupId" value="${form.targetGroupId}"/>
                            <c:param name="noImages" value="${form.noImages}"/>
                        </c:url>
                        <a href="${downloadHtmlLink}" class="btn btn-icon btn-secondary mobile-hidden" data-tooltip="<mvc:message code="default.HTML.code"/>" data-prevent-load="">
                            <i class="icon icon-file-code"></i>
                        </a>
                    </c:if>

                    <c:url var="previewPDFLink" value="/mailing/preview/pdf.action">
                        <c:param name="mailingId" value="${form.mailingId}"/>
                        <c:param name="format" value="${form.format}"/>
                        <c:param name="modeType" value="${form.modeType}"/>
                        <c:param name="size" value="${form.size}"/>
                        <c:param name="customerID" value="${form.customerID}"/>
                        <c:param name="targetGroupId" value="${form.targetGroupId}"/>
                        <c:param name="noImages" value="${form.noImages}"/>
                    </c:url>

                    <a href="${previewPDFLink}" class="btn btn-icon btn-secondary mobile-hidden" data-tooltip="<mvc:message code="mailing.preview.pdf" />" data-prevent-load="">
                        <i class="icon icon-file-pdf"></i>
                    </a>

                    <a href="${previewLink}" class="btn btn-icon btn-secondary mobile-hidden" target="_blank" data-tooltip="<mvc:message code="mailing.open_preview"/>">
                        <i class="icon icon-external-link-alt"></i>
                    </a>

                    <div class="dropdown mobile-visible">
                        <button class="btn btn-sm-horizontal btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                            <i class="icon icon-wrench"></i>
                            <span><mvc:message code="default.View"/></span>
                        </button>
                        <ul class="dropdown-menu dropdown-menu--select">
                            <li>
                                <a href="${downloadHtmlLink}" class="dropdown-item" data-prevent-load="">
                                    <i class="icon icon-file-code"></i>
                                    <mvc:message code="default.HTML.code"/>
                                </a>
                            </li>
                            <li>
                                <a href="${previewPDFLink}" class="dropdown-item" data-prevent-load="">
                                    <i class="icon icon-file-pdf"></i>
                                    <mvc:message code="mailing.preview.pdf" />
                                </a>
                            </li>
                            <li>
                                <a href="${previewLink}" class="dropdown-item" target="_blank">
                                    <i class="icon icon-external-link-alt"></i>
                                    <mvc:message code="mailing.open_preview"/>
                                </a>
                            </li>
                        </ul>
                    </div>

                    <div class="dropdown">
                        <button class="btn btn-sm-horizontal btn-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                            <i class="icon icon-eye"></i>
                            <span><mvc:message code="default.View"/></span>
                        </button>

                        <div class="dropdown-menu form-column" data-action="refresh-preview">
                            <div>
                                <label for="preview-size" class="form-label"><mvc:message code="default.Size"/></label>
                                <mvc:select id="preview-size" path="size" cssClass="form-control">
                                    <c:forEach var="previewSize" items="${previewSizes}">
                                        <mvc:option value="${previewSize.value}">
                                            <mvc:message code="${previewSize.msgCode}"/>
                                        </mvc:option>
                                    </c:forEach>
                                </mvc:select>
                            </div>

                            <c:if test="${not empty availablePreviewFormats}">
                                <div class="mt-2">
                                    <label for="preview-format" class="form-label"><mvc:message code="action.Format"/></label>
                                    <mvc:select id="preview-format" path="format" cssClass="form-control">
                                        <c:forEach var="mediaTypeCode" items="${availablePreviewFormats}">
                                            <c:choose>
                                                <c:when test="${mediaTypeCode eq EMAIL_TYPE_CODE}">
                                                    <mvc:option value="${MailingPreviewHelper.INPUT_TYPE_TEXT}"><mvc:message code="Text"/></mvc:option>
                                                    <c:if test="${form.emailFormat gt 0}">
                                                        <mvc:option value="${MailingPreviewHelper.INPUT_TYPE_HTML}"><mvc:message code="HTML"/></mvc:option>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <mvc:option value="${mediaTypeCode + 1}"><mvc:message code='mailing.MediaType.${mediaTypeCode}'/></mvc:option>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </mvc:select>
                                </div>
                            </c:if>

                            <div class="form-check form-switch mt-2">
                                <mvc:checkbox id="preview-no-images" path="noImages" cssClass="form-check-input" role="switch"/>
                                <label class="form-label form-check-label fw-medium" for="preview-no-images"><mvc:message code="predelivery.without.images"/></label>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>
        </div>

        <c:if test="${form.containsHeader}">
            <div class="tile-header d-flex flex-column" style="align-items: unset" data-show-by-checkbox="#sender-info-toggle">
                <mvc:message var="errorDyntagsMsg" code="error.template.dyntags"/>

                <div class="input-group" data-tooltip="${isTagFailureInSubject ? errorDyntagsMsg : ''}">
                    <span class="input-group-text disabled ${isTagFailureInSubject ? 'text-danger' : ''}"><mvc:message code="mailing.Subject"/></span>
                    <input id="preview-subject" type="text" class="form-control" value="${fn:escapeXml(form.subject)}" readonly>
                </div>

                <c:if test="${form.preHeader ne null}">
                    <div class="input-group" data-tooltip="${isTagFailureInPreHeader ? errorDyntagsMsg : ''}">
                        <span class="input-group-text disabled ${isTagFailureInPreHeader ? 'text-danger' : ''}"><mvc:message code="mailing.preheader"/></span>
                        <input id="preview-preheader" type="text" class="form-control" value="${fn:escapeXml(form.preHeader)}" readonly>
                    </div>
                </c:if>

                <c:set var="attachments">
                    <c:forEach var="component" items="${components}" varStatus="status">
                        <c:set var="isVisibleComponent" value="${false}"/>
                        <c:choose>
                            <c:when test="${form.modeType eq ModeType.TARGET_GROUP}">
                                <c:set var="isVisibleComponent" value="${component.targetID eq form.targetGroupId}"/>
                            </c:when>
                            <c:otherwise>
                                <emm:CustomerMatchTarget customerID="${form.customerID}" targetID="${component.targetID}">
                                    <c:set var="isVisibleComponent" value="${true}"/>
                                </emm:CustomerMatchTarget>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${isVisibleComponent}">
                            <option value='<c:url value="/sc?compID=${component.id}&mailingID=${form.mailingId}&customerID=${form.customerID}&targetGroupID=${form.targetGroupId}"/>'>${component.componentName}</option>
                        </c:if>
                    </c:forEach>
                </c:set>

                <c:if test="${not empty attachments}">
                    <div class="input-group w-100">
                        <span class="input-group-text disabled"><mvc:message code="mailing.Attachments"/></span>
                        <select id="attachments" class="form-control">
                            ${attachments}
                        </select>
                        <a href="#" class="btn btn-icon btn-primary" data-action="download-attachment"><i class="icon icon-download"></i></a>
                    </div>
                </c:if>

                <div class="input-group" data-tooltip="${isTagFailureInFromAddress ? errorDyntagsMsg : ''}">
                    <span class="input-group-text disabled ${isTagFailureInFromAddress ? 'text-danger' : ''}"><mvc:message code="ecs.From"/></span>
                    <input id="preview-from" type="text" class="form-control" value="${fn:escapeXml(form.senderEmail)}" readonly>
                </div>

                <div id="to-input-group" class="input-group">
                    <span class="input-group-text disabled"><mvc:message code="To"/></span>
                    <mvc:select id="preview-mode" path="modeType" cssClass="form-control"
                            data-action="change-stored-header-data"
                            data-select-options="dropdownAutoWidth: true, width: 'auto'">
                        <mvc:option value="${ModeType.RECIPIENT}">
                            <mvc:message code="recipient.TestSubscriber"/>
                        </mvc:option>
                        <mvc:option value="${ModeType.MANUAL}">
                            <mvc:message code="mailing.preview.input"/>
                        </mvc:option>
                        <mvc:option value="${ModeType.TARGET_GROUP}">
                            <mvc:message code="target.Target"/>
                        </mvc:option>
                    </mvc:select>

                    <c:choose>
                        <c:when test="${form.modeType eq ModeType.RECIPIENT}">
                            <mvc:select id="recipients" path="customerATID" cssClass="form-control" data-action="change-header-data" data-sort="alphabetic">
                                <mvc:option value="0" selected="${form.customerATID == 0}" data-no-sort=""><mvc:message code="default.select.email"/></mvc:option>
                                <c:forEach var="recipient" items="${previewRecipients}">
                                    <c:set var="customerId" value="${recipient.id}"/>
                                    <c:set var="customerName" value="${recipient.firstname} ${recipient.lastname} &lt;${recipient.email}&gt;"/>
                                    <mvc:option value="${customerId}" data-email="${recipient.email}">${customerName}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </c:when>
                        <c:when test="${form.modeType eq ModeType.TARGET_GROUP}">
                            <mvc:select id="target-groups" path="targetGroupId" data-action="change-stored-header-data" cssClass="form-control">
                                <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                                <mvc:options items="${availableTargetGroups}" itemLabel="targetName" itemValue="id"/>
                            </mvc:select>
                        </c:when>
                        <c:when test="${form.modeType eq ModeType.MANUAL}">
                            <div class="d-flex flex-grow-1">
                                <mvc:text path="customerEmail" cssClass="form-control" data-action="change-personalized-test-recipients" />
                                <button type="button" id="recipient-input-refresh" class="btn btn-icon btn-primary rounded-start-0" data-action="update-preview">
                                    <i class="icon icon-play-circle"></i>
                                </button>
                            </div>
                        </c:when>
                    </c:choose>
                </div>
                <%@include file="fragments/preview-add-to-test-run.jspf" %>
            </div>
        </c:if>

        <div class="tile-body js-scrollable overflow-auto" data-controller="iframe-progress" data-initializer="iframe-progress">
            <c:choose>
                <c:when test="${empty mailingListExist or mailingListExist}">
                    <div id="preview-progress" class="progress loop" style="display:none;"></div>
                    <div class="flex-center">
                        <div class="flex-center flex-grow-1">
                            <iframe class="default-iframe js-simple-iframe" name="previewFrame" src="${previewLink}"
                                    data-max-width="${form.width}" data-media-query="${form.mediaQuery}"
                                    style="width: ${form.width}; height: 0">
                                Your Browser does not support IFRAMEs, please update!
                            </iframe>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="notification-simple notification-simple--lg notification-simple--info">
                        <p><mvc:message code="error.mailing.mailinglist.deleted"/></p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </mvc:form>
</div>
