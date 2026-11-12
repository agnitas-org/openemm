/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.database.fulltext.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator;
import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchReservedLiteralsConfig;
import com.agnitas.emm.core.commons.database.fulltext.impl.factory.FulltextSearchReservedLiteralsFactory;
import com.agnitas.emm.core.commons.database.fulltext.operator.Operator;
import com.agnitas.emm.core.commons.database.fulltext.operator.factory.OperatorFactory;
import com.agnitas.emm.core.commons.database.fulltext.word.WordProcessor;
import com.agnitas.emm.core.commons.database.fulltext.word.factory.WordProcessorFactory;
import com.agnitas.messages.Message;
import com.agnitas.util.FulltextSearchInvalidQueryException;
import com.agnitas.util.FulltextSearchQueryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("fulltextSearchQueryGenerator")
public class FulltextSearchQueryGeneratorImpl implements FulltextSearchQueryGenerator {

    private static final Logger logger = LogManager.getLogger(FulltextSearchQueryGeneratorImpl.class);

    private static final String ESCAPE_SYMBOL = "\"";

    private static final String OPERATOR_DELIMITERS = "+\\s()" + ESCAPE_SYMBOL;

    private static final String SPLIT_REGEX = String.format("((?<=[%s])|(?=[%s]))", OPERATOR_DELIMITERS, OPERATOR_DELIMITERS);

    private static final String SANITIZE_REGEX = "\\s+";

    private static final String WHITESPACE = " ";

    private static final String OP_PAR = "(";

    private static final String CL_PAR = ")";

    private static final String DOUBLE_QUOTES = "\"";

    private static final String QUOTES_ERROR = "Unmatched \"";

    private static final String PAR_ERROR = "Unmatched parenthesis";

    private static final String[] FORBIDDEN_OPERATORS_AT_START = {"+"};

    private final Map<String, Operator> operators;
    private final Set<WordProcessor> wordProcessors;
    private final FulltextSearchReservedLiteralsConfig reservedLiteralsConfig;

    public FulltextSearchQueryGeneratorImpl(
            OperatorFactory operatorFactory,
            WordProcessorFactory wordProcessorFactory,
            FulltextSearchReservedLiteralsFactory reservedLiteralsFactory
    ) {
        this.operators = operatorFactory.createOperators();
        this.wordProcessors = wordProcessorFactory.createWordProcessors();
        this.reservedLiteralsConfig = reservedLiteralsFactory.createdReservedLiteralsConfig();
    }

    @Override
    public String tryGenerateSpecificQuery(String searchQuery) {
        try {
            return generateSpecificQuery(searchQuery);
        } catch (FulltextSearchQueryException e) {
            logger.error("Cannot transform full text search query: {}", searchQuery);
            return reservedLiteralsConfig.getFallbackQuery(searchQuery);
        }
    }

    @Override
    public String generateSpecificQuery(String searchQuery) {
        searchQuery = sanitize(searchQuery);
        searchQuery = escapeDatabaseSpecificSymbols(searchQuery);
        String[] tokens = getCorrectedTokens(searchQuery);
        String generatedQuery = generateSpecificQuery(tokens);
        reservedLiteralsConfig.validateGeneratedQuery(searchQuery, generatedQuery);
        return generatedQuery;
    }

    private String[] getCorrectedTokens(String searchQuery) {
        String[] tokens = searchQuery.split(SPLIT_REGEX);

        checkTokensValidity(tokens);

        if (StringUtils.isBlank(searchQuery) || isContainsAnyControlCharacters(searchQuery)) {
            return tokens;
        }

        for (int oddIndex = 0; oddIndex < tokens.length; oddIndex += 2) {
            tokens[oddIndex] = tokens[oddIndex] + "*";
        }

        return tokens;
    }

    private void checkTokensValidity(String[] tokens) {
        if (haveForbiddenOperatorAtStart(tokens)) {
            throw new FulltextSearchInvalidQueryException(
                    "Search phrase starts with invalid symbol!",
                    Message.of("error.search.character", String.join(", ", FORBIDDEN_OPERATORS_AT_START))
            );
        }

        if (haveSeveralOperatorsInRow(tokens)) {
            throw new FulltextSearchInvalidQueryException(
                    "Search phrase has several operators in a row!",
                    Message.of("error.search.operator.row")
            );
        }

        reservedLiteralsConfig.validateTokens(tokens);
    }

    private boolean haveForbiddenOperatorAtStart(String[] tokens) {
        if (tokens.length <= 0) {
            return false;
        }

        return Arrays.stream(FORBIDDEN_OPERATORS_AT_START)
                .anyMatch(o -> o.equals(tokens[0]));
    }

    private boolean haveSeveralOperatorsInRow(String[] tokens) {
        String previousToken = null;
        for (String token : tokens) {
            if (operators.containsKey(token) && token.equals(previousToken)) {
                return true;
            }
            previousToken = token;
        }

        return false;
    }

