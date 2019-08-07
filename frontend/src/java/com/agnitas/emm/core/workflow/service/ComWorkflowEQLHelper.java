/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import static com.agnitas.emm.core.target.eql.emm.eql.EqlUtils.getValidOperator;
import static org.agnitas.target.TargetNode.OPERATOR_IS;
import static org.agnitas.util.DbColumnType.GENERIC_TYPE_CHAR;
import static org.agnitas.util.DbColumnType.GENERIC_TYPE_DATE;
import static org.agnitas.util.DbColumnType.GENERIC_TYPE_DOUBLE;
import static org.agnitas.util.DbColumnType.GENERIC_TYPE_INTEGER;
import static org.agnitas.util.DbColumnType.GENERIC_TYPE_VARCHAR;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.agnitas.beans.ProfileField;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.util.StringUtil;
import com.agnitas.emm.core.target.eql.emm.eql.EQLCreationException;
import com.agnitas.emm.core.target.eql.emm.eql.EqlUtils;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolver;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolverFactory;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowRule;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;

@Component("WorkflowEQLHelper")
public class ComWorkflowEQLHelper {
    
    private static final Logger logger = Logger.getLogger(ComWorkflowEQLHelper.class);
 
	private static final String DATE_CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
	private static final String DATE_SYSDATE = "SYSDATE";
	private static final String DATE_NOW = "NOW()";
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###########", new DecimalFormatSymbols(Locale.US));
    private static final String DEFAULT_EQL_DATE_PATTERN = DateUtilities.YYYYMMDD;
    
    /** Factory for profile field resolver. */
	private EmmProfileFieldResolverFactory profileFieldResolverFactory;
	
	private ColumnInfoService columnInfoService;
    
    public ComWorkflowEQLHelper(EmmProfileFieldResolverFactory profileFieldResolverFactory, ColumnInfoService columnInfoService) {
        this.profileFieldResolverFactory = profileFieldResolverFactory;
        this.columnInfoService = columnInfoService;
    }
    
    public String generateDecisionEQL(int companyId, WorkflowDecision decision, boolean isPositiveDecisionCase) {
        if (WorkflowUtils.isReactionCriteriaDecision(decision)) {
            return getReactionDecisionEQL(decision, isPositiveDecisionCase);
        }
        
        if (WorkflowUtils.isProfileFieldCriteriaDecision(decision)) {
            return generateRuleEQL(companyId, decision, true);
        }
        
        return "";
    }
    
    public String generateDateEQL(int companyId, String field, int operator, String dateFormat, String value) {
        return generateDateEQL(companyId, field, operator, dateFormat, value, false);
    }
    
    public String generateDateEQL(int companyId, String field, int operator, String dateFormat, String value, boolean disableThreeValuedLogic) {
        try {
            final EmmProfileFieldResolver profileFieldResolver = profileFieldResolverFactory.newInstance(companyId);
            String resolvedFieldName = profileFieldResolver.resolveProfileFieldColumnName(field);
            return generateDateEQL(resolvedFieldName, field, operator, dateFormat, value, disableThreeValuedLogic);
            
        } catch (ProfileFieldResolveException | EQLCreationException e) {
            logger.error("Cannot generate date EQL", e);
        }
        
        return "";
    }
    
    public String generateRuleEQL(int companyId, WorkflowDecision decision, boolean disableThreeValuedLogic) {
        return generateRuleEQL(companyId, decision.getRules(), decision.getProfileField(), decision.getDateFormat(), disableThreeValuedLogic);
    }
    
    public String generateRuleEQL(int companyId, List<WorkflowRule> rules, String profileField, String dateFormat, boolean disableThreeValuedLogic) {
        try {
            final EmmProfileFieldResolver profileFieldResolver = profileFieldResolverFactory.newInstance(companyId);
            
            String type = "unknownType";
            try {
                ProfileField field = columnInfoService.getColumnInfo(companyId, profileField);
                type = StringUtils.defaultString(field.getDataType(), type);
            } catch (Exception e) {
                logger.error("Cannot find field type for companyId " + companyId + " and column '" + profileField + "'", e);
            }
            String fieldName = profileFieldResolver.resolveProfileFieldColumnName(profileField);
            return generateRuleEQL(fieldName, profileField, type, rules, dateFormat, disableThreeValuedLogic);
            
        } catch (ProfileFieldResolveException | EQLCreationException e) {
            logger.error("Cannot generate rule EQL", e);
        }
        
        return "";
    }
    
