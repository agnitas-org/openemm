/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.agnitas.backend.dao.TagDAO;
import org.agnitas.util.Log;

public class Code {
	private static final String DEFAULT_LINK_CHARSET = "UTF-8";
	/**
	 * Reference to global configuration
	 */
	private Data data;
	/**
	 * Name of the code segment
	 */
	private String name;
	/**
	 * Language of code
	 */
	private String lang;
	/**
	 * The code itself
	 */
	private String code;
	/**
	 * List of requested columns
	 */
	private List<String> requestedColumns;
	/**
	 * List of requested vouchers
	 */
	private List<String> requestedVouchers;
	/**
	 * List of required parameters
	 */
	private List<String> requiredParameter;
	/**
	 * List of requested links
	 */
	private List<String> requestedLinks;
	/**
	 * Charset for parameters for links
	 */
	private String requestedLinksCharset;
	/**
	 * Reference definitions
	 */
	private List<String> requestedReferences;
	/**
	 * if this entry is valid, e.g. all variables are set
	 */
	private boolean valid;
	/**
	 * keep track of missing entries
	 */
	private List<String> missing;

	/**
	 * Constructor for storing the code for a scripted tag
	 *
	 * @param codeName the unique name for this code
	 */
	public Code(Data nData, String codeName) {
		data = nData;
		name = codeName;
		lang = null;
		code = null;
		requestedColumns = null;
		requestedVouchers = null;
		requiredParameter = null;
		requestedLinks = null;
		requestedLinksCharset = null;
		requestedReferences = null;
		valid = false;
		missing = new ArrayList<>();
	}

	/**
	 * Retreive and parse the code from the database
	 *
	 * @return true, on success, false otherwise
	 */
	public boolean retrieveCode() throws SQLException {
		if (!valid) {
			valid = fetchCodeFromDatabase();
			if (valid) {
				parseCodeForConfiguration();
			}
		}
		return valid;
	}

	private static Pattern validVoucher = Pattern.compile("^[a-z][a-z0-9_]*$", Pattern.CASE_INSENSITIVE);

	/**
	 * Find all referenced database related stuff and the parameter
	 * values for the calling agnTag.
	 *
	 * @param predef the referenced database columns
	 * @param parms  the default value for the agnTag paramtere
	 * @throws Exception
	 */
	public void requestFields(Set<String> predef, Map<String, String> parameter) throws Exception {
		resolveReferenceParameter(parameter);
		resolveDatabaseColumns(predef, parameter);
		resolveVouchers(predef, parameter);
		validateRequiredParameter(parameter);
		resolveLinks(parameter);
	}

	public boolean isValid() {
		return valid;
	}

	public String getCode() {
		return valid ? code : null;
	}

	public String getLanguage() {
		return valid ? lang : null;
	}

	public String getName() {
		return name;
	}

	private boolean fetchCodeFromDatabase() throws SQLException {
		TagDAO.Function function = data.getTagFunction(name);

		if (function != null) {
			lang = function.lang();
			code = function.code();
		} else {
			data.logging(Log.WARNING, "code", "No function for " + name + " found");
		}
		return function != null;
	}

	private static Pattern optPattern = Pattern.compile("^[^a-zA-Z0-9\\\\]*([a-zA-Z_][a-zA-Z0-9_]*):[ \t]*(.+);[ \t]*[\r\n]*");

	private void parseCodeForConfiguration() {
		while (true) {
			Matcher m = optPattern.matcher(code);

			if (m.lookingAt()) {
				String opt = m.group(1);
				String param = m.group(2).trim();

				switch (opt) {
					case "db":
						parseDatabaseRequirements(param);
						break;
					case "voucher":
						parseVoucherRequirements(param);
						break;
					case "link":
						parseLinkToMeassure(param);
						break;
					case "linkcharset":
						parseLinkCharset(param);
						break;
					case "require":
						parseRequiredParameter(param);
						break;
					case "ref":
						parseReferenceDefinition(param);
						break;
					case "comment":
					case "coding":
						break;
					default:
						data.logging(Log.WARNING, "code", "Unknown option \"" + opt + "\" with paramater \"" + param + "\" found in " + name);
						break;
				}
				code = code.substring(m.end());
			} else {
				break;
			}
		}
	}

	private void parseDatabaseRequirements(String param) {
		requestedColumns = splitList(param, requestedColumns);
	}

