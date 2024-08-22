package org.alfresco.model;

public final class BoeingContentStyleModel {

    public static String SECTION_FONT_NAME = "Arial";
    public static Double SECTION_FONT_SIZE = 12.0;
    public static Boolean SECTION_FONT_BOLD = true;   // true | false

    public static String TITLE_FONT_NAME = "Arial";
    public static Double TITLE_FONT_SIZE = 18.0;
    public static Boolean TITLE_FONT_BOLD = true;   // true | false


    public static String getSectionFontName(){
        return BoeingContentStyleModel.SECTION_FONT_NAME;
    }
    public static void setSectionFontName(String fontName){
        BoeingContentStyleModel.SECTION_FONT_NAME = fontName;
    }

    public static Double getSectionFontSize(){
        return BoeingContentStyleModel.SECTION_FONT_SIZE;
    }
    public static void setSectionFontSize(Double fontSize){
        BoeingContentStyleModel.SECTION_FONT_SIZE = fontSize;
    }

    public static Boolean isSectionFontBold(){
        return BoeingContentStyleModel.SECTION_FONT_BOLD;
    }
    public static void setSectionFontBold(Boolean isBold){
        BoeingContentStyleModel.SECTION_FONT_BOLD = isBold;
    }


    public static String getTitleFontName(){
        return BoeingContentStyleModel.TITLE_FONT_NAME;
    }
    public static void setTitleFontName(String fontName){
        BoeingContentStyleModel.TITLE_FONT_NAME = fontName;
    }

    public static Double getTitleFontSize(){
        return BoeingContentStyleModel.TITLE_FONT_SIZE;
    }
    public static void setTitleFontSize(Double fontSize){
        BoeingContentStyleModel.TITLE_FONT_SIZE = fontSize;
    }

    public static Boolean isTitleFontBold(){
        return BoeingContentStyleModel.TITLE_FONT_BOLD;
    }
    public static void setTitleFontBold(Boolean isBold){
        BoeingContentStyleModel.TITLE_FONT_BOLD = isBold;
    }


}
