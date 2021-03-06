package com.alessiodp.oreannouncer.common.storage.sql;

import com.alessiodp.core.common.configuration.Constants;
import com.alessiodp.core.common.storage.sql.ISQLTable;
import com.alessiodp.oreannouncer.common.configuration.data.ConfigMain;
import com.google.common.io.ByteStreams;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SQLTable implements ISQLTable {
	PLAYERS, BLOCKS, VERSIONS;
	// VERSIONS must be first, it needs to be created before others.
	
	@Getter private String tableName;
	@Getter private String createQuery;
	private static int version;
	private static String varcharSize;
	private static String charset;
	
	SQLTable() {
	}
	
	public static void setupTables(int ver, InputStream schema) {
		version = ver;
		varcharSize = Integer.toString(ConfigMain.STORAGE_SETTINGS_GENERAL_SQL_VARCHARSIZE);
		charset = ConfigMain.STORAGE_SETTINGS_MYSQL_CHARSET;
		
		PLAYERS.tableName = ConfigMain.STORAGE_SETTINGS_GENERAL_SQL_TABLES_PLAYERS;
		BLOCKS.tableName = ConfigMain.STORAGE_SETTINGS_GENERAL_SQL_TABLES_BLOCKS;
		VERSIONS.tableName = ConfigMain.STORAGE_SETTINGS_GENERAL_SQL_TABLES_VERSIONS;
		
		parseSchema(schema);
	}
	
	public static SQLTable getExactEnum(String str) {
		SQLTable ret = null;
		if (str != null && !str.isEmpty()) {
			switch (str.toLowerCase()) {
				case "players":
					ret = PLAYERS;
					break;
				case "blocks":
					ret = BLOCKS;
					break;
				case "versions":
					ret = VERSIONS;
					break;
				default:
					ret = null;
					break;
			}
		}
		return ret;
	}
	
	@Override
	public String getTypeName() {
		return this.name();
	}
	
	@Override
	public int getVersion() {
		return version;
	}
	
	@Override
	public String formatQuery(String query) {
		return SQLTable.formatGenericQuery(query);
	}
	
	public static String formatGenericQuery(String query) {
		return query
				.replace("{table_players}", PLAYERS.tableName)
				.replace("{table_blocks}", BLOCKS.tableName)
				.replace("{table_versions}", VERSIONS.tableName)
				.replace("{charset}", charset)
				.replace("{version}", Integer.toString(version))
				.replace("{varcharsize}", varcharSize);
	}
	
	public static void parseSchema(InputStream schema) {
		try {
			byte[] data = ByteStreams.toByteArray(schema);
			String dataString = new String(data, StandardCharsets.UTF_8);
			
			Matcher m = Pattern
					.compile(Constants.DATABASE_SCHEMA_DIVIDER, Pattern.CASE_INSENSITIVE)
					.matcher(dataString);
			while (m.find()) {
				SQLTable table = SQLTable.getExactEnum(m.group(1));
				if (table != null) {
					table.createQuery = m.group(2);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
