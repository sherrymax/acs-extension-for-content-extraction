package org.alfresco.model;

import org.alfresco.service.namespace.QName;

public final class BoeingContentModel {

    public static String namespace = "http://www.boeing.com/model/onepppm/1.0";
    public static String aspectName = "content-extractor";
    public static String writingSetProperty = "writingSet";
    public static String referencesListProperty = "referencesList";

    public static String authorityReferencesProperty = "authorityReferences";
    public static String authorityReferencesListProperty = "authorityReferencesList";


    public static String getNamespace(){
        return BoeingContentModel.namespace;
    }

    public static String getAspectName(){
        return BoeingContentModel.aspectName;
    }

    public static String getWritingSetProperty(){
        return BoeingContentModel.writingSetProperty;
    }

    public static String getReferencesListProperty(){
        return BoeingContentModel.referencesListProperty;
    }

    public static String getAuthorityReferencesProperty(){
        return BoeingContentModel.authorityReferencesProperty;
    }

    public static String getAuthorityReferencesListProperty(){
        return BoeingContentModel.authorityReferencesListProperty;
    }

    public static final QName ASPECT_BOEING_ONEPPPM = QName.createQName(BoeingContentModel.getNamespace(), BoeingContentModel.getAspectName());
    public static final QName PROP_WRITING_SET = QName.createQName(BoeingContentModel.getNamespace(), BoeingContentModel.getWritingSetProperty());
    public static final QName PROP_REFERENCES_LIST = QName.createQName(BoeingContentModel.getNamespace(), BoeingContentModel.getReferencesListProperty());
    public static final QName PROP_AUTHORITY_REFERENCES = QName.createQName(BoeingContentModel.getNamespace(), BoeingContentModel.getAuthorityReferencesProperty());
    public static final QName PROP_AUTHORITY_REFERENCES_LIST = QName.createQName(BoeingContentModel.getNamespace(), BoeingContentModel.getAuthorityReferencesListProperty());

}
