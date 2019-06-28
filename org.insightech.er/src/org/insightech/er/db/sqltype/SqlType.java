package org.insightech.er.db.sqltype;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.insightech.er.db.impl.oracle.OracleDBManager;

public class SqlType implements Serializable {
	private static final long serialVersionUID = -8273043043893517634L;

	public static final String SQL_TYPE_ID_SERIAL = "serial";

	public static final String SQL_TYPE_ID_BIG_SERIAL = "bigserial";

	public static final String SQL_TYPE_ID_INTEGER = "integer";

	public static final String SQL_TYPE_ID_BIG_INT = "bigint";

	public static final String SQL_TYPE_ID_CHAR = "character";

	public static final String SQL_TYPE_ID_VARCHAR = "varchar";

	private static final Pattern NEED_LENGTH_PATTERN = Pattern
			.compile(".+\\([a-zA-Z][,\\)].*");

	private static final Pattern NEED_DECIMAL_PATTERN1 = Pattern
			.compile(".+\\([a-zA-Z],[a-zA-Z]\\)");

	private static final Pattern NEED_DECIMAL_PATTERN2 = Pattern
			.compile(".+\\([a-zA-Z]\\).*\\([a-zA-Z]\\)");

	private String name;

	private Class javaClass;

	private boolean needArgs;

	boolean fullTextIndexable;

	private static Map<String, Map<TypeKey, SqlType>> dbSqlTypeMap = new HashMap<String, Map<TypeKey, SqlType>>();

	private static Map<String, Map<SqlType, String>> dbSqlTypeToAliasMap = new HashMap<String, Map<SqlType, String>>();

	private static Map<String, Map<String, SqlType>> dbAliasToSqlTypeMap = new HashMap<String, Map<String, SqlType>>();

	private static Map<String, CustomTypeInfo> customTypeInfoMap = new HashMap<String, CustomTypeInfo>();

	static {
		try {
			SqlTypeFactory.load();

		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError(e);
		}
	}

	public static class TypeKey {
		private String alias;

		private int size;

		private int decimal;

		public TypeKey(String alias, int size, int decimal) {
			if (alias != null) {
				alias = alias.toUpperCase();
			}

			this.alias = alias;

			if (size == Integer.MAX_VALUE) {
				this.size = 0;
			} else {
				this.size = size;
			}

			this.decimal = decimal;
		}

		@Override
		public boolean equals(Object obj) {
			TypeKey other = (TypeKey) obj;

			if (this.alias == null) {
				if (other.alias == null) {
					if (this.size == other.size
							&& this.decimal == other.decimal) {
						return true;
					}
					return false;

				} else {
					return false;
				}

			} else {
				if (this.alias.equals(other.alias) && this.size == other.size
						&& this.decimal == other.decimal) {
					return true;
				}
			}

			return false;
		}

		@Override
		public int hashCode() {
			if (this.alias == null) {
				return (this.size * 10) + this.decimal;
			}
			return (this.alias.hashCode() * 100) + (this.size * 10)
					+ this.decimal;
		}

		@Override
		public String toString() {
			return "TypeKey [alias=" + alias + ", size=" + size + ", decimal="
					+ decimal + "]";
		}

	}

	public SqlType(String name, Class javaClass, boolean needArgs,
			boolean fullTextIndexable,String customTypeFile) {
		this.name = name;
		this.javaClass = javaClass;
		this.needArgs = needArgs;
		this.fullTextIndexable = fullTextIndexable;
	}

	public static void setDBAliasMap(
			Map<String, Map<SqlType, String>> dbSqlTypeToAliasMap,
			Map<String, Map<String, SqlType>> dbAliasToSqlTypeMap,
			Map<String, Map<TypeKey, SqlType>> dbSqlTypeMap) {
		SqlType.dbSqlTypeMap = dbSqlTypeMap;
		SqlType.dbSqlTypeToAliasMap = dbSqlTypeToAliasMap;
		SqlType.dbAliasToSqlTypeMap = dbAliasToSqlTypeMap;
	}

	public static void setCustomTypeInfoMap(Map<String, CustomTypeInfo> customTypeInfoMap) {
		SqlType.customTypeInfoMap = customTypeInfoMap;
	}