    private boolean isContainsAnyControlCharacters(String searchQuery) {
        if (StringUtils.isBlank(searchQuery)) {
            return false;
        }

        Pattern compile = Pattern.compile("[\"?*)(+%]");
        return compile.matcher(searchQuery).find() || reservedLiteralsConfig.isContainsDateBaseDependentControlCharacters(searchQuery);
    }

    private String generateSpecificQuery(String[] tokens) {
        LinkedList<String> operandStack = new LinkedList<>();
        LinkedList<Operator> operatorStack = new LinkedList<>();
        int matchingIndex;
        boolean previousIsOperand = false;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (operators.containsKey(token)) {
                if (WHITESPACE.equals(token) &&
                        ((i == 0 || operators.containsKey(tokens[i - 1])) ||
                                (i == tokens.length - 1 || operators.containsKey(tokens[i + 1])))) {
                    continue;
                }
                operatorStack.push(operators.get(token));
                previousIsOperand = false;
                continue;
            }

            // Operands written without operator in between (e.g. "word(suffix)") are joined without separator by default
            if (previousIsOperand && reservedLiteralsConfig.isOperatorRequiredBetweenOperands()) {
                operatorStack.push(operators.get(WHITESPACE));
            }

            if (token.equals(OP_PAR)) {
                matchingIndex = getMatchingIndex(tokens, i, CL_PAR);
                checkMatchingIndex(i, matchingIndex, PAR_ERROR);
                token = generateSpecificQuery(Arrays.copyOfRange(tokens, i + 1, matchingIndex));
                operandStack.push(OP_PAR + token + CL_PAR);
                i = matchingIndex;
            } else if (token.equals(DOUBLE_QUOTES)) {
                matchingIndex = getMatchingIndex(tokens, i, DOUBLE_QUOTES);
                checkMatchingIndex(i, matchingIndex, QUOTES_ERROR);
                token = String.join("", Arrays.copyOfRange(tokens, i + 1, matchingIndex));
                operandStack.push(DOUBLE_QUOTES + reservedLiteralsConfig.escapeWord(token) + DOUBLE_QUOTES);
                i = matchingIndex;
            } else {
                token = reservedLiteralsConfig.isReservedWord(token)
                        ? reservedLiteralsConfig.escapeWord(token)
                        : token;

                operandStack.push(token);
            }
            previousIsOperand = true;
        }

        return applyDatabaseSpecificOperators(operandStack, operatorStack);
    }

    private String applyDatabaseSpecificOperators(LinkedList<String> operandStack, LinkedList<Operator> operatorStack) {
        operandStack = operandStack.stream()
                .map(this::applyWordProcessors)
                .collect(Collectors.toCollection(LinkedList::new));

        // With operators between all operands, a missing operand means an operator without operand (e.g. "(a(b)+)")
        if (reservedLiteralsConfig.isOperatorRequiredBetweenOperands() && !operatorStack.isEmpty()
                && getRequiredOperandsCount(operatorStack) != operandStack.size()) {
            throw new FulltextSearchQueryException("Operator without operand");
        }

        List<String> operands = new ArrayList<>();
        while (!operatorStack.isEmpty()) {
            Operator operator = operatorStack.pop();
            while (operands.size() != operator.getOperandsCount()) {
                operands.add(operandStack.pop());
            }
            Collections.reverse(operands);
            operandStack.push(operator.process(operands));
            operands.clear();
        }
        return StringUtils.join(operandStack, "");
    }

    private static int getRequiredOperandsCount(List<Operator> operators) {
        int count = 1;
        for (Operator operator : operators) {
            count += operator.getOperandsCount() - 1;
        }
        return count;
    }

    private String applyWordProcessors(String word) {
        String result = word;
        for (WordProcessor processor : wordProcessors) {
            result = processor.process(result);
        }
        return result;
    }

    private int getMatchingIndex(String[] tokens, int startFrom, String symbol) {
        int matchingCounter = 0;
        for (int i = startFrom + 1; i < tokens.length; i++) {
            String current = tokens[i];
            if (OP_PAR.equals(current)) {
                matchingCounter++;
            } else if (current.equals(symbol) && matchingCounter == 0) {
                return i;
            } else if (CL_PAR.equals(current)) {
                matchingCounter--;
            }
        }
        return -1;
    }

    private String sanitize(String query) {
        if (StringUtils.isNotBlank(query)) {
            return reservedLiteralsConfig.sanitize(query.replaceAll(SANITIZE_REGEX, WHITESPACE).trim());
        } else {
            return "";
        }
    }

    private void checkMatchingIndex(int start, int matchingIndex, String error) {
        if (start >= matchingIndex) {
            throw new FulltextSearchQueryException(error);
        }
    }

    private String escapeDatabaseSpecificSymbols(String query) {
        StringBuilder escapedQuery = new StringBuilder();
        for (int i = 0; i < query.length(); i++) {
            char symbol = query.charAt(i);
            if (reservedLiteralsConfig.isReservedCharacter(symbol)) {
                escapedQuery.append(reservedLiteralsConfig.escapeCharacter(symbol));
            } else {
                escapedQuery.append(symbol);
            }
        }
        return escapedQuery.toString();
    }
}
