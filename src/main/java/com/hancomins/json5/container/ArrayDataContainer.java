package com.hancomins.json5.container;

import com.hancomins.json5.CommentObject;
import com.hancomins.json5.CommentPosition;

public interface ArrayDataContainer extends BaseDataContainer {
    void add(Object value);
    Object get(int index);
    void set(int index, Object value);
    void setComment(int index, String comment, CommentPosition position);
    String getComment(int index, CommentPosition position);
    void remove(int index);
    int size();
    CommentObject<Integer> getCommentObject(int index);

    default void setComment(String comment, CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                setComment(-1, comment, commentPosition);
        }
    }

    default String getComment(CommentPosition commentPosition) {
        switch (commentPosition) {
            case HEADER:
            case FOOTER:
                getComment(-1, commentPosition);
        }
        return null;
    }
}
