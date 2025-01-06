package com.hancomins.cson.serializer.mapper;

import com.hancomins.cson.CSONObject;
import com.hancomins.cson.CommentObject;
import com.hancomins.cson.CommentPosition;
import com.hancomins.cson.container.DataIterator;
import com.hancomins.cson.container.FormatType;
import com.hancomins.cson.container.KeyValueDataContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContainerOfWildKeyValueType implements KeyValueDataContainer {

    private List<WildObjectSchemaRack> racks = new ArrayList<>();


    public void put(Object key, Object value) {

    }



    @Override
    public void put(String key, Object value) {

    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void setComment(String key, String comment, CommentPosition type) {

    }

    @Override
    public String getComment(String key, CommentPosition type) {
        return "";
    }

    @Override
    public CommentObject<String> getCommentObject(String key) {
        return null;
    }

    @Override
    public Set<String> keySet() {
        return Set.of();
    }

    @Override
    public String getLastAccessedKey() {
        return "";
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setSourceFormat(FormatType formatType) {

    }

    @Override
    public void setComment(CommentObject<?> commentObject) {

    }

    @Override
    public DataIterator<?> iterator() {
        return null;
    }


    private static class WildObjectSchemaRack {
        private Map<Object, Object> map;
        private CSONObject csonObject;
        private GenericItem genericItem;
        private boolean retired = false;
    }
}
