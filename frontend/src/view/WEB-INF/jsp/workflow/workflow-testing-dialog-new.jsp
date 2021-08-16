<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<script id="testing-modal" type="text/x-mustache-template">
    <div class="modal modal-wide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title"> <mvc:message code="workflow.single" />:&nbsp; {{- shortname}} </h4>
                </div>

                <div class="modal-body">
                    <div class="form-group">
                        <div class="col-xs-12">
                            <div class="well">
                                {{ if (isStart) { }}
                                    <mvc:message code="workflow.test.start.question"/>
                                {{ } else { }}
                                    <mvc:message code="workflow.test.stop.question"/>
                                {{ } }}
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">
                                <mvc:message code="button.Cancel"/>
                            </span>
                        </button>

                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-form-set="status: {{- newStatus }}" data-action="start-stop-dry-run">
                            <i class="icon icon-check"></i>
                            <span class="text">
                                {{ if (isStart) { }}
                                    <mvc:message code="button.Start"/>
                                {{ } else { }}
                                    <mvc:message code="default.Yes"/>
                                {{ } }}
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
