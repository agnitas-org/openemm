/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.sql;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import org.agnitas.util.DbUtilities;

import com.agnitas.emm.core.target.eql.ast.AbstractAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AbstractRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.AtomExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ClickedInMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ConstantListEqlNode;
import com.agnitas.emm.core.target.eql.ast.ContainsRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.EmptyRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.InRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.LikeRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NegExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.NotOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.NumericConstantAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.OpenedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.ProfileFieldAtomEqlNode;
import com.agnitas.emm.core.target.eql.ast.ReceivedMailingRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.StartsWithRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.StringConstantWithEscapeCharsAtomEqlNode;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorException;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorImplementationException;
import com.agnitas.emm.core.target.eql.codegen.CodeLocation;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;
import com.agnitas.emm.core.target.eql.codegen.InvalidTypeException;
import com.agnitas.emm.core.target.eql.codegen.UnhandledDataTypeException;
import com.agnitas.emm.core.target.eql.codegen.UnhandledOperatorException;
import com.agnitas.emm.core.target.eql.codegen.UnknownLinkIdFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownMailingIdFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownProfileFieldFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownReferenceTableColumnFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnknownReferenceTableFaultyCodeException;
import com.agnitas.emm.core.target.eql.codegen.UnsupportedOperatorForDataTypeException;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingResolverException;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldNameResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ReferenceTableResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.UnknownReferenceTableColumnException;
import com.agnitas.emm.core.target.eql.codegen.resolver.UnknownReferenceTableException;
import com.agnitas.emm.core.target.eql.codegen.util.StringUtil;
import com.agnitas.emm.core.target.eql.codegen.validate.LinkIdValidationException;
import com.agnitas.emm.core.target.eql.codegen.validate.LinkIdValidator;
import com.agnitas.emm.core.target.eql.codegen.validate.MailingIdValidationException;
import com.agnitas.emm.core.target.eql.codegen.validate.MailingIdValidator;

/**
 * Code generator callback for generation of SQL code.
 */
public class DefaultCommonSqlCodeGeneratorCallback implements SqlCodeGeneratorCallback {

	/** Stack holding the code fragments generated for syntax nodes. */
	protected final Stack<CodeFragment> codeStack;

	/** Resolver for profile field names to corresponding types. */
	private final ProfileFieldTypeResolver profileFieldTypeResolver;

	/** Resolver for profile field names to corresponding database names. */
	private final ProfileFieldNameResolver profileFieldNameResolver;

	/** Resolver for mailing IDs to mailing types. */
	private final MailingTypeResolver mailingTypeResolver;

	/** Checks mailing IDs for validity. */
	protected final MailingIdValidator mailingIdValidator;

	/** Checks link IDs for validity. */
	private final LinkIdValidator linkIdValidator;

	/** SQL dialect used to generate dialect-specific code. */
	private final SqlDialect sqlDialect;

	/** Properties of SQL code. */
	protected final DefaultSqlCodeProperties codeProperties;

	/** Company ID to generate code for. */
	protected final int companyId;

	/** Name of the reference table specified in REFERENCES ... HAVING ... */
	protected String referenceTableNameForReferencesHaving;

	/**
	 * Creates a new callback instance for generating SQL code.
	 * 
	 * @param companyId
	 *            ID of company to generate code for
	 * @param profileFieldNameResolver
	 *            resolver for profile field names
	 * @param profileFieldTypeResolver
	 *            resolver for profile field types
	 * @param mailingIdValidator
	 *            validation for mailing IDs
	 * @param mailingTypeResolver
	 *            resolver for mailing types
	 * @param linkIdValidator
	 *            validation for link IDs
	 * @param sqlDialect
	 *            SQL-dialect depending code generator
	 */
	public DefaultCommonSqlCodeGeneratorCallback(final int companyId,
			final ProfileFieldNameResolver profileFieldNameResolver,
			final ProfileFieldTypeResolver profileFieldTypeResolver,
			final MailingIdValidator mailingIdValidator, 
			final MailingTypeResolver mailingTypeResolver,
			final LinkIdValidator linkIdValidator, 
			final SqlDialect sqlDialect) {
		this.codeStack = new Stack<>();

		this.companyId = companyId;
		this.profileFieldTypeResolver = profileFieldTypeResolver;
		this.profileFieldNameResolver = profileFieldNameResolver;
		this.mailingIdValidator = mailingIdValidator;
		this.mailingTypeResolver = mailingTypeResolver;
		this.linkIdValidator = linkIdValidator;
		this.sqlDialect = sqlDialect;
		this.codeProperties = new DefaultSqlCodeProperties();
	}

	@Override
	public void error(CodeGeneratorException e) {
		// Clear code stack in case of an error
		this.codeStack.clear();
	}

