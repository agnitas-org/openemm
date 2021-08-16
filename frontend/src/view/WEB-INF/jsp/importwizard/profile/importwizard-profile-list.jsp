<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<%--@elvariable id="importProfileForm" type="org.agnitas.web.forms.ImportProfileForm"--%>

<agn:agnForm action="/importprofile">
    <input type="hidden" id="setDefault" name="setDefault" value=""/>

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "import-profile-overview": {
                "rows-count": ${importProfileForm.numberOfRows}
            }
        }
    </script>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="import.ProfileAdministration" />
            </h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text"><bean:message key="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
                        <li>
                            <label class="label">
                                <html:radio property="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
        <div class="tile-content">
            <div class="table-wrapper">


                <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    id="profile"
                    name="profileList"
                    pagesize="${importProfileForm.numberOfRows}"
                    requestURI="/importprofile.do?action=${ACTION_LIST}"
                    excludedParams="*" defaultsort="1">

                    <display:column headerClass="js-table-sort" sortProperty="name" property="name" titleKey="import.ImportProfile" sortable="true" />
                    <display:column titleKey="recipient.importprofile.defaultprofile">


                        <c:choose>
                            <c:when test="${importProfileForm.defaultProfileId == profile.id}">
                                <button type="button" class="btn btn-regular profile_checkbox_btn--checked" disabled="disabled">
                                    <i class="icon icon-check-square-o"></i>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="btn btn-regular profile_checkbox_btn" data-form-set="setDefault: 'setDefault', defaultProfileId: '${profile.id}'" data-form-submit>
                                    <i class="icon icon-square-o"></i>
                                </button>
                            </c:otherwise>
                        </c:choose>

                    </display:column>
                    <display:column class="table-actions">

                        <c:set var="importProfileDeleteMessage" scope="page">
                            <bean:message key="recipient.importprofile.delete"/>
                        </c:set>
                        <agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                            data-tooltip="${importProfileDeleteMessage}"
                            page="/importprofile.do?action=${ACTION_CONFIRM_DELETE}&profileId=${profile.id}&fromListPage=true">
                            <i class="icon icon-trash-o"></i>
                        </agn:agnLink>

                        <html:link styleClass="hidden js-row-show" page="/importprofile.do?action=${ACTION_VIEW}&profileId=${profile.id}">
                        </html:link>
                    </display:column>
                </display:table>

            </div>
        </div>
    </div>

</agn:agnForm>
