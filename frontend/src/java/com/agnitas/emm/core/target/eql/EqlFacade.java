/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.target.eql.ast.BooleanExpressionTargetRuleEqlNode;
import com.agnitas.emm.core.target.eql.ast.analysis.RequireMailtrackingSyntaxTreeAnalyzer;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerationFlags;
import com.agnitas.emm.core.target.eql.codegen.CodeGenerator;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorException;
import com.agnitas.emm.core.target.eql.codegen.beanshell.BeanShellCodeGeneratorCallback;
import com.agnitas.emm.core.target.eql.codegen.beanshell.BeanShellCodeGeneratorCallbackFactory;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ReferenceTableResolveException;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCodeGeneratorCallback;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCodeGeneratorCallbackFactory;
import com.agnitas.emm.core.target.eql.parser.EqlParser;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxErrorException;
import com.agnitas.emm.core.target.eql.referencecollector.ReferenceCollector;
import com.agnitas.emm.core.target.eql.referencecollector.SimpleReferenceCollector;

/**
 * Facade encapsulating EQL conversion logic.
 */
public class EqlFacade {

	private static final transient Logger LOGGER = Logger.getLogger(EqlFacade.class);

	/** Parser for EQL code. */
	private final EqlParser eqlParser;
	
	/** Code generator. */
	private final CodeGenerator codeGenerator;

	private final SqlCodeGeneratorCallbackFactory sqlCodeGeneratorCallbackFactory;
	
	@Deprecated
	private final BeanShellCodeGeneratorCallbackFactory beanShellCodeGeneratorCallbackFactory;
		
