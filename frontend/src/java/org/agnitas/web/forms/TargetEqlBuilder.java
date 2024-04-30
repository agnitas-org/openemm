/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.target.ConditionalOperator;
import org.agnitas.web.forms.helper.EmptyStringFactory;
import org.agnitas.web.forms.helper.ZeroIntegerFactory;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.list.GrowthList;
import org.apache.commons.collections4.list.LazyList;
import org.apache.commons.lang3.StringUtils;

/*
 * TODO Remove class when recipient search has been completely switched
 * to QueryBuilder. (GWUA-4678?)
 */
@Deprecated(forRemoval = true)
public class TargetEqlBuilder {

    private static transient final Factory<String> emptyStringFactory = new EmptyStringFactory();
	private static transient final Factory<Integer> zeroIntegerFactory = new ZeroIntegerFactory();
	private static transient final Factory<ConditionalOperator[]> nullFactory = FactoryUtils.nullFactory();

    private String shortname;
    private String description;

    private List<String> columnAndTypeList;
    private List<Integer> chainOperatorList;
    private List<Integer> parenthesisOpenedList;
    private List<Integer> primaryOperatorList;
    private List<String> primaryValueList;
    private List<Integer> parenthesisClosedList;
    private List<String> dateFormatList;
    private List<Integer> secondaryOperatorList;
    private List<String> secondaryValueList;
    private List<ConditionalOperator[]> validTargetOperatorsList;
    private List<String> columnNameList;
    private List<Integer> columnTypeList;

    private String columnAndTypeNew;
    private int chainOperatorNew;
    private int parenthesisOpenedNew;
    private int primaryOperatorNew;
    private String primaryValueNew;
    private int parenthesisClosedNew;
    private String dateFormatNew;
    private int secondaryOperatorNew;
    private String secondaryValueNew;

