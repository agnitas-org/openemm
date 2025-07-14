package com.agnitas.backend;

import static com.agnitas.util.ImageUtils.MOBILE_IMAGE_PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.util.Log;

/**
 * class to keep track of all available images
 */
public class Imagepool {
	public final static String	MAILING = "mailing";
	public final static String	MEDIAPOOL = "mediapool";
	public final static String	MEDIAPOOL_BACKGROUND = "mediapoolbg";

	private Data			data;
	private Set <String>		imageNames;
	private Map <String, Image>	inUse;

	public Imagepool (Data nData) {
		data = nData;
		imageNames = new HashSet <> ();
		inUse = new HashMap <> ();
	}

	public void addImage (BlockData block, boolean use) {
		if ((block != null) &&
		    (block.type == BlockData.RELATED_BINARY) &&
		    (block.cid != null) &&
		    block.isImage) {
			imageNames.add (block.cid);
			if (use) {
				markInUse (block.cid, MAILING, new Image (block.id, block.cid, block.cid, null), block.cidEmit);
			}
		}
	}

	public String findSourceFor (String name) {
		data.logging(Log.DEBUG, "imagepool", "Default implementation, ignore name parameter!");
		return MAILING;
	}

	public Image findImage(String name, String filename, boolean isMobile) {
		if (isMobile) {
			String mobileName = MOBILE_IMAGE_PREFIX + name;
			if (imageNames.contains(mobileName)) {
				return new Image(0, mobileName, MOBILE_IMAGE_PREFIX + filename, null);
			}
		}

		return new Image(0, name, filename, null);
	}

	public Image findMediapoolImage (String name, String filename, boolean isMobile) {
	        data.logging(Log.DEBUG, "imagepool", "Default implementation, ignore 'name', 'filename' and 'isMobile' parameter!");
		return null;
	}

	public void markInUse (String name, String source, Image image, String link) {
		image.link (link);
		inUse.put (source + ":" + name, image);
	}

	public List<BlockData> getMediapoolImagesUsed () {
        data.logging(Log.DEBUG, "imagepool", "Default implementation!");
		return new ArrayList<>();
	}

	public Set <String> getComponentsUsed () {
		return inUse
			.keySet ()
			.stream ()
			.map (k -> k.split (":", 2))
			.filter (id -> id[0].equals (MAILING))
			.map (id -> id[1])
			.collect (HashSet::new, HashSet::add, HashSet::addAll);
	}
}
