/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

public class TextTableBuilder {

	private final List<List<String>> columnNames;
	private final List<List<String>> content;
	private final List<String> columnColors;
	private final List<String> lineColors;
	private final List<Boolean> columnRightAligned;

	private int currentLineIndex = -1;
	private int currentColumnIndex = -1;
	
	public TextTableBuilder() {
		columnNames = new ArrayList<>();
		columnColors = new ArrayList<>();
		content = new ArrayList<>();
		columnRightAligned = new ArrayList<>();
		lineColors = new ArrayList<>();
		
		// first row contains headers
		columnNames.add(new ArrayList<>());
	}
	
	public TextTableBuilder(String... columnNames) {
		this();
		
		for (String columnName : columnNames) {
			addColumn(columnName);
		}
	}
	
	public void addColumn(String columnName) {
		addColumn(columnName, null, false);
	}
	
	public void addColumn(String columnName, String columnNameLine2, boolean alignRight) {
		addColumn(columnName, columnNameLine2, alignRight, null);
	}
	
	public void addColumn(String columnName, String columnNameLine2, boolean alignRight, String farbe) {
		columnNames.getFirst().add(columnName);
		columnColors.add(farbe);
		
		// add second row
		if (columnNameLine2 != null && columnNames.size() == 1) {
			columnNames.add(new ArrayList<>());
		}
		
		if (columnNames.size() > 1) {
			while (columnNames.get(1).size() < columnNames.get(0).size() - 1) {
				columnNames.get(1).add("");
			}
		}
		
		if (columnNameLine2 != null) {
			columnNames.get(1).add(columnNameLine2);
		} else if (columnNames.size() > 1) {
			columnNames.get(1).add("");
		}
		
		columnRightAligned.add(alignRight);
		
		// bring all rows to length of headers
		for (List<String> line : content) {
			while (line.size() < columnNames.getFirst().size()) {
				line.add("");
			}
		}
	}
	
	public void startNewLine() {
		startNewLine(null);
	}
	
	public void startNewLine(String farbe) {
		content.add(new ArrayList<>());
		lineColors.add(farbe);
		currentLineIndex++;
		currentColumnIndex = 0;
		
		// bring new row to length of headers
		while (content.get(currentLineIndex).size() < columnNames.getFirst().size()) {
			content.get(currentLineIndex).add("");
		}
	}
	
	public void addValueToCurrentLine(String value) {
		if (currentLineIndex == -1) {
			throw new IllegalStateException("No line has been created to extend");
		}

		if (content.get(currentLineIndex).size() <= currentColumnIndex) {
			throw new IllegalStateException("Attempt to write to a column that has not been created yet");
		}
		
		if (value == null) {
			value = "";
		}
		content.get(currentLineIndex).set(currentColumnIndex, value);
		currentColumnIndex++;
	}

	@Override
	public String toString() {
		List<StringBuilder> columnNamesLines = new ArrayList<>();
		for (int lineIndex = 0; lineIndex < columnNames.size(); lineIndex++) {
			columnNamesLines.add(new StringBuilder());
		}
		
		List<StringBuilder> contentLines = new ArrayList<>();
		for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
			contentLines.add(new StringBuilder());
		}
		
