<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<agn:agnForm action="/mailing_compare" data-form-type="search">
    <html:hidden property="action"/>
    <html:hidden styleId="reportFormat" property="reportFormat" value="html"/>
    <html:hidden property="recipientType" value="ALL_SUBSCRIBERS" />

    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-targetGroup">
                <i class="icon tile-toggle icon-angle-up"></i>
                <bean:message key="Targets"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit-static>
                        <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Refresh"/></span>
                    </button>
                </li>
            </ul>
        </div>
        <div id="tile-targetGroup" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <bean:message key="Targetgroups"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <c:set var="addTargetGroupMessage" scope="page">
                        <bean:message key="addTargetGroup" />
                    </c:set>
                    <agn:agnSelect property="selectedTargets"  styleClass="form-control js-select" multiple="" data-placeholder="${addTargetGroupMessage}">
                        <c:forEach var="target" items="${targetGroups}" varStatus="rowCounter">
                            <html:option value="${target.id}">${target.targetName}</html:option>
                        </c:forEach>
                    </agn:agnSelect >
                </div>
            </div>
        </div>
    </div>

    <div class="tile" data-form-content>

        <jsp:include page="birt-mailing-comp-stat-recipientfilter.jsp" flush="false">
            <jsp:param name="isCompareBtnShow" value="true"/>
        </jsp:include>

        <div class="tile-content" data-form-content>

            <div class="table-control pull-left">
                <div class="well"><bean:message key="error.NrOfMailings"/></div>
            </div>

            <div class="table-wrapper">
                <display:table class="table table-bordered table-striped table-hover js-table" id="mailing" name="mailings" requestURI="/mailing_compare.do?action=${ACTION_LIST}&__fromdisplaytag=true" excludedParams="*" sort="list">
                    <display:column titleKey="statistic.compare" sortable="false" class="js-checkable">
                        <input type="checkbox" name="MailCompID_${mailing.id}" data-checkbox-restriction/>
                    </display:column>
                    <display:column titleKey="Mailing" sortable="true" sortProperty="shortname">
                        <span class="multiline-auto">${mailing.shortname}</span>
                        <%-- we need this hidden link to provide edit-URL --%>
                        <html:link styleClass="hidden js-row-show" page="/mailing_stat.do?action=${ACTION_MAILINGSTAT}&mailingID=${mailing.id}"/>
                    </display:column>
                    <display:column titleKey="Description" sortable="true" sortProperty="description">
                        <span class="multiline-auto">${mailing.description}</span>
                    </display:column>
                    <display:column titleKey="mailing.senddate" sortable="true" property="senddate" sortProperty="senddate" format="{0,date,yyyy-MM-dd}"/>
                </display:table>
            </div>

        </div>
    </div>

    <script type="text/javascript">
        var maximumChecked = 10;

        $('[data-checkbox-restriction]').change(function (){
            updateCheckboxRestriction();
        });

        function updateCheckboxRestriction(){
            if ($('[data-checkbox-restriction]:checked').length >= maximumChecked){
                $('[data-checkbox-restriction]:not(:checked)').attr("disabled", true);
            } else {
                $('[data-checkbox-restriction]').removeAttr("disabled");
            }
        }

        updateCheckboxRestriction();
    </script>
</agn:agnForm>
