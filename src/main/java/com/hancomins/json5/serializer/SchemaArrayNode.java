package com.hancomins.json5.serializer;




public class SchemaArrayNode extends SchemaObjectNode {

    @Override
    public NodeType getNodeType() {
        return NodeType.ARRAY;
    }
}
