package net.fabricmc.installer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HardcodedMetaHandler extends MetaHandler {

	private final List<GameVersion> versions = new ArrayList<>();
	
	public HardcodedMetaHandler() {
		super(null);
	}
	
	public List<GameVersion> getVersions() {
		return Collections.unmodifiableList(versions);
	}
	
	public HardcodedMetaHandler addVersion(String version, boolean stable) {
		versions.add(new GameVersion(version, stable));
		return this;
	}
	
	public void load() {
		complete(versions);
	}
	
	public GameVersion getLatestVersion(boolean snapshot){
		return versions.get(0); // cursed legacy doesn't exactly need snapshot logic.
	}
}
