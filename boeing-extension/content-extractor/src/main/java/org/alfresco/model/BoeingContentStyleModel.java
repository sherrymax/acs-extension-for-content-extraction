package org.alfresco.model;

public final class BoeingContentStyleModel {

    public static String SECTION_FONT_NAME = "";
    public static Double SECTION_FONT_SIZE = 0.0;
    public static Boolean SECTION_FONT_BOLD = false;   // true | false

    public static String TITLE_FONT_NAME = "";
    public static Double TITLE_FONT_SIZE = 0.0;
    public static Boolean TITLE_FONT_BOLD = false;   // true | false

    public static String getSectionFontName(){
        return BoeingContentStyleModel.SECTION_FONT_NAME;
    }

    public static Double getSectionFontSize(){
        return BoeingContentStyleModel.SECTION_FONT_SIZE;
    }

    public static Boolean isSectionFontBold(){
        return BoeingContentStyleModel.SECTION_FONT_BOLD;
    }

    public static String getTitleFontName(){
        return BoeingContentStyleModel.TITLE_FONT_NAME;
    }

    public static Double getTitleFontSize(){
        return BoeingContentStyleModel.TITLE_FONT_SIZE;
    }

    public static Boolean isTitleFontBold(){
        return BoeingContentStyleModel.TITLE_FONT_BOLD;
    }

}
