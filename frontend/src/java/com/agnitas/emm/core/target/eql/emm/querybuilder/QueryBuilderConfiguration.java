/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.ClickedInMailingRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.ContainsRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.DateRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.DefaultRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.EmptyRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.GenericRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.LikeRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.ModRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.NotContainsRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.NotEmptyRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.NotLikeRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.NotStartsWithRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.OpenedMailingRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.ReceivedMailingRuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.RuleConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.converter.StartsWithRuleConverter;
import org.apache.commons.lang3.ArrayUtils;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.impl.ProfileFieldImpl;
import com.agnitas.emm.core.target.eql.emm.resolver.EmmProfileFieldResolverFactory;

public class QueryBuilderConfiguration {

    private List<ProfileField> independentFields = new ArrayList<>();

    protected Map<String, RuleConverter> filterConvertersByName = new HashMap<>();

    private Map<String, RuleConverter> filterConvertersByOperator = new HashMap<>();

    private Map<String, RuleConverter> filterConvertersByType = new HashMap<>();

    private RuleConverter defaultRuleConverter;
    
    private EmmProfileFieldResolverFactory profileFieldResolverFactory;
    
    public QueryBuilderConfiguration() {
    	addIndependentField("Received mailing", "VARCHAR", "target.rule.mailingReceived");
		addIndependentField("Opened mailing", "VARCHAR", "target.rule.mailingOpened");
		addIndependentField("Clicked in mailing", "VARCHAR", "target.rule.mailingClicked");

		filterConvertersByName.put("received mailing", new ReceivedMailingRuleConverter());
		filterConvertersByName.put("opened mailing", new OpenedMailingRuleConverter());
		filterConvertersByName.put("clicked in mailing", new ClickedInMailingRuleConverter());

		filterConvertersByOperator.put("mod", new ModRuleConverter());
		filterConvertersByOperator.put("like", new LikeRuleConverter());
		filterConvertersByOperator.put("not_like", new NotLikeRuleConverter());
		filterConvertersByOperator.put("contains", new ContainsRuleConverter());
		filterConvertersByOperator.put("not_contains", new NotContainsRuleConverter());
		filterConvertersByOperator.put("begins_with", new StartsWithRuleConverter());
		filterConvertersByOperator.put("not_begins_with", new NotStartsWithRuleConverter());
		filterConvertersByOperator.put("is_empty", new EmptyRuleConverter());
		filterConvertersByOperator.put("is_not_empty", new NotEmptyRuleConverter());

		filterConvertersByType.put("date", new DateRuleConverter());

		defaultRuleConverter = new DefaultRuleConverter();
    }

    public List<ProfileField> getIndependentFields() {
        return independentFields;
    }

    public List<ProfileField> getIndependentFieldsExcluding(TargetRuleKey[] excludedRulesKeys) {
        final List<ProfileField> fields = getIndependentFields();

        if(ArrayUtils.isEmpty(excludedRulesKeys)) {
            return new ArrayList<>(fields);
        }

        Collection<String> excludedRulesNames = Stream.of(excludedRulesKeys).map(TargetRuleKey::getShortname).collect(Collectors.toList());
        return fields.stream().filter(pf -> !excludedRulesNames.contains(pf.getShortname())).collect(Collectors.toList());
    }

	protected void addIndependentField(String shortname, String dataType, String label) {
		ProfileFieldImpl field = new ProfileFieldImpl();
    	field.setShortname(shortname);
    	field.setDataType(dataType);
    	field.setLabel(label);
    	independentFields.add(field);
	}

    public Map<String, RuleConverter> getFilterConvertersByName() {
        return filterConvertersByName;
    }

    public RuleConverter getDefaultRuleConverter() {
        return defaultRuleConverter;
    }

    public Map<String, RuleConverter> getFilterConvertersByOperator() {
        return filterConvertersByOperator;
    }

    public Map<String, RuleConverter> getFilterConvertersByType() {
        return filterConvertersByType;
    }

	public void setDefaultRuleConverter(DefaultRuleConverter defaultRuleConverter) {
		this.defaultRuleConverter = defaultRuleConverter;
	}

    public void setProfileFieldResolverFactory(EmmProfileFieldResolverFactory profileFieldResolverFactory) {
        this.profileFieldResolverFactory = profileFieldResolverFactory;
        for (RuleConverter ruleConverter : filterConvertersByType.values()) {
        	if (ruleConverter instanceof GenericRuleConverter) {
        		((GenericRuleConverter) ruleConverter).setProfileFieldResolverFactory(this.profileFieldResolverFactory);
        	}
        }

    	if (defaultRuleConverter instanceof GenericRuleConverter) {
    		((GenericRuleConverter) defaultRuleConverter).setProfileFieldResolverFactory(this.profileFieldResolverFactory);
    	}
    }
}
