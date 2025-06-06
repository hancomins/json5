package com.hancomins.json5.container;

import com.hancomins.json5.CommentObject;
import com.hancomins.json5.CommentPosition;

import java.util.Set;


public interface KeyValueDataContainer extends BaseDataContainer {
    void put(String key, Object value);
    Object get(String key);
    void remove(String key);
    void setComment(String key, String comment, CommentPosition type);
    String getComment(String key, CommentPosition type);
    CommentObject<String> getCommentObject(String key);
    Set<String> keySet();
    String getLastAccessedKey();

    default void setComment(String comment, CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                setComment(null, comment, commentPosition);
        }
    }

    default String getComment(CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                return getComment(null, commentPosition);
        }
        return null;
    }

}
