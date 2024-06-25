<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="settings_campaign_form_item campaign-mailing-sort">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label">
                <mvc:message code="workflow.mailing.sortBy"/>
            </label>
        </div>
        <div class="col-sm-8">
            <div id="shortname_sort" class="sort sort-btn">
                <div data-action="${param.mailingSelectorSorter}-shortname" class="sort-icon unselectable"
                     unselectable="on"  title='<mvc:message code="workflow.mailing.SortAlphabetically"/>'>A-Z
                </div>
                <div class="arrows">
                    <div class="arrowUp" data-action="${param.mailingSelectorSorter}-shortname"></div>
                    <div class="arrowDown" data-action="${param.mailingSelectorSorter}-shortname"></div>
                </div>
            </div>
        </div>
        <div class="col-sm-8 col-sm-push-4">
            <div id="date_sort" class="sort">
                <div data-action="${param.mailingSelectorSorter}-date" class="sort-icon"
                     data-tooltip='<mvc:message code="workflow.mailing.SortByDate"/>'>
                    <i class="icon icon-calendar-o"></i>
                </div>
                <div class="arrows">
                    <div class="arrowUp" data-action="${param.mailingSelectorSorter}-date"></div>
                    <div class="arrowDown" data-action="${param.mailingSelectorSorter}-date"></div>
                </div>
            </div>
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label">
                <mvc:message code="Mailing"/>
            </label>
        </div>
        <div class="col-sm-8">
            <select name="${param.selectName}" data-action="${param.mailingSelectorEventHandler}" class="form-control js-select" ></select>
        </div>
    </div>
</div>