	@Override
	public final void finishedWithEmptyTargetRule() {
		assert codeStack.isEmpty();

		// Create some dummy condition
		codeStack.push(new CodeFragment("1 = 1", DataType.BOOLEAN, null));
	}

	/**
	 * Checks, if code fragment evaluates to one of the listed types. If not, an
	 * InvalidTypeException is thrown.
	 * 
	 * @param location
	 *            code location
	 * @param fragment
	 *            code fragment to verify
	 * @param expectedTypes
	 *            list of expected types
	 * 
	 * @throws InvalidTypeException
	 *             if code fragment does not evaluate to one of the listed types
	 */
	public void checkType(CodeLocation location, CodeFragment fragment, DataType... expectedTypes) throws InvalidTypeException {
		DataType actualType = fragment.evaluatesToType();

		for (DataType expectedType : expectedTypes) {
			if (actualType == expectedType) {
				return;
			}
		}

		throw new InvalidTypeException(location, actualType, expectedTypes);
	}

	@Override
	public SqlCode getSqlCode() throws CodeGeneratorException {
		if (codeStack.isEmpty()) {
			throw new CodeGeneratorImplementationException("No SQL code generated.");
		}

		// When code is generated successfully, only one elements is on stack
		assert (codeStack.size() == 1);

		// Peek, not pop, elements from stack. Otherwise, the a subsequent call to getCode() will fail.
		CodeFragment code = codeStack.peek();

		// Only assert(), because callback methods with type checks guarantees BOOLEAN type here
		assert code.evaluatesToType(DataType.BOOLEAN);

		// There must be no reference tables that are not used for code generation
		assert (code.getUnusedReferenceTables().size() == 0);

		return new SqlCode(code.getCode(), this.codeProperties);
	}

	@Override
	public void postOrderNotOperatorBooleanEqlNode(NotOperatorBooleanEqlNode node) throws CodeGeneratorException {
		assert (codeStack.size() >= 1);

		CodeFragment code = codeStack.pop();
		checkType(node.getStartLocation(), code, DataType.BOOLEAN);
		assert (code.getUnusedReferenceTables().size() == 0);

		CodeFragment newCode = new CodeFragment("(NOT " + code.getCode() + ")", DataType.BOOLEAN, null);

		codeStack.push(newCode);
	}

	@Override
	public void postOrderBinaryOperatorBooleanEqlNode(BinaryOperatorBooleanEqlNode node) throws CodeGeneratorException {
		assert (codeStack.size() >= 2);

		CodeFragment right = codeStack.pop();
		checkType(node.getStartLocation(), right, DataType.BOOLEAN);

		CodeFragment left = codeStack.pop();
		checkType(node.getStartLocation(), left, DataType.BOOLEAN);

		assert (left.getUnusedReferenceTables().size() == 0);
		assert (right.getUnusedReferenceTables().size() == 0);

		switch (node.getOperator()) {
		case AND:
			codeStack.push(new CodeFragment("(" + left.getCode() + " AND " + right.getCode() + ")", DataType.BOOLEAN, null));
			break;
		case OR:
			codeStack.push(new CodeFragment("(" + left.getCode() + " OR " + right.getCode() + ")", DataType.BOOLEAN, null));
			break;

		default:
			throw new UnhandledOperatorException(node.getOperator());
		}
	}

	@Override
	public void postOrderBinaryOperatorRelationalEqlNode(BinaryOperatorRelationalEqlNode node, EqlDateFormat dateFormat) throws CodeGeneratorException {
		assert (codeStack.size() >= 2);

		final CodeFragment right = codeStack.pop();
		final CodeFragment left = codeStack.pop();

		switch (left.evaluatesToType()) {
		case NUMERIC:
			binaryRelationalOperationOnNumeric(node, left, right);
			break;
			
		case TEXT:
			binaryRelationalOperationOnText(node, left, right);
			break;

		case DATE:
			binaryRelationalOperationOnDate(node, left, right, dateFormat);
			break;

		default:
			throw new UnhandledDataTypeException(left, left.evaluatesToType());
		}

	}
	
	private void binaryRelationalOperationOnNumeric(final BinaryOperatorRelationalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		checkType(node.getStartLocation(), left, DataType.NUMERIC);
		checkType(node.getStartLocation(), right, DataType.NUMERIC);
		
		// Both operands must be of same type
		checkType(node.getStartLocation(), right, left.evaluatesToType());

		final Function<String, String> identity = Function.identity();
		
		binaryRelationalOperationWithOperandTransformation(node, left, identity, right, identity);
	}
	
	private void binaryRelationalOperationOnText(final BinaryOperatorRelationalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		checkType(node.getStartLocation(), left, DataType.TEXT);
		checkType(node.getStartLocation(), right, DataType.TEXT);

		// Both operands must be of same type
		checkType(node.getStartLocation(), right, left.evaluatesToType());

		final Function<String, String> toLower = x -> String.format("lower(%s)", x);
		
		binaryRelationalOperationWithOperandTransformation(node, left, toLower, right, toLower);
	}
	
