package org.insightech.er.db.sqltype;

import java.util.HashMap;
import java.util.Map;

import org.insightech.er.db.sqltype.SqlType.TypeKey;

public class CustomTypeInfo {
	private Map<String, Map<SqlType, String>> dbSqlTypeToAliasMap = new HashMap<String, Map<SqlType, String>>();
	private Map<String, Map<String, SqlType>> dbAliasToSqlTypeMap = new HashMap<String, Map<String, SqlType>>();
	private Map<String, Map<TypeKey, SqlType>> dbSqlTypeMap = new HashMap<String, Map<TypeKey, SqlType>>();

	public Map<String, Map<SqlType, String>> getDbSqlTypeToAliasMap() {
		return dbSqlTypeToAliasMap;
	}

	public Map<String, Map<String, SqlType>> getDbAliasToSqlTypeMap() {
		return dbAliasToSqlTypeMap;
	}

	public Map<String, Map<TypeKey, SqlType>> getDbSqlTypeMap() {
		return dbSqlTypeMap;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CustomTypeInfo) {
			CustomTypeInfo customTypeInfo = (CustomTypeInfo) obj;
			return this.dbSqlTypeToAliasMap.equals(customTypeInfo.getDbSqlTypeToAliasMap())
					&& this.dbAliasToSqlTypeMap.equals(customTypeInfo.getDbAliasToSqlTypeMap())
					&& this.dbSqlTypeMap.equals(customTypeInfo.getDbSqlTypeMap());
		}
		return super.equals(obj);
	}
}
