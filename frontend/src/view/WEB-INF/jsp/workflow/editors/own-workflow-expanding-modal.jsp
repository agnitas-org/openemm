<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="allWorkflows" type="java.util.List<com.agnitas.emm.core.workflow.beans.Workflow>"--%>

<script id="own-workflow-expanding-modal" type="text/x-mustache-template">
    <div class="modal modal-wide">
        <div class="modal-dialog">
            <div class="modal-content" data-initializer="own-workflow-expanding-modal">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                    <h4 class="modal-title">
                        <mvc:message code="workflow.ownCampaign"/>
                    </h4>
                </div>

                <div class="modal-body">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="workflow-select">
                                <mvc:message code="Workflow"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <select id="workflow-select" class="form-control js-select">
                                <c:forEach var="workflow" items="${allWorkflows}">
                                    <option value="${workflow.workflowId}">${fn:escapeXml(workflow.shortname)}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-8 col-sm-push-4">
                            <label class="radio-inline">
                                <input type="radio" name="copyContent" value="true" checked="checked"/>
                                <mvc:message code="workflow.ownWorkflow.withContent"/>
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="copyContent" value="false"/>
                                <mvc:message code="workflow.ownWorkflow.onlyWorkflow"/>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>

                        <c:choose>
                            <c:when test="${fn:length(allWorkflows) > 0}">
                                <button type="button" class="btn btn-primary btn-large" data-dismiss="modal" data-action="expand-own-workflow">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.Apply"/></span>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn btn-primary btn-large disabled" disabled="disabled">
                                    <i class="icon icon-check"></i>
                                    <span class="text"><mvc:message code="button.Apply"/></span>
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
