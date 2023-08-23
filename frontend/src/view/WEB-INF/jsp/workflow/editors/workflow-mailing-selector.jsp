<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<div class="settings_campaign_form_item campaign-mailing-sort">
    <div class="form-group">
        <div class="col-sm-4">
            <label class="control-label">
                <bean:message key="workflow.mailing.sortBy"/>
            </label>
        </div>
        <div class="col-sm-8">
            <div id="shortname_sort" class="sort sort-btn">
                <div data-action="${param.mailingSelectorSorter}-shortname" class="sort-icon unselectable"
                     unselectable="on"  title='<bean:message key="workflow.mailing.SortAlphabetically"/>'>A-Z
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
                     data-tooltip='<bean:message key="workflow.mailing.SortByDate"/>'>
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
                <bean:message key="Mailing"/>
            </label>
        </div>
        <div class="col-sm-8">
            <select name="${param.selectName}" data-action="${param.mailingSelectorEventHandler}" class="form-control js-select" ></select>
        </div>
    </div>
</div>
