<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<agn:agnForm action="/importwizard" data-form="resource">
    <html:hidden property="action"/>
    <input type="hidden" name="mlists_back" id="mlists_back" value="">

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="ImportClassic"/></h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-set="mlists_back: mlists_back" data-form-submit>
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="active"><span>7</span></li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="SubscribeLists"/>
                <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_6/SubscribeLists.xml"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <c:forEach var="mailinglist" items="${mailinglists}">
                    <div class="form-group">
                        <div class="col-sm-offset-4 col-sm-8">
                            <div class="checkbox">
                                <label class="import_classic_malists_label" for='agn_mlid_${mailinglist.id}'>
                                    <input type="checkbox" id='agn_mlid_${mailinglist.id}' name="agn_mlid_${mailinglist.id}">
                                    ${mailinglist.shortname} (ID ${mailinglist.id})
                                </label>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <div class="tile-footer">
                <logic:equal property="importIsRunning" value="false" name="importWizardForm">
                    <a href="#" class="btn btn-large pull-left" data-form-set="mlists_back: mlists_back" data-form-submit>
                        <i class="icon icon-angle-left"></i>
                        <span class="text"><bean:message key="button.Back"/></span>
                    </a>
                    <button type="button" class="btn btn-large btn-primary pull-right" data-form-submit>
                        <span class="text"><bean:message key="ImportData"/></span>
                        <i class="icon icon-angle-right"></i>
                    </button>
                </logic:equal>
                <logic:equal property="importIsRunning" value="true" name="importWizardForm">
                    <bean:message key="error.classicimport.alreadyrunning"/>
                </logic:equal>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
          