    public String generateNumericEQL(int companyId, String field, int operator, String value, int operator2, String value2, boolean disableThreeValuedLogic) {
        try {
            final EmmProfileFieldResolver profileFieldResolver = profileFieldResolverFactory.newInstance(companyId);
            
            String resolvedFieldName = profileFieldResolver.resolveProfileFieldColumnName(field);
            return generateNumericEQL(resolvedFieldName, operator, value, operator2, value2, disableThreeValuedLogic);
            
        } catch (ProfileFieldResolveException | EQLCreationException e) {
            logger.error("Cannot generate rule EQL", e);
        }
        
        return "";
    }
    
    private String generateNumericEQL(String resolvedFieldName, int operator, String value, boolean disableThreeValuedLogic) throws EQLCreationException {
        return generateNumericEQL(resolvedFieldName, operator, value, 0, "0", disableThreeValuedLogic);
    }
    
    private String generateNumericEQL(String resolvedFieldName, int operator, String value, int operator2, String value2, boolean disableThreeValuedLogic) throws EQLCreationException {
    
        TargetOperator targetOperator = getValidOperator(TargetNodeNumeric.getValidOperators(), operator);
        TargetOperator targetOperator2 = getValidOperator(TargetNodeNumeric.getValidOperators(), operator2);
        
        if (targetOperator == null || targetOperator == TargetNode.OPERATOR_MOD && targetOperator2 == null) {
            throw new EQLCreationException("No or invalid primary operator defined");
        }
        
        if(targetOperator == OPERATOR_IS) {
            return String.format("%s %s", resolvedFieldName, EqlUtils.getIsEmptyOperatorValue(value));
        }
        
        double floatValue = NumberUtils.toDouble(value, 0.0f);
        value = DECIMAL_FORMAT.format(floatValue);
    
        if (targetOperator == TargetNode.OPERATOR_MOD) {
            double floatValue2 = NumberUtils.toDouble(value2, 0.0f);
            value2 = DECIMAL_FORMAT.format(floatValue2);
            return EqlUtils.makeEquation(resolvedFieldName, targetOperator, value, targetOperator2, value2, disableThreeValuedLogic);
        } else {
            return EqlUtils.makeEquation(resolvedFieldName, targetOperator, value, disableThreeValuedLogic);
        }
    }
    
    private String getTrackingVetoEQL(boolean isExcludeVetoed) {
        if(isExcludeVetoed) {
            try {
                return " OR " + generateNumericEQL("`sys_tracking_veto`", TargetNode.OPERATOR_EQ.getOperatorCode(), "1", true);
            } catch (EQLCreationException e) {
                logger.error("Cannot get tracking veto EQL", e);
            }
        }
        
        return "";
    }
    
    private String generateDateEQL(String resolvedFieldName, String field, int operatorCode, String dateFormat, String value, boolean disableThreeValuedLogic) throws EQLCreationException {
        TargetOperator targetOperator = getValidOperator(TargetNodeDate.getValidOperators(), operatorCode);
        
        if (targetOperator == null) {
            throw new EQLCreationException("No or invalid primary operator defined");
        }
        
        if(targetOperator == OPERATOR_IS) {
            return String.format("%s %s", resolvedFieldName, EqlUtils.getIsEmptyOperatorValue(value));
        }
        
        String eql;
        String dateFormatEQL = EqlUtils.toEQLDateFormat(StringUtils.defaultIfEmpty(dateFormat, DEFAULT_EQL_DATE_PATTERN));

        if (DATE_CURRENT_TIMESTAMP.equalsIgnoreCase(field) || DATE_SYSDATE.equalsIgnoreCase(field) || DATE_NOW.equalsIgnoreCase(field)) {
            eql = String.format("TODAY %s %s DATEFORMAT %s", targetOperator.getOperatorSymbol(), StringUtil.makeEqlStringConstant(value), StringUtil.makeEqlStringConstant(dateFormatEQL));
        } else {
            value = StringUtils.upperCase(value);
    
            if (value.startsWith(DATE_CURRENT_TIMESTAMP)) {
                value = "TODAY" + value.substring(DATE_CURRENT_TIMESTAMP.length());
            } else if (value.startsWith(DATE_SYSDATE)) {
                value = "TODAY" + value.substring(DATE_SYSDATE.length());
            } else if (value.startsWith(DATE_NOW)) {
                value = "TODAY" + value.substring(DATE_NOW.length());
            } else {
                value = StringUtil.makeEqlStringConstant(value);
            }
    
            value += " DATEFORMAT '" + dateFormatEQL + "'";
    
            eql = EqlUtils.makeEquation(resolvedFieldName, targetOperator, value, disableThreeValuedLogic);
        }
		
        return eql;
    }
 
