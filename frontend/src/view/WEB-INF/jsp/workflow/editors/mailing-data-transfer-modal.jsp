<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<script id="mailing-data-transfer-modal" type="text/x-mustache-template">
    <div class="modal modal-wide">
        <div class="modal-dialog">
            <div class="modal-content" data-initializer="mailing-data-transfer-modal">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                    <h4 class="modal-title">
                        <bean:message key="workflow.settings.overtake.title"/>
                    </h4>
                </div>

                <div class="modal-body">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <bean:message key="workflow.mailing.transferQuestion"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <ul class="options-for-transfer list-group">
                                {{ if (paramsToAsk.includes(Def.MAILING_PARAM_PLANNED_DATE)) { }}
                                <li class="list-group-item">
                                    <label class="checkbox-inline unselectable">
                                        <input data-mailing-param="{{= Def.MAILING_PARAM_PLANNED_DATE }}" type="checkbox"/>
                                        <bean:message key="mailing.plan.date"/>
                                    </label>
                                </li>
                                {{ } }}
                                {{ if (paramsToAsk.includes(Def.MAILING_PARAM_MAILING_LIST)) { }}
                                <li class="list-group-item">
                                    <label class="checkbox-inline unselectable">
                                        <input data-mailing-param="{{= Def.MAILING_PARAM_MAILING_LIST }}" type="checkbox"/>
                                        <bean:message key="mailinglist"/>
                                    </label>
                                </li>
                                {{ } }}
                                {{ if (paramsToAsk.includes(Def.MAILING_PARAM_TARGET_GROUPS)) { }}
                                <li class="list-group-item">
                                    <label class="checkbox-inline unselectable">
                                        <input data-mailing-param="{{= Def.MAILING_PARAM_TARGET_GROUPS }}" type="checkbox"/>
                                        <bean:message key="Targetgroups"/>
                                    </label>
                                </li>
                                {{ } }}
                                {{ if (paramsToAsk.includes(Def.MAILING_PARAM_ARCHIVE)) { }}
                                <li class="list-group-item">
                                    <label class="checkbox-inline unselectable">
                                        <input data-mailing-param="{{= Def.MAILING_PARAM_ARCHIVE }}" type="checkbox"/>
                                        <bean:message key="mailing.archive"/>
                                    </label>
                                </li>
                                {{ } }}
                                {{ if (paramsToAsk.includes(Def.MAILING_PARAM_LIST_SPLIT)) { }}
                                <li class="list-group-item">
                                    <label class="checkbox-inline unselectable">
                                        <input data-mailing-param="{{= Def.MAILING_PARAM_LIST_SPLIT }}" type="checkbox"/>
                                        <bean:message key="mailing.listsplit"/>
                                    </label>
                                </li>
                                {{ } }}
                                {{ if (paramsToAsk.includes(Def.MAILING_PARAM_SEND_DATE)) { }}
                                <li class="list-group-item">
                                    <label class="checkbox-inline unselectable">
                                        <input data-mailing-param="{{= Def.MAILING_PARAM_SEND_DATE }}" type="checkbox"/>
                                        <bean:message key="mailing.SendingTime"/>
                                    </label>
                                </li>
                                {{ } }}
                                {{ if (paramsToAsk.length > 1) { }}
                                <li class="list-group-item">
                                    <label class="checkbox-inline unselectable">
                                        <input id="transferAllSettings" type="checkbox"/>
                                        <bean:message key="workflow.mailing.transfer.allSettings"/>
                                    </label>
                                </li>
                                {{ } }}
                            </ul>

                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-xs-12">
                            <div class="well">
                                <bean:message key="workflow.mailing.transfer.notice"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">
                                <bean:message key="default.No"/>
                            </span>
                        </button>

                        <button type="button" class="btn btn-primary btn-large" data-dismiss="modal" data-action="transfer-mailing-data">
                            <i class="icon icon-check"></i>
                            <span class="text">
                                <bean:message key="workflow.mailing.transfer"/>
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