	public static Map<String, CustomTypeInfo> getCustomTypeInfoMap() {
		return SqlType.customTypeInfoMap;
	}

	public String getId() {
		return this.name;
	}

	public Class getJavaClass() {
		return this.javaClass;
	}

	public boolean doesNeedArgs() {
		return this.needArgs;
	}

	public boolean isFullTextIndexable() {
		return this.fullTextIndexable;
	}

	public static SqlType valueOf(String database, String[] customTypes, String alias) {
		SqlType sqlType = dbAliasToSqlTypeMap.get(database).get(alias);
		if (customTypes != null) {
			for (String customType : customTypes) {
				Map<String, SqlType> customAliasToSqlTypeMap = getCustomAliasToSqlTypeMap(database, customType);
				if (customAliasToSqlTypeMap != null && customAliasToSqlTypeMap.containsKey(alias)) {
					sqlType = customAliasToSqlTypeMap.get(alias);
				}
			}
		}
		return sqlType;
	}

	public static SqlType valueOf(String database,String[] customTypes ,String alias, int size,
			int decimal) {
		if (alias == null) {
			return null;
		}

		Map<TypeKey, SqlType> sqlTypeMap=new HashMap<SqlType.TypeKey, SqlType>();
		sqlTypeMap.putAll(dbSqlTypeMap.get(database));

		if (customTypes != null) {
			for (String customType : customTypes) {
				sqlTypeMap.putAll(getCustomSqlTypeMap(database, customType));
			}
		}


		TypeKey typeKey = new TypeKey(alias, size, decimal);
		SqlType sqlType = sqlTypeMap.get(typeKey);

		if (sqlType != null) {
			return sqlType;
		}

		if (decimal > 0) {
			decimal = -1;

			typeKey = new TypeKey(alias, size, decimal);
			sqlType = sqlTypeMap.get(typeKey);

			if (sqlType != null) {
				return sqlType;
			}
		}

		if (size > 0) {
			size = -1;

			typeKey = new TypeKey(alias, size, decimal);
			sqlType = sqlTypeMap.get(typeKey);

			if (sqlType != null) {
				return sqlType;
			}
		}

		typeKey = new TypeKey(alias, 0, 0);
		sqlType = sqlTypeMap.get(typeKey);

		return sqlType;
	}

	private static Map<TypeKey, SqlType> getCustomSqlTypeMap(String database, String customType) {
		CustomTypeInfo customTypeInfo = customTypeInfoMap.get(customType);
		if (customTypeInfo != null) {
			return customTypeInfo.getDbSqlTypeMap().get(database);
		}
		return new HashMap<SqlType.TypeKey, SqlType>();
	}

	private static Map<String, SqlType> getCustomAliasToSqlTypeMap(String database, String customType) {
		CustomTypeInfo customTypeInfo = customTypeInfoMap.get(customType);
		if (customTypeInfo != null) {
			return customTypeInfo.getDbAliasToSqlTypeMap().get(database);
		}
		return new HashMap<String, SqlType>();
	}

	private static Map<SqlType, String> getCustomSqlTypeToAliasMap(String database, String customType) {
		CustomTypeInfo customTypeInfo = customTypeInfoMap.get(customType);
		if (customTypeInfo != null) {
			return customTypeInfo.getDbSqlTypeToAliasMap().get(database);
		}
		return new HashMap<SqlType, String>();
	}

	public static SqlType valueOfId(String id, String database, String[] customTypes) {
		SqlType sqlType = null;

		if (id == null) {
			return null;
		}

		Map<SqlType, String> aliasMap = dbSqlTypeToAliasMap.get(database);
		if(aliasMap!=null) {
			for (SqlType type : aliasMap.keySet()) {
				if (id.equals(type.getId())) {
					sqlType = type;
				}
			}
		}

		if (customTypes != null) {
			for (String customType : customTypes) {
				Map<SqlType, String> customSqlTypeToAliasMap = getCustomSqlTypeToAliasMap(database, customType);
				if (customSqlTypeToAliasMap != null) {
					for (SqlType type : customSqlTypeToAliasMap.keySet()) {
						if (id.equals(type.getId())) {
							sqlType = type;
						}
					}
				}
			}
		}

		return sqlType;
	}

