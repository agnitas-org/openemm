package com.agnitas.emm.core.mediatypes.common;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import com.agnitas.emm.core.Permission;

/**
 * Enumeration of all available media types.
 */
public enum MediaTypes {

	/** Email. */
	EMAIL(0, null, Permission.MEDIATYPE_EMAIL, 0, "Text", "email", "agnText", "agnHtml");

	/** Code for media type. Used by DB. */
	private final int code;
	
	/** Profile field assigned to this media type. */
	private final String assignedProfileField;
	
	/** Priority for default type. */
	private final int defaultTypePriority;
	
	/** List of component names for the media type. */
	private final String[] componentNames;
	
	/** Required permission for this media type. */
	private final Permission requiredPermission;
	
	private final String key;

	/** Identifier used in webhook messages. */
	private final String webhookIdentifier;

	/**
	 * Creates enum item.
	 * 
	 * A profile field can be assigned to a media type. If assigned (not <code>null</code>), this profile field can be used for different operations.
	 * For example, the profile field will be checked to be not empty when setting the binding status.
	 * 
	 * @param profileField name of profile field assigned to this media type or <code>null</code>
	 * @param code code of media type
	 * @param componentNames names of components of media type
	 */
	MediaTypes(final int code, final String profileField, final Permission requiredPermission, final int defaultTypePriority, final String key, final String webhookIdentifier, final String... componentNames) {
		this.code = code;
		this.assignedProfileField = profileField;
		this.defaultTypePriority = defaultTypePriority;
		this.requiredPermission = Objects.requireNonNull(requiredPermission, "Required permission is null");
		this.key = key;
		this.webhookIdentifier = Objects.requireNonNull(webhookIdentifier, "webhook identifier");
		this.componentNames = Arrays.copyOf(componentNames, componentNames.length);
	}
	
	public static final MediaTypes[] valuesSortedByDefaultValuePriority() {
		final MediaTypes[] array = MediaTypes.values();
		
		Arrays.sort(array, (a,b) -> a.defaultTypePriority - b.defaultTypePriority);
		
		return array;
	}
	
	/**
	 * Returns the code of the media type.
	 * 
	 * @return code of the media type
	 */
	public int getMediaCode() {
		return code;
	}
	
	/**
	 * Returns the media type for a given code or <code>null</code> if code is unknown.
	 * 
	 * @param code code
	 * 
	 * @return media type for code or <code>null</code>
	 */
	public static MediaTypes getMediaTypeForCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (MediaTypes type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Checks, if given name is name of a component of the media type.
	 * Check is case-sensitive.
	 * 
	 * @param componentName component name
	 * 
	 * @return <code>true</code> if name is component name of media type
	 */
	public boolean isComponentNameForMediaType(String componentName) {
		for (String name : this.componentNames) {
			if (name.equals(componentName)) {
				return true;
			}
		}
		return false;
	}
	
	public final String[] getComponentNames() {
		return Arrays.copyOf(this.componentNames, this.componentNames.length);
	}
	
	public static MediaTypes findMediatypeForComponentName(String name) {
		for (MediaTypes type : values()) {
			if (type.isComponentNameForMediaType(name)) {
				return type;
			}
		}
		return null;
	}
	
	public static MediaTypes getMediatypeByName(String name) {
		for (MediaTypes type : values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid MediaTypes name: " + name);
	}
	
	public final Permission getRequiredPermission() {
		return this.requiredPermission;
	}
	
	/**
	 * Returns the profile field assigned to this media type or <code>null</code> if no profile field is assigned.
	 * 
	 * @return assigned profile field or <code>null</code>
	 */
	public final String getAssignedProfileField() {
		return this.assignedProfileField;
	}

	public final String getKey() {
		return key;
	}

	/**
	 * Returns the identifier used in webhook messages.
	 *
	 * @return identifier used in webhook messages
	 */
	public final String getWebhookIdentifier() {
		return this.webhookIdentifier;
	}
}
