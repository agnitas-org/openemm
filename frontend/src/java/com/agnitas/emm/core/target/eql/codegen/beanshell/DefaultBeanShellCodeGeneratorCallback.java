/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.eql.codegen.beanshell;

import java.util.Objects;
import java.util.Stack;

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
import com.agnitas.emm.core.target.eql.codegen.CodeFragment;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorException;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorImplementationException;
import com.agnitas.emm.core.target.eql.codegen.DataType;
import com.agnitas.emm.core.target.eql.codegen.EqlDateFormat;
import com.agnitas.emm.core.target.eql.codegen.UnhandledDataTypeException;
import com.agnitas.emm.core.target.eql.codegen.UnhandledOperatorException;
import com.agnitas.emm.core.target.eql.codegen.UnsupportedOperatorForCodeTargetException;
import com.agnitas.emm.core.target.eql.codegen.UnsupportedOperatorForDataTypeException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldNameResolver;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldTypeResolver;
import com.agnitas.emm.core.target.eql.codegen.util.DataTypeUtils;
import com.agnitas.emm.core.target.eql.codegen.util.StringUtil;

public class DefaultBeanShellCodeGeneratorCallback implements BeanShellCodeGeneratorCallback {

	/** Resolver for profile field names to corresponding database names. */
	private final ProfileFieldNameResolver profileFieldNameResolver;
	
	/** Resolver for profile field names to their data types. */
	private final ProfileFieldTypeResolver profileFieldTypeResolver;
	
	protected final Stack<CodeFragment> codeStack;
	
	public DefaultBeanShellCodeGeneratorCallback(final ProfileFieldNameResolver profileFieldNameResolver, final ProfileFieldTypeResolver profileFieldTypeResolver) {
		this.codeStack = new Stack<>();
		this.profileFieldNameResolver = Objects.requireNonNull(profileFieldNameResolver, "ProfileFieldNameResolver is null");
		this.profileFieldTypeResolver = Objects.requireNonNull(profileFieldTypeResolver, "ProfileFieldTypeResolver is null");
	}

	@Override
	public final void finishedWithEmptyTargetRule() throws CodeGeneratorException {
		assert codeStack.isEmpty();
		
		// Create some dummy condition
		codeStack.push(new CodeFragment("true", DataType.BOOLEAN, null));
	}

	@Override
	public final void error(final CodeGeneratorException e) {
		// In case of an error, clear the code stack
		this.codeStack.clear();
	}

	@Override
	public final void postOrderNotOperatorBooleanEqlNode(final NotOperatorBooleanEqlNode node) throws CodeGeneratorException {
		assert !codeStack.empty();
		
		final CodeFragment code = DataTypeUtils.requireDataType(codeStack.pop(), node.getStartLocation(), DataType.BOOLEAN);
		
		final CodeFragment newCode = new CodeFragment(
				String.format("!%s", code.getCode()),
				DataType.BOOLEAN,
				null);
				
		codeStack.push(newCode);
	}

	@Override
	public final void postOrderBinaryOperatorBooleanEqlNode(final BinaryOperatorBooleanEqlNode node) throws CodeGeneratorException {
		assert codeStack.size() >= 2;
		
		final CodeFragment right = DataTypeUtils.requireDataType(codeStack.pop(), node.getStartLocation(), DataType.BOOLEAN);
		final CodeFragment left = DataTypeUtils.requireDataType(codeStack.pop(), node.getStartLocation(), DataType.BOOLEAN);
		
		final String operatorCode = OperatorUtils.beanShellOperatorSymbol(node);

		codeStack.push(new CodeFragment(
				String.format("(%s %s %s)", left.getCode(), operatorCode, right.getCode()),
				DataType.BOOLEAN,
				null
				));
	}
	
