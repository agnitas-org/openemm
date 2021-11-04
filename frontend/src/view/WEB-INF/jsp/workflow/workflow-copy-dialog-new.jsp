<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<script id="workflow-copy-modal" type="text/x-mustache-template">
    <div class="modal modal-wide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title"> <mvc:message code="workflow.ownWorkflow.copyWorkflowTitle" /> </h4>
                </div>

                <div class="modal-body">
                    <div class="form-group">
                        <div class="col-xs-12">
                            <div class="well">

                                {{ if (hasContent) { }}
                                    <mvc:message code="workflow.copy.withContentQuestion"/>
                                {{ } else { }}
                                    <mvc:message code="workflow.copy.question"/>
                                {{ } }}
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        {{ if (hasContent) { }}
                            <button type="button" class="btn btn-regular btn-primary js-confirm-positive" data-dismiss="modal" data-confirm-positive="true">
                                <span class="text">
                                    <mvc:message code="workflow.copy.btn.withContent"/>
                                </span>
                            </button>

                            <button type="button" class="btn btn-regular btn-primary js-confirm-positive" data-dismiss="modal" data-confirm-positive="false">
                                <span class="text">
                                    <mvc:message code="workflow.copy.btn.onlyChain"/>
                                </span>
                            </button>
                        {{ } else { }}
                            <button type="button" class="btn btn-regular btn-primary js-confirm-positive" data-dismiss="modal" data-confirm-positive="false">
                                <span class="text">
                                    <mvc:message code="button.Copy"/>
                                </span>
                            </button>
                        {{ } }}

                        <button type="button" class="btn btn-regular js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">
                                <mvc:message code="button.Cancel"/>
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
