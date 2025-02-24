package com.hancomins.json5.container.cson;


import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;

class BinaryCSONParseIterator implements BinaryCSONBufferReader.ParseCallback {
	String selectKey = null;
	JSON5Element currentElement;
	JSON5Element root;
	byte[] versionRaw;
	
	public JSON5Element release() {
		JSON5Element result = root;
		selectKey = null;
		currentElement = null;
		root = null;
		return result;
	}
	
	
	
	@Override
	public void onVersion(byte[] versionRaw) {
		this.versionRaw = versionRaw;
	}
	
	@Override
	public void onValue(Object value) {	
		if(currentElement.getType() == JSON5Element.ElementType.Object) {
			((JSON5Object) currentElement).put(selectKey, value);
			selectKey = null;
		} else {
			((JSON5Array) currentElement).put(value);
		}
	}
	
	@Override
	public void onOpenObject() {
		JSON5Object obj = new JSON5Object();
		obj.setVersion(this.versionRaw);
		if(currentElement == null)
		{
			currentElement = obj;
			return;
		}				
		else if(currentElement.getType() == JSON5Element.ElementType.Object) {
			((JSON5Object) currentElement).put(selectKey, obj);
			selectKey = null;
		} else {
			((JSON5Array) currentElement).add(obj);
		}
		// todo 개서필요.
		//obj.setParents(currentElement);
		currentElement = obj;
	}
	
	@Override
	public void onOpenArray() {
		JSON5Array obj = new JSON5Array();
		obj.setVersion(this.versionRaw);
		if(currentElement == null)
		{
			currentElement = obj;
			return;
		}		
		else if(currentElement.getType() == JSON5Element.ElementType.Object) {
			((JSON5Object) currentElement).put(selectKey, obj);
			selectKey = null;
		} else {
			((JSON5Array) currentElement).add(obj);
		}
		// 개선 필요.
		//obj.setParents(currentElement);
		currentElement = obj;
		
		
	}
	
	@Override
	public void onKey(String key) {
		selectKey = key;
	}
	
	@Override
	public void onCloseObject() {
		onCloseCSONElement();
	}
	
	@Override
	public void onCloseArray() {
		onCloseCSONElement();
	}
	
	private void onCloseCSONElement() {
		// todo 개선 필욘
		//JSON5Element parents =  currentElement.getParents();
		/*if(parents ==null) {
			root = currentElement;
			return;
		}
		currentElement = parents;*/
	}

	
}
