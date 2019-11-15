/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql;

import java.util.Objects;

import org.agnitas.target.TargetRepresentation;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerator;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorException;
import com.agnitas.emm.core.target.eql.codegen.beanshell.BeanShellCodeGeneratorCallback;
import com.agnitas.emm.core.target.eql.codegen.beanshell.BeanShellCodeGeneratorCallbackFactory;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ReferenceTableResolveException;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCodeGeneratorCallback;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCodeGeneratorCallbackFactory;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlDialect;
import com.agnitas.emm.core.target.eql.codegen.validate.MailingIdValidator;
import com.agnitas.emm.core.target.eql.emm.legacy.EqlToTargetRepresentationConversionException;
import com.agnitas.emm.core.target.eql.emm.legacy.EqlToTargetRepresentationConverter;
import com.agnitas.emm.core.target.eql.emm.legacy.EqlToTargetRepresentationConverterFactory;
import com.agnitas.emm.core.target.eql.emm.legacy.TargetRepresentationToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.legacy.TargetRepresentationToEqlConverter;
import com.agnitas.emm.core.target.eql.parser.EqlParser;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;
import com.agnitas.emm.core.target.eql.referencecollector.SimpleReferenceCollector;

/**
 * Facade encapsulating EQL conversion logic.
 */
public class EqlFacade {

	/** Parser for EQL code. */
	private final EqlParser eqlParser;
	
	/** Code generator. */
	private final CodeGenerator codeGenerator;

	/** Converter for {@link TargetRepresentation} to EQL code. */
	private TargetRepresentationToEqlConverter legacyToEqlConverter;
	
	/** Validator for mailing IDs. */
	private MailingIdValidator mailingIdValidator;
	
	/** SQL dialect used for code generation and conversion. */
	private SqlDialect sqlDialect;
	
	private final SqlCodeGeneratorCallbackFactory sqlCodeGeneratorCallbackFactory;
	private final BeanShellCodeGeneratorCallbackFactory beanShellCodeGeneratorCallbackFactory;
	
	private final EqlToTargetRepresentationConverterFactory eqlToTargetRepresentationConverterFactory;
	
	/**
	 * Creates a new EQL facade instance.
	 */
	public EqlFacade(final EqlParser eqlParser, final CodeGenerator codeGenerator, final SqlCodeGeneratorCallbackFactory sqlCodeGeneratorCallbackFactory, final BeanShellCodeGeneratorCallbackFactory beanShellCodeGeneratorCallbackFactory, final EqlToTargetRepresentationConverterFactory eqlToTargetRepresentationConverterFactory) {
		this.eqlParser = Objects.requireNonNull(eqlParser, "EqlParser is null");
		this.codeGenerator = Objects.requireNonNull(codeGenerator, "Code generator is null");
		this.sqlCodeGeneratorCallbackFactory = Objects.requireNonNull(sqlCodeGeneratorCallbackFactory, "SQL code generator callback factory is null");
		this.beanShellCodeGeneratorCallbackFactory = Objects.requireNonNull(beanShellCodeGeneratorCallbackFactory, "BeanShell code generator callback factory is null");
		this.eqlToTargetRepresentationConverterFactory = Objects.requireNonNull(eqlToTargetRepresentationConverterFactory, "EQL to TargetRepresentation converter factory is null");
	}
	
	/**
	 * Converts a {@link TargetRepresentation} instance to EQL code.
	 * 
	 * @param target target group to convert
	 * 
	 * @return EQL code from given target group
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	public String convertTargetRepresentationToEql(ComTarget target) throws TargetRepresentationToEqlConversionException {
		return convertTargetRepresentationToEql(target.getTargetStructure(), target.getCompanyID());
	}
	
	/**
	 * Converts a {@link TargetRepresentation} instance to EQL code. Follows three-valued logic.
	 * 
	 * @param representation {@link TargetRepresentation} to convert
	 * @param companyId company ID to use for conversion
	 * 
	 * @return EQL code from given {@link TargetRepresentation}
	 * 
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	public String convertTargetRepresentationToEql(TargetRepresentation representation, int companyId) throws TargetRepresentationToEqlConversionException {
		return this.legacyToEqlConverter.convertToEql(representation, companyId);
	}

	/**
	 * Converts a {@link TargetRepresentation} instance to EQL code.
	 *
	 * @param representation {@link TargetRepresentation} to convert
	 * @param companyId company ID to use for conversion
	 * @param disableThreeValuedLogic whether ({@code true}) or not ({@code false}) disable three-valued logic (generate
	 *                                an EQL having additional conditions in order to make sure that negated expression
	 *                                selects everything that direct expression doesn't)
	 *
	 * @return EQL code from given {@link TargetRepresentation}
	 *
	 * @throws TargetRepresentationToEqlConversionException on errors during conversion
	 */
	public String convertTargetRepresentationToEql(TargetRepresentation representation, int companyId, boolean disableThreeValuedLogic) throws TargetRepresentationToEqlConversionException {
		return this.legacyToEqlConverter.convertToEql(representation, companyId, disableThreeValuedLogic);
	}