	@Override
	public final void postOrderBinaryOperatorRelationalEqlNode(final BinaryOperatorRelationalEqlNode node, final EqlDateFormat dateFormat) throws CodeGeneratorException {
		assert codeStack.size() >= 2;
		
		final CodeFragment right = codeStack.pop();
		final CodeFragment left = codeStack.pop();
		
		final DataType expectedTypeOnRightSide = left.evaluatesToType(DataType.DATE)
				? DataType.NUMERIC
				: left.evaluatesToType();
		
		DataTypeUtils.requireDataType(right, node.getStartLocation(), expectedTypeOnRightSide);
		
		switch(left.evaluatesToType()) {
		case NUMERIC:
			numericPostOrderBinaryOperatorRelationalEqlNode(node, left, right);
			break;
			
		case TEXT:
			alphanumericPostOrderBinaryOperatorRelationalEqlNode(node, left, right);
			break;
			
		case DATE:
			datePostOrderBinaryOperatorRelationalEqlNode(node, left, right);
			break;
			
		default:
			throw new UnhandledDataTypeException(left, left.evaluatesToType());
		}
	}
	
	private final void numericPostOrderBinaryOperatorRelationalEqlNode(final BinaryOperatorRelationalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		final String operatorSymbol = OperatorUtils.numericBeanShellOperatorSymbol(node);
		
		codeStack.push(new CodeFragment(
				String.format("(%s %s %s)", left.getCode(), operatorSymbol, right.getCode()),
				DataType.BOOLEAN,
				null));
	}
	
	// For alphanumeric values, we cannot use a generic form of operator handling. Each oeprator must be handled individually.
	private final void alphanumericPostOrderBinaryOperatorRelationalEqlNode(final BinaryOperatorRelationalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		switch(node.getOperator()) {
		case EQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compareStringsIgnoreCase(%s,%s) == 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;

		case NEQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compareStringsIgnoreCase(%s,%s) != 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;
			
		case GEQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compareStringsIgnoreCase(%s,%s) >= 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;
			
		case GT:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compareStringsIgnoreCase(%s,%s) > 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;

		case LEQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compareStringsIgnoreCase(%s,%s) <= 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;

		case LT:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compareStringsIgnoreCase(%s,%s) < 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;
			
		default:
			throw new UnhandledOperatorException(node.getOperator());
		}

	}
	
	private final void datePostOrderBinaryOperatorRelationalEqlNode(final BinaryOperatorRelationalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		switch(node.getOperator()) {
		case EQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compare(%s,%s) == 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;

		case NEQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compare(%s,%s) != 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;
			
		case GEQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compare(%s,%s) >= 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;
			
		case GT:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compare(%s,%s) > 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;

		case LEQ:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compare(%s,%s) <= 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;

		case LT:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.compare(%s,%s) < 0", left.getCode(), right.getCode()),
					DataType.BOOLEAN,
					null));
			break;
			
