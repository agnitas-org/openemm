<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="targetGroupList" type="java.util.List"--%>
<%--@elvariable id="selectionMax" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/statistics/mailing/comparison/list.action" modelAttribute="form"
          data-form="resource"
          data-resource-selector="#available-mailings">
    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-targetGroup">
                <i class="icon tile-toggle icon-angle-up"></i>
                <mvc:message code="Targets"/>
            </a>
        </div>

        <div id="tile-targetGroup" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="Targetgroups"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:message var="placeholderTargetGroupSelect" code="addTargetGroup"/>
                    <mvc:select path="targetIds" cssClass="form-control js-select" multiple="true"
                                data-placeholder="${placeholderTargetGroupSelect}">
                        <mvc:options items="${targetGroupList}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="default.Overview"/></h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><mvc:message code="Export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header">
                            <mvc:message code="statistics.exportFormats"/>
                        </li>
                        <li>
                            <c:url var="exportUrl" value="/statistics/mailing/comparison/export.action"/>
                            <a href="#" tabindex="-1" data-form-set="reportFormat:csv" data-form-submit-static data-form-url="${exportUrl}" data-prevent-load="">
                                <i class="icon icon-file-excel-o"></i>
                                <mvc:message code="user.export.csv"/>
                            </a>
                        </li>
                    </ul>
                </li>
                <li>
                    <c:url var="compareUrl" value="/statistics/mailing/comparison/compare.action"/>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit data-form-url="${compareUrl}">
                        <i class="icon icon-search"></i>
                        <span class="text"><mvc:message code="statistic.compare"/></span>
                    </button>
                </li>
            </ul>
        </div>

        <div id="available-mailings" class="tile-content">
            <div class="table-control pull-left">
                <div class="well"><mvc:message code="error.NrOfMailings"/></div>
            </div>

            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table"
                               id="mailing"
                               name="mailings"
                               requestURI="/statistics/mailing/comparison/list.action"
                               excludedParams="*" sort="list">

                    <display:setProperty name="basic.msg.empty_list_row" value=" "/>

                    <display:column class="js-checkable" sortable="false"
                                    titleKey="statistic.compare">
                        <input type="checkbox" name="bulkIds" value="${mailing.id}" data-select-restrict/>
                    </display:column>

                    <display:column property="shortname" titleKey="Mailing"
                                    sortable="true" sortProperty="shortname" headerClass="js-table-sort"/>

                    <display:column property="description" titleKey="Description"
                                    sortable="true" sortProperty="description" headerClass="js-table-sort"/>

                    <display:column property="senddate" titleKey="mailing.senddate" format="{0, date, ${adminDateFormat}}"
                                    sortable="true" sortProperty="senddate" headerClass="js-table-sort"/>


                    <display:column class="table-actions hidden" headerClass="hidden">
                        <c:url var="viewMailingStatisticLink" value="/statistics/mailing/${mailing.id}/view.action"/>
                        <a href="${viewMailingStatisticLink}" class="hidden js-row-show"></a>
                    </display:column>

                </display:table>
            </div>
        </div>

    </div>
</mvc:form>

<script>
    (function(){
      var MAX = ${selectionMax};
      var SELECTOR = '[data-select-restrict]';
      AGN.Lib.Action.new({'change': '[data-select-restrict]'}, function() {
		  var selected = $(SELECTOR + ':checked').length;
		  if (selected >= MAX) {
		    $(SELECTOR + ':not(:checked)').attr('disabled', true);
          } else {
		    $(SELECTOR).removeAttr('disabled');
          }
		});
  })();
</script>
