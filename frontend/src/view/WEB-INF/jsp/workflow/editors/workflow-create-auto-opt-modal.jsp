<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="allWorkflows" type="java.util.List<com.agnitas.emm.core.workflow.beans.Workflow>"--%>

<script id="create-auto-opt-modal" type="text/x-mustache-template">
    <div class="modal modal-wide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                    <h4 class="modal-title">
                        <mvc:message code="mailing.autooptimization"/>
                    </h4>
                </div>

                <div class="modal-body">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="auto-opt-mailings-count">
                                <mvc:message code="workflow.autooptimization.mailing.count"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <select id="auto-opt-mailings-count" name="confirm-choice" class="form-control js-select">
                                <option>2</option>
                                <option>3</option>
                                <option>4</option>
                                <option>5</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Apply"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