	private void resolveDatabaseColumns(Set<String> predef, Map<String, String> parameter) throws Exception {
		if (requestedColumns != null) {
			for (String scolumn : requestedColumns) {
				String column = data.substituteString(scolumn, parameter, missing);
				boolean optional = false;
				String[] columns;

				showMissing("column", scolumn);
				if (column.endsWith("?")) {
					optional = true;
					column = column.substring(0, column.length() - 1).trim();
				}
				columns = resolveParameter(column, parameter, "database column", optional);
				if (columns != null) {
					for (String c : columns) {
						c = c.trim();
						if ((c.length() > 2) && c.startsWith("/") && c.endsWith("/")) {
							Pattern cpat;
							String cname, ref;
							int o;
							Reference rmatch;

							c = c.substring(1, c.length() - 1);
							o = c.indexOf('.');
							rmatch = null;
							if (o != -1) {
								ref = c.substring(0, o);
								cname = c.substring(o + 1);
								if (ref.length() > 0) {
									for (String rname : data.references.keySet()) {
										if (ref.equalsIgnoreCase(rname)) {
											rmatch = data.references.get(rname);
											if (data.fullfillReference(rmatch)) {
												break;
											}
										}
									}
									if (rmatch == null) {
										throw new Exception("Unknown reference table in \"" + c + "\" requested.");
									}
								} else {
									ref = null;
								}
							} else {
								ref = null;
								cname = c;
							}
							try {
								cpat = Pattern.compile(cname, Pattern.CASE_INSENSITIVE);
							} catch (PatternSyntaxException e) {
								throw new Exception("Invalid pattern for requested columns \"" + c + "\"", e);
							}

							int count = 0;

							for (Column col : data.layout) {
								String cref = col.getRef();

								if (((ref == null) && (cref == null)) || ((ref != null) && (cref != null) && ref.equalsIgnoreCase(cref))) {
									Matcher mt = cpat.matcher(col.getName());

									if (mt.matches()) {
										predef.add(col.getQname());
										++count;
									}
								}
							}
							if ((!optional) && (count == 0)) {
								throw new Exception("Referenced column wildcard \"" + c + "\" in \"" + column + "\" not found in database");
							}
						} else if (c.length() > 0) {
							if (data.columnByName(c) == null) {
								throw new Exception("Referenced column \"" + c + "\" in \"" + column + "\" not found in database");
							}
							predef.add(c.toLowerCase());
						}
					}
				}
			}
		}
	}

	private void parseVoucherRequirements(String param) {
		requestedVouchers = splitList(param, requestedVouchers);
	}

	private void resolveVouchers(Set<String> predef, Map<String, String> parameter) throws Exception {
		if (requestedVouchers != null) {
			for (String svoucher : requestedVouchers) {
				String voucher = data.substituteString(svoucher, parameter, missing);
				boolean optional = false;
				String[] vouchers;

				showMissing("voucher", svoucher);
				if (voucher.endsWith("?")) {
					optional = true;
					voucher = voucher.substring(0, voucher.length() - 1).trim();
				}
				vouchers = resolveParameter(voucher, parameter, "voucher", optional);
				if (vouchers != null) {
					for (String v : vouchers) {
						v = v.trim();
						if (v.length() > 0) {
							Matcher mt = validVoucher.matcher(v);

							if (!mt.matches()) {
								throw new Exception("Voucher \"" + v + "\" is invalid");
							}
							String column = "vc_" + v + ".voucher_code";
							if (data.columnByName(column) == null) {
								throw new Exception("Voucher \"" + v + "\" not found");
							}
							predef.add(column.toLowerCase());
						}
					}
				}
			}
		}
	}

	private void parseLinkToMeassure(String param) {
		requestedLinks = addElement(param, requestedLinks);
	}

	private void parseLinkCharset(String param) {
		if (param.length() > 0) {
			requestedLinksCharset = param;
		} else {
			requestedLinksCharset = null;
		}
	}

