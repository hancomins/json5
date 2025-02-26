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


public class JSON5Array extends JSON5Element implements Collection<Object>, Cloneable {

	private ArrayList<Object> list = new ArrayList<>();
	private ArrayList<CommentObject<Integer>> commentObjectList = null;


	public static JSON5Array fromCollection(Collection<?> collection) {
		return JSON5Serializer.collectionToJSON5Array(collection);
	}

	public static JSON5Array fromCollection(Collection<?> collection, WritingOptions writingOptions) {
		JSON5Array JSON5Array = JSON5Serializer.collectionToJSON5Array(collection);
		JSON5Array.setWritingOptions(writingOptions);
		return JSON5Array;
	}

	public static <T> Collection<T> toCollection(JSON5Array JSON5Array, Class<T> clazz) {
		return JSON5Serializer.json5ArrayToList(JSON5Array, clazz, JSON5Array.getWritingOptions(), false, null);
	}

	public static <T> Collection<T> toCollection(JSON5Array JSON5Array, Class<T> clazz, boolean ignoreError) {
		return JSON5Serializer.json5ArrayToList(JSON5Array, clazz, JSON5Array.getWritingOptions(), ignoreError, null);
	}


	public JSON5Array() {
		super(ElementType.Array);
	}



	public JSON5Array(Reader stringSource) throws JSON5Exception {
		super(ElementType.Array);
		parse(stringSource, ParsingOptions.getDefaultParsingOptions());
	}

	public JSON5Array(Reader stringSource, WritingOptions writingOptions) {
		super(ElementType.Array);
		parse(stringSource, ParsingOptions.getDefaultParsingOptions());
		this.setWritingOptions(writingOptions);
	}

	public JSON5Array(Reader source, ParsingOptions<?> options) throws JSON5Exception {
		super(ElementType.Array);
		parse(source, options);
	}


	public JSON5Array(String jsonArray) throws JSON5Exception {
		super(ElementType.Array);
		NoSynchronizedStringReader noSynchronizedStringReader = new NoSynchronizedStringReader(jsonArray);
		parse(noSynchronizedStringReader, ParsingOptions.getDefaultParsingOptions());
		noSynchronizedStringReader.close();;
	}

	public JSON5Array(String jsonArray, ParsingOptions<?> options) throws JSON5Exception {
		super(ElementType.Array);
		NoSynchronizedStringReader noSynchronizedStringReader = new NoSynchronizedStringReader(jsonArray);
		parse(noSynchronizedStringReader, options);
		noSynchronizedStringReader.close();
	}

	public JSON5Array(String jsonArray, WritingOptions options) throws JSON5Exception {
		super(ElementType.Array);
		NoSynchronizedStringReader noSynchronizedStringReader = new NoSynchronizedStringReader(jsonArray);
		parse(noSynchronizedStringReader, ParsingOptions.getDefaultParsingOptions());
		noSynchronizedStringReader.close();
		this.setWritingOptions(options);
	}



	public JSON5Array(WritingOptions writingOptions) {

		super(ElementType.Array, writingOptions);
	}


	private void parse(Reader stringReader, ParsingOptions<?> options) {
		StringFormatType type = options.getFormatType();
		/*if(JsonParsingOptions.isPureJSONOption(options)) {
			PureJSONParser.parsePureJSON(stringReader, this, options);
		} else {*/
			//new JSONParser(new JSONTokener(stringReader, (JsonParsingOptions)options)).parseArray(this);
			//new JSON5ParserV((JsonParsingOptions) options).parsePureJSON(stringReader, this);
			 JSON5Parser.parse(stringReader, (JsonParsingOptions) options, new JSON5ArrayDataContainer(this), JSON5Object.KeyValueDataContainerFactory, JSON5Array.ArrayDataContainerFactory);

		//}
	}


	public JSON5Array(int capacity) {
		super(ElementType.Array);
		this.list.ensureCapacity(capacity);
	}

	public JSON5Array(Collection<?> objects) {
		super(ElementType.Array);
		list.addAll(objects);
	}

