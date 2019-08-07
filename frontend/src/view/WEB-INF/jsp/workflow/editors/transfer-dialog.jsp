<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<div id="${param.containerId}-transfer-dialog" class="transfer-dialog-body" style="display: none;">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label">
                <bean:message key="workflow.mailing.transferQuestion"/>
            </label>
        </div>
        <div class="col-sm-8">
            <ul class="options-for-transfer list-group">
                <li class="list-group-item">
                    <label class="checkbock-inline">
                        <input id="${param.containerId}-transferPlanedFor" name="transferPlanedFor" value="1" type="checkbox" />
                        <bean:message key="mailing.plan.date"/>
                    </label>
                </li>
                <li class="list-group-item">
                    <label class="checkbock-inline">
                        <input id="${param.containerId}-transferMailingList" name="transferMailingList" value="1" type="checkbox" />
                        <bean:message key="mailinglist"/>
                    </label>
                </li>
                <li class="list-group-item">
                    <label class="checkbock-inline">
                        <input id="${param.containerId}-transferTargetGroups" name="transferTargetGroups" value="1" type="checkbox" />
                        <bean:message key="mailing.archive"/>
                    </label>
                </li>
                <li class="list-group-item">
                    <label class="checkbock-inline">
                        <input id="${param.containerId}-transferArchive" name="transferArchive" value="1" type="checkbox" />
                        <bean:message key="mailing.archive"/>
                    </label>
                </li>
                <li class="list-group-item">
                    <label class="checkbock-inline">
                        <input id="${param.containerId}-transferListSplit" name="transferListSplit" value="1" type="checkbox" />
                        <bean:message key="mailing.listsplit"/>
                    </label>
                </li>
                <li class="list-group-item">
                    <label class="checkbock-inline">
                        <input id="${param.containerId}-transferDeliveryTime" name="transferDeliveryTime" value="1" type="checkbox" />
                        <bean:message key="mailing.SendingTime"/>
                    </label>
                </li>
                <li class="list-group-item">
                    <label class="checkbock-inline">
                        <input id="${param.containerId}-transferAllSettings" name="transferAllSettings" value="1" type="checkbox" />
                        <bean:message key="workflow.mailing.transfer.allSettings"/>
                    </label>
                </li>
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

    <hr>
    <div class="form-group">
        <div class="col-xs-12">
            <div class="btn-group">
                <a href="#" class="btn btn-regular" data-action="${param.baseMailingEditor}-no">
                    <bean:message key="default.No"/>
                </a>
                <a href="#" class="btn btn-regular btn-primary" data-action="${param.baseMailingEditor}-transfer-data">
                    <bean:message key="workflow.mailing.transfer"/>
                </a>
            </div>
        </div>
    </div>
</div>
