package com.hancomins.json5;


import com.hancomins.json5.container.FormatWriter;
import com.hancomins.json5.container.cson.BinaryCSONDataType;
import com.hancomins.json5.options.ParsingOptions;
import com.hancomins.json5.options.WritingOptions;
import com.hancomins.json5.util.NullValue;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract  class JSON5Element implements Iterable<Object>  {

    private WritingOptions writingOptions = WritingOptions.getDefaultWritingOptions();

	private static final Pattern BASE64_PREFIX_REPLACE_PATTERN = Pattern.compile("(?i)^base64,");
	private static final Pattern BASE64_PREFIX_PATTERN = Pattern.compile("^((?i)base64,)([a-zA-Z0-9+/]*={0,2})$");
	private CommentObject<?> headTailCommentObject = null;

	private JSON5Path json5Path = null;


	//private JSON5Element parents = null;
	private byte[] versionRaw = BinaryCSONDataType.VER_RAW;
	private final ElementType type;




	protected boolean allowJsonPathKey = true;
	private boolean allowRawValue = false;
	private boolean unknownObjectToString = false;


	protected JSON5Element(ElementType type, ParsingOptions<?> parsingOptions, WritingOptions writingOptions) {
		this.type = type;
        this.writingOptions = writingOptions;
	}

	protected JSON5Element(ElementType type, WritingOptions writingOptions) {
		this.type = type;
		this.writingOptions = writingOptions;
	}

	protected JSON5Element(ElementType type) {
		this.type = type;
	}

	protected JSON5Element setAllowRawValue(boolean allowRawValue) {
		this.allowRawValue = allowRawValue;
		return this;
	}

	@SuppressWarnings({"unchecked", "unused"})
	public <T extends JSON5Element> T setAllowJSON5PathKey(boolean allowJSON5PathKey) {
		this.allowJsonPathKey = allowJSON5PathKey;
		return (T)this;
	}

	@SuppressWarnings({"unchecked", "unused"})
	public <T extends JSON5Element> T setUnknownObjectToString(boolean unknownObjectToString) {
		this.unknownObjectToString = unknownObjectToString;
		return (T)this;
	}

	@SuppressWarnings({"unchecked", "unused"})
	public <T extends JSON5Element> T setWritingOptions(WritingOptions writingOptions) {
		this.writingOptions = writingOptions;
        //noinspection unchecked
        return (T) this;
	}


	protected boolean isUnknownObjectToString() {
		return unknownObjectToString;
	}

	public boolean isAllowRawValue() {
		return allowRawValue;
	}


	public WritingOptions getWritingOptions() {
		return writingOptions;
	}



	@SuppressWarnings({"unchecked", "UnusedReturnValue"})
	public <T extends JSON5Element> T setHeaderComment(String comment) {
		if(headTailCommentObject == null) {
			headTailCommentObject = CommentObject.forRootElement();
		}
		headTailCommentObject.setComment(CommentPosition.HEADER, comment);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public  <T extends JSON5Element> T setFooterComment(String comment) {
		if(headTailCommentObject == null) {
			headTailCommentObject = CommentObject.forRootElement();
		}
		headTailCommentObject.setComment(CommentPosition.FOOTER, comment);
		return (T) this;
	}



	public String getHeaderComment() {
		return  headTailCommentObject == null ? null : headTailCommentObject.getComment(CommentPosition.HEADER);
	}

	public String getFooterComment() {
		return headTailCommentObject == null ? null : headTailCommentObject.getComment(CommentPosition.FOOTER);
	}


	protected final JSON5Path getJSON5Path() {
		if(json5Path == null) {
			json5Path = new JSON5Path(this);
		}
		return json5Path;
	}



	protected abstract void write(FormatWriter writer);


	public abstract String toString(WritingOptions option);


	public enum ElementType { Object, Array}



	/*public void setParents(JSON5Element parents) {
		this.parents = parents;
	}*/



	/*public JSON5Element getParents() {
		return parents;
	}*/

	public ElementType getType() {
		return type;
	}


	public String getVersion() {
		return Short.toString(ByteBuffer.wrap(versionRaw).getShort());
	}

	public void setVersion(byte[] versionRaw) {
		this.versionRaw = versionRaw;
	}



	@SuppressWarnings("unchecked")
	public static <T extends JSON5Element> T clone(T element) {
		if(element == null) return null;
		if(element instanceof JSON5Object) {
			return (T) ((JSON5Object)element).clone();
		}
		return (T) ((JSON5Array)element).clone();
	}


	public static boolean isBase64String(String value) {
		return BASE64_PREFIX_PATTERN.matcher(value).matches();
	}

	public static byte[] base64StringToByteArray(String value) {
		value = BASE64_PREFIX_REPLACE_PATTERN.matcher(value).replaceAll("");
		return Base64.getDecoder().decode(value);
	}

	public abstract void clear();



	protected static boolean containsNoStrict(Collection<Object> valueList, Object value) {
		for(Object obj : valueList) {
			boolean result = Objects.equals(obj, value);
			if(result) return true;
			else if(obj == NullValue.Instance || obj == null) {
				if(value == NullValue.Instance || value == null) return true;
				else continue;
			} else if(value == null) {
				return false;
			}
			String strObj = obj instanceof byte[] ? Base64.getEncoder().encodeToString((byte[]) obj) : obj.toString();
			String strValue = value instanceof byte[] ? Base64.getEncoder().encodeToString((byte[])value) : value.toString();
			if(strObj.equals(strValue)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JSON5Element) {
			return JSON5Elements.equals(this, (JSON5Element)obj);
		}
		return false;
	}

	public boolean equalsIgnoreTypes(Object json5Element) {
		if(json5Element instanceof JSON5Element) {
			return JSON5Elements.equalsIgnoreTypes(this, (JSON5Element)json5Element);
		}
		else if(json5Element instanceof String) {
			return this.toString().equals(json5Element);
		}
		return false;
	}



}
