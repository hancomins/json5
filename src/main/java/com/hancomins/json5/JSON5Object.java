package com.hancomins.json5;


import com.hancomins.json5.container.*;
import com.hancomins.json5.container.json5.JSON5Parser;
import com.hancomins.json5.container.json5.JSON5Writer;
import com.hancomins.json5.options.*;
import com.hancomins.json5.serializer.JSON5Serializer;
import com.hancomins.json5.util.DataConverter;
import com.hancomins.json5.util.NoSynchronizedStringReader;
import com.hancomins.json5.util.NullValue;

import java.io.Reader;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public class JSON5Object extends JSON5Element implements Cloneable {


	protected Map<String, Object> dataMap = new LinkedHashMap<>();
	private Map<String, CommentObject<String>> keyValueCommentMap;

	public static JSON5Object fromObject(Object obj) {
		return JSON5Serializer.toJSON5Object(obj);
	}

	@SuppressWarnings("unused")
	public static JSON5Object fromObject(Object obj, WritingOptions writingOptions) {
		JSON5Object json5Object = JSON5Serializer.toJSON5Object(obj);
		json5Object.setWritingOptions(writingOptions);
		return json5Object;
	}

	@SuppressWarnings("unused")
	public static <T> T toObject(JSON5Object json5Object, Class<T> clazz) {
		return JSON5Serializer.fromJSON5Object(json5Object, clazz);
	}
	@SuppressWarnings("unused")
	public static <T> T toObject(JSON5Object json5Object, T object) {
		return JSON5Serializer.fromJSON5Object(json5Object, object);
	}




	public Set<Entry<String, Object>> entrySet() {
		return this.dataMap.entrySet();
	}


	@SuppressWarnings("unused")
	public boolean has(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getJSON5Path().has(key);
		}
		return dataMap.containsKey(key);
	}


	public Map<String, Object> toMap() {
		Map<String, Object> results = new HashMap<>();
		for (Entry<String, Object> entry : this.entrySet()) {
			Object value;
			if (entry.getValue() == null) {
				value = null;
			} else if (entry.getValue() instanceof JSON5Object) {
				value = ((JSON5Object) entry.getValue()).toMap();
			} else if (entry.getValue() instanceof JSON5Array) {
				value = ((JSON5Array) entry.getValue()).toList();
			} else {
				value = entry.getValue();
			}
			results.put(entry.getKey(), value);
		}
		return results;
	}

	public JSON5Object(String json) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader =  new NoSynchronizedStringReader(json);
		parse(reader, ParsingOptions.getDefaultParsingOptions());
		reader.close();
	}

	public JSON5Object(Reader reader) {
		super(ElementType.Object);
		parse(reader, ParsingOptions.getDefaultParsingOptions());
	}


	public JSON5Object(WritingOptions writingOptions) {
		super(ElementType.Object, writingOptions);
	}

	public JSON5Object(String json, ParsingOptions<?> options) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader = new NoSynchronizedStringReader(json);
		parse(reader, options);
		reader.close();
	}

	public JSON5Object(String json, WritingOptions writingOptions) {
		super(ElementType.Object);
		NoSynchronizedStringReader reader = new NoSynchronizedStringReader(json);
		parse(reader, ParsingOptions.getDefaultParsingOptions());
		reader.close();
		this.setWritingOptions(writingOptions);

	}

	public JSON5Object(Reader reader, ParsingOptions<?> options) {
		super(ElementType.Object);
		parse(reader, options);
	}

	private void parse(Reader stringReader, ParsingOptions<?> options) {
		StringFormatType type = options.getFormatType();
		JSON5Parser.parse(stringReader, (JsonParsingOptions) options, new JSON5KeyValueDataContainer(this), JSON5Object.KeyValueDataContainerFactory, JSON5Array.ArrayDataContainerFactory);

	}





	public JSON5Object() {
		super(ElementType.Object);
	}



	@SuppressWarnings("unused")
	public boolean remove(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getJSON5Path().remove(key);
		}
		return dataMap.remove(key) != null;
	}

	private Object getFromDataMap(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getJSON5Path().get(key);
		}
		Object obj = dataMap.get(key);
		copyHeadTailCommentToValueObject(key, obj);
		return obj;
	}

	private void copyHeadTailCommentToValueObject(String key, Object obj) {
		if(keyValueCommentMap != null && obj instanceof JSON5Element && !keyValueCommentMap.isEmpty()) {
			CommentObject<String> commentObject = keyValueCommentMap.get(key);
			if(commentObject == null) return;
			CommentObject<String> copiedCommentObject = commentObject.copy();
			((JSON5Element)obj).setHeaderComment(copiedCommentObject.getComment(CommentPosition.BEFORE_VALUE));
			((JSON5Element)obj).setFooterComment(copiedCommentObject.getComment(CommentPosition.AFTER_VALUE));
		}
	}



	private void putToDataMap(String key, Object value) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			this.getJSON5Path().put(key, value);
			return;
		}
		dataMap.put(key, value);
	}


	public void putByParser(String key, Object value) {
		dataMap.put(key, value);
	}




	public JSON5Object put(String key, Object value) {
		if(value == null) {
			putToDataMap(key, NullValue.Instance);
			return this;
		}
		else if(value instanceof Number) {
			putToDataMap(key, value);
		} else if(value instanceof CharSequence) {
			putToDataMap(key, value);
		}  else if(value instanceof JSON5Element) {
			if(value == this) {
				value = clone((JSON5Element) value);
			}
			putToDataMap(key, value);
		} else if(value instanceof Character || value instanceof Boolean || value instanceof byte[] || value instanceof NullValue) {
			putToDataMap(key, value);

		}
		else if(value.getClass().isArray()) {
			JSON5Array JSON5Array = new JSON5Array();
			int length = Array.getLength(value);
			for(int i = 0; i < length; i++) {
				JSON5Array.put(Array.get(value, i));
			}
			putToDataMap(key, JSON5Array);
		} else if(value instanceof Collection) {
			JSON5Array JSON5Array = new JSON5Array();
			for(Object obj : (Collection<?>)value) {
				JSON5Array.put(obj);
			}
			putToDataMap(key, JSON5Array);
		}
		else if(JSON5Serializer.serializable(value.getClass())) {
			JSON5Object json5Object = JSON5Serializer.toJSON5Object(value);
			putToDataMap(key, json5Object);
		}
		// todo MAP 처리.
		else if(isAllowRawValue()) {
			putToDataMap(key, value);
		} else if(isUnknownObjectToString()) {
			putToDataMap(key, value + "");
		}
		return this;
	}




	public Set<String> keySet() {
		return this.dataMap.keySet();
	}


	public boolean isNull(String key) {
		Object obj = getFromDataMap(key);
		return obj instanceof NullValue;
	}



	public Object opt(String key) {
		return get(key);
	}

	public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
		Object obj = get(key);
		T result =  DataConverter.toEnum(enumType, obj);
		if(result == null) {
			throw new JSON5Exception(key, obj, enumType.getTypeName());
		}

		return result;
	}


	@SuppressWarnings("DuplicatedCode")
	@Deprecated
	public Boolean optBoolean(String key) {
		return getBoolean(key);
	}



	@Deprecated
	public byte optByte(String key) {

		return getByte(key);
	}


	@Deprecated
	public byte[] optByteArray(String key) {
		return getByteArray(key);
	}

	@Deprecated
	public char optChar(String key) {
		return getChar(key);
	}




	@Deprecated
	public short optShort(String key) {
		return getShort(key);
	}


	@Deprecated
	public int optInt(String key) {
		return getInteger(key);
	}


	@Deprecated
	public float optFloat(String key) {
		return getFloat(key);
	}


	@Deprecated
	public long optLong(String key) {
		return getLong(key);
	}



	@Deprecated
	public double optDouble(String key) {
		return getDouble(key);
	}

	@Deprecated
	public String optString(String key) {
		return getString(key);
	}

	@SuppressWarnings("DuplicatedCode")
	@Deprecated
	public JSON5Object optJSON5Object(String key) {
		return getJSON5Object(key);
	}


	@Deprecated
	public JSON5Array optJSON5Array(String key) {
		return getJSON5Array(key);
	}

	@SuppressWarnings("DuplicatedCode")
	@Deprecated
	public <T> List<T> optList(String key, Class<T> valueType) {
		return getList(key, valueType);
	}

	@Deprecated
	public <T> T optObject(String key, Class<T> clazz) {
		return getObject(key, clazz);
	}


	public Object get(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		return obj;
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean def) {
		Object obj = get(key);
		return DataConverter.toBoolean(obj, def);
	}

	public byte getByte(String key) {
		return getByte(key, (byte)0);
	}

	public byte getByte(String key, byte def) {
		Object number = get(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toByte(number, def);
	}


	public byte[] getByteArray(String key) {
		return getByteArray(key, null);
	}

	@SuppressWarnings("unused")
	public byte[] getByteArray(String key,byte[] def) {
		Object obj = get(key);
		if(obj == null) {
			return def;
		}
		byte[] buffer = DataConverter.toByteArray(obj);
		if(buffer == null) {
			return def;
		}
		return buffer;

	}


	public short getShort(String key) {
		return getShort(key, (short)0);
	}

	public short getShort(String key, short def) {
		Object number = get(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toShort(number, def);
	}

	@SuppressWarnings("unused")
	public char getChar(String key) {
		return getChar(key, '\0');
	}

	public char getChar(String key, char def) {
		Object obj = get(key);
		if(obj == null) {
			return def;
		}

		return DataConverter.toChar(obj, def);

	}


	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int def) {
		Object number = get(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toInteger(number, def);

	}

	public float getFloat(String key) {
		return getFloat(key, Float.NaN);
	}


	public float getFloat(String key, float def) {
		Object obj = get(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toFloat(obj, def);
	}

	public long getLong(String key) {
		return getLong(key, 0);
	}

	public long getLong(String key, long def) {
		Object number = get(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toLong(number, def);
	}

	public double getDouble(String key) {
		return getDouble(key, Double.NaN);
	}

	public double getDouble(String key, double def) {
		Object obj = get(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toDouble(obj, def);
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public String getString(String key,String def) {
		Object obj = get(key);
		return obj == null ? def : DataConverter.toString(obj);
	}

	public JSON5Array getJSON5Array(String key) {
		return getJSON5Array(key, null);
	}

	public JSON5Array getJSON5Array(String key, JSON5Array def) {
		@SuppressWarnings("DuplicatedCode")
		Object obj = get(key);
		if(obj == null) {
			return def;
		}
		JSON5Array JSON5Array = DataConverter.toArray(obj, true);
		if(JSON5Array == null) {
			return def;
		}
		return JSON5Array;
	}

	public JSON5Array getWrapJSON5Array(String key) {
		@SuppressWarnings("DuplicatedCode")
		Object object = get(key);
		if(object == null) {
			return new JSON5Array();
		}
		JSON5Array JSON5Array = DataConverter.toArray(object, true);
		if(JSON5Array == null) {
			return new JSON5Array().put(object);
		}
		return JSON5Array;
	}

	public JSON5Object getJSON5Object(String key) {
		return getJSON5Object(key, null);
	}

	public JSON5Object getJSON5Object(String key, JSON5Object def) {
		@SuppressWarnings("DuplicatedCode")
		Object obj = get(key);
		if(obj == null) {
			return def;
		}
		JSON5Object json5Object = DataConverter.toObject(obj, true);
		if(json5Object == null) {
			return def;
		}
		return json5Object;
	}


	public <T> T getObject(String key, Class<T> clazz) {
		return getObject(key, clazz, null);
	}

	public <T> T getObject(String key, Class<T> clazz, T defaultObject) {
		try {
			JSON5Object json5Object = optJSON5Object(key);
			return JSON5Serializer.fromJSON5Object(json5Object, clazz);
		} catch (Exception e) {
			return defaultObject;
		}
	}


	public <T> List<T> getList(String key, Class<T> valueType) {
		return getList(key, valueType, null);
	}

	public <T> List<T> getList(String key, Class<T> valueType, T defaultValue) {
		try {
			JSON5Array JSON5Array = getJSON5Array(key);
			if(JSON5Array == null) {
				return null;
			}
			return JSON5Serializer.json5ArrayToList(JSON5Array, valueType, JSON5Array.getWritingOptions(), true, defaultValue);
		} catch (Exception e) {
			if(defaultValue != null) {
				List<T> result = new ArrayList<>();
				result.add(defaultValue);
				return result;
			} else {
				//noinspection unchecked
				return Collections.EMPTY_LIST;
			}
		}
	}




	/**
	 * 다른 JSON5Object를 병합한다.
	 * @param json5Object 병합할 JSON5Object
	 */
	public void merge(JSON5Object json5Object) {
		Set<String> keys = json5Object.keySet();
		for(String key : keys) {
			Object value = json5Object.get(key);
			if(value instanceof JSON5Object) {
				JSON5Object childObject = optJSON5Object(key);
				if(childObject == null) {
					childObject = new JSON5Object();
					put(key, childObject);
				}
				childObject.merge((JSON5Object)value);
			} else if(value instanceof JSON5Array) {
				JSON5Array childArray = optJSON5Array(key);
				if(childArray == null) {
					childArray = new JSON5Array();
					put(key, childArray);
				}
				childArray.merge((JSON5Array)value);
			} else {
				put(key, value);
			}
		}
	}

	/**
	 * 교집합을 반환한다.
	 * @param json5Object 교집합을 구할 JSON5Object
	 * @return 교집합
	 */
	public JSON5Object intersect(JSON5Object json5Object) {
		JSON5Object result = new JSON5Object();
		Set<String> keys = json5Object.keySet();
		for(String key : keys) {
			Object value = json5Object.get(key);
			if(value instanceof JSON5Object) {
				JSON5Object childObject = optJSON5Object(key);
				if(childObject == null) {
					continue;
				}
				result.put(key, childObject.intersect((JSON5Object)value));
			} else if(value instanceof JSON5Array) {
				JSON5Array childArray = optJSON5Array(key);
				if(childArray == null) {
					continue;
				}
				result.put(key, childArray.intersect((JSON5Array)value));
			} else {
				if(has(key)) {
					result.put(key, value);
				}
			}
		}
		return result;
	}

	/**
	 * 교집합을 제외한 부분을 반환한다.
	 * @param json5Object 교집합을 제외할 JSON5Object
	 * @return 교집합을 제외한 부분
	 */
	public JSON5Object subtractIntersection(JSON5Object json5Object) {
		JSON5Object result = new JSON5Object();
		Set<String> keys = json5Object.keySet();
		for(String key : keys) {
			Object value = json5Object.get(key);
			if(value instanceof JSON5Object) {
				JSON5Object childObject = optJSON5Object(key);
				if(childObject == null) {
					continue;
				}
				result.put(key, childObject.subtractIntersection((JSON5Object)value));
			} else if(value instanceof JSON5Array) {
				JSON5Array childArray = optJSON5Array(key);
				if(childArray == null) {
					continue;
				}
				result.put(key, childArray.subtractIntersection((JSON5Array)value));
			} else {
				if(!has(key)) {
					result.put(key, value);
				}
			}
		}
		return result;

	}



	public boolean isEmpty() {
		return dataMap.isEmpty();
	}




	/**
	 * @deprecated use {@link #optInt(String)} instead
	 *
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public int getInteger(String key) {
		return getInt(key);
	}


	public JSON5Object setComment(CommentPosition position, String key, String comment) {
		CommentObject<String> commentObject = getOrCreateCommentObject(key);
		commentObject.setComment(position, comment);
		return this;
	}


	public JSON5Object setCommentForKey(String key, String comment) {
		setComment(CommentPosition.BEFORE_KEY, key, comment);
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public JSON5Object setCommentForValue(String key, String comment) {
		setComment(CommentPosition.BEFORE_VALUE, key, comment);
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public JSON5Object setCommentAfterValue(String key, String comment) {
		setComment(CommentPosition.AFTER_VALUE, key, comment);
		return this;
	}


	@SuppressWarnings("UnusedReturnValue")
	public JSON5Object setCommentAfterKey(String key, String comment) {
		setComment(CommentPosition.AFTER_KEY, key, comment);
		return this;
	}

	@SuppressWarnings("unused")
	public JSON5Object setComment(String key, String comment) {
		return setCommentForKey(key, comment);
	}

	private JSON5Object setComment(String key, CommentObject<String> commentObject) {
		if(keyValueCommentMap == null) {
			keyValueCommentMap = new LinkedHashMap<>();
		}
		keyValueCommentMap.put(key, commentObject);
		return this;
	}

	public String getComment(CommentPosition position, String key) {
		if(keyValueCommentMap == null) return null;
		CommentObject<String> commentObject = keyValueCommentMap.get(key);
		if(commentObject == null) return null;
		return commentObject.getComment(position);
	}

	public String getCommentForKey(String key) {
		return getComment(CommentPosition.BEFORE_KEY, key);
	}

	public String getCommentAfterKey(String key) {
		return getComment(CommentPosition.AFTER_KEY, key);
	}


	public String getCommentForValue(String key) {
		return getComment(CommentPosition.BEFORE_VALUE, key);
	}


	public String getCommentAfterValue(String key) {
		return getComment(CommentPosition.AFTER_VALUE, key);
	}




	private CommentObject<String> getOrCreateCommentObject(String key) {
		if(keyValueCommentMap == null) {
			keyValueCommentMap = new LinkedHashMap<>();
		}
		return keyValueCommentMap.computeIfAbsent(key, k -> CommentObject.forKeyValueContainer(key));
	}



	/**
	 * @deprecated use optWrapJSON5Array instead of this method @see optWrapJSON5Array
	 */
	@Deprecated
	public JSON5Array getWrapArrayf(String key) {
		return getWrapJSON5Array(key);
	}

	/**
	 * @deprecated use optJSON5Array instead of this method @see optJSON5Array
	 */
	@Deprecated
	public JSON5Array getArray(String key, JSON5Array def) {
		return getJSON5Array(key, def);
	}

	/**
	 * @deprecated use optJSON5Array instead of this method @see optJSON5Array
	 */
	@Deprecated
	public JSON5Array getArray(String key) {
		return getJSON5Array(key, null);
	}


	/**
	 * @deprecated use optJSON5Array instead of this method @see optJSON5Array
	 */
	@Deprecated
	public JSON5Array optArray(String key) {
		return getJSON5Array(key);
	}



	/**
	 * @deprecated use optJSON5Object instead of this method @see optJSON5Object
	 */
	@Deprecated
	public JSON5Object getObject(String key) {
		return optJSON5Object(key);
	}

	/**
	 * @deprecated use optJSON5Object instead of this method @see optJSON5Object
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public JSON5Object getObject(String key, JSON5Object def) {
		return getJSON5Object(key, def);
	}





	@Override
	public String toString() {
		return toString(getWritingOptions());
	}

	@Override
	public String toString(WritingOptions writingOptions) {
		if(writingOptions instanceof JSON5WriterOption) {
			JSON5Writer jsonWriter = new JSON5Writer((JSON5WriterOption)writingOptions);
			write(jsonWriter);
			return jsonWriter.toString();
		}
		return toString();

	}

	@Override
	public void clear() {
		dataMap.clear();
	}

	public boolean containsValue(Object value) {
		boolean result = dataMap.containsValue(value);
		if(value == null && !result) {
			return dataMap.containsValue(NullValue.Instance);
		}
		return result;
	}

	public boolean containsValueNoStrict(Object value) {
		Collection<Object> values = dataMap.values();
		return containsNoStrict(values, value);

	}







	@Override
	protected void write(FormatWriter writer) {
		writer.write(new JSON5KeyValueDataContainer(this));

	}




	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public JSON5Object clone() {
		JSON5Object json5Object = new JSON5Object();
		for (Entry<String, Object> entry : dataMap.entrySet()) {
			String key = entry.getKey();
			Object obj = entry.getValue();
			if (obj instanceof JSON5Array) json5Object.put(key, ((JSON5Array) obj).clone());
			else if (obj instanceof JSON5Object) json5Object.put(key, ((JSON5Object) obj).clone());
			else if (obj instanceof CharSequence) json5Object.put(key, ((CharSequence) obj).toString());
			else if (obj == NullValue.Instance) json5Object.put(key,NullValue.Instance);
			else if (obj instanceof byte[]) {
				byte[] bytes = (byte[]) obj;
				byte[] newBytes = new byte[bytes.length];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				json5Object.put(key, newBytes);
			} else json5Object.put(key, obj);
		}
		return json5Object;
	}

	public int size() {
		return dataMap.size();
	}




	public Collection<Object> values() {
		return dataMap.values();
	}

	public Iterator<Entry<String, Object>> iteratorEntry() {
		return new Iterator<Entry<String, Object>>() {
			final Iterator<Entry<String, Object>> entryIter = new ArrayList<>(dataMap.entrySet()).iterator();
			String key = null;
			@Override
			public boolean hasNext() {
				return entryIter.hasNext();
			}

			@Override
			public Entry<String, Object> next() {
				Entry<String, Object> entry = entryIter.next();
				Object object = entry.getValue();
				key = entry.getKey();
				if(object == NullValue.Instance) {
					entry.setValue(null);
				}
				return entry;
			}

			@Override
			public void remove() {
				if(key == null) {
					throw new IllegalStateException();
				}
				dataMap.remove(key);
			}
		};
	}



	@Override
	public Iterator<Object> iterator() {


		return new Iterator<Object>() {
			final Iterator<Entry<String, Object>> entryIterator = iteratorEntry();

			@Override
			public boolean hasNext() {
				return entryIterator.hasNext();
			}

			@Override
			public Object next() {
				return entryIterator.next().getValue();
			}

			@Override
			public void remove() {
				entryIterator.remove();
			}
		};
	}



	final static KeyValueDataContainerFactory KeyValueDataContainerFactory = () -> new JSON5KeyValueDataContainer(new JSON5Object());


	static class JSON5KeyValueDataContainer implements KeyValueDataContainer {

		final JSON5Object json5Object;
		private String lastKey;

		JSON5KeyValueDataContainer(JSON5Object json5Object) {
			this.json5Object = json5Object;
		}

		@Override
		public void put(String key, Object value) {
			lastKey = key;
			if(value instanceof JSON5KeyValueDataContainer) {
				json5Object.put(key, ((JSON5KeyValueDataContainer) value).json5Object);
				return;
			} else if(value instanceof ArrayDataContainer) {
				json5Object.put(key, ((JSON5Array.JSON5ArrayDataContainer) value).array);
				return;
			}
			json5Object.dataMap.put(key, value);
		}

		@Override
		public Object get(String key) {
			lastKey = key;
			return json5Object.dataMap.get(key);
		}

		@Override
		public String getLastAccessedKey() {
			return lastKey;
		}

		@Override
		public void remove(String key) {
			lastKey = key;
			json5Object.dataMap.remove(key);
		}

		@Override
		public void setComment(String key, String comment, CommentPosition type) {
			switch (type) {
				case DEFAULT:
				case BEFORE_KEY:
					json5Object.setCommentForKey(key, comment);
					break;
				case BEFORE_VALUE:
					json5Object.setCommentForValue(key, comment);
					break;
				case AFTER_KEY:
					json5Object.setCommentAfterKey(key, comment);
					break;
				case AFTER_VALUE:
					json5Object.setCommentAfterValue(key, comment);
					break;
				case HEADER:
					json5Object.setHeaderComment(comment);
					break;
				case FOOTER:
					json5Object.setFooterComment(comment);
					break;

			}

		}

		@Override
		public String getComment(String key, CommentPosition type) {
			switch (type) {
				case DEFAULT:
				case BEFORE_KEY:
					return json5Object.getCommentForKey(key);
				case BEFORE_VALUE:
					return json5Object.getCommentForValue(key);
				case AFTER_KEY:
					return json5Object.getCommentAfterKey(key);
				case AFTER_VALUE:
					return json5Object.getCommentAfterValue(key);
				case HEADER:
					return json5Object.getHeaderComment();
				case FOOTER:
					return json5Object.getFooterComment();
			}
			return null;
		}

		@Override
		public CommentObject<String> getCommentObject(String key) {
			if(json5Object.keyValueCommentMap == null) {
				return null;
			}
			return json5Object.keyValueCommentMap.get(key);
		}

		@Override
		public Set<String> keySet() {
			return json5Object.dataMap.keySet();
		}

		@Override
		public int size() {
			return json5Object.dataMap.size();
		}

		@Override
		public void setSourceFormat(FormatType formatType) {
			switch (formatType) {
				case JSON:
					json5Object.setWritingOptions(WritingOptions.json());
					break;
				case JSON5:
					json5Object.setWritingOptions(WritingOptions.json5());
					break;
				case JSON5_PRETTY:
					json5Object.setWritingOptions(WritingOptions.json5Pretty());
					break;
				case JSON_PRETTY:
					json5Object.setWritingOptions(WritingOptions.jsonPretty());
					break;
			}

		}

		@Override
		public void setComment(CommentObject<?> commentObject) {
			Object index = commentObject.getIndex();
			if(index instanceof String) {
				//noinspection unchecked
				json5Object.setComment((String)index, (CommentObject<String>)commentObject);
			}
		}



		@Override
		public DataIterator<?> iterator() {
			return new EntryDataIterator(json5Object.dataMap.entrySet().iterator(), json5Object.dataMap.size(), true);
		}
	}

	private static class EntryDataIterator extends DataIterator<Entry<String, Object>>{

		public EntryDataIterator(Iterator<Entry<String, Object>> iterator, int size, boolean isEntryValue) {
			super(iterator, size, isEntryValue);
		}

		@Override
		public Entry<String, Object> next() {
			Entry<String, Object> originEntry = super.next();
			Entry<String, Object> entry = new AbstractMap.SimpleEntry<>(originEntry.getKey(), originEntry.getValue());
			Object value = entry.getValue();
			if(value instanceof NullValue) {
				entry.setValue(null);
			} else if(value instanceof JSON5Object) {
				entry.setValue(new JSON5KeyValueDataContainer((JSON5Object)value));
			} else if(value instanceof JSON5Array) {
				entry.setValue(new JSON5Array.JSON5ArrayDataContainer((JSON5Array)value));
			} else {
				entry.setValue(value);
			}
			return entry;
		}
	}

}
