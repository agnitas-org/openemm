<%@page import="org.apache.commons.text.StringEscapeUtils"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
         import="org.agnitas.util.*, java.util.*, java.text.*, org.agnitas.web.forms.*,  org.agnitas.web.*, com.agnitas.web.ComImportWizardForm, com.agnitas.web.ComImportWizardAction" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>


<% int tmpOffset = 0;
    int tmpOffset2 = 0;
    int tmpSize = 0;
    if (session.getAttribute("importWizardForm") != null) {
        tmpOffset = ((ComImportWizardForm) session.getAttribute("importWizardForm")).getPreviewOffset();
        tmpSize = ((ComImportWizardForm) session.getAttribute("importWizardForm")).getParsedContent().size();
    }

%>

<agn:agnForm action="/importwizard" enctype="multipart/form-data" data-form="resource">
    <html:hidden property="action"/>

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline"><i class="icon icon-file-o"></i> <bean:message key="import.Wizard"/></h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-set="verify_back: verify_back" data-form-submit>
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="active"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li>
                                <a href="#" data-form-submit>
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="import.csv_analysis"/>
                <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_4/CsvAnalysis.xml"></button>
            </div>
            <div class="tile-content tile-content-forms">
                <div class="table-wrapper">
                    <table class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th><bean:message key="csv_used_column"/></th>
                                <th><bean:message key="csv_unused_column_csv"/></th>
                                <th><bean:message key="csv_unused_column_db"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="align-top">
                                    <logic:iterate id="element" name="importWizardForm" property="csvAllColumns" scope="session"
                                                   type="CsvColInfo">
                                        <logic:equal name="element" property="active" value="true">
                                            <bean:write name="element" property="name"/><br>
                                        </logic:equal>
                                    </logic:iterate>
                                </td>
                                <td class="align-top">
                                    <logic:iterate id="element" name="importWizardForm" property="csvAllColumns" scope="session"
                                                   type="CsvColInfo">
                                        <logic:notEqual name="element" property="active" value="true">
                                            <bean:write name="element" property="name"/><br>
                                        </logic:notEqual>
                                    </logic:iterate>
                                </td>
                                <td class="align-top">
                                    <logic:iterate id="hashelement" name="importWizardForm" property="dbAllColumns" scope="session">
                                        <bean:define id="element" name="hashelement" property="value"/>
                                        <logic:notEqual name="element" property="active" value="true">
                                            <bean:write name="element" property="name"/><br>
                                        </logic:notEqual>
                                    </logic:iterate>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="tile-separator"></div>
                <div class="inline-tile">
                    <div class="inline-tile-header">
                        <h2><bean:message key="default.Preview"/></h2>
                    </div>
                    <div class="inline-tile-content">
                        <div class="table-wrapper">
                            <table class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <logic:iterate id="element" name="importWizardForm" property="csvAllColumns" scope="session"
                                                       type="CsvColInfo">
                                            <logic:equal name="element" property="active" value="true">
                                                <th><bean:write name="element" property="name"/></th>
                                            </logic:equal>
                                        </logic:iterate>
                                    </tr>
                                </thead>
                                <tbody>
                                <%
                                    Object leElement = null;
                                    Class leClass = null;
                                    SimpleDateFormat aFormatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, (Locale) session.getAttribute(org.apache.struts.Globals.LOCALE_KEY));
                                    aFormatter.applyPattern(aFormatter.toPattern().replaceFirst("y+", "yyyy").replaceFirst(", ", " "));
                                %>
                                <logic:iterate id="element2" indexId="element2idx" name="importWizardForm"
                                               offset="<%= Integer.toString(tmpOffset) %>" length="5" property="parsedContent"
                                               scope="session">
                                    <tr>
                                        <logic:iterate id="element3" name="element2" scope="page">
                                            <td><% leElement = pageContext.getAttribute("element3");
                                                String value = null;
                                                if (leElement != null) {
                                                    leClass = leElement.getClass();
                                                    if (leClass.getName().equals("java.lang.String")) {
                                                        value = StringEscapeUtils.escapeHtml4((String) leElement);
                                                    } else if (leClass.getName().equals("java.lang.Double")) {
                                                        value = "" + ((Double) leElement).longValue();
                                                    }
                                                    if (leClass.getName().equals("java.util.Date")) {
                                                        value = aFormatter.format((java.util.Date) leElement);
                                                    }
                                                /*if(leClass.getName().equals("java.lang.String")) {
                                                    out.print("<input name=\"dummy\" type=\"text\" size=\"13\" value=\""+StringEscapeUtils.escapeHtml((String)leElement)+"\" readonly>");
                                                }
                                                if(leClass.getName().equals("java.lang.Double")) {
                                                    out.print("<input name=\"dummy\" type=\"text\" size=\"8\" value=\""+((Double)leElement).longValue()+"\" readonly>");
                                                }
                                                if(leClass.getName().equals("java.util.Date")) {
                                                    out.print("<input name=\"dummy\" type=\"text\" size=\"13\" value=\""+aFormatter.format((java.util.Date)leElement)+"\" readonly>");
                                                }*/
                                                } else {
                                                    value = "";
                                                } %>
                                                <input class="form-control" name="dummy" type="text" size="13" value="<%= value %>" readonly>
                                            </td>
                                        </logic:iterate>
                                    </tr>
                                </logic:iterate>
                                </tbody>
                            </table>
                            <div class="table-controls">
                                <div class="table-control pull-right">
                                    <ul class="pagination">
                                        <logic:iterate id="element2" indexId="element2idx" name="importWizardForm"
                                                       offset="<%= Integer.toString(tmpOffset) %>" length="5" property="parsedContent"
                                                       scope="session">
                                            <logic:equal name="element2idx" value="<%= Integer.toString(tmpOffset) %>">
                                                <% tmpOffset2 = tmpOffset - 5;
                                                    if (tmpOffset2 < 0) tmpOffset2 = 0; %>
                                                <li>
                                                    <html:link
                                                            styleClass="js-table-paginate"
                                                            page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_PREVIEW_SCROLL + "&previewOffset=" + tmpOffset2 %>'>
                                                        <i class="icon icon-angle-left"></i>
                                                        <bean:message key="button.Previous"/>
                                                    </html:link>
                                                </li>
                                            </logic:equal>
                                            <logic:equal name="element2idx" value="<%= Integer.toString(tmpOffset+1) %>">
                                                <% tmpOffset2 = tmpOffset + 5;
                                                    if (tmpOffset2 >= tmpSize) tmpOffset2 = tmpSize - 5;
                                                    if (tmpOffset2 < 0) tmpOffset2 = 0; %>
                                                <li>
                                                    <html:link
                                                        page='<%= "/importwizard.do?action=" + ComImportWizardAction.ACTION_PREVIEW_SCROLL + "&previewOffset=" + tmpOffset2 %>'>
                                                        <bean:message key="button.Next"/>
                                                        <i class="icon icon-angle-right"></i>
                                                    </html:link>
                                                </li>
                                            </logic:equal>
                                        </logic:iterate>
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-set="verify_back: verify_back" data-form-submit>
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <button type="button" class="btn btn-large btn-primary pull-right" data-form-submit>
                    <span class="text"><bean:message key="button.Proceed"/></span>
                    <i class="icon icon-angle-right"></i>
                </button>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>