	private void resolveLinks(Map<String, String> parameter) throws Exception {
		if (valid && (requestedLinks != null)) {
			Map<String, String> urlparameter = new HashMap<>(parameter);

			if (requestedLinksCharset != null) {
				String rlc = data.substituteString(requestedLinksCharset, parameter, missing);

				showMissing("character set for link", requestedLinksCharset);
				requestedLinksCharset = rlc;
				if (requestedLinksCharset.startsWith(">")) {
					String parm = requestedLinksCharset.substring(1).trim();

					requestedLinksCharset = parameter.get(parm);
					if (requestedLinksCharset == null) {
						throw new Exception("Missing parameter link character set: " + parm);
					}
					rlc = data.substituteString(requestedLinksCharset, parameter, missing);
					showMissing("character set for indirect link", requestedLinksCharset);
					requestedLinksCharset = rlc;
				}
			}
			for (Map.Entry<String, String> kv : parameter.entrySet()) {
				String key = kv.getKey();
				String value = kv.getValue();
				String coded;

				try {
					coded = URLEncoder.encode(value, requestedLinksCharset != null ? requestedLinksCharset : DEFAULT_LINK_CHARSET);
				} catch (java.io.UnsupportedEncodingException e) {
					coded = value;
				}
				urlparameter.put(key + "[url]", coded);
			}
			for (String requestedLink : requestedLinks) {
				String[] parts = requestedLink.split("[ \t]+", 2);
				String link = parts[0];
				String comment = parts.length == 2 ? parts[1] : name + ": auto generated";
				boolean isAdminLink = false;
				boolean useLink = true;

				for (int state = 0; state < 2; ++state) {
					String check = null;
					boolean match = false;
					boolean invert = false;

					switch (state) {
						case 0:
							check = "!";
							break;
						case 1:
							check = "?";
							break;
						default:
							throw new Exception("Invalid state");
					}
					if (link.startsWith(check)) {
						link = link.substring(1).trim();
						if (link.startsWith("[")) {
							int end = link.indexOf("]");

							if (end > 1) {
								String cond = link.substring(1, end);

								if (cond.startsWith("!")) {
									invert = true;
									cond = cond.substring(1);
								}

								int equal = cond.indexOf("=");

								if (equal > 0) {
									String var = cond.substring(0, equal).trim();
									String val = cond.substring(equal + 1).trim();
									String pval = parameter.get(var);

									if (pval != null) {
										match = pval.equals(val);
									}
								} else {
									if (cond.startsWith("?")) {
										cond = cond.substring(1);
										match = parameter.containsKey(cond.trim());
									} else {
										String pval = parameter.get(cond.trim());

										if (pval != null) {
											if (pval.equals("")) {
												match = true;
											} else {
												match = StringOps.atob(pval, false);
											}
										}
									}
								}
								link = link.substring(end + 1).trim();
							}
						} else {
							match = true;
						}
						if (invert) {
							match = !match;
						}
						switch (state) {
							case 0:
								isAdminLink = match;
								break;
							case 1:
								useLink = match;
								break;
							default:
								break;
						}
					}
				}
				if (useLink) {
					link = data.substituteString(link, urlparameter, missing);
					showMissing("link", link);
					if (link.startsWith(">")) {
						String parm = link.substring(1).trim();

						link = parameter.get(parm);
						if (link == null) {
							throw new Exception("Missing parameter \"" + parm + "\" (expected URL)");
						}
						link = data.substituteString(link, urlparameter, missing);
						showMissing("indirect link", link);
					}
					if (data.requestURL(link, data.substituteString(comment, parameter), isAdminLink) == null) {
						throw new Exception("Required link \"" + link + "\" cannot be used");
					}
				}
			}
		}
	}

	private void parseRequiredParameter(String param) {
		requiredParameter = splitList(param, requiredParameter);
	}

	private void validateRequiredParameter(Map<String, String> parameter) throws Exception {
		if (requiredParameter != null) {
			for (String require : requiredParameter) {
				if (!parameter.containsKey(require)) {
					throw new Exception("Missing required parameter \"" + require + "\"");
				}
			}
		}
	}

	private void parseReferenceDefinition(String param) {
		requestedReferences = addElement(param, requestedReferences);
	}

	private void resolveReferenceParameter(Map<String, String> parameter) throws Exception {
		if (requestedReferences != null) {
			for (String ref : requestedReferences) {
				String sref = data.substituteString(ref, parameter, missing);

				showMissing("reference table", ref);
				if (!data.addReference(sref, false)) {
					throw new Exception("Invalid reference requested: " + sref + " (coming from " + ref + ")");
				}
			}
		}
	}

	private List<String> splitList(String str, List<String> target) {
		for (String element : str.split(",")) {
			element = element.trim();
			if (element.length() > 0) {
				if (target == null) {
					target = new ArrayList<>();
				}
				target.add(element);
			}
		}
		return target;
	}

	private List<String> addElement(String str, List<String> target) {
		str = str.trim();
		if (str.length() > 0) {
			if (target == null) {
				target = new ArrayList<>();
			}
			target.add(str);
		}
		return target;
	}

	private String[] resolveParameter(String element, Map<String, String> parameter, String ident, boolean optional) throws Exception {
		String[] elements;

		if (element.startsWith(">")) {
			String param = element.substring(1).trim();

			element = parameter.get(param);
			if (element == null) {
				if (!optional) {
					throw new Exception(StringOps.fill("Missing parameter \"${param}\" (expected ${ident})", "param", param, "ident", ident));
				}
				elements = null;
			} else {
				elements = data.substituteString(element, parameter, missing).split(",");
				showMissing("indirect " + ident, element);
			}
		} else {
			elements = new String[1];
			elements[0] = element;
		}
		return elements;
	}

	private void showMissing(String... what) throws Exception {
		if (missing.size() > 0) {
			throw new Exception("Missing parameter" + (missing.size() > 1 ? "s " : " ") + missing.stream().map(e -> "\"" + e + "\"").reduce((s, e) -> s + ", " + e).orElse("") + " in " + Arrays.stream(what).reduce((s, e) -> s + " " + e).orElse("unspecified"));
		}
	}
}
