package com.hancomins.json5;

import com.hancomins.json5.serializer.path.JSON5PathExtractor;
import java.util.List;

public class JSON5Path {

    private final JSON5Element JSON5Element;

    public JSON5Path(JSON5Element JSON5Element) {
        this.JSON5Element = JSON5Element;
    }

    public Boolean optBoolean(String path) {
        return optBoolean(path, null);
    }

    public Boolean optBoolean(String path, Boolean defaultValue) {
        Object obj = get(path);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if(obj instanceof Number) {
            return ((Number)obj).intValue() == 1;
        } else if(obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return defaultValue;
    }

    public Double optDouble(String path) {
        return optDouble(path, null);
    }

    public Double optDouble(String path, Double defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0 : 0.0;
        }
        else if(obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Float optFloat(String path) {
        return optFloat(path, null);
    }

    public Float optFloat(String path, Float defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).floatValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0f : 0.0f;
        }
        else if(obj instanceof String) {
            try {
                return Float.parseFloat((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }


        return defaultValue;
    }

    public Long optLong(String path) {
        return optLong(path, null);
    }

    public Long optLong(String path, Long defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).longValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1L : 0L;
        }
        else if(obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Short optShort(String path) {
        return optShort(path, null);
    }



    public Short optShort(String path, Short defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).shortValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (short)1 : (short)0;
        }
        else if(obj instanceof String) {
            try {
                return Short.parseShort((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Byte optByte(String path) {
        return optByte(path, null);
    }

    public Byte optByte(String path, Byte defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).byteValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (byte)1 : (byte)0;
        }
        else if(obj instanceof String) {
            try {
                return Byte.parseByte((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }


    public Integer optInteger(String path) {
        return optInteger(path, null);
    }

    public Integer optInteger(String path, Integer defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).intValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1 : 0;
        }
        else if(obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String optString(String path, String defaultValue) {
        Object obj = get(path);
        if (obj instanceof String) {
            return (String) obj;
        } else if(obj instanceof Number) {
            return String.valueOf(obj);
        }
        return defaultValue;
    }

    public String optString(String path) {
        return optString(path, null);
    }

    public JSON5Object optJSON5Object(String path) {
        Object obj = get(path);
        if (obj instanceof JSON5Object) {
            return (JSON5Object) obj;
        }
        return null;
    }


    public JSON5Array optJSON5Array(String path) {
        Object obj = get(path);
        if (obj instanceof JSON5Array) {
            return (JSON5Array) obj;
        }
        return null;
    }


    private JSON5Element obtainOrCreateChild(JSON5Element JSON5Element, PathItem pathItem) {
        if(JSON5Element instanceof JSON5Object && !pathItem.isInArray() && pathItem.isObject()) {
            JSON5Object json5Object = (JSON5Object) JSON5Element;
            String name = pathItem.getName();
            if(pathItem.isArrayValue()) {
                JSON5Array childJSON5Array = json5Object.optJSON5Array(name);
                if(childJSON5Array == null) {
                    childJSON5Array = new JSON5Array();
                    childJSON5Array.setAllowRawValue(JSON5Element.isAllowRawValue())
                                    .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                    json5Object.put(name, childJSON5Array);
                }

                return childJSON5Array;
            } else {
                JSON5Object childJSON5Object = json5Object.optJSON5Object(name);
                if(childJSON5Object == null) {
                    childJSON5Object = new JSON5Object();
                    childJSON5Object.setAllowRawValue(JSON5Element.isAllowRawValue())
                            .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                    json5Object.put(name, childJSON5Object);
                }
                return childJSON5Object;
            }
        } else if(JSON5Element instanceof JSON5Array && pathItem.isInArray()) {
            JSON5Array JSON5Array = (JSON5Array) JSON5Element;
            int index = pathItem.getIndex();
            if(pathItem.isObject()) {
                JSON5Object childJSON5Object = JSON5Array.getJSON5Object(index);
                if(childJSON5Object == null) {
                    childJSON5Object = new JSON5Object();
                    childJSON5Object.setAllowRawValue(JSON5Element.isAllowRawValue())
                            .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                    JSON5Array.set(index, childJSON5Object);
                    if(pathItem.isArrayValue()) {
                        JSON5Array childJSON5Array = new JSON5Array();
                        childJSON5Array.setAllowRawValue(JSON5Element.isAllowRawValue())
                                .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                        childJSON5Object.put(pathItem.getName(), childJSON5Array);
                        return childJSON5Array;
                    }
                    JSON5Object childAndChildJSON5Object = new JSON5Object();
                    childAndChildJSON5Object.setAllowRawValue(JSON5Element.isAllowRawValue())
                            .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                    childJSON5Object.put(pathItem.getName(), childAndChildJSON5Object);
                    return childAndChildJSON5Object;
                } else  {
                    if(pathItem.isArrayValue()) {
                        JSON5Array childChildJSON5Array = childJSON5Object.optJSON5Array(pathItem.getName());
                        if (childChildJSON5Array == null) {
                            childChildJSON5Array = new JSON5Array();
                            childChildJSON5Array.setAllowRawValue(JSON5Element.isAllowRawValue())
                                    .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                            childJSON5Object.put(pathItem.getName(), childChildJSON5Array);
                        }
                        return childChildJSON5Array;
                    } else {
                        JSON5Object childAndChildJSON5Object = childJSON5Object.optJSON5Object(pathItem.getName());
                        if (childAndChildJSON5Object == null) {
                            childAndChildJSON5Object = new JSON5Object();
                            childAndChildJSON5Object.setAllowRawValue(JSON5Element.isAllowRawValue())
                                    .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                            childJSON5Object.put(pathItem.getName(), childAndChildJSON5Object);
                        }
                        return childAndChildJSON5Object;
                    }
                }
            }
            else if(pathItem.isArrayValue()) {
                JSON5Array childJSON5Array = JSON5Array.getJSON5Array(index);
                if(childJSON5Array == null) {
                    childJSON5Array = new JSON5Array();
                    childJSON5Array.setAllowRawValue(JSON5Element.isAllowRawValue())
                            .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                    JSON5Array.set(index, childJSON5Array);
                }
                return childJSON5Array;
            }


            throw new IllegalArgumentException("Invalid path. " + pathItem);
        } else {
            throw new IllegalArgumentException("Invalid path. " + pathItem);
        }
    }



    private void putValue(JSON5Element JSON5Element, PathItem pathItem, Object value) {
        if(pathItem.isInArray()) {
            if(pathItem.isObject()) {
                int index = pathItem.getIndex();
                JSON5Object childJSON5Object = ((JSON5Array) JSON5Element).getJSON5Object(index);
                if(childJSON5Object == null) {
                    childJSON5Object = new JSON5Object();
                    JSON5Element.setAllowRawValue(JSON5Element.isAllowRawValue())
                            .setUnknownObjectToString(JSON5Element.isUnknownObjectToString());
                    ((JSON5Array) JSON5Element).set(index, childJSON5Object);
                }
                childJSON5Object.put(pathItem.getName(), value);
            } else {
                ((JSON5Array) JSON5Element).set(pathItem.getIndex(), value);
            }
        } else {
            ((JSON5Object) JSON5Element).put(pathItem.getName(), value);
        }
    }



    public JSON5Path put(String path, Object value) {
        List<PathItem> list = PathItem.parseMultiPath2(path);
        JSON5Element lastJSON5Element = this.JSON5Element;
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = list.size(); i < n; ++i) {
            PathItem pathItem = list.get(i);
            if(pathItem.isEndPoint()) {
                putValue(lastJSON5Element, pathItem, value);
                break;
            }
            lastJSON5Element = obtainOrCreateChild(lastJSON5Element, pathItem);
        }
        return this;
    }


    public boolean remove(String path) {
        List<PathItem> pathItemList = PathItem.parseMultiPath2(path);
        Object parents = JSON5Element;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = pathItemList.size(); i < n; ++i) {
            PathItem pathItem = pathItemList.get(i);

            if (pathItem.isEndPoint()) {
                if (pathItem.isInArray()) {
                    if(pathItem.isObject()) {
                        JSON5Object endPointObject = ((JSON5Array) parents).getJSON5Object(pathItem.getIndex());
                        if(endPointObject == null) return false;
                        endPointObject.remove(pathItem.getName());
                        return true;
                    }
                    else {
                        ((JSON5Array)parents).remove(pathItem.getIndex());
                        return true;
                    }
                } else {
                    ((JSON5Object) parents).remove(pathItem.getName());
                    return true;
                }
            }
            else if((parents instanceof JSON5Object && pathItem.isInArray()) || (parents instanceof JSON5Array && !pathItem.isInArray())) {
                return false;
            }
            else {
                if (pathItem.isInArray()) {
                    assert parents instanceof JSON5Array;
                    parents = ((JSON5Array) parents).get(pathItem.getIndex());
                    if(pathItem.isObject() && parents instanceof JSON5Object) {
                        parents = ((JSON5Object) parents).opt(pathItem.getName());
                    }
                } else {
                    assert parents instanceof JSON5Object;
                    parents = ((JSON5Object) parents).opt(pathItem.getName());
                }
            }
        }
        return false;
    }

    public boolean has(String path) {
        // JSON5PathExtractor 사용으로 통합
        return JSON5PathExtractor.pathExists(this.JSON5Element, path);
    }



    public Object get(String path) {
        // JSON5PathExtractor 사용으로 통합
        Object result = JSON5PathExtractor.extractValue(this.JSON5Element, path);
        return JSON5PathExtractor.isMissingValue(result) ? null : result;
    }

}
