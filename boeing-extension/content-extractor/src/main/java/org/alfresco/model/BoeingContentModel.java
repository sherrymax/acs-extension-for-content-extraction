package org.alfresco.model;

import org.alfresco.service.namespace.QName;

public final class BoeingContentModel {

    /*
    public String boeing_namespace = "http://www.boeing.com/model/onepppm/1.0";
    public static String tsg_namespace = "http://www.alfresco.org/model/dictionary/1.0";
    public static String boeing_aspectName = "content-extractor";
    public static String writingSetProperty = "writingSet";
    public static String referencesListProperty = "referencesList";
    public static String authorityReferencesProperty = "authorityReferences";
    public static String authorityReferencesListProperty = "authorityReferencesList";
    public static String AUTHORITY_REFERENCE = "AUTHORITY REFERENCE";
    */

    public static String boeing_namespace = "";
    public static String boeing_aspectName = "";
    public static String writingSetProperty = "";
    public static String referencesListProperty = "";
    public static String authorityReferencesProperty = "";
    public static String authorityReferencesListProperty = "";
    public static String AUTHORITY_REFERENCE_TITLE = "";


    public static String getBoeingNamespace(){
        return BoeingContentModel.boeing_namespace;
    }

    public static String getBoeingAspectName(){
        return BoeingContentModel.boeing_aspectName;
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

    public static String getAuthorityReferencesSectionTitle(){
        return BoeingContentModel.AUTHORITY_REFERENCE_TITLE;
    }




}