	private String generateStringEQL(String resolvedFieldName, int operator, String value, boolean disableThreeValuedLogic) throws EQLCreationException {
        TargetOperator targetOperator = getValidOperator(TargetNodeString.getValidOperators(), operator);

		if (targetOperator == TargetNode.OPERATOR_IS) {
		    return String.format("%s %s", resolvedFieldName, EqlUtils.getIsEmptyOperatorValue(value));
		}
  
		if (targetOperator == TargetNode.OPERATOR_MOD) {
			throw new EQLCreationException("No or invalid primary operator defined");
		}
		
        Object eqlValue;
        if (targetOperator == TargetNode.OPERATOR_LIKE || targetOperator == TargetNode.OPERATOR_NLIKE) {
            eqlValue = StringUtil.makeEqlMatchingPattern(value);
        } else {
            eqlValue = StringUtil.makeEqlStringConstant(value);
        }
        return EqlUtils.makeEquation(resolvedFieldName, targetOperator, eqlValue, disableThreeValuedLogic);
	}
    
    private String getReactionDecisionEQL(WorkflowDecision decision, boolean isPositiveDecisionCase) {
        StringBuilder builder = new StringBuilder();
        int mailingId = decision.getMailingId();
        boolean isExcludeVetoed = !decision.isIncludeVetoed() && !isPositiveDecisionCase;
        
        switch (decision.getReaction()) {
            case OPENED:
                builder.append(String.format("OPENED MAILING %d", mailingId));
                break;
            
            case CLICKED:
                builder.append(String.format("CLICKED IN MAILING %d", mailingId));
                break;
            
            case OPENED_AND_CLICKED:
                builder.append(String.format("OPENED MAILING %1$d AND CLICKED IN MAILING %1$d", mailingId));
                break;
            
            case OPENED_OR_CLICKED:
                String eql = String.format("OPENED MAILING %1$d OR CLICKED IN MAILING %1$d", mailingId);
                if(isExcludeVetoed) {
                    builder.append("(").append(eql).append(")");
                } else {
                    builder.append(eql);
                }
                break;
            
            case BOUGHT:
                builder.append(String.format("REVENUE BY MAILING %d", mailingId));
                break;
            
            case CLICKED_LINK:
                builder.append(String.format("CLICKED LINK %d IN MAILING %d", decision.getLinkId(), mailingId));
                break;
            
            default:
                break;
        }
        
        builder.append(getTrackingVetoEQL(isExcludeVetoed));
        
        return builder.toString();
    }
    
    private String generateRuleEQL(String resolvedFieldName, String field, String type, List<WorkflowRule> rules, String dateFormat, boolean disableThreeValuedLogic) throws EQLCreationException {
	    List<String> expressions = new ArrayList<>();
        
        for (WorkflowRule rule : rules) {
            
            int operator = rule.getPrimaryOperator();
            String value = rule.getPrimaryValue();
            StringBuilder expression = new StringBuilder();
            
            if(!expressions.isEmpty()) {
                expression.append(EqlUtils.convertChainOperator(rule.getChainOperator())).append(" ");
            }
            
            if(rule.getParenthesisOpened() == 1) {
                expression.append("(");
            }
    
            switch (StringUtils.trimToEmpty(DbColumnType.dbType2String(type))) {
				case GENERIC_TYPE_VARCHAR:
				case GENERIC_TYPE_CHAR:
					expression.append(generateStringEQL(resolvedFieldName, operator, value, disableThreeValuedLogic));
					break;
				case GENERIC_TYPE_INTEGER:
				case GENERIC_TYPE_DOUBLE:
					expression.append(generateNumericEQL(resolvedFieldName, operator, value, disableThreeValuedLogic));
					break;
				case GENERIC_TYPE_DATE:
					expression.append(generateDateEQL(resolvedFieldName, field, operator, dateFormat, value, disableThreeValuedLogic));
					break;
				default:
					//nothing to do
			}
			
			if(rule.getParenthesisClosed() == 1) {
                expression.append(")");
            }
            
            expressions.add(expression.toString());
        }
        
        return StringUtils.join(expressions, " ");
    }
    
}
