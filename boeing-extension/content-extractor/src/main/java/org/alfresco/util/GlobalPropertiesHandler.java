package org.alfresco.util;

import org.alfresco.model.BoeingContentModel;
import org.alfresco.model.BoeingContentStyleModel;

public class GlobalPropertiesHandler {

    private String customerName = "";
    public BoeingContentModel boeingContentModel = new BoeingContentModel();
    public BoeingContentStyleModel boeingContentStyleModel = new BoeingContentStyleModel();


    public String getCustomerName() {
        return this.customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public void setBoeingNamespace(String boeingNamespace) {
        this.boeingContentModel.boeing_namespace = boeingNamespace;
    }
    public void setBoeingAspectName(String boeingAspectName) {
        this.boeingContentModel.boeing_aspectName = boeingAspectName;
    }
    public void setWritingSetProperty(String writingSetProperty) {
        this.boeingContentModel.writingSetProperty = writingSetProperty;
    }
    public void setReferencesListProperty(String referencesListProperty) {
        this.boeingContentModel.referencesListProperty = referencesListProperty;
    }
    public void setAuthorityReferencesProperty(String authorityReferencesProperty) {
        this.boeingContentModel.authorityReferencesProperty = authorityReferencesProperty;
    }
    public void setAuthorityReferencesListProperty(String authorityReferencesListProperty) {
        this.boeingContentModel.authorityReferencesListProperty = authorityReferencesListProperty;
    }
    public void setAuthorityReferencesSectionTitle(String authorityReferencesSectionTitle) {
        this.boeingContentModel.AUTHORITY_REFERENCE_TITLE = authorityReferencesSectionTitle;
    }

    /////////////////////////////////////////

    public void setSectionFontName(String fontName) {
        this.boeingContentStyleModel.SECTION_FONT_NAME = fontName;
    }
    public void setSectionFontSize(Double fontSize) {
        this.boeingContentStyleModel.SECTION_FONT_SIZE = fontSize;
    }
    public void setSectionFontBold(Boolean isBold) {
        this.boeingContentStyleModel.SECTION_FONT_BOLD = isBold;
    }
    public void setTitleFontName(String fontName) {
        this.boeingContentStyleModel.TITLE_FONT_NAME = fontName;
    }
    public void setTitleFontSize(Double fontSize) {
        this.boeingContentStyleModel.TITLE_FONT_SIZE = fontSize;
    }
    public void setTitleFontBold(Boolean isBold) {
        this.boeingContentStyleModel.TITLE_FONT_BOLD = isBold;
    }
}