    public TargetEqlBuilder() {

        columnAndTypeList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        chainOperatorList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        parenthesisOpenedList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        primaryOperatorList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        primaryValueList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        parenthesisClosedList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        dateFormatList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        secondaryOperatorList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
        secondaryValueList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        validTargetOperatorsList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), nullFactory));
        columnNameList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), emptyStringFactory));
        columnTypeList = GrowthList.growthList(LazyList.lazyList(new ArrayList<>(), zeroIntegerFactory));
    }

    public void reset() {
        // Reset form fields for new rule
        columnAndTypeNew = null;
        chainOperatorNew = 0;
        parenthesisOpenedNew = 0;
        primaryOperatorNew = 0;
        primaryValueNew = null;
        parenthesisClosedNew = 0;
        dateFormatNew = null;
        secondaryOperatorNew = 0;
        secondaryValueNew = null;
    }

    public void clearRules() {
        columnAndTypeList.clear();
        chainOperatorList.clear();
        parenthesisOpenedList.clear();
        primaryOperatorList.clear();
        primaryValueList.clear();
        parenthesisClosedList.clear();
        dateFormatList.clear();
        secondaryOperatorList.clear();
        secondaryValueList.clear();
        columnNameList.clear();
        columnTypeList.clear();
    }

    public void removeRule(int index) {
        safeRemove(columnAndTypeList, index);
        safeRemove(chainOperatorList, index);
        safeRemove(parenthesisOpenedList, index);
        safeRemove(primaryOperatorList, index);
        safeRemove(primaryValueList, index);
        safeRemove(parenthesisClosedList, index);
        safeRemove(dateFormatList, index);
        safeRemove(secondaryOperatorList, index);
        safeRemove(secondaryValueList, index);
        safeRemove(columnNameList, index);
        safeRemove(columnTypeList, index);
	}

    /**
	 * Removes and index safely from list. If index does not exists, nothing happens.
	 *
	 * @param list list to remove index from
	 * @param index index to be removed
	 */
	private void safeRemove(List<?> list, int index) {
		if (list.size() > index && index >= 0) {
			list.remove(index);
		}
	}

	public void addNewRule(int index) {
		setColumnAndType(index, getColumnAndTypeNew());
		setChainOperator(index, getChainOperatorNew());
		setParenthesisOpened(index, getParenthesisOpenedNew());
		setPrimaryOperator(index, getPrimaryOperatorNew());
		setPrimaryValue(index, getPrimaryValueNew());
		setParenthesisClosed(index, getParenthesisClosedNew());
		setDateFormat(index, getDateFormatNew());
		setSecondaryOperator(index, getSecondaryOperatorNew());
		setSecondaryValue(index, getSecondaryValueNew());
	}

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumTargetNodes() {
		return this.columnAndTypeList.size();
	}

	public String getColumnAndType(int index) {
		return this.columnAndTypeList.get(index);
	}

	public void setColumnAndType(int index, String value) {
		this.columnAndTypeList.set(index, value);
	}

	public int getChainOperator(int index) {
		return this.chainOperatorList.get(index);
	}

	public void setChainOperator(int index, int value) {
		this.chainOperatorList.set(index, value);
	}

	public int getParenthesisOpened(int index) {
		return this.parenthesisOpenedList.get(index);
	}

	public void setParenthesisOpened(int index, int value) {
		this.parenthesisOpenedList.set(index, value);
	}

	public int getPrimaryOperator(int index) {
		return this.primaryOperatorList.get(index);
	}

	public void setPrimaryOperator(int index, int value) {
		this.primaryOperatorList.set(index, value);
	}

	public String getPrimaryValue(int index) {
		return this.primaryValueList.get(index);
	}

	public void setPrimaryValue(int index, String value) {
		this.primaryValueList.set(index, value);
	}

	public int getParenthesisClosed(int index) {
		return this.parenthesisClosedList.get(index);
	}

	public void setParenthesisClosed(int index, int value) {
		this.parenthesisClosedList.set(index, value);
	}

	public String getDateFormat(int index) {
		return this.dateFormatList.get(index);
	}

	public void setDateFormat(int index, String value) {
		this.dateFormatList.set(index, value);
	}

	public int getSecondaryOperator(int index) {
		return this.secondaryOperatorList.get(index);
	}

	public void setSecondaryOperator(int index, int value) {
		this.secondaryOperatorList.set(index, value);
	}

	public String getSecondaryValue(int index) {
		return this.secondaryValueList.get(index);
	}

	public void setSecondaryValue(int index, String value) {
		this.secondaryValueList.set(index, value);
	}

	public List<String> getAllColumnsAndTypes() {
		return this.columnAndTypeList;
	}

	public void setValidTargetOperators(int index, ConditionalOperator[] operators) {
		this.validTargetOperatorsList.set(index, operators);
	}

	public ConditionalOperator[] getValidTargetOperators(int index) {
		return this.validTargetOperatorsList.get(index);
	}

	public void setColumnName(int index, String value) {
		this.columnNameList.set(index, value);
	}

	public String getColumnName(int index) {
		return this.columnNameList.get(index);
	}

	public void setColumnType(int index, int type) {
		this.columnTypeList.set(index, type);
	}

	public int getColumnType(int index) {
		return this.columnTypeList.get(index);
	}

	public String getColumnAndTypeNew() {
		return columnAndTypeNew;
	}

	public void setColumnAndTypeNew(String columnAndTypeNew) {
		this.columnAndTypeNew = columnAndTypeNew;
	}

	public int getChainOperatorNew() {
		return chainOperatorNew;
	}

	public void setChainOperatorNew(int chainOperatorNew) {
		this.chainOperatorNew = chainOperatorNew;
	}

	public int getParenthesisOpenedNew() {
		return parenthesisOpenedNew;
	}

	public void setParenthesisOpenedNew(int parenthesisOpenedNew) {
		this.parenthesisOpenedNew = parenthesisOpenedNew;
	}

	public int getPrimaryOperatorNew() {
		return primaryOperatorNew;
	}

	public void setPrimaryOperatorNew(int primaryOperatorNew) {
		this.primaryOperatorNew = primaryOperatorNew;
	}

	public String getPrimaryValueNew() {
		return primaryValueNew;
	}

	public void setPrimaryValueNew(String primaryValueNew) {
		this.primaryValueNew = primaryValueNew;
	}

	public int getParenthesisClosedNew() {
		return parenthesisClosedNew;
	}

	public void setParenthesisClosedNew(int parenthesisClosedNew) {
		this.parenthesisClosedNew = parenthesisClosedNew;
	}

	public String getDateFormatNew() {
		return dateFormatNew;
	}

	public void setDateFormatNew(String dateFormatNew) {
		this.dateFormatNew = dateFormatNew;
	}

	public int getSecondaryOperatorNew() {
		return secondaryOperatorNew;
	}

	public void setSecondaryOperatorNew(int secondaryOperatorNew) {
		this.secondaryOperatorNew = secondaryOperatorNew;
	}

	public String getSecondaryValueNew() {
		return secondaryValueNew;
	}

	public void setSecondaryValueNew(String secondaryValueNew) {
		this.secondaryValueNew = secondaryValueNew;
	}

	@Deprecated
    public boolean checkParenthesisBalance() {
    	int opened = 0;
    	int closed = 0;

    	int lastIndex = this.getNumTargetNodes();

    	for (int index = 0; index < lastIndex; index++) {
    		opened += this.getParenthesisOpened(index);
    		closed += this.getParenthesisClosed(index);
    	}

    	return opened == closed;
    }

    /**
     * Get index of conditional field
     *
     * @param field
     * @return found index of field column otherwise return -1
     */
    public int findConditionalIndex(String field) {
	    final List<String> columns = getAllColumnsAndTypes();

		int index = 0;
		for (String columnToCheck : columns) {
			if (columnToCheck.equals(field)) {
				return index;
			}
			index++;
		}

		return -1;
    }

    public void addSimpleRule(int lastIndex, String field, ConditionalOperator operator, String value) {
        setColumnAndType(lastIndex, field);
        setChainOperator(lastIndex, getChainOperatorNew());
        setParenthesisOpened(lastIndex, getParenthesisOpenedNew());
        setPrimaryOperator(lastIndex, operator.getOperatorCode());
        setPrimaryValue(lastIndex, StringUtils.trim(value));
        setParenthesisClosed(lastIndex, getParenthesisClosedNew());
        setDateFormat(lastIndex, getDateFormatNew());
        setSecondaryOperator(lastIndex, getSecondaryOperatorNew());
        setSecondaryValue(lastIndex, getSecondaryValueNew());
    }
}
