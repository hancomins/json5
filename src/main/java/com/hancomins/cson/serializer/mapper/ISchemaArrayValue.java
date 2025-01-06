package com.hancomins.cson.serializer.mapper;

import java.util.List;

interface ISchemaArrayValue extends ISchemaValue {

     SchemaType getEndpointValueType();

     Class<?> getEndpointValueTypeClass();


     List<GenericItem> getGenericItems();

     default GenericItem getGenericItem() {
         if(getGenericItems().isEmpty()) {
             return null;
         }
            return getGenericItems().get(0);
     }

    default GenericItem getGenericItem(int index) {
        List<GenericItem> collectionItems = getGenericItems();
        if(index < 0 || index >= collectionItems.size()) {
            return null;
        }
        return collectionItems.get(index);
    }







     static boolean equalsCollectionTypes(List<GenericItem> a, List<GenericItem> b) {
            if(a.size() != b.size()) {
                return false;
            }
            for(int i = 0; i < a.size(); i++) {
                if(!a.get(i).compatibleCollectionType(b.get(i))) {
                    return false;
                }

            }
            return true;
     }




}