	/**
	 * Creates a new EQL facade instance.
	 */
	public EqlFacade(final EqlParser eqlParser, final CodeGenerator codeGenerator, final SqlCodeGeneratorCallbackFactory sqlCodeGeneratorCallbackFactory, final BeanShellCodeGeneratorCallbackFactory beanShellCodeGeneratorCallbackFactory) {
		this.eqlParser = Objects.requireNonNull(eqlParser, "EqlParser is null");
		this.codeGenerator = Objects.requireNonNull(codeGenerator, "Code generator is null");
		this.sqlCodeGeneratorCallbackFactory = Objects.requireNonNull(sqlCodeGeneratorCallbackFactory, "SQL code generator callback factory is null");
		this.beanShellCodeGeneratorCallbackFactory = Objects.requireNonNull(beanShellCodeGeneratorCallbackFactory, "BeanShell code generator callback factory is null");
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
	public SqlCode convertEqlToSql(final String eql, final int companyId) throws EqlParserException, CodeGeneratorException, ReferenceTableResolveException, ProfileFieldResolveException {
		return convertEqlToSql(eql, companyId, CodeGenerationFlags.DEFAULT_FLAGS);
	}
	
	/**
	 * Converts the given EQL code to SQL. Given flags controls behavior.
	 * 
	 * @param eql EQL code to convert
	 * @param companyId company ID
	 * @param flags flags to alter code generation behavior
	 * 
	 * @return SQL code
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws CodeGeneratorException on errors generating SQL code
	 * @throws ReferenceTableResolveException on errors resolving reference tables
	 * @throws ProfileFieldResolveException on errors resolving profile fields
	 */
	public SqlCode convertEqlToSql(final String eql, final int companyId, final CodeGenerationFlags flags) throws EqlParserException, CodeGeneratorException, ReferenceTableResolveException, ProfileFieldResolveException {
		SimpleReferenceCollector referencedItems = new SimpleReferenceCollector();
		
		return convertEqlToSql(eql, companyId, referencedItems, flags);
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
		return convertEqlToSql(eql, companyId, referenceCollector, CodeGenerationFlags.DEFAULT_FLAGS);
	}
		
	/**
	 * Converts the given EQL code to SQL.
	 * 
	 * @param eql EQL code to convert
	 * @param companyId company ID
	 * @param referenceCollector collector for referenced items
	 * @param flags flags to alter code generation behavior
	 * 
	 * @return SQL code
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws CodeGeneratorException on errors generating SQL code
	 * @throws ReferenceTableResolveException on errors resolving reference tables
	 * @throws ProfileFieldResolveException on errors resolving profile fields
	 */
	public final SqlCode convertEqlToSql(final String eql, final int companyId, final ReferenceCollector referenceCollector, final CodeGenerationFlags flags) throws EqlParserException, CodeGeneratorException, ReferenceTableResolveException, ProfileFieldResolveException {
		final BooleanExpressionTargetRuleEqlNode node = this.eqlParser.parseEql(eql);
		
		node.collectReferencedItems(referenceCollector);
		
		final SqlCodeGeneratorCallback callback = this.sqlCodeGeneratorCallbackFactory.newCodeGeneratorCallback(companyId);
		this.codeGenerator.generateCode(node, callback, flags);

		return callback.getSqlCode();
	}
	
	/**
	 * Converts the given EQL code to BeanShell.
	 * 
	 * @param eql EQL code
	 * @param companyId company ID
	 * 
	 * @return BeanShell code
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws CodeGeneratorException on errors generating BeanShell code
	 * @throws ProfileFieldResolveException on errors resolving profile fields
	 */
	@Deprecated // No replacement
	public final String convertEqlToBeanShellExpression(final String eql, final int companyId) throws EqlParserException, CodeGeneratorException, ProfileFieldResolveException {
		final BooleanExpressionTargetRuleEqlNode node = this.eqlParser.parseEql(eql);
		
		final BeanShellCodeGeneratorCallback callback = this.beanShellCodeGeneratorCallbackFactory.newCodeGeneratorCallback(companyId);
		this.codeGenerator.generateCode(node, callback);

		return callback.getBeanShellCode();
		
	}
	
	/**
	 * Converts EQL code of given target group to BeanShell.
	 * 
	 * @param target target group
	 * 
	 * @return BeanShell code
	 * 
	 * @throws EqlParserException on errors parsing the EQL code
	 * @throws CodeGeneratorException on errors generating BeanShell code
	 * @throws ProfileFieldResolveException on errors resolving profile fields
	 */
	@Deprecated // No replacement
	public final String convertEqlToBeanShellExpression(final ComTarget target) throws EqlParserException, CodeGeneratorException, ProfileFieldResolveException {
		return convertEqlToBeanShellExpression(target.getEQL(), target.getCompanyID());
	}
	

	public EqlAnalysisResult analyseEql(final String eql) throws EqlParserException {
		final BooleanExpressionTargetRuleEqlNode node = this.eqlParser.parseEql(eql);
		
		final RequireMailtrackingSyntaxTreeAnalyzer mailtrackingAnalyzer = new RequireMailtrackingSyntaxTreeAnalyzer();
		node.traverse(mailtrackingAnalyzer);
		
		return new EqlAnalysisResult(mailtrackingAnalyzer.isMailtrackingRequired());
	}

	public EqlDetailedAnalysisResult analyseEqlSafely(final String eql) {
		try {
			final BooleanExpressionTargetRuleEqlNode node = this.eqlParser.parseEql(eql);

			final RequireMailtrackingSyntaxTreeAnalyzer mailtrackingAnalyzer = new RequireMailtrackingSyntaxTreeAnalyzer();
			node.traverse(mailtrackingAnalyzer);

			return new EqlDetailedAnalysisResult(new EqlAnalysisResult(mailtrackingAnalyzer.isMailtrackingRequired()), Collections.emptyList());
		} catch (final EqlSyntaxErrorException e) {
			List<EqlSyntaxError> syntaxErrors = e.getErrors();

			return new EqlDetailedAnalysisResult(null, syntaxErrors);
		} catch (EqlParserException e) {
			LOGGER.error("Error in EQL analysis.", e);
			return new EqlDetailedAnalysisResult(null, Collections.emptyList());
		}
	}

}