	public JSON5Array(byte[] json5) {
		super(ElementType.Array);
		BinaryCSONParser parser = new BinaryCSONParser(JSON5Object.KeyValueDataContainerFactory, JSON5Array.ArrayDataContainerFactory);
        try {
            parser.parse(new ByteArrayInputStream(json5), new JSON5ArrayDataContainer(this));
        } catch (IOException e) {
			// todo 메시지 추가해야한다.
			throw new JSON5Exception(e);
        }
    }


	public JSON5Array(byte[] binary, int offset, int len) {
		super(ElementType.Array);
		this.list = ((JSON5Array) BinaryCSONParser.parse(binary, offset, len)).list;
	}





	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		boolean result = list.contains(o);
		if(!result && o == null) {
			return list.contains(NullValue.Instance);
		}
		return result;
	}

	public boolean containsNoStrict(Object value) {
		return containsNoStrict(list, value);
	}


	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return toList().toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray(Object[] a) {
		return toList().toArray(a);
	}

	public List<Object> toList() {
		List<Object> results = new ArrayList<Object>(this.list.size());
		for (Object element : this.list) {
			if (element == null) {
				results.add(null);
			} else if (element instanceof JSON5Array) {
				results.add(((JSON5Array) element).toList());
			} else if (element instanceof JSON5Object) {
				results.add(((JSON5Object) element).toMap());
			} else {
				results.add(element);
			}
		}
		return results;
	}


	public String getComment(CommentPosition commentPosition, int index) {
		CommentObject<Integer> commentObject = getCommentObject(index);
		if(commentObject == null) return null;
		return commentObject.getComment(commentPosition);
	}

	public JSON5Array setComment(CommentPosition commentPosition, int index, String comment) {
		CommentObject<Integer> commentObject = getOrCreateCommentObject(index);
		commentObject.setComment(commentPosition, comment);
		return this;
	}


	@SuppressWarnings("unused")
	public String getCommentForValue(int index) {
		return getComment(CommentPosition.BEFORE_VALUE, index);
	}

	@SuppressWarnings("unused")
	public String getCommentAfterValue(int index) {
		return getComment(CommentPosition.AFTER_VALUE, index);
	}

	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public JSON5Array setCommentForValue(int index, String comment) {
		setComment(CommentPosition.BEFORE_VALUE, index, comment);
		return this;
	}

	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public JSON5Array setCommentAfterValue(int index, String comment) {
		setComment(CommentPosition.AFTER_VALUE, index, comment);
		return this;
	}


	private CommentObject<Integer> getOrCreateCommentObject(int index) {
		CommentObject<Integer> commentObject = getCommentObject(index);
		if(commentObject == null) {
			commentObject = CommentObject.forArrayContainer(index);
			setCommentObject(index, commentObject);
		}
		return commentObject;
	}


	public CommentObject<Integer> getCommentObject(int index) {
		if(commentObjectList == null) return null;
		if(index >= commentObjectList.size()) return null;
		return commentObjectList.get(index);
	}



	@SuppressWarnings("unused")
	public void setCommentObject(int index, CommentObject<Integer> commentObject) {
		if(commentObjectList == null) {
			commentObjectList = new ArrayList<>();
		}
		if(commentObjectList.size() <= index) {
			ensureCapacityOfCommentObjects(index);
		}
		commentObjectList.set(index, commentObject);
	}


	private void ensureCapacityOfCommentObjects(int index) {
		//commentObjectList.ensureCapacity(list.size());
		for (int i = commentObjectList.size(), n = index + 1; i < n; i++) {
			commentObjectList.add(null);
		}
	}




	protected void addAtJSONParsing(Object value) {
		if(value instanceof String && JSON5Element.isBase64String((String)value)) {
			value = JSON5Element.base64StringToByteArray((String)value);
		}
		list.add(value);
	}


	protected void addCommentObjects(CommentObject<Integer> commentObject) {
		if(commentObjectList == null) {
			commentObjectList = new ArrayList<>();
		}
		commentObjectList.add(commentObject);
	}


	public JSON5Array put(Object e) {
		if(!add(e)) {
			throw new JSON5Exception("put error. can't put " + e.getClass() + " to JSON5Array.");
		}
		return this;
	}

	public JSON5Array put(Object... e) {
		for(Object obj : e) {
			if(!add(obj)) {
				throw new JSON5Exception("put error. can't put " + obj.getClass() + " to JSON5Array.");
			}
		}
		return this;
	}

	@SuppressWarnings("unused")
	public JSON5Array putAll(Object e) {
		if(e instanceof  Collection) {
			for(Object obj : (Collection<?>)e) {
				if(!add(obj)) {
					throw new JSON5Exception("putAll error. can't put " + obj.getClass() + " to JSON5Array.");
				}
			}
		} else if(e.getClass().isArray()) {
			for(int i = 0, n = Array.getLength(e); i < n; ++i) {
				Object obj = Array.get(e, i);
				if(!add(obj)) {
					throw new JSON5Exception("putAll error. can't put " + obj.getClass() + " to JSON5Array.");
				}
			}
		} else {
			throw new JSON5Exception("putAll error. can't put " + e.getClass()+ " to JSON5Array.");
		}
		return this;
	}



	public JSON5Array set(int index, Object e) {
		int size = list.size();
		Object value = convert(e);
		if(index >= size) {
			for(int i = size; i < index; i++) {
				add(null);
			}
			list.add(value);
		} else {
			list.set(index, value);
		}
		return this;
	}

	public JSON5Array setList(Collection<?> collection) {
		for (Object obj : collection) {
			if(!add(obj)) {
				throw new JSON5Exception("new JSON5Array(Collection) error. can't put " + obj.getClass() + " to JSON5Array.");
			}
		}
		return this;
	}



	private Object convert(Object e) {
		if(e == null) {
			return NullValue.Instance;
		}
		else if(e instanceof Number) {
			return e;
		} else if(e instanceof CharSequence) {
			return e.toString();
		} else if(e instanceof JSON5Element) {
			if(e == this) e = ((JSON5Array)e).clone();
			return e;
		}
		else if(e instanceof Character || e instanceof Boolean || e instanceof byte[]) {
			return e;
		} else if(e.getClass().isArray()) {
			JSON5Array array = new JSON5Array();
			for(int i = 0, n = Array.getLength(e); i < n; ++i) {
				array.add(Array.get(e, i));
			}
			return array;
		} else if(e instanceof  Collection) {
			JSON5Array array = new JSON5Array();
			for(Object obj : (Collection<?>)e) {
				//noinspection UseBulkOperation
				array.add(obj);
			}
			return array;
		} else if(JSON5Serializer.serializable(e.getClass())) {
			return JSON5Serializer.toJSON5Object(e);
		}
		else if(isAllowRawValue()) {
			return e;
		}
		return isUnknownObjectToString() ? e + "" : null;
	}





	@Override
	public boolean add(Object e) {
		Object value = convert(e);
		if(value == null) {
			return false;
		}
		list.add(value);
		return true;
	}


	@SuppressWarnings("UnusedReturnValue")
	public boolean addAll(Object e) {
		if(e instanceof  Collection) {
			for(Object obj : (Collection<?>)e) {
				if(!add(obj)) {
					return false;
				}
			}
		} else if(e.getClass().isArray()) {
			for(int i = 0, n = Array.getLength(e); i < n; ++i) {
				if(!add(Array.get(e, i))) {
					return false;
				}
			}
		}
		return true;
	}





	/**
	 * @deprecated use {@link #optJSON5Object(int)} instead.
	 */
	@Deprecated
	public JSON5Object optObject(int index) {
		return getJSON5Object(index);
	}








	public boolean isNull(int index) {
		Object obj = list.get(index);
		return obj == null || obj instanceof NullValue;
	}


	@Deprecated
	public Object opt(int index) {
		return get(index);
	}

	private void copyHeadTailCommentToValueObject(int index, Object obj) {
		if(commentObjectList != null && obj instanceof JSON5Element && !commentObjectList.isEmpty()) {
			CommentObject<Integer> valueCommentObject = commentObjectList.get(index);
			if(valueCommentObject != null) {
				((JSON5Element)obj).setHeaderComment(valueCommentObject.getComment(CommentPosition.BEFORE_VALUE));
				((JSON5Element)obj).setFooterComment(valueCommentObject.getComment(CommentPosition.AFTER_VALUE));
			}
		}
	}

	public <T extends Enum<T>> T getEnum(int index, Class<T> enumType) {
		Object obj = get(index);

		T result =  DataConverter.toEnum(enumType, obj);
		if(result == null) {
			throw new JSON5Exception(index, obj, enumType.getTypeName());
		}
		return result;
	}


	@Deprecated
	public boolean optBoolean(int index) {
		return getBoolean(index);
	}

	@Deprecated
	public byte optByte(final int index) {
		return getByte(index);
	}




	@Deprecated
	public byte[] optByteArray(int index) {
		return getByteArray(index);
	}


	@Deprecated
	public char optChar(int index) {
		return getChar(index);

	}


	@Deprecated
	public short optShort(int index) {
		return getShort(index);
	}


	@Deprecated
	public int optInt(int index) {
		return getInteger(index);
	}



	@Deprecated
	public float optFloat(int index) {
		return getFloat(index);
	}


	@Deprecated
	public long optLong(int index) {
		return getLong(index);
	}



	@Deprecated
	public double optDouble(int index) {
		return getDouble(index);
	}


	@Deprecated
	public String optString(int index) {
		return getString(index);
	}

	@Deprecated
	public JSON5Array optJSON5Array(int index) {
		return getJSON5Array(index);
	}

	@Deprecated
	public JSON5Object optJSON5Object(int index) {
		return getJSON5Object(index);
	}


	@Deprecated
	public <T> List<T> optList(int index, Class<T> valueType) {
		return getList(index, valueType);
	}



	@Deprecated
	public <T> T optObject(int index, Class<T> clazz) {
		return getObject(index, clazz);
	}


	public Object get(int index) {
		if(index < 0 || index >= list.size()) {
			return null;
		}
		Object obj = list.get(index);
		if(obj instanceof NullValue) return null;
		copyHeadTailCommentToValueObject(index, obj);
		return obj;
	}


	public boolean getBoolean(int index, boolean def) {
		Object obj = get(index);
		return DataConverter.toBoolean(obj, def);
	}

	public boolean getBoolean(int index) {
		return getBoolean(index, false);
	}


	public byte getByte(int index) {
		return getByte(index, (byte)0);
	}

	@SuppressWarnings("unused")
	public byte getByte(int index, byte def) {
		Object number = get(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toByte(number, def);
	}



	public byte[] getByteArray(int index) {
		return getByteArray(index, null);
	}

	@SuppressWarnings("unused")
	public byte[] getByteArray(int index, byte[] def) {
		Object obj = get(index);
		if(obj == null) {
			return def;
		}
		byte[] buffer =  DataConverter.toByteArray(obj);
		if(buffer == null) {
			return def;
		}
		return buffer;

	}



	public short getShort(int index) {
		return getShort(index, (short)0);
	}

	public short getShort(int index, short def) {
		Object number = get(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toShort(number, def);
	}

	@SuppressWarnings("unused")
	public char getChar(int index) {
		return getChar(index, '\0');
	}

	public char getChar(int index, char def) {
		Object obj = get(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toChar(obj,def);
	}


	public int getInt(int index) {
		return getInt(index, 0);
	}

	public int getInt(int index, int def) {
		Object number = get(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toInteger(number, def);

	}

	public float getFloat(int index) {
		return getFloat(index, Float.NaN);
	}


	public float getFloat(int index, float def) {
		Object obj = get(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toFloat(obj, def);
	}

	public long getLong(int index) {
		return getLong(index, 0);
	}

	public long getLong(int index, long def) {
		Object number = get(index);
		if(number == null) {
			return def;
		}
		return DataConverter.toLong(number, def);
	}

	public double getDouble(int index) {
		return getDouble(index, Double.NaN);
	}

	public double getDouble(int index, double def) {
		Object obj = get(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toDouble(obj, def);
	}

	public String getString(int index) {
		return getString(index, null);
	}

	public String getString(int index, String def) {
		Object obj = get(index);
		if(obj == null) {
			return def;
		}
		return DataConverter.toString(obj);
	}

	public JSON5Array getJSON5Array(int index) {
		return getJSON5Array(index, null);
	}

	public JSON5Array getJSON5Array(int index, JSON5Array def) {
		Object obj = get(index);
		if(obj == null) {
			return def;
		}
		JSON5Array JSON5Array = DataConverter.toArray(obj, true);
		if(JSON5Array == null) {
			return def;
		}
		return JSON5Array;
	}

	@Deprecated
	public JSON5Array getWrapJSON5Array(int index) {
		Object object = get(index);
		if(object == null) {
			return new JSON5Array();
		}
		JSON5Array JSON5Array = DataConverter.toArray(object, true);
		if(JSON5Array == null) {
			return new JSON5Array().put(object);
		}
		return JSON5Array;
	}

	public JSON5Object getJSON5Object(int index) {
		return getJSON5Object(index, null);
	}

	public JSON5Object getJSON5Object(int index, JSON5Object def) {
		Object obj = get(index);
		if(obj == null) {
			return def;
		}
		JSON5Object json5Object = DataConverter.toObject(obj, true);
		if(json5Object == null) {
			return def;
		}
		return json5Object;
	}


	public <T> T getObject(int index, Class<T> clazz) {
		return getObject(index, clazz, null);
	}

	public <T> T getObject(int index, Class<T> clazz, T defaultObject) {
		try {
			JSON5Object json5Object = getJSON5Object(index);
			return JSON5Serializer.fromJSON5Object(json5Object, clazz);
		} catch (Exception e) {
			return defaultObject;
		}
	}


	public <T> List<T> getList(int index, Class<T> valueType) {
		return getList(index, valueType, null);
	}

	public <T> List<T> getList(int index, Class<T> valueType, T defaultValue) {
		try {
			JSON5Array JSON5Array = getJSON5Array(index);
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
				return Collections.emptyList();
			}
		}
	}













	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	public boolean remove(int index) {
		try {
			list.remove(index);
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public boolean containsAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.containsAll(c);
	}


	@Override
	public boolean addAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		for(Object obj : c) {
			if(!add(obj)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * &#064;Deprecated  use {@link #subtractIntersection(JSON5Array)} instead.
	 * @param c collection containing elements to be removed from this collection
     */
	@Deprecated
	@Override
	public boolean removeAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(@SuppressWarnings({"rawtypes", "RedundantSuppression"}) Collection c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@SuppressWarnings("unused")
	public JSON5ArrayEnumerator enumeration() {
		return new JSON5ArrayEnumerator(this);
	}


	public static class JSON5ArrayEnumerator implements Enumeration<Object>  {
		int index = 0;
		JSON5Array array = null;

		private JSON5ArrayEnumerator(JSON5Array array) {
			this.array = array;
		}

		@Override
		public Object nextElement() {
			if(hasMoreElements()) {
				return array.get(index++);
			}
			return null;
		}


		@Deprecated
		public JSON5Array getArray() {
			return array.getJSON5Array(index++);
		}

		public JSON5Array getJSON5Array() {
			return array.getJSON5Array(index++);
		}

		@SuppressWarnings("unused")
		@Deprecated
		public int getInteger() {
			return array.getInteger(index++);
		}

		public int getInt() {
			return array.getInteger(index++);
		}

		@SuppressWarnings("unused")
		@Deprecated
		public int optInteger() {
			return array.getInteger(index++);
		}


		public short getShort() {
			return array.getShort(index++);
		}

		public float getFloat() {
			return array.getFloat(index++);
		}

		public String getString() {
			return array.getString(index++);
		}


		public boolean getBoolean() {
			return array.getBoolean(index++);
		}


		@Override
		public boolean hasMoreElements() {
			return !(index >= array.size());
		}
	}




	public byte[] toBytes() {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		BinaryCSONWriter writer = new BinaryCSONWriter(byteArrayOutputStream);
		writer.write(new JSON5ArrayDataContainer(this));
		return byteArrayOutputStream.toByteArray();
	}




	@Override
	protected void write(FormatWriter writer) {
		writer.write(new JSON5ArrayDataContainer(this));
	}


	@Override
	public String toString() {
		return toString(getWritingOptions());
	}

	public String toString(WritingOptions writingOptions) {
		if(writingOptions instanceof JSON5WriterOption) {
			JSON5Writer jsonWriter  = new JSON5Writer((JSON5WriterOption)writingOptions);
			write(jsonWriter);
			return jsonWriter.toString();
		}
		return this.toString();
	}



	@Override
	@SuppressWarnings({"MethodDoesntCallSuperMethod", "ForLoopReplaceableByForEach"})
	public JSON5Array clone() {
		JSON5Array array = new JSON5Array();
		for(int i = 0, n = list.size(); i < n; ++i) {
			Object obj = list.get(i);
			if(obj instanceof JSON5Array) array.add(((JSON5Array)obj).clone());
			else if(obj instanceof JSON5Object) array.add(((JSON5Object)obj).clone());
			else if(obj == NullValue.Instance) array.add(null);
			else if(obj instanceof CharSequence) array.add(((CharSequence)obj).toString());
			else if(obj instanceof byte[]) {
				byte[] bytes = (byte[])obj;
				byte[] newBytes = new byte[bytes.length];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				array.add(newBytes);
			}
			else array.add(obj);
		}
		return array;
	}

	/**
	 * 다른 JSON5Array와 병합한다.
	 * @param JSON5Array 병합할 JSON5Array
	 */
	public void merge(JSON5Array JSON5Array) {
		for(int i = 0, n = JSON5Array.size(); i < n; ++i) {
			Object newObj = JSON5Array.get(i);
			Object originObj = get(i);
			if(originObj == null) {
				add(newObj);
			} else if(originObj instanceof JSON5Array && newObj instanceof JSON5Array) {
				((JSON5Array)originObj).merge((JSON5Array)newObj);
			} else if(originObj instanceof JSON5Object && newObj instanceof JSON5Object) {
				((JSON5Object)originObj).merge((JSON5Object)newObj);
			} else {
				set(i, newObj);
			}
		}
	}


	/**
	 * 교집합을 반환한다.
	 * @param JSON5Array 교집합을 구할 JSON5Array
	 * @return 교집합
	 */
	public JSON5Array intersect(JSON5Array JSON5Array) {
		JSON5Array result = new JSON5Array();
		for(int i = 0, n = JSON5Array.size(); i < n; ++i) {
			Object newObj = JSON5Array.get(i);
			Object originObj = get(i);
			if(originObj == null) {
				continue;
			} else if(originObj instanceof JSON5Array && newObj instanceof JSON5Array) {
				result.add(((JSON5Array)originObj).intersect((JSON5Array)newObj));
			} else if(originObj instanceof JSON5Object && newObj instanceof JSON5Object) {
				result.add(((JSON5Object)originObj).intersect((JSON5Object)newObj));
			} else if(originObj.equals(newObj)) {
				result.add(originObj);
			}
		}
		return result;
	}

	/**
	 * 교집합을 제외한 값을 반환한다.
	 * @param JSON5Array 교집합을 제외할 JSON5Array
	 * @return 교집합을 제외한 값
	 */
	public JSON5Array subtractIntersection(JSON5Array JSON5Array) {
		JSON5Array result = new JSON5Array();
		for(int i = 0, n = size(); i < n; ++i) {
			Object originObj = get(i);
			Object newObj = JSON5Array.get(i);
			if(originObj == null) {
				continue;
			} else if(originObj instanceof JSON5Array && newObj instanceof JSON5Array) {
				result.add(((JSON5Array)originObj).subtractIntersection((JSON5Array)newObj));
			} else if(originObj instanceof JSON5Object && newObj instanceof JSON5Object) {
				result.add(((JSON5Object)originObj).subtractIntersection((JSON5Object)newObj));
			} else if(!originObj.equals(newObj)) {
				result.add(originObj);
			}
		}
		return result;

	}




	/**
	 * @deprecated use {@link #getJSON5Object(int)} instead.
	 */
	@Deprecated
	public JSON5Object getObject(int index) {
		return getJSON5Object(index);
	}



	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public int getInteger(int index) {
		return getInt(index);
	}

	@Deprecated
	public int getInteger(int index, int def) {
		return getInt(index, def);
	}



	/**
	 * @deprecated use {@link #getJSON5Array(int)} instead.
	 */
	@Deprecated
	public JSON5Array getArray(int index) {
		return getJSON5Array(index);
	}


	/**
	 * @deprecated use {@link #getJSON5Array(int, JSON5Array)} instead.
	 */
	@Deprecated
	public JSON5Array getArray(int index, JSON5Array def) {
		return getJSON5Array(index, def);
	}

	/**
	 * @deprecated use {@link #optInt(int)} instead.
	 */

	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public int optInteger(int index) {
		return optInt(index);
	}



	static ArrayDataContainerFactory ArrayDataContainerFactory = new ArrayDataContainerFactory() {
		@Override
		public ArrayDataContainer create() {
			return new JSON5ArrayDataContainer(new JSON5Array());
		}
	};

	static class JSON5ArrayDataContainer implements ArrayDataContainer  {
		final JSON5Array array;

		protected JSON5ArrayDataContainer(JSON5Array array) {
			this.array = array;
		}


		@Override
		public void add(Object value) {
			if(value instanceof JSON5Object.JSON5KeyValueDataContainer) {
				array.list.add(((JSON5Object.JSON5KeyValueDataContainer) value).json5Object);
				return;
			} else if(value instanceof ArrayDataContainer) {
				array.list.add(((JSON5ArrayDataContainer) value).array);
				return;
			}
			array.list.add(value);
		}

		@Override
		public Object get(int index) {
			return array.list.get(index);
		}

		@Override
		public void set(int index, Object value) {
			array.set(index, value);

		}


		@Override
		public void setComment(int index, String comment, CommentPosition position) {
			switch (position) {
				case HEADER:
					array.setHeaderComment(comment);
					break;
				case FOOTER:
					array.setFooterComment(comment);
					break;
				case DEFAULT:
				case BEFORE_VALUE:
				    array.setCommentForValue(index, comment);
					break;
				case AFTER_VALUE:
					array.setCommentAfterValue(index, comment);
					break;
			}

		}

		@Override
		public String getComment(int index, CommentPosition position) {
			switch (position) {
				case HEADER:
					return array.getHeaderComment();
				case FOOTER:
					return array.getFooterComment();
				case DEFAULT:
				case BEFORE_VALUE:
					return array.getCommentForValue(index);
				case AFTER_VALUE:
					return array.getCommentAfterValue(index);
			}
			return null;
		}

		@Override
		public void remove(int index) {
			array.list.remove(index);
		}

		@Override
		public int size() {
			return array.list.size();
		}

		@Override
		public CommentObject<Integer> getCommentObject(int index) {
			return array.getCommentObject(index);
		}

		@Override
		public void setSourceFormat(FormatType formatType) {
			switch (formatType) {
				case JSON:
					array.setWritingOptions(WritingOptions.json());
					break;
				case JSON5:
					array.setWritingOptions(WritingOptions.json5());
					break;
				case JSON5_PRETTY:
					array.setWritingOptions(WritingOptions.json5Pretty());
					break;
				case JSON_PRETTY:
					array.setWritingOptions(WritingOptions.jsonPretty());
					break;

			}

		}

		@Override
		public void setComment(CommentObject<?> commentObject) {
			Object index = commentObject.getIndex();
			if(index instanceof Integer) {
                //noinspection unchecked
                array.setCommentObject((Integer)index, (CommentObject<Integer>)commentObject);
			}
		}

		@Override
		public DataIterator<Object> iterator() {
			return new ArrayDataIterator(array.list.iterator(), array.list.size(), false);
		}

		private static class ArrayDataIterator extends DataIterator<Object> {

			public ArrayDataIterator(Iterator<Object> iterator, int size, boolean isEntryValue) {
				super(iterator, size, isEntryValue);
			}

			@Override
			public Object next() {
				Object value = super.next();
				if(value instanceof NullValue) {
					return null;
				} else if(value instanceof JSON5Object) {
					return new JSON5Object.JSON5KeyValueDataContainer((JSON5Object)value);
				} else if(value instanceof JSON5Array) {
					return new JSON5ArrayDataContainer((JSON5Array)value);
				}
				return value;
			}
		}

	}







}
