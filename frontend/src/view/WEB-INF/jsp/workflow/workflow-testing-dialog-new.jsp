<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
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
                                {{ if (startTesting) { }}
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

                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal" data-confirm-positive="{{- startTesting }}">
                            <i class="icon icon-check"></i>
                            <span class="text">
                                {{ if (startTesting) { }}
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
