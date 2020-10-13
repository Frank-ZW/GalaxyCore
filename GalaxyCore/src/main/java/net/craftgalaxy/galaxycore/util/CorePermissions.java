package net.craftgalaxy.galaxycore.util;

public class CorePermissions {

	/*
	 * Class to handle the permission nodes for every command and bypass
	 * Much easier to have a set of static Strings represent the permissions
	 * instead of rewriting the permission itself.
	 */
	public static final String ALERTS_PERMISSION = "galaxycore.alerts";
	public static final String PINGS_PERMISSION = "galaxycore.pings";
	public static final String FREEZE_PERMISSION = "galaxycore.freeze";
	public static final String AUTHENTICATE_PERMISSION = "galaxycore.authenticate";
	public static final String SILENCE_CHAT_PERMISSION = "galaxycore.mutechat";
	public static final String SLOW_CHAT_PERMISSION = "galaxycore.slowchat";
	public static final String CHAT_FILTER_NOTIFICATION = "galaxycore.chat.notification";
	public static final String CHAT_FILTER_BYPASS = "galaxycore.chat.bypass";
	public static final String ADMIN_CHAT_PERMISSION = "galaxycore.adminchat";
	public static final String BOT_BYPASS_PERMISSION = "galaxycore.antibot.bypass";
	public static final String ALTS_PERMISSION = "galaxycore.alts";
	public static final String TROLL_PERMISSION = "galaxycore.troll";
	public static final String CLEAR_CHAT_PERMISSION = "galaxycore.clearchat";
	public static final String MINERS_PERMISSION = "galaxycore.miners";
	public static final String REPORT_NOTIFICATION = "galaxycore.report.notification";
	public static final String SHUTDOWN_PERMISSION = "galaxycore.shutdown";
	public static final String MANAGERS_PERMISSION = "galaxycore.managers";                 // Give this to founders and Darian and Cap specifically
	public static final String STAFF_TEAM_PERMISSION = "galaxycore.staffteam";              // Give this to staff team members
	public static final String ADDRESSES_PERMISSION = "galaxycore.addresses";
}