		// iterate over columns
		for (int columnIndex = 0; columnIndex < columnNames.getFirst().size(); columnIndex++) {
			int size = getMaxSizeOfColumn(columnIndex);
			
			for (int lineIndex = 0; lineIndex < columnNames.size(); lineIndex++) {
				if (columnIndex > 0) {
					columnNamesLines.get(lineIndex).append(" ");
				}
				
				columnNamesLines.get(lineIndex).append(trimStringToLengthAlignedLeft(columnNames.get(lineIndex).get(columnIndex), size));
			}
			
			// iterate over rows
			for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
				if (columnIndex > 0) {
					contentLines.get(lineIndex).append(" ");
				}
				
				if (columnRightAligned.get(columnIndex)) {
					contentLines.get(lineIndex).append(trimStringToLengthAlignedRight(content.get(lineIndex).get(columnIndex), size));
				} else {
					contentLines.get(lineIndex).append(trimStringToLengthAlignedLeft(content.get(lineIndex).get(columnIndex), size));
				}
			}
		}
		
		StringBuilder text = new StringBuilder();
		for (int lineIndex = 0 ; lineIndex < columnNamesLines.size(); lineIndex++) {
			text.append(columnNamesLines.get(lineIndex)).append("\n");
		}
		// separate headers
		text.append(AgnUtils.repeatString("-", columnNamesLines.getFirst().length()) + "\n");
		for (int lineIndex = 0 ; lineIndex < contentLines.size(); lineIndex++) {
			text.append(contentLines.get(lineIndex)).append("\n");
		}
		
		return text.toString();
	}
	
	public String toCsvString() {
		StringBuilder csvText = new StringBuilder();
		
		// write headers first
		// iterate over header lines
		for (int lineIndex = 0; lineIndex < columnNames.size(); lineIndex++) {
			// iterate over columns
			for (int columnIndex = 0; columnIndex < columnNames.getFirst().size(); columnIndex++) {
				csvText.append("\"").append(columnNames.get(lineIndex).get(columnIndex)).append("\";");
			}
			csvText.append("\n");
		}
		
		// write data rows
		// iterate over rows
		for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
			// iterate over columns
			for (int columnIndex = 0; columnIndex < content.getFirst().size(); columnIndex++) {
				csvText.append("\"").append(content.get(lineIndex).get(columnIndex)).append("\";");
			}
			csvText.append("\n");
		}
		
		return csvText.toString();
	}
	
	public String toHtmlString(String tableTitle) {
		return toHtmlString(tableTitle, 0);
	}
	
	public String toHtmlString(String tableTitle, int fontsizeInPt) {
		StringBuilder htmlTableStringBuffer = new StringBuilder();
		htmlTableStringBuffer.append(StringEscapeUtils.escapeHtml4(tableTitle)).append("<br>\n");
		if (fontsizeInPt > 0) {
			htmlTableStringBuffer.append("<STYLE TYPE=\"text/css\"><!--TD{font-family: Arial; font-size: ").append(fontsizeInPt).append("pt;}---></STYLE>");
		}
		htmlTableStringBuffer.append("<table border='1' cellspacing='1' cellpadding='1'>\n");
		// Alternative for full width: "<table border='1' width='100%' cellspacing='1' cellpadding='1'>\n");
	
		int rowcount = 0;
		
		htmlTableStringBuffer.append("<tr align='center'>");
		// itearate over columns
		for (int columnIndex = 0 ; columnIndex < columnNames.get(0).size(); columnIndex++) {
			if (columnColors.get(columnIndex) == null) {
				htmlTableStringBuffer.append("<td>");
			} else {
				htmlTableStringBuffer.append("<td bgcolor='").append(convertXlsColor2HtmlColor(columnColors.get(columnIndex))).append("'>");
			}
			
			htmlTableStringBuffer.append(StringEscapeUtils.escapeHtml4(columnNames.get(0).get(columnIndex)).replace(" ", "&nbsp;").replace("-", "&#8209;"));
			if (columnNames.size() > 1) {
				htmlTableStringBuffer.append("<br/>").append(StringEscapeUtils.escapeHtml4(columnNames.get(1).get(columnIndex)).replace(" ", "&nbsp;").replace("-", "&#8209;"));
			}
			htmlTableStringBuffer.append("</td>");
		}
		htmlTableStringBuffer.append("</tr>\n");
		
		// itearate over rows
		for (int lineIndex = 0; lineIndex < content.size(); lineIndex++) {
			if (lineColors.get(lineIndex) != null) {
				htmlTableStringBuffer.append("<tr align='right' bgcolor='").append(lineColors.get(lineIndex)).append("'>");
			} else if (rowcount % 2 == 0) {
				htmlTableStringBuffer.append("<tr align='right' bgcolor='cornflowerblue'>");
			} else {
				htmlTableStringBuffer.append("<tr align='right' bgcolor='lightblue'>");
			}
			
			// itearate over columns
			for (int columnIndex = 0; columnIndex < columnNames.getFirst().size(); columnIndex++) {
				// remove leading apostroph
				String cellContentString = content.get(lineIndex).get(columnIndex);
				if (cellContentString != null && cellContentString.startsWith("'") && !cellContentString.endsWith("'")) {
					cellContentString = cellContentString.substring(1);
				}
				htmlTableStringBuffer.append("<td align='").append(columnRightAligned.get(columnIndex) ? "right" : "left").append("'>").append(StringEscapeUtils.escapeHtml4(cellContentString).replace(" ", "&nbsp;").replace("-", "&#8209;")).append("</td>");
			}
			htmlTableStringBuffer.append("</tr>\n");

			rowcount++;
		}

		htmlTableStringBuffer.append("</table>\n");
		htmlTableStringBuffer.append("<br>\n");
		htmlTableStringBuffer.append("<br>\n");
	
		return htmlTableStringBuffer.toString();
	}
	
	private int getMaxSizeOfColumn(int index) {
		int maxSize = 0;
		for (List<String> line : columnNames) {
			maxSize = Math.max(line.get(index).length(), maxSize);
		}
		for (List<String> line : content) {
			maxSize = Math.max(line.get(index).length(), maxSize);
		}
		return maxSize;
	}
	
	public static String convertXlsColor2HtmlColor(String xlsColor) {
		if ("VERY_LIGHT_YELLOW".equalsIgnoreCase(xlsColor)) {
			return "yellow";
		} else if ("LIGHT_GREEN".equalsIgnoreCase(xlsColor)) {
			return "lime";
		} else if ("LAVENDER".equalsIgnoreCase(xlsColor)) {
			return "fuchsia";
		} else if ("TAN".equalsIgnoreCase(xlsColor)) {
			return "fuchsia";
		} else if ("TURQUOISE".equalsIgnoreCase(xlsColor)) {
			return "aqua";
		} else if ("YELLOW".equalsIgnoreCase(xlsColor)) {
			return "yellow";
		} else if ("LIGHT_TURQUOISE".equalsIgnoreCase(xlsColor)) {
			return "aqua";
		} else if ("LIGHT_ORANGE".equalsIgnoreCase(xlsColor)) {
			return "red";
		} else if ("ROSE".equalsIgnoreCase(xlsColor)) {
			return "fuchsia";
		} else if ("WOCHENENDE".equalsIgnoreCase(xlsColor)) {
			// special color
			return "coral";
		} else {
			return "white";
		}
	}
	
	public int getColumnCount() {
		return columnNames.getFirst().size();
	}
	
	public String getHeader(int headerLineIndex, int columnIndex) throws Exception {
		if (columnNames.size() <= headerLineIndex || columnNames.get(headerLineIndex).size() <= columnIndex) {
			throw new Exception("Invalid data query for column headings row index " + headerLineIndex + " column index " + columnIndex);
		}
		return columnNames.get(headerLineIndex).get(columnIndex);
	}
	
	public String getData(int lineIndex, int columnIndex) {
		if (content.size() <= lineIndex || content.get(lineIndex).size() <= columnIndex) {
			throw new IndexOutOfBoundsException("Invalid data query for row index " + lineIndex + " column index " + columnIndex);
		}
		return content.get(lineIndex).get(columnIndex);
	}

	public static String trimStringToLengthAlignedRight(String inputString, int length) {
		return String.format("%" + length + "s", inputString);
	}

	public static String trimStringToLengthAlignedLeft(String inputString, int length) {
		return String.format("%-" + length + "s", inputString);
	}
}