	private void binaryRelationalOperationOnDate(final BinaryOperatorRelationalEqlNode node, final CodeFragment left, final CodeFragment right, final EqlDateFormat dateFormat) throws CodeGeneratorException {
		checkType(node.getStartLocation(), left, DataType.DATE, DataType.TEXT);
		checkType(node.getStartLocation(), right, DataType.DATE, DataType.TEXT);
		
		// Date comparison required DATEFORMAT specifier
		if(dateFormat == null) {
			throw new MissingDateFormatException();
		}
		
		final Function<String, String> leftFn = left.evaluatesToType(DataType.DATE)
				? x -> this.sqlDialect.dateToString(x, dateFormat)
				: Function.identity();
				
		final Function<String, String> rightFn = right.evaluatesToType(DataType.DATE)
				? x -> this.sqlDialect.dateToString(x, dateFormat)
				: Function.identity();
		
		binaryRelationalOperationWithOperandTransformation(node, left, leftFn, right, rightFn);
	}
	
	private void binaryRelationalOperationWithOperandTransformation(final BinaryOperatorRelationalEqlNode node, final CodeFragment left, final Function<String, String> transformLeft, final CodeFragment right, final Function<String, String> transformRight) throws CodeGeneratorException {
		// Use identity function if transformation is null
		final Function<String, String> fnLeft = transformLeft != null ? transformLeft : Function.identity();
		final Function<String, String> fnRight = transformRight != null ? transformRight : Function.identity();
		
		
		final String operatorCode = OperatorUtil.eqlOperatorToSqlOperator(node.getOperator());

		final String relationalCode = String.format("(%s %s %s)", fnLeft.apply(left.getCode()), operatorCode, fnRight.apply(right.getCode()));
		
		final Set<String> set = new HashSet<>(left.getUnusedReferenceTables());
		set.addAll(right.getUnusedReferenceTables());
		
		if(set.isEmpty()) {
			codeStack.push(new CodeFragment(relationalCode, DataType.BOOLEAN, null));
		} else {
			try {
				// Reference table access using "tab.col" is not allowed inside a REFERENCES-HAVING-statement
				assert (referenceTableNameForReferencesHaving == null);
				
				codeStack.push(new CodeFragment(makeReferenceTableFrame(relationalCode, set), DataType.BOOLEAN, null));
			} catch (UnknownReferenceTableException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e.getTableName(), e);
			} catch (UnknownReferenceTableColumnException e) {
				throw new UnknownReferenceTableColumnFaultyCodeException(node.getStartLocation(), e.getTableName(), e.getColumnName(), e);
			} catch (ReferenceTableResolveException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e);
			}
		}
	}

	@Override
	public void postOrderEmptyRelationalEqlNode(EmptyRelationalEqlNode node) throws CodeGeneratorException {
		assert (codeStack.size() >= 1);

		CodeFragment child = codeStack.pop();
		// TODO: Which datatype is expected here??? Or can we accept any
		// datatype?

		StringBuffer buffer = new StringBuffer();

		if(node.getNotFlag()) {
			buffer.append("(NOT ");
		}

		buffer.append("(");
		buffer.append(this.sqlDialect.isEmpty(child));
		buffer.append(")");
		
		if(node.getNotFlag()) {
			buffer.append(")");
		}

		if (child.getUnusedReferenceTables().size() == 0) {
			codeStack.push(new CodeFragment(buffer.toString(), DataType.BOOLEAN, null));
		} else {
			try {
				// Reference table access using "tab.col" is  not allowed inside a REFERENCES-HAVING-statement
				assert (referenceTableNameForReferencesHaving == null); 
				
				codeStack.push(new CodeFragment(makeReferenceTableFrame(buffer.toString(), child.getUnusedReferenceTables()), DataType.BOOLEAN, null));
			} catch (UnknownReferenceTableException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e.getTableName(), e);
			} catch (UnknownReferenceTableColumnException e) {
				throw new UnknownReferenceTableColumnFaultyCodeException(node.getStartLocation(), e.getTableName(), e.getColumnName(), e);
			} catch (ReferenceTableResolveException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e);
			}
		}
	}

	@Override
	public void postOrderLikeRelationalEqlNode(LikeRelationalEqlNode node, StringConstantWithEscapeCharsAtomEqlNode pattern) throws CodeGeneratorException {
		// Use "!" to escape SQL wildcard symbols
		final char SQL_ESCAPE_CHAR = '!';

		String string = StringUtil.replaceEscapedChars(pattern.getValue());

		// We do case-insensitive comparison by converting every string to lower
		// case, so we need to do that with the matching pattern too.
		String likePattern = StringUtil.convertEqlToSqlLikePattern(string, SQL_ESCAPE_CHAR).toLowerCase();

		CodeFragment left = codeStack.pop();
		Set<String> set = left.getUnusedReferenceTables();
		// TODO: Which datatype is expected here??? Or can we accept any datatype?

		StringBuffer buffer = new StringBuffer();
		buffer.append("(lower(");
		buffer.append(left.getCode());
		buffer.append(")");

		if (node.getNotFlag()) {
			buffer.append(" NOT");
		}

		buffer.append(" LIKE '");
		buffer.append(likePattern.replace("'", "''"));
		buffer.append("' ESCAPE '");
		buffer.append(SQL_ESCAPE_CHAR);
		buffer.append("')");

		if (set.size() == 0) {
			codeStack.push(new CodeFragment(buffer.toString(), DataType.BOOLEAN, null));
		} else {
			try {
				// Reference table access using "tab.col" is  not allowed inside a REFERENCES-HAVING-statement
				assert (referenceTableNameForReferencesHaving == null);
				
				codeStack.push(new CodeFragment(makeReferenceTableFrame(buffer.toString(), set), DataType.BOOLEAN, null));
			} catch (UnknownReferenceTableException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e.getTableName(), e);
			} catch (UnknownReferenceTableColumnException e) {
				throw new UnknownReferenceTableColumnFaultyCodeException(node.getStartLocation(), e.getTableName(), e.getColumnName(), e);
			} catch (ReferenceTableResolveException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e);
			}
		}
	}

	@Override
	public void postOrderContainsRelationalEqlNode(ContainsRelationalEqlNode node, StringConstantWithEscapeCharsAtomEqlNode stringNode) throws CodeGeneratorException {
		// Use "!" to escape SQL wildcard symbols
		final char SQL_ESCAPE_CHAR = '!';

		String string = StringUtil.replaceEscapedChars(stringNode.getValue());

		// We do case-insensitive comparison by converting every string to lower
		// case, so we need to do that with the matching pattern too.
		String pattern = "%" + StringUtil.convertEqlToSqlString(string, SQL_ESCAPE_CHAR).toLowerCase() + "%";

		CodeFragment left = codeStack.pop();
		Set<String> set = left.getUnusedReferenceTables();

		StringBuilder builder = new StringBuilder();
		builder.append("(LOWER(");
		builder.append(left.getCode());
		builder.append(")");

		if (node.getNotFlag()) {
			builder.append(" NOT");
		}

		builder.append(" LIKE '");
		builder.append(pattern.replace("'", "''"));
		builder.append("' ESCAPE '");
		builder.append(SQL_ESCAPE_CHAR);
		builder.append("')");

		if (set.size() == 0) {
			codeStack.push(new CodeFragment(builder.toString(), DataType.BOOLEAN, null));
		} else {
			try {
				// Reference table access using "tab.col" is not allowed inside a REFERENCES-HAVING-statement
				assert (referenceTableNameForReferencesHaving == null);
				
				codeStack.push(new CodeFragment(makeReferenceTableFrame(builder.toString(), set), DataType.BOOLEAN, null));
			} catch (UnknownReferenceTableException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e.getTableName(), e);
			} catch (UnknownReferenceTableColumnException e) {
				throw new UnknownReferenceTableColumnFaultyCodeException(node.getStartLocation(), e.getTableName(), e.getColumnName(), e);
			} catch (ReferenceTableResolveException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e);
			}
		}
	}

	@Override
	public void postOrderStartsWithRelationalEqlNode(StartsWithRelationalEqlNode node, StringConstantWithEscapeCharsAtomEqlNode stringNode) throws CodeGeneratorException {
		// Use "!" to escape SQL wildcard symbols
		final char SQL_ESCAPE_CHAR = '!';

		String string = StringUtil.replaceEscapedChars(stringNode.getValue());

		// We do case-insensitive comparison by converting every string to lower
		// case, so we need to do that with the matching pattern too.
		String pattern = StringUtil.convertEqlToSqlString(string, SQL_ESCAPE_CHAR).toLowerCase() + "%";

		CodeFragment left = codeStack.pop();
		Set<String> set = left.getUnusedReferenceTables();

		StringBuilder builder = new StringBuilder();
		builder.append("(LOWER(");
		builder.append(left.getCode());
		builder.append(")");

		if (node.getNotFlag()) {
			builder.append(" NOT");
		}

		builder.append(" LIKE '");
		builder.append(pattern.replace("'", "''"));
		builder.append("' ESCAPE '");
		builder.append(SQL_ESCAPE_CHAR);
		builder.append("')");

		if (set.size() == 0) {
			codeStack.push(new CodeFragment(builder.toString(), DataType.BOOLEAN, null));
		} else {
			try {
				// Reference table access using "tab.col" is not allowed inside a REFERENCES-HAVING-statement
				assert (referenceTableNameForReferencesHaving == null);

				codeStack.push(new CodeFragment(makeReferenceTableFrame(builder.toString(), set), DataType.BOOLEAN, null));
			} catch (UnknownReferenceTableException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e.getTableName(), e);
			} catch (UnknownReferenceTableColumnException e) {
				throw new UnknownReferenceTableColumnFaultyCodeException(node.getStartLocation(), e.getTableName(), e.getColumnName(), e);
			} catch (ReferenceTableResolveException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e);
			}
		}
	}

	@Override
	public void postOrderInRelationalEqlNode(InRelationalEqlNode node) throws CodeGeneratorException {
		assert (codeStack.size() >= 2);

		CodeFragment right = codeStack.pop();
		CodeFragment left = codeStack.pop();

		Set<String> set = new HashSet<>(left.getUnusedReferenceTables());
		set.addAll(right.getUnusedReferenceTables());

		// Check, that type of list elements matches type of expression of list
		// side
		checkType(node.getStartLocation(), right, left.evaluatesToType());

		StringBuffer buffer = new StringBuffer();

		buffer.append(left.getCode());

		if (node.getNotFlag()) {
			buffer.append(" NOT");
		}

		buffer.append(" IN ");
		buffer.append(right.getCode());

		if (set.size() == 0) {
			// No reference tables in sub trees, so use code unmodified
			codeStack.push(new CodeFragment(buffer.toString(), DataType.BOOLEAN, null));
		} else {
			// At least one of the sub tree contains a reference table
			// expression, embed code in reference table code frame
			try {
				// Reference table access using "tab.col" is not allowed inside a REFERENCES-HAVING-statement
				assert (referenceTableNameForReferencesHaving == null);
				
				codeStack.push(new CodeFragment(makeReferenceTableFrame(buffer.toString(), set), DataType.BOOLEAN, null));
			} catch (UnknownReferenceTableException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e.getTableName(), e);
			} catch (UnknownReferenceTableColumnException e) {
				throw new UnknownReferenceTableColumnFaultyCodeException(node.getStartLocation(), e.getTableName(), e.getColumnName(), e);
			} catch (ReferenceTableResolveException e) {
				throw new UnknownReferenceTableFaultyCodeException(node.getStartLocation(), e);
			}
		}
	}

	@Override
	public void postOrderOpenedMailingRelationalEqlNode(final OpenedMailingRelationalEqlNode node) throws CodeGeneratorException {
		final CodeFragment deviceQueryFragment = node.hasDeviceQuery() ? codeStack.pop() : null;

		final String tableName = node.hasDeviceQuery() 
				? String.format("onepixellog_device_%d_tbl", companyId)
				: String.format("onepixellog_%d_tbl", companyId);

		try {
			this.mailingIdValidator.validateMailingId(node.getMailingId(), this.companyId);

			final StringBuffer buffer = new StringBuffer();

			buffer.append("EXISTS (SELECT 1 FROM ");
			buffer.append(tableName).append(" t");

			// If OPENED MAILING is refined by a device query, add additional tables
			if (node.hasDeviceQuery()) {
				buffer.append(buildDeviceQueryTableList());
			}

			buffer.append(" WHERE t.customer_id=cust.customer_id AND t.mailing_id=");
			buffer.append(node.getMailingId());
			buffer.append(" AND t.company_id=");
			buffer.append(companyId);

			if (node.hasDeviceQuery()) {
				assert deviceQueryFragment != null;
				buffer.append(" AND (").append(deviceQueryFragment.getCode()).append(")");
			}

			buffer.append(")");

			codeStack.push(new CodeFragment(buffer.toString(), DataType.BOOLEAN, null));

			// Update properties of generated SQL code
			this.codeProperties.encounteredSubselect();
			this.codeProperties.encounteredNonCustomerTable();
		} catch (final MailingIdValidationException e) {
			throw new UnknownMailingIdFaultyCodeException(node.getMailingId(), node.getStartLocation(), e);
		}
	}

	@Override
	public void postOrderReceivedMailingRelationalEqlNode(ReceivedMailingRelationalEqlNode node) throws CodeGeneratorException {
		int mailingId = node.getMailingId();

		try {
			this.mailingIdValidator.validateMailingId(mailingId, this.companyId);

			MailingType mailingType = this.mailingTypeResolver.resolveMailingType(mailingId, companyId);

			StringBuffer buffer = new StringBuffer();

			if (mailingType == MailingType.INTERVAL) {
				createCodeReceivedMailingForIntervalMailing(mailingId, buffer);
			} else {
				createCodeReceivedMailingForNonIntervalMailing(mailingId, buffer);
			}

			codeStack.push(new CodeFragment(buffer.toString(), DataType.BOOLEAN, null));
		} catch (MailingIdValidationException e) {
			throw new UnknownMailingIdFaultyCodeException(mailingId, node.getStartLocation(), e);
		} catch (MailingResolverException e) {
			throw new UnknownMailingIdFaultyCodeException(mailingId, node.getStartLocation(), e);
		}
	}

	/**
	 * Generated SQL code to check, if a recipient received an interval mailing.
	 * 
	 * @param mailingId
	 *            mailing ID
	 * @param buffer
	 *            buffer for writing code
	 */
	private void createCodeReceivedMailingForIntervalMailing(final int mailingId, final StringBuffer buffer) {
		buffer.append("EXISTS (SELECT 1 FROM interval_track_");
		buffer.append(companyId);
		buffer.append("_tbl track WHERE track.mailing_id = ");
		buffer.append(mailingId);
		buffer.append(" AND track.customer_id = cust.customer_id)");

		// Update properties of generated SQL code
		this.codeProperties.encounteredSubselect();
		this.codeProperties.encounteredNonCustomerTable();
	}

	/**
	 * Generated SQL code to check, if a recipient received a non-interval mailing.
	 * 
	 * @param mailingId
	 *            mailing ID
	 * 
	 * @param buffer
	 *            buffer for writing code
	 */
	private void createCodeReceivedMailingForNonIntervalMailing(final int mailingId, final StringBuffer buffer) {
		// Mailing in test mode is not represented in success_XXX_tbl
		// So we check how mailing is sent, then we look in the mailtrack table if
		// mailing is in 'test' or 'admin' mode,
		// and we look in success table if mailing is in 'world' mode.

		buffer.append("(" + "((SELECT status_field " + "FROM maildrop_status_tbl "
				+ "WHERE status_id = (SELECT MAX(status_id) " + "FROM maildrop_status_tbl " + "WHERE mailing_id = "
				+ mailingId + ")) IN ('T', 'A') " + "AND EXISTS(SELECT 1 " + "FROM mailtrack_" + companyId
				+ "_tbl mt, maildrop_status_tbl mds " + "WHERE mds.company_id =  " + companyId
				+ " AND mds.mailing_id = " + mailingId
				+ " AND mds.status_id = mt.maildrop_status_id AND mt.customer_id = cust.customer_id)) "
				+ "OR EXISTS(SELECT 1 " + "FROM success_" + companyId + "_tbl succ " + "WHERE succ.mailing_id = "
				+ mailingId + " AND succ.customer_id = cust.customer_id)" + ")");

		// Update properties of generated SQL code
		this.codeProperties.encounteredSubselect();
		this.codeProperties.encounteredNonCustomerTable();
	}

	@Override
	public final void postOrderClickedMailingRelationalEqlNode(final ClickedInMailingRelationalEqlNode node) throws CodeGeneratorException {
		final CodeFragment deviceQueryFragment = node.hasDeviceQuery() ? codeStack.pop() : null;

		try {
			this.mailingIdValidator.validateMailingId(node.getMailingId(), this.companyId);

			// If there is a node representing a link ID, check it!
			if (node.getLinkId() != null) {
				this.linkIdValidator.validateLinkId(node.getMailingId(), node.getLinkId(), this.companyId);
			}

			final StringBuffer buffer = new StringBuffer();

			// Build the WHERE clause
			buffer.append("EXISTS (SELECT 1 FROM ");

			buffer.append("rdirlog_");
			buffer.append(companyId);
			buffer.append("_tbl t");

			// If CLICKED IN MAILING is refined by a device query, add additional tables
			if (node.hasDeviceQuery()) {
				buffer.append(buildDeviceQueryTableList());
			}

			buffer.append(" WHERE t.company_id=");
			buffer.append(companyId);
			buffer.append(" AND t.customer_id=cust.customer_id");
			buffer.append(" AND t.mailing_id=");
			buffer.append(node.getMailingId());

			if (node.getLinkId() != null) {
				buffer.append(" AND t.url_id=");
				buffer.append(node.getLinkId());
			}

			if (node.hasDeviceQuery()) {
				assert deviceQueryFragment != null;
				buffer.append(" AND (").append(deviceQueryFragment.getCode()).append(")");
			}

			buffer.append(")");

			codeStack.push(new CodeFragment(buffer.toString(), DataType.BOOLEAN, null));

			// Update properties of generated SQL code
			this.codeProperties.encounteredSubselect();
			this.codeProperties.encounteredNonCustomerTable();
		} catch (MailingIdValidationException e) {
			throw new UnknownMailingIdFaultyCodeException(node.getMailingId(), node.getStartLocation(), e);
		} catch (LinkIdValidationException e) {
			throw new UnknownLinkIdFaultyCodeException(node.getMailingId(), node.getLinkId(), node.getStartLocation(),
					e);
		}
	}


	@Override
	public void postOrderConstantListEqlNode(ConstantListEqlNode node) throws CodeGeneratorException {
		// According to EQL grammar specification, an expression list must
		// contain at least one element
		assert (node.listSize() >= 1);

		// We need to reverse the order of the nodes representing the list
		// elements, because the last element is on top of stack and we will
		// read elements in reversed order from stack.
		DataType listElementType = null;
		Stack<CodeFragment> elementStack = new Stack<>();
		for (int i = 0; i < node.listSize(); i++) {
			CodeFragment fragment = codeStack.pop();
			elementStack.push(fragment);

			// Do type checking.
			if (i > 0) {
				// Check, that type of all elements are identical
				checkType(node.getStartLocation(), fragment, listElementType);
			} else {
				// First element defines expected type for following elements
				listElementType = fragment.evaluatesToType();
			}
		}
		// Now, there are no code fragments representing list elements on code
		// stack. All of these fragments are on elementStack now and sorted in
		// reversed order.

		StringBuffer buffer = new StringBuffer();
		buffer.append("(");
		for (int i = 0; i < node.listSize(); i++) {
			CodeFragment fragment = elementStack.pop();

			if (i > 0) {
				buffer.append(",");
			}

			buffer.append(fragment.getCode());
		}
		buffer.append(")");

		// Data type of list is defined by data type of (first) list element
		codeStack.push(new CodeFragment(buffer.toString(), listElementType, null));
	}

	@Override
	public void postOrderBinaryOperatorExpressionalEqlNode(BinaryOperatorExpressionalEqlNode node) throws CodeGeneratorException {
		assert (codeStack.size() >= 2);

		CodeFragment right = codeStack.pop();
		CodeFragment left = codeStack.pop();

		if (left.evaluatesToType(DataType.DATE)) {
			checkType(node.getStartLocation(), right, DataType.NUMERIC);
		} else {
			checkType(node.getStartLocation(), right, left.evaluatesToType());
		}

		switch (left.evaluatesToType()) {
		case NUMERIC:
			numericPostOrderBinaryOperatorExpressionalEqlNode(node, left, right);
			break;

		case TEXT:
			alphanumericPostOrderBinaryOperatorExpressionalEqlNode(node, left, right);
			break;

		case DATE:
			datePostOrderBinaryOperatorExpressionalEqlNode(node, left, right);
			break;

		default:
			throw new UnhandledDataTypeException(left, left.evaluatesToType());
		}
	}

	/**
	 * Does code generation for binary expression operators on numeric types.
	 * 
	 * @param node
	 *            the operator node itself
	 * @param left
	 *            left sub-expression
	 * @param right
	 *            right sub-expression
	 * 
	 * @throws CodeGeneratorException
	 *             on errors generating code
	 */
	private void numericPostOrderBinaryOperatorExpressionalEqlNode(BinaryOperatorExpressionalEqlNode node, CodeFragment left, CodeFragment right) throws CodeGeneratorException {
		checkType(node.getStartLocation(), right, DataType.NUMERIC);

		Set<String> set = new HashSet<>(left.getUnusedReferenceTables());
		set.addAll(right.getUnusedReferenceTables());

		switch (node.getOperator()) {
		case ADD:
			codeStack.push(new CodeFragment("(" + left.getCode() + " + " + right.getCode() + ")", DataType.NUMERIC, set));
			break;
		case SUB:
			codeStack.push(new CodeFragment("(" + left.getCode() + " - " + right.getCode() + ")", DataType.NUMERIC, set));
			break;
		case MUL:
			codeStack.push(new CodeFragment("(" + left.getCode() + " * " + right.getCode() + ")", DataType.NUMERIC, set));
			break;
		case DIV:
			codeStack.push(
					new CodeFragment("(" + left.getCode() + " / " + right.getCode() + ")", DataType.NUMERIC, set));
			break;
		case MOD:
			codeStack.push(new CodeFragment("mod(" + left.getCode() + ", " + right.getCode() + ")", DataType.NUMERIC, set));
			break;

		default:
			throw new UnhandledOperatorException(node.getOperator());
		}
	}

	/**
	 * Does code generation for binary expression operators on string types.
	 * 
	 * @param node
	 *            the operator node itself
	 * @param left
	 *            left sub-expression
	 * @param right
	 *            right sub-expression
	 * 
	 * @throws CodeGeneratorException
	 *             on errors generating code
	 */
	private void alphanumericPostOrderBinaryOperatorExpressionalEqlNode(BinaryOperatorExpressionalEqlNode node, CodeFragment left, CodeFragment right) throws CodeGeneratorException {
		checkType(node.getStartLocation(), right, DataType.TEXT);

		Set<String> set = new HashSet<>(left.getUnusedReferenceTables());
		set.addAll(right.getUnusedReferenceTables());

		switch (node.getOperator()) {
		case ADD:
			codeStack.push(new CodeFragment(sqlDialect.stringConcat(left.getCode(), right.getCode()), DataType.TEXT, set));
			break;

		default:
			throw new UnsupportedOperatorForDataTypeException(node.getStartLocation(), node.getOperator(), DataType.TEXT);
		}
	}

	/**
	 * Does code generation for binary expression operators on numeric types.
	 * 
	 * @param node
	 *            the operator node itself
	 * @param left
	 *            left sub-expression
	 * @param right
	 *            right sub-expression
	 * 
	 * @throws CodeGeneratorException
	 *             on errors generating code
	 */
	private final void datePostOrderBinaryOperatorExpressionalEqlNode(final BinaryOperatorExpressionalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		checkType(node.getStartLocation(), right, DataType.NUMERIC);

		final Set<String> set = new HashSet<>(left.getUnusedReferenceTables());
		set.addAll(right.getUnusedReferenceTables());

		switch (node.getOperator()) {
		case ADD:
			codeStack.push(new CodeFragment(sqlDialect.dateAddDays(left.getCode(), right.getCode()), DataType.DATE, set));
			break;
		case SUB:
			codeStack.push(new CodeFragment(sqlDialect.dateSubDays(left.getCode(), right.getCode()), DataType.DATE, set));
			break;

		case MUL:
		case DIV:
		case MOD:
			throw new UnsupportedOperatorForDataTypeException(node.getStartLocation(), node.getOperator(), DataType.DATE);

		default:
			throw new UnhandledOperatorException(node.getOperator());
		}
	}

	@Override
	public void postOrderNegExpressionalEqlNode(NegExpressionalEqlNode node) throws CodeGeneratorException {
		assert (codeStack.size() >= 1);

		CodeFragment child = codeStack.pop();
		checkType(node.getStartLocation(), child, DataType.NUMERIC);

		codeStack.push(new CodeFragment("(-" + child.getCode() + ")", DataType.NUMERIC, child.getUnusedReferenceTables()));
	}

	@Override
	public void terminalNumericConstantAtomEqlNode(NumericConstantAtomEqlNode node) throws CodeGeneratorException {
		codeStack.push(new CodeFragment(node.getValue(), DataType.NUMERIC, null));
	}

	@Override
	public void terminalProfileFieldAtomEqlNode(ProfileFieldAtomEqlNode node) throws CodeGeneratorException {
		assert this.referenceTableNameForReferencesHaving == null;
		
		try {
			DataType type = this.profileFieldTypeResolver.resolveProfileFieldType(node.getName());
			String profileFieldName = "cust." + this.profileFieldNameResolver.resolveProfileFieldName(node.getName());

			codeStack.push(new CodeFragment(profileFieldName, type, null));
		} catch (ProfileFieldResolveException e) {
			throw new UnknownProfileFieldFaultyCodeException(node, e);
		}
	}

	@Override
	public void terminalStringConstantWithEscapeCharsAtomEqlNode(StringConstantWithEscapeCharsAtomEqlNode node) throws CodeGeneratorException {
		/*
		 * We do not need to check, if the string constant is used as matching pattern
		 * (-> LIKE operator), because, for the LIKE operator, this method is never
		 * called.
		 */
		codeStack.push(new CodeFragment("'" + DbUtilities.escapeSinglesQuotes(node.getValue()) + "'", DataType.TEXT, null));
	}
	
	@Override
	public void terminalTodayAtomEqlNode(AbstractAtomEqlNode node) throws CodeGeneratorException {
		codeStack.push(new CodeFragment(sqlDialect.today(), DataType.DATE, null));
	}


	protected String makeReferenceTableFrame(String condition, Set<String> referenceTableNames) throws ReferenceTableResolveException {
		throw new UnsupportedOperationException();
	}
		
	protected String buildDeviceQueryTableList() {
		throw new UnsupportedOperationException();
	}
	
	// --------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// Unused callbacks
	@Override
	public void postOrderRelationalBooleanEqlNode(RelationalBooleanEqlNode node) throws CodeGeneratorException {
		/* No code generation here. */ }

	@Override
	public void postOrderAbstractRelationalEqlNode(AbstractRelationalEqlNode node) throws CodeGeneratorException {
		/* No code generation here. */ }

	@Override
	public void postOrderAbstractBooleanEqlNode(AbstractBooleanEqlNode node) throws CodeGeneratorException {
		/* No code generation here. */ }

	@Override
	public void postOrderAbstractExpressionalEqlNode(AbstractExpressionalEqlNode node) throws CodeGeneratorException {
		/* No code generation here. */ }

	@Override
	public void postOrderAtomExpressionalEqlNode(AtomExpressionalEqlNode node) throws CodeGeneratorException {
		/* No code generation here. */ }

	@Override
	public void finished() throws CodeGeneratorException {
		/* No code generation here. */ }

}