	/**
	 * Converts EQL code to legacy {@link TargetRepresentation}. Since not every EQL code is convertible to {@link TargetRepresentation} this method can
	 * throw an {@link EqlToTargetRepresentationConversionException}. The company ID is required for resolving the types of mailings or profile fields.
	 * 
	 * @param eql EQL code to convert
	 * @param companyId company ID
	 * 
	 * @return {@link TargetRepresentation} generated from given EQL
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws EqlToTargetRepresentationConversionException on errors converting EQL
	 */
	public final TargetRepresentation convertEqlToTargetRepresentation(final String eql, final int companyId) throws EqlParserException, EqlToTargetRepresentationConversionException {
		final BooleanExpressionTargetRuleEqlNode eqlNode = this.eqlParser.parseEql(eql);
		
		
		try {
			final EqlToTargetRepresentationConverter converter = this.eqlToTargetRepresentationConverterFactory.newConverter(companyId);
			
			return converter.convertToTargetRepresentation(eqlNode);
		} catch(final EqlToTargetRepresentationConversionException e) {
			throw e;
		} catch(final ProfileFieldResolveException e) {
			throw new EqlToTargetRepresentationConversionException("Error converting EQL to TargetRepresentation", e);
		}
	}
	
	/**
	 * Converts the given EQL code to SQL.
	 * 
	 * @param eql EQL code to convert
	 * @param companyId company ID
	 * 
	 * @return SQL code
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws CodeGeneratorException on errors generating SQL code
	 * @throws ReferenceTableResolveException on errors resolving reference tables
	 * @throws ProfileFieldResolveException on errors resolving profile fields
	 */
	public SqlCode convertEqlToSql(String eql, int companyId) throws EqlParserException, CodeGeneratorException, ReferenceTableResolveException, ProfileFieldResolveException {
		SimpleReferenceCollector referencedItems = new SimpleReferenceCollector();
		
		return convertEqlToSql(eql, companyId, referencedItems);
	}
	
	/**
	 * Converts the given EQL code to SQL.
	 * 
	 * @param eql EQL code to convert
	 * @param companyId company ID
	 * @param referenceCollector collector for referenced items
	 * 
	 * @return SQL code
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws CodeGeneratorException on errors generating SQL code
	 * @throws ReferenceTableResolveException on errors resolving reference tables
	 * @throws ProfileFieldResolveException on errors resolving profile fields
	 */
	public final SqlCode convertEqlToSql(final String eql, final int companyId, final ReferenceCollector referenceCollector) throws EqlParserException, CodeGeneratorException, ReferenceTableResolveException, ProfileFieldResolveException {
		final BooleanExpressionTargetRuleEqlNode node = this.eqlParser.parseEql(eql);
		
		node.collectReferencedItems(referenceCollector);
		
		final SqlCodeGeneratorCallback callback = this.sqlCodeGeneratorCallbackFactory.newCodeGeneratorCallback(companyId);
		this.codeGenerator.generateCode(node, callback);

		return callback.getSqlCode();
	}
	
	/**
	 * Converts the given EQL code to BeanShell.
	 * 
	 * @param target target group 
	 * 
	 * @return BeanShell code
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws CodeGeneratorException on errors generating BeanShell code
	 * @throws ProfileFieldResolveException on errors resolving profile fields
	 */
	public final String convertEqlToBeanShellExpression(final ComTarget target) throws EqlParserException, CodeGeneratorException, ProfileFieldResolveException {
		final BooleanExpressionTargetRuleEqlNode node = this.eqlParser.parseEql(target.getEQL());
		
		final BeanShellCodeGeneratorCallback callback = this.beanShellCodeGeneratorCallbackFactory.newCodeGeneratorCallback(target.getCompanyID());
		this.codeGenerator.generateCode(node, callback);

		return callback.getBeanShellCode();
	}
	
	// -------------------------------------------------------------------------------------------------------------------------------------- Dependency Injection
	/**
	 * Sets the {@link TargetRepresentation}-to-EQL-converter.
	 * 
	 * @param converter converter from {@link TargetRepresentation} to EQL
	 */
	@Required
	public void setTargetRepresentationToEqlConverter(TargetRepresentationToEqlConverter converter) {
		this.legacyToEqlConverter = converter;
	}
	
	/**
	 * Sets the validator for mailing IDs.
	 * 
	 * @param validator validator for mailing IDs.
	 */
	@Required
	public void setMailingIdValidator(MailingIdValidator validator) {
		this.mailingIdValidator = validator;
	}

	/**
	 * Sets the SQL dialect used for code generation and conversion.
	 * 
	 * @param dialect SQL dialect
	 */
	@Required
	public void setSqlDialect(SqlDialect dialect) {
		this.sqlDialect = dialect;
	}

}
