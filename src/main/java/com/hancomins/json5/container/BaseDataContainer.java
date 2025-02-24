package com.hancomins.json5.container;

import com.hancomins.json5.CommentObject;
import com.hancomins.json5.CommentPosition;


public interface BaseDataContainer  {
    int size();
    void setSourceFormat(FormatType formatType);
    void setComment(String comment, CommentPosition commentPosition);
    void setComment(CommentObject<?> commentObject);
    String getComment(CommentPosition commentPosition);
    DataIterator<?> iterator();

    default void end() {
    }

}
