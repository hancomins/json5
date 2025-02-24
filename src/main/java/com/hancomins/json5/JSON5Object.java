package com.hancomins.json5;


import com.hancomins.json5.container.*;
import com.hancomins.json5.container.cson.BinaryCSONParser;
import com.hancomins.json5.container.cson.BinaryCSONWriter;
import com.hancomins.json5.container.json5.JSON5Parser;
import com.hancomins.json5.container.json5.JSON5Writer;
import com.hancomins.json5.options.*;
import com.hancomins.json5.serializer.JSON5Serializer;
import com.hancomins.json5.util.DataConverter;
import com.hancomins.json5.util.NoSynchronizedStringReader;
import com.hancomins.json5.util.NullValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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



	public JSON5Object(byte[] binaryCSON) {
		super(ElementType.Object);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binaryCSON);
		BinaryCSONParser parser = new BinaryCSONParser(JSON5Object.KeyValueDataContainerFactory, JSON5Array.ArrayDataContainerFactory);
		try {
			parser.parse(byteArrayInputStream, new CSONKeyValueDataContainer(this));
		} catch (IOException e) {
			throw new JSON5Exception(e);
		}
	}



	public JSON5Object(byte[] binaryCSON, int offset, int length) {
		super(ElementType.Object);
		JSON5Object json5Object = (JSON5Object) BinaryCSONParser.parse(binaryCSON, offset, length);
		this.dataMap = json5Object.dataMap;
	}



	public Set<Entry<String, Object>> entrySet() {
		return this.dataMap.entrySet();
	}


	@SuppressWarnings("unused")
	public boolean has(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().has(key);
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
		/*if(JsonParsingOptions.isPureJSONOption(options)) {
			PureJSONParser.parsePureJSON(stringReader, this, options);
		} else {*/
			//JSON5Parser.parsePureJSON(stringReader, this, (JsonParsingOptions)options);

			//JSON5Parser
			//JSON5ParserV parserV = new JSON5ParserV((JsonParsingOptions) options);
			//parserV.parsePureJSON(stringReader, this);
			//parserV.reset();

			//new( (JsonParsingOptions)options).parsePureJSON(stringReader, this);

		JSON5Parser.parse(stringReader, (JsonParsingOptions) options, new CSONKeyValueDataContainer(this), JSON5Object.KeyValueDataContainerFactory, JSON5Array.ArrayDataContainerFactory);



		//}


	}





	public JSON5Object() {
		super(ElementType.Object);
	}



	@SuppressWarnings("unused")
	public boolean remove(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().remove(key);
		}
		return dataMap.remove(key) != null;
	}

	private Object getFromDataMap(String key) {
		if(allowJsonPathKey && key.startsWith("$.")) {
			key = key.substring(2);
			return this.getCsonPath().get(key);
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
			this.getCsonPath().put(key, value);
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



	public Object get(String key) {
		Object obj =  getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		else if(obj == null) throw new JSON5IndexNotFoundException(ExceptionMessages.getJSON5ObjectKeyNotFound(key));
		return obj;
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
	public Boolean getBoolean(String key) {
		 Object obj = get(key);
		if(obj instanceof Boolean) {
			return (Boolean)obj;
		} else if("true".equalsIgnoreCase(obj + "")) {
			return true;
		} else if("false".equalsIgnoreCase(obj + "")) {
			return false;
		}
		throw new JSON5Exception(key, obj, boolean.class.getTypeName());
	}



	public byte getByte(String key) {
		Object number = get(key);
        return DataConverter.toByte(number, (byte) 0, ((value, type) -> {
			throw new JSON5Exception(key, value, type.getTypeName());
		}));
	}


	public byte[] getByteArray(String key) {
		Object obj = get(key);
		byte[] byteArray = DataConverter.toByteArray(obj);
		if(byteArray == null) {
			throw new JSON5Exception(key, obj, byte[].class.getTypeName());
		}
		return byteArray;
	}


	public char getChar(String key) {
		Object number = get(key);
		return DataConverter.toChar(number, (char)0, (value, type) -> {
			throw new JSON5Exception(key, value, type.getTypeName());
		});
	}



	public short getShort(String key) {
		Object number = get(key);
		return DataConverter.toShort(number, (value, type) -> {
			throw new JSON5Exception(key, value, type.getTypeName());
		});
	}


	public int getInt(String key) {
		Object number = get(key);
		return DataConverter.toInteger(number, (value, type) -> {
			throw new JSON5Exception(key, value, type.getTypeName());
		});
	}


	public float getFloat(String key) {
		Object number = get(key);
		return DataConverter.toFloat(number, (value, type) -> {
			throw new JSON5Exception(key, value, type.getTypeName());
		});
	}


	public long getLong(String key) {
		Object number = get(key);
		return DataConverter.toLong(number, (value, type) -> {
			throw new JSON5Exception(key, value, type.getTypeName());
		});
	}



	public double getDouble(String key) {
		Object number = get(key);
		return DataConverter.toDouble(number, (value, type) -> {
			throw new JSON5Exception(key, value, type.getTypeName());
		});
	}

	public String getString(String key) {
		Object obj = get(key);
		if(obj == null) {
			return null;
		}

		return DataConverter.toString(obj);
	}

	@SuppressWarnings("DuplicatedCode")
    public JSON5Object getJSON5Object(String key) {
		Object obj = get(key);
		if(obj == null) {
			return null;
		}
		JSON5Object json5Object = DataConverter.toObject(obj, true);
		if(json5Object == null) {
			throw new JSON5Exception(key, obj, JSON5Object.class.getTypeName());
		}
		return json5Object;
	}


	public JSON5Array getJSON5Array(String key) {
		Object obj = get(key);
		JSON5Array JSON5Array = DataConverter.toArray(obj, true);
		if(JSON5Array == null) {
			throw new JSON5Exception(key, obj, JSON5Array.class.getTypeName());
		}
		return JSON5Array;
	}

	@SuppressWarnings("DuplicatedCode")
    public <T> List<T> getList(String key, Class<T> valueType) {
		JSON5Array JSON5Array = getJSON5Array(key);
		if(JSON5Array == null) {
			return null;
		}
		try {
			return JSON5Serializer.json5ArrayToList(JSON5Array, valueType, JSON5Array.getWritingOptions(), false, null);
		} catch (Throwable e) {
			throw new JSON5Exception(key, JSON5Array, "List<" + valueType.getTypeName() + ">", e);
		}

	}

	public <T> T getObject(String key, Class<T> clazz) {
		JSON5Object json5Object = getJSON5Object(key);
		try {
			return JSON5Serializer.fromJSON5Object(json5Object, clazz);
		} catch (Throwable e) {
			throw new JSON5Exception(key, json5Object, clazz.getTypeName(),e);
		}
	}

	public Object opt(String key) {
		Object obj = getFromDataMap(key);
		if(obj instanceof NullValue) return null;
		return obj;
	}

	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	public boolean optBoolean(String key, boolean def) {
		Object obj = opt(key);
		return DataConverter.toBoolean(obj, def);
	}

	public byte optByte(String key) {
		return optByte(key, (byte)0);
	}

	public byte optByte(String key, byte def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toByte(number, def);
	}


	public byte[] optByteArray(String key) {
		return optByteArray(key, null);
	}

	@SuppressWarnings("unused")
	public byte[] optByteArray(String key,byte[] def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		byte[] buffer =  DataConverter.toByteArray(obj);
		if(buffer == null) {
			return def;
		}
		return buffer;

	}


	public short optShort(String key) {
		return optShort(key, (short)0);
	}

	public short optShort(String key, short def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toShort(number, def);
	}

	@SuppressWarnings("unused")
	public char optChar(String key) {
		return optChar(key, '\0');
	}

	public char optChar(String key, char def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}

		return DataConverter.toChar(obj, def);

	}


	public int optInt(String key) {
		return optInt(key, 0);
	}

	public int optInt(String key, int def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toInteger(number, def);

	}

	public float optFloat(String key) {
		return optFloat(key, Float.NaN);
	}


	public float optFloat(String key, float def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toFloat(obj, def);
	}

	public long optLong(String key) {
		return optLong(key, 0);
	}

	public long optLong(String key, long def) {
		Object number = opt(key);
		if(number == null) {
			return def;
		}
		return DataConverter.toLong(number, def);
	}

	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}

	public double optDouble(String key, double def) {
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		return DataConverter.toDouble(obj, def);
	}

	public String optString(String key) {
		return optString(key, null);
	}

	public String optString(String key,String def) {
		Object obj = opt(key);
		return obj == null ? def : DataConverter.toString(obj);
	}

	public JSON5Array optJSON5Array(String key) {
		return optJSON5Array(key, null);
	}

	public JSON5Array optJSON5Array(String key, JSON5Array def) {
		@SuppressWarnings("DuplicatedCode")
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		JSON5Array JSON5Array = DataConverter.toArray(obj, true);
		if(JSON5Array == null) {
			return def;
		}
		return JSON5Array;
	}

	public JSON5Array optWrapCSONArray(String key) {
		@SuppressWarnings("DuplicatedCode")
		Object object = opt(key);
		if(object == null) {
			return new JSON5Array();
		}
		JSON5Array JSON5Array = DataConverter.toArray(object, true);
		if(JSON5Array == null) {
			return new JSON5Array().put(object);
		}
		return JSON5Array;
	}

	public JSON5Object optJSON5Object(String key) {
		return optJSON5Object(key, null);
	}

	public JSON5Object optJSON5Object(String key, JSON5Object def) {
		@SuppressWarnings("DuplicatedCode")
		Object obj = opt(key);
		if(obj == null) {
			return def;
		}
		JSON5Object json5Object = DataConverter.toObject(obj, true);
		if(json5Object == null) {
			return def;
		}
		return json5Object;
	}


	public <T> T optObject(String key, Class<T> clazz) {
		return optObject(key, clazz, null);
	}

	public <T> T optObject(String key, Class<T> clazz, T defaultObject) {
		try {
			JSON5Object json5Object = optJSON5Object(key);
			return JSON5Serializer.fromJSON5Object(json5Object, clazz);
		} catch (Exception e) {
			return defaultObject;
		}
	}


	public <T> List<T> optList(String key, Class<T> valueType) {
		return optList(key, valueType, null);
	}

	public <T> List<T> optList(String key, Class<T> valueType, T defaultValue) {
		try {
			JSON5Array JSON5Array = optJSON5Array(key);
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
	 * 다른 CSONObject를 병합한다.
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
	 * @deprecated use optWrapCSONArray instead of this method @see optWrapCSONArray
	 */
	@Deprecated
	public JSON5Array optWrapArrayf(String key) {
		return optWrapCSONArray(key);
	}

	/**
	 * @deprecated use optJSON5Array instead of this method @see optJSON5Array
	 */
	@Deprecated
	public JSON5Array optArray(String key, JSON5Array def) {
		return optJSON5Array(key, def);
	}

	/**
	 * @deprecated use optJSON5Array instead of this method @see optJSON5Array
	 */
	@Deprecated
	public JSON5Array optArray(String key) {
		return optJSON5Array(key, null);
	}


	/**
	 * @deprecated use optJSON5Array instead of this method @see optJSON5Array
	 */
	@Deprecated
	public JSON5Array getArray(String key) {
		return getJSON5Array(key);
	}



	/**
	 * @deprecated use optCSONObject instead of this method @see optCSONObject
	 */
	@Deprecated
	public JSON5Object optObject(String key) {
		return optJSON5Object(key);
	}

	/**
	 * @deprecated use optCSONObject instead of this method @see optCSONObject
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public JSON5Object optObject(String key, JSON5Object def) {
		return optJSON5Object(key, def);
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




	public byte[] toBytes() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BinaryCSONWriter writer = new BinaryCSONWriter(outputStream);
		writer.write(new CSONKeyValueDataContainer(this));
		return outputStream.toByteArray();

	}



	@Override
	protected void write(FormatWriter writer) {
		writer.write(new CSONKeyValueDataContainer(this));

		/*Iterator<Entry<String, Object>> iter = dataMap.entrySet().iterator();
		boolean isComment = writer.isComment() && keyValueCommentMap != null;

		// root 오브젝트가 아닌 경우에는 주석을 무시한다.
		if(root) {
			writer.writeComment(getHeadComment(), false,"","\n" );
		}
		writer.openObject();
		while(iter.hasNext()) {
			Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object obj = entry.getValue();
			KeyValueCommentObject keyValueCommentObject = isComment ? keyValueCommentMap.get(key) : null;
			writer.nextCommentObject(keyValueCommentObject == null ? null : keyValueCommentObject.keyCommentObject);
			writer.nextCommentObject(keyValueCommentObject == null ? null : keyValueCommentObject.valueCommentObject);

			if (obj == null || obj instanceof NullValue) writer.key(key).nullValue();
			else if (obj instanceof JSON5Element) {
				//writer.key(key);
				//((JSON5Element) obj).write(writer, false);
				writer.key(key).value((JSON5Element)obj);
			} else if (obj instanceof Byte) {
				writer.key(key).value((byte) obj);
			} else if (obj instanceof Short) writer.key(key).value((short) obj);
			else if (obj instanceof Character) writer.key(key).value((char) obj);
			else if (obj instanceof Integer) writer.key(key).value((int) obj);
			else if (obj instanceof Float) writer.key(key).value((float) obj);
			else if (obj instanceof Long) writer.key(key).value((long) obj);
			else if (obj instanceof Double) writer.key(key).value((double) obj);
			else if (obj instanceof String) writer.key(key).value((String) obj);
			else if (obj instanceof Boolean) writer.key(key).value((boolean) obj);
			else if (obj instanceof BigDecimal) writer.key(key).value(obj);
			else if(obj instanceof BigInteger) writer.key(key).value(obj);
			else if (obj instanceof byte[]) writer.key(key).value((byte[]) obj);
			else if (isAllowRawValue()) {
				writer.key(key).value(obj.toString());
			}

		}
		writer.closeObject();
		if(root) {
			writer.writeComment(getTailComment(), false,"\n","" );
		}*/

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



	final static KeyValueDataContainerFactory KeyValueDataContainerFactory = () -> new CSONKeyValueDataContainer(new JSON5Object());


	static class CSONKeyValueDataContainer implements KeyValueDataContainer {

		final JSON5Object json5Object;
		private String lastKey;

		CSONKeyValueDataContainer(JSON5Object json5Object) {
			this.json5Object = json5Object;
		}

		@Override
		public void put(String key, Object value) {
			lastKey = key;
			if(value instanceof CSONKeyValueDataContainer) {
				json5Object.put(key, ((CSONKeyValueDataContainer) value).json5Object);
				return;
			} else if(value instanceof ArrayDataContainer) {
				json5Object.put(key, ((JSON5Array.CSONArrayDataContainer) value).array);
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
				entry.setValue(new CSONKeyValueDataContainer((JSON5Object)value));
			} else if(value instanceof JSON5Array) {
				entry.setValue(new JSON5Array.CSONArrayDataContainer((JSON5Array)value));
			} else {
				entry.setValue(value);
			}
			return entry;
		}
	}

}
