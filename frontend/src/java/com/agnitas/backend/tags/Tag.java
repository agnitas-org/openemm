package com.agnitas.backend.tags;

import	java.util.List;
import	java.util.Map;
import	java.util.Set;
import	com.agnitas.backend.Data;
import	com.agnitas.backend.Custinfo;
import	com.agnitas.backend.EMMTag;

public abstract class Tag {
	static public boolean has (String name) {
		return false;
	}
	static public Tag get (String name) {
		return null;
	}
	abstract public void setup (Data nData, EMMTag nReference, String nSpecification, String nName, Map <String, String> nParameters);
	abstract public String name ();
	abstract public void value (String nValue);
	abstract public String value ();
	abstract public boolean isGlobalValue ();
	abstract public boolean isFixedValue ();
	abstract public void initialize () throws TagException;
	abstract public void collectAllowedParameters (List <String> collect);
	abstract public void requestFields (Set <String> predef);
	abstract public void makeValue (Custinfo cinfo);
}
