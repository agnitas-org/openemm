/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;

public enum HtmlColor {
	BLACK("Black", 0x000000),
	NAVY("Navy", 0x000080),
	DARKBLUE("DarkBlue", 0x00008B),
	MEDIUMBLUE("MediumBlue", 0x0000CD),
	BLUE("Blue", 0x0000FF),
	DARKGREEN("DarkGreen", 0x006400),
	GREEN("Green", 0x008000),
	TEAL("Teal", 0x008080),
	DARKCYAN("DarkCyan", 0x008B8B),
	DEEPSKYBLUE("DeepSkyBlue", 0x00BFFF),
	DARKTURQUOISE("DarkTurquoise", 0x00CED1),
	MEDIUMSPRINGGREEN("MediumSpringGreen", 0x00FA9A),
	LIME("Lime", 0x00FF00),
	SPRINGGREEN("SpringGreen", 0x00FF7F),
	AQUA("Aqua", 0x00FFFF),
	CYAN("Cyan", 0x00FFFF),
	MIDNIGHTBLUE("MidnightBlue", 0x191970),
	DODGERBLUE("DodgerBlue", 0x1E90FF),
	LIGHTSEAGREEN("LightSeaGreen", 0x20B2AA),
	FORESTGREEN("ForestGreen", 0x228B22),
	SEAGREEN("SeaGreen", 0x2E8B57),
	DARKSLATEGRAY("DarkSlateGray", 0x2F4F4F),
	DARKSLATEGREY("DarkSlateGrey", 0x2F4F4F),
	LIMEGREEN("LimeGreen", 0x32CD32),
	MEDIUMSEAGREEN("MediumSeaGreen", 0x3CB371),
	TURQUOISE("Turquoise", 0x40E0D0),
	ROYALBLUE("RoyalBlue", 0x4169E1),
	STEELBLUE("SteelBlue", 0x4682B4),
	DARKSLATEBLUE("DarkSlateBlue", 0x483D8B),
	MEDIUMTURQUOISE("MediumTurquoise", 0x48D1CC),
	INDIGO("Indigo", 0x4B0082),
	DARKOLIVEGREEN("DarkOliveGreen", 0x556B2F),
	CADETBLUE("CadetBlue", 0x5F9EA0),
	CORNFLOWERBLUE("CornflowerBlue", 0x6495ED),
	REBECCAPURPLE("RebeccaPurple", 0x663399),
	MEDIUMAQUAMARINE("MediumAquaMarine", 0x66CDAA),
	DIMGRAY("DimGray", 0x696969),
	DIMGREY("DimGrey", 0x696969),
	SLATEBLUE("SlateBlue", 0x6A5ACD),
	OLIVEDRAB("OliveDrab", 0x6B8E23),
	SLATEGRAY("SlateGray", 0x708090),
	SLATEGREY("SlateGrey", 0x708090),
	LIGHTSLATEGRAY("LightSlateGray", 0x778899),
	LIGHTSLATEGREY("LightSlateGrey", 0x778899),
	MEDIUMSLATEBLUE("MediumSlateBlue", 0x7B68EE),
	LAWNGREEN("LawnGreen", 0x7CFC00),
	CHARTREUSE("Chartreuse", 0x7FFF00),
	AQUAMARINE("Aquamarine", 0x7FFFD4),
	MAROON("Maroon", 0x800000),
	PURPLE("Purple", 0x800080),
	OLIVE("Olive", 0x808000),
	GRAY("Gray", 0x808080),
	GREY("Grey", 0x808080),
	SKYBLUE("SkyBlue", 0x87CEEB),
	LIGHTSKYBLUE("LightSkyBlue", 0x87CEFA),
	BLUEVIOLET("BlueViolet", 0x8A2BE2),
	DARKRED("DarkRed", 0x8B0000),
	DARKMAGENTA("DarkMagenta", 0x8B008B),
	SADDLEBROWN("SaddleBrown", 0x8B4513),
	DARKSEAGREEN("DarkSeaGreen", 0x8FBC8F),
	LIGHTGREEN("LightGreen", 0x90EE90),
	MEDIUMPURPLE("MediumPurple", 0x9370DB),
	DARKVIOLET("DarkViolet", 0x9400D3),
	PALEGREEN("PaleGreen", 0x98FB98),
	DARKORCHID("DarkOrchid", 0x9932CC),
	YELLOWGREEN("YellowGreen", 0x9ACD32),
	SIENNA("Sienna", 0xA0522D),
	BROWN("Brown", 0xA52A2A),
	DARKGRAY("DarkGray", 0xA9A9A9),
	DARKGREY("DarkGrey", 0xA9A9A9),
	LIGHTBLUE("LightBlue", 0xADD8E6),
	GREENYELLOW("GreenYellow", 0xADFF2F),
	PALETURQUOISE("PaleTurquoise", 0xAFEEEE),
	LIGHTSTEELBLUE("LightSteelBlue", 0xB0C4DE),
	POWDERBLUE("PowderBlue", 0xB0E0E6),
	FIREBRICK("FireBrick", 0xB22222),
	DARKGOLDENROD("DarkGoldenRod", 0xB8860B),
	MEDIUMORCHID("MediumOrchid", 0xBA55D3),
	ROSYBROWN("RosyBrown", 0xBC8F8F),
	DARKKHAKI("DarkKhaki", 0xBDB76B),
	SILVER("Silver", 0xC0C0C0),
	MEDIUMVIOLETRED("MediumVioletRed", 0xC71585),
	INDIANRED("IndianRed", 0xCD5C5C),
	PERU("Peru", 0xCD853F),
	CHOCOLATE("Chocolate", 0xD2691E),
	TAN("Tan", 0xD2B48C),
	LIGHTGRAY("LightGray", 0xD3D3D3),
	LIGHTGREY("LightGrey", 0xD3D3D3),
	THISTLE("Thistle", 0xD8BFD8),
	ORCHID("Orchid", 0xDA70D6),
	GOLDENROD("GoldenRod", 0xDAA520),
	PALEVIOLETRED("PaleVioletRed", 0xDB7093),
	CRIMSON("Crimson", 0xDC143C),
	GAINSBORO("Gainsboro", 0xDCDCDC),
	PLUM("Plum", 0xDDA0DD),
	BURLYWOOD("BurlyWood", 0xDEB887),
	LIGHTCYAN("LightCyan", 0xE0FFFF),
	LAVENDER("Lavender", 0xE6E6FA),
	DARKSALMON("DarkSalmon", 0xE9967A),
	VIOLET("Violet", 0xEE82EE),
	PALEGOLDENROD("PaleGoldenRod", 0xEEE8AA),
	LIGHTCORAL("LightCoral", 0xF08080),
	KHAKI("Khaki", 0xF0E68C),
	ALICEBLUE("AliceBlue", 0xF0F8FF),
	HONEYDEW("HoneyDew", 0xF0FFF0),
	AZURE("Azure", 0xF0FFFF),
	SANDYBROWN("SandyBrown", 0xF4A460),
	WHEAT("Wheat", 0xF5DEB3),
	BEIGE("Beige", 0xF5F5DC),
	WHITESMOKE("WhiteSmoke", 0xF5F5F5),
	MINTCREAM("MintCream", 0xF5FFFA),
	GHOSTWHITE("GhostWhite", 0xF8F8FF),
	SALMON("Salmon", 0xFA8072),
	ANTIQUEWHITE("AntiqueWhite", 0xFAEBD7),
	LINEN("Linen", 0xFAF0E6),
	LIGHTGOLDENRODYELLOW("LightGoldenRodYellow", 0xFAFAD2),
	OLDLACE("OldLace", 0xFDF5E6),
	RED("Red", 0xFF0000),
	FUCHSIA("Fuchsia", 0xFF00FF),
	MAGENTA("Magenta", 0xFF00FF),
	DEEPPINK("DeepPink", 0xFF1493),
	ORANGERED("OrangeRed", 0xFF4500),
	TOMATO("Tomato", 0xFF6347),
	HOTPINK("HotPink", 0xFF69B4),
	CORAL("Coral", 0xFF7F50),
	DARKORANGE("DarkOrange", 0xFF8C00),
	LIGHTSALMON("LightSalmon", 0xFFA07A),
	ORANGE("Orange", 0xFFA500),
	LIGHTPINK("LightPink", 0xFFB6C1),
	PINK("Pink", 0xFFC0CB),
	GOLD("Gold", 0xFFD700),
	PEACHPUFF("PeachPuff", 0xFFDAB9),
	NAVAJOWHITE("NavajoWhite", 0xFFDEAD),
	MOCCASIN("Moccasin", 0xFFE4B5),
	BISQUE("Bisque", 0xFFE4C4),
	MISTYROSE("MistyRose", 0xFFE4E1),
	BLANCHEDALMOND("BlanchedAlmond", 0xFFEBCD),
	PAPAYAWHIP("PapayaWhip", 0xFFEFD5),
	LAVENDERBLUSH("LavenderBlush", 0xFFF0F5),
	SEASHELL("SeaShell", 0xFFF5EE),
	CORNSILK("Cornsilk", 0xFFF8DC),
	LEMONCHIFFON("LemonChiffon", 0xFFFACD),
	FLORALWHITE("FloralWhite", 0xFFFAF0),
	SNOW("Snow", 0xFFFAFA),
	YELLOW("Yellow", 0xFFFF00),
	LIGHTYELLOW("LightYellow", 0xFFFFE0),
	IVORY("Ivory", 0xFFFFF0),
	WHITE("White", 0xFFFFFF),
	
	SCHWARZ("Schwarz", 0x000000),
	WEIß("Weiß", 0xFFFFFF),
	WEISS("Weiss", 0xFFFFFF);
	
	private String name;
	
	private int rgbHexCode;
	
	public String getName() {
		return name;
	}
	
	public int getColorCode() {
		return rgbHexCode;
	}
	
	public Color getColor() {
		return new Color(rgbHexCode);
	}
		
	private HtmlColor(String name, int rgbHexCode) {
		this.name = name;
		this.rgbHexCode = rgbHexCode;
	}
	
	public static Color getColorByCodeOrName(String colorCodeOrName) {
		if (StringUtils.isNotBlank(colorCodeOrName)) {
			colorCodeOrName = colorCodeOrName.trim();
			HtmlColor htmlColor;
			try {
				// Do NOT iterate through colorCodeOrName.values() to improve performance by direct pick
				htmlColor = HtmlColor.valueOf(colorCodeOrName.toUpperCase());
				if (htmlColor != null) {
					return htmlColor.getColor();
				} else {
					return null;
				}
			} catch (IllegalArgumentException e) {
				return new Color(Integer.parseInt(colorCodeOrName, 16));
			}
		} else {
			return null;
		}
	}
}
