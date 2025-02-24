package com.hancomins.json5.serializer;

interface ISchemaNode {


    NodeType getNodeType();

    ISchemaNode copyNode();

}
