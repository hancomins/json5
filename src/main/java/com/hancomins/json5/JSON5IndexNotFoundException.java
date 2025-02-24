package com.hancomins.json5;

public class JSON5IndexNotFoundException extends JSON5Exception {


	JSON5IndexNotFoundException(String object, int key) {
		super( object +  "['" + key + "'] is not found.");
	}

	JSON5IndexNotFoundException(String object, String key) {
		super( object +  "['" + key + "'] is not found.");
	}

	JSON5IndexNotFoundException(String object) {
		super(object);
	}
	
	JSON5IndexNotFoundException(Exception cause) {
		super(cause);
	}
}