	public static SqlType valueOfId(String id) {
		SqlType sqlType = null;

		if (id == null) {
			return null;
		}

		for (Map<SqlType, String> aliasMap : dbSqlTypeToAliasMap.values()) {
			for (SqlType type : aliasMap.keySet()) {
				if (id.equals(type.getId())) {
					sqlType = type;
				}
			}
		}

//		for (CustomTypeInfo customTypeInfo : customTypeInfoMap.values()) {
//			for (Map<SqlType, String> customSqlTypeToAliasMap : customTypeInfo.getDbSqlTypeToAliasMap().values()) {
//				for (SqlType type : customSqlTypeToAliasMap.keySet()) {
//					if (id.equals(type.getId())) {
//						sqlType = type;
//					}
//				}
//			}
//		}

		return sqlType;
	}

	public boolean isNeedLength(String database, String[] customTypes) {
		String alias = this.getAlias(database, customTypes);
		if (alias == null) {
			return false;
		}

		Matcher matcher = NEED_LENGTH_PATTERN.matcher(alias);

		if (matcher.matches()) {
			return true;
		}

		return false;
	}

	public boolean isNeedDecimal(String database, String[] customTypes) {
		String alias = this.getAlias(database, customTypes);
		if (alias == null) {
			return false;
		}

		Matcher matcher = NEED_DECIMAL_PATTERN1.matcher(alias);

		if (matcher.matches()) {
			return true;
		}

		matcher = NEED_DECIMAL_PATTERN2.matcher(alias);

		if (matcher.matches()) {
			return true;
		}

		return false;
	}

	public boolean isNeedCharSemantics(String database) {
		if (!OracleDBManager.ID.equals(database)) {
			return false;
		}

		if (this.name.startsWith(SQL_TYPE_ID_CHAR)
				|| this.name.startsWith(SQL_TYPE_ID_VARCHAR)) {
			return true;
		}

		return false;
	}

	public boolean isTimestamp() {
		if (this.javaClass == Date.class) {
			return true;
		}

		return false;
	}

	public boolean isNumber() {
		if (Number.class.isAssignableFrom(this.javaClass)) {
			return true;
		}

		return false;
	}

	public static List<String> getAliasList(String database, String[] customTypes) {
		Map<SqlType, String> aliasMap = dbSqlTypeToAliasMap.get(database);

		Set<String> aliases = new LinkedHashSet<String>();

		for (Entry<SqlType, String> entry : aliasMap.entrySet()) {
			String alias = entry.getValue();
			aliases.add(alias);
		}

		if (customTypes != null) {
			for (String customType : customTypes) {
				Map<SqlType, String> customSqlTypeToAliasMap = getCustomSqlTypeToAliasMap(database, customType);
				if (customSqlTypeToAliasMap != null) {
					for (Entry<SqlType, String> entry : customSqlTypeToAliasMap.entrySet()) {
						String alias = entry.getValue();
						aliases.add(alias);
					}
				}
			}
		}

		List<String> list = new ArrayList<String>(aliases);

		Collections.sort(list);

		return list;
	}

	public String getAlias(String database, String[] customTypes) {
		Map<SqlType, String> aliasMap = dbSqlTypeToAliasMap.get(database);

		String result = aliasMap.get(this);

		if (customTypes != null) {
			for (String customType : customTypes) {
				Map<SqlType, String> customSqlTypeToAliasMap = getCustomSqlTypeToAliasMap(database, customType);
				if (customSqlTypeToAliasMap != null && customSqlTypeToAliasMap.containsKey(this)) {
					result = customSqlTypeToAliasMap.get(this);
				}
			}
		}

		return result;
	}

	public boolean isUnsupported(String database, String[] customTypes) {
		String alias = this.getAlias(database, customTypes);

		if (alias == null) {
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof SqlType)) {
			return false;
		}

		SqlType type = (SqlType) obj;

		return Objects.equals(this.name, type.name)
				&& Objects.equals(this.javaClass.getName(), type.javaClass.getName())
				&& Objects.equals(this.needArgs, type.needArgs)
				&& Objects.equals(this.fullTextIndexable, type.fullTextIndexable);
	}

	@Override
	public int hashCode() {
		return Objects.hash( this.name, this.javaClass.getName(), this.needArgs, this.fullTextIndexable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.getId();
	}
}
