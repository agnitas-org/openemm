<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.widget.enums.WidgetType" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="doiMailings" type="java.util.Map<java.lang.Integer, com.agnitas.emm.core.mailing.bean.LightweightMailingWithMailingList>"--%>
<%--@elvariable id="rdirDomain" type="java.lang.String"--%>

<div class="tiles-container" data-editable-view="${agnEditViewKey}" data-controller="widget-creation">
    <script type="application/json" data-initializer="widget-creation">
        {
          "rdirDomain": ${emm:toJson(rdirDomain)}
        }
    </script>
    <div id="settings-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Settings" /></h1>
        </div>

        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label for="widget-type" class="form-label"><mvc:message code="default.Type" /></label>
                <select id="widget-type" class="form-control" readonly>
                    <c:forEach var="widgetType" items="${WidgetType.values()}">
                        <option value="${widgetType}"><mvc:message code="${widgetType.messageKey}" /></option>
                    </c:forEach>
                </select>
            </div>

            <mvc:form servletRelativeAction="/widget/subscribe/token.action" modelAttribute="subscribeSettingsForm" cssClass="vstack gap-inherit"
                      data-show-by-select="#widget-type" data-show-by-select-values="${WidgetType.SUBSCRIBE}">
                <div class="d-flex flex-column gap-inherit" data-field="double-select" data-provider="opt" data-provider-src="SubscribeWidgetDOIMailings">
                    <div>
                        <label for="subscribe-widget-mailinglist" class="form-label"><mvc:message code="Mailinglist" /></label>
                        <mvc:select id="subscribe-widget-mailinglist" path="mailinglistId" cssClass="form-control js-double-select-trigger">
                            <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname" />
                        </mvc:select>
                    </div>

                    <div>
                        <label for="subscribe-widget-doi-mailing" class="form-label"><mvc:message code="facebook.leadAds.doiMailing" /></label>
                        <mvc:select id="subscribe-widget-doi-mailing" path="doiMailingId" cssClass="form-control js-double-select-target" data-field="required">
                            <%-- Loads by JS --%>
                        </mvc:select>
                    </div>
                </div>

                <div>
                    <label for="subscribe-widget-success-msg" class="form-label">
                        <mvc:message code="userform.widget.success" />
                    </label>
                    <mvc:textarea id="subscribe-widget-success-msg" path="successMessage" cssClass="form-control" rows="1" />
                </div>

                <div>
                    <label for="subscribe-widget-error-msg" class="form-label">
                        <mvc:message code="userform.widget.error" />
                    </label>
                    <mvc:textarea id="subscribe-widget-error-msg" path="errorMessage" cssClass="form-control" rows="1" />
                </div>
            </mvc:form>
        </div>
    </div>

    <div id="preview-tile" class="tile" style="flex: 3" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Preview" /></h1>
        </div>

        <div class="tile-body vstack gap-3 js-scrollable">
            <div class="notification-simple notification-simple--info notification-simple--lg">
                <span><mvc:message code="info.userform.widget.preview" /></span>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
  (() => {
    AGN.Opt.SubscribeWidgetDOIMailings = {
      <c:forEach var="mailinglistsMailings" items="${doiMailings}" varStatus="status1">
      '${mailinglistsMailings.key}': {
        <c:forEach var="doiMailing" items="${mailinglistsMailings.value}" varStatus="status2">
        '${doiMailing.mailingID}': '${doiMailing.shortname}'<c:if test="${not status2.last}">,</c:if>
        </c:forEach>
      }<c:if test="${not status1.last}">,</c:if>
      </c:forEach>
    };
  })();
</script>

<script id="widget-preview-block" type="text/x-mustache-template">
    <div class="input-group">
        <input id="widgetCode" type="text" class="form-control" value="{{= _.escape(widgetCode) }}" readonly>
        <button type="button" class="btn btn-info btn-icon" data-copyable data-copyable-target="#widgetCode" data-tooltip='<mvc:message code="button.Copy"/>'>
            <i class="icon icon-copy"></i>
        </button>
    </div>

    {{ print(widgetCode); }}
</script>
