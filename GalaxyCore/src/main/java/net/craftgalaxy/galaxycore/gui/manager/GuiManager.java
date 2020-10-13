package net.craftgalaxy.galaxycore.gui.manager;

import net.craftgalaxy.galaxycore.gui.GuiFolder;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

	private List<GuiFolder> folders;
	private static GuiManager instance;

	public GuiManager() {
		this.folders = new ArrayList<>();
	}

	public static void enable() {
		GuiManager.instance = new GuiManager();
	}

	public static void disable() {
		GuiManager.instance.folders.clear();
		GuiManager.instance = null;
	}

	public static GuiManager getInstance() {
		return instance;
	}

	public void addFolder(GuiFolder folder) {
		this.folders.add(folder);
	}

	public List<GuiFolder> getFolders() {
		return this.folders;
	}

	public void removeFolder(GuiFolder folder) {
		this.folders.remove(folder);
	}
}