		default:
			throw new UnhandledOperatorException(node.getOperator());
		}
	}

	@Override
	public final void postOrderEmptyRelationalEqlNode(final EmptyRelationalEqlNode node) throws CodeGeneratorException {
		assert !codeStack.empty();
		
		final CodeFragment code = codeStack.pop();
		
		if(node.getNotFlag()) {
			// IS NOT EMPTY
			codeStack.push(new CodeFragment(
					String.format("!BeanShellRuntimeUtils.isEmpty(%s)", code.getCode()),
					DataType.BOOLEAN,
					null
					));
		} else {
			// IS EMPTY
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.isEmpty(%s)", code.getCode()),
					DataType.BOOLEAN,
					null
					));
		}
	}

	@Override
	public final void postOrderLikeRelationalEqlNode(final LikeRelationalEqlNode node, final StringConstantWithEscapeCharsAtomEqlNode stringConstantWithEscapeCharsAtomEqlNode) throws CodeGeneratorException {
		assert !codeStack.empty();
		
		final CodeFragment code = codeStack.pop();

		if(code.evaluatesToType(DataType.TEXT)) {
			final String bshCode = String.format("AgnUtils.match(AgnUtils.toLowerCase(%s), AgnUtils.toLowerCase(%s))", 
					node.getNotFlag() ? "!" : "",
					code.getCode(),
					quote(stringConstantWithEscapeCharsAtomEqlNode));
			
			codeStack.push(new CodeFragment(bshCode, DataType.BOOLEAN, null));
		} else {
			throw new UnsupportedOperatorForDataTypeException(node.getStartLocation(), "LIKE", code.evaluatesToType());
		}

	}

	@Override
	public final void postOrderContainsRelationalEqlNode(final ContainsRelationalEqlNode node, final StringConstantWithEscapeCharsAtomEqlNode stringConstantWithEscapeCharsAtomEqlNode) throws CodeGeneratorException {
		assert !codeStack.isEmpty();
		
		final CodeFragment code = DataTypeUtils.requireDataType(codeStack.pop(), node.getStartLocation(), DataType.TEXT);

		codeStack.push(new CodeFragment(
				String.format("BeanShellRuntimeUtils.containsIgnoreCase(%s,\"%s\")", code.getCode(), quote(stringConstantWithEscapeCharsAtomEqlNode)),
				DataType.BOOLEAN,
				null));
	}

	@Override
	public final void postOrderStartsWithRelationalEqlNode(final StartsWithRelationalEqlNode node, final StringConstantWithEscapeCharsAtomEqlNode stringConstantWithEscapeCharsAtomEqlNode) throws CodeGeneratorException {
		assert !codeStack.isEmpty();
		
		final CodeFragment code = DataTypeUtils.requireDataType(codeStack.pop(), node.getStartLocation(), DataType.TEXT);
	
		codeStack.push(new CodeFragment(
				String.format("BeanShellRuntimeUtils.startsWithIgnoreCase(%s,\"%s\")", code.getCode(), quote(stringConstantWithEscapeCharsAtomEqlNode)),
				DataType.BOOLEAN,
				null));
	}

	@Override
	public final void postOrderBinaryOperatorExpressionalEqlNode(final BinaryOperatorExpressionalEqlNode node) throws CodeGeneratorException {
		assert codeStack.size() >= 2;
		
		final CodeFragment right = codeStack.pop();
		final CodeFragment left = codeStack.pop();
		
		
		final DataType expectedTypeOnRightSide = left.evaluatesToType(DataType.DATE)
				? DataType.NUMERIC
				: left.evaluatesToType();
		
		DataTypeUtils.requireDataType(right, node.getStartLocation(), expectedTypeOnRightSide);


		switch(left.evaluatesToType()) {
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
	
	private final void numericPostOrderBinaryOperatorExpressionalEqlNode(final BinaryOperatorExpressionalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		final String operatorSymbol = OperatorUtils.numericBeanShellOperatorSymbol(node);
		
		codeStack.push(new CodeFragment(
				String.format("(%s %s %s)", left.getCode(), operatorSymbol, right.getCode()),
				DataType.NUMERIC,
				null));
	}
	
	private final void alphanumericPostOrderBinaryOperatorExpressionalEqlNode(final BinaryOperatorExpressionalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		switch (node.getOperator()) {
		case ADD:
			codeStack.push(new CodeFragment(
					String.format("BeanShellRuntimeUtils.concat(%s,%s)", left.getCode(), right.getCode()), 
					DataType.TEXT, 
					null));
			break;

		default:
			throw new UnsupportedOperatorForDataTypeException(node.getStartLocation(), node.getOperator(), DataType.TEXT);
		}
	}
	
	private final void datePostOrderBinaryOperatorExpressionalEqlNode(final BinaryOperatorExpressionalEqlNode node, final CodeFragment left, final CodeFragment right) throws CodeGeneratorException {
		// TODO Implement
		throw new UnhandledOperatorException(node.getOperator());
	}

	@Override
	public final void postOrderNegExpressionalEqlNode(final NegExpressionalEqlNode node) throws CodeGeneratorException {
		assert !codeStack.isEmpty();
		
		final CodeFragment code = DataTypeUtils.requireDataType(codeStack.pop(), node.getStartLocation(), DataType.NUMERIC);
		
		codeStack.push(new CodeFragment(
				String.format("(-%s)", code.getCode()),
				DataType.NUMERIC,
				null));
	}

	@Override
	public final void terminalNumericConstantAtomEqlNode(final NumericConstantAtomEqlNode node) throws CodeGeneratorException {
		codeStack.push(new CodeFragment(
				String.format("%s", node.getValue()),
				DataType.NUMERIC,
				null));
	}

	@Override
	public final void terminalProfileFieldAtomEqlNode(final ProfileFieldAtomEqlNode node) throws CodeGeneratorException {
		try {
			final String identifier = this.profileFieldNameResolver.resolveProfileFieldName(node.getName());
			final DataType type = this.profileFieldTypeResolver.resolveProfileFieldType(node.getName());
			
			codeStack.push(new CodeFragment(
					String.format("%s", identifier),
					type,
					null));
		} catch(final ProfileFieldResolveException e) {
			throw new CodeGeneratorException(String.format("Unable to resolve profile field name '%s'", node.getName()), e);
		} 
	}

	@Override
	public final void terminalStringConstantWithEscapeCharsAtomEqlNode(final StringConstantWithEscapeCharsAtomEqlNode node) throws CodeGeneratorException {
		codeStack.push(new CodeFragment(
				String.format("\"%s\"", quote(node)),
				DataType.TEXT,
				null));
	}

	@Override
	public final void terminalTodayAtomEqlNode(final AbstractAtomEqlNode node) throws CodeGeneratorException {
		codeStack.push(new CodeFragment(
				"new Date()",
				DataType.DATE,
				null));
	}

	@Override
	public final String getBeanShellCode() throws CodeGeneratorImplementationException {
		if(this.codeStack.isEmpty())
			throw new CodeGeneratorImplementationException("No BeanShell code generated.");
		
		assert this.codeStack.size() == 1;
		
		return this.codeStack.pop().getCode();
	}
	
	private static final String quote(final StringConstantWithEscapeCharsAtomEqlNode constant) {
		return StringUtil.replaceEscapedChars(constant.getValue()).replace("\"", "\\\"");
	}

	// -------------------------------------------------------=[ callbacks of unsupported operators ]=--- 

	@Override
	public final void postOrderInRelationalEqlNode(final InRelationalEqlNode node) throws CodeGeneratorException {
		throw new UnsupportedOperatorForCodeTargetException(node.getStartLocation(), "IN");
	}

	@Override
	public final void postOrderOpenedMailingRelationalEqlNode(final OpenedMailingRelationalEqlNode node) throws CodeGeneratorException {
		throw new UnsupportedOperatorForCodeTargetException(node.getStartLocation(), "OPENED MAILING");
	}

	@Override
	public final void postOrderReceivedMailingRelationalEqlNode(final ReceivedMailingRelationalEqlNode node) throws CodeGeneratorException {
		throw new UnsupportedOperatorForCodeTargetException(node.getStartLocation(), "RECEIVED MAILING");
	}

	@Override
	public final void postOrderClickedMailingRelationalEqlNode(final ClickedInMailingRelationalEqlNode node) throws CodeGeneratorException {
		throw new UnsupportedOperatorForCodeTargetException(node.getStartLocation(), "CLICKED IN MAILING");
	}

	@Override
	public final void postOrderConstantListEqlNode(final ConstantListEqlNode node) throws CodeGeneratorException {
		throw new UnsupportedOperatorForCodeTargetException(node.getStartLocation(), "Lists");
	}

	// -------------------------------------------------------=[ unused callback ]=--- 
	@Override
	public final void postOrderAbstractExpressionalEqlNode(final AbstractExpressionalEqlNode node) throws CodeGeneratorException {
		// No code generation here
	}

	@Override
	public final void postOrderRelationalBooleanEqlNode(final RelationalBooleanEqlNode node) throws CodeGeneratorException {
		// No code generation here
	}

	@Override
	public final void postOrderAbstractRelationalEqlNode(final AbstractRelationalEqlNode node) throws CodeGeneratorException {
		// No code generation here
	}

	@Override
	public final void postOrderAbstractBooleanEqlNode(final AbstractBooleanEqlNode node) throws CodeGeneratorException {
		// No code generation here
	}

	@Override
	public final void postOrderAtomExpressionalEqlNode(final AtomExpressionalEqlNode node) throws CodeGeneratorException {
		// No code generation here
	}

	@Override
	public final void finished() throws CodeGeneratorException {
		// No code generation here
	}

}
