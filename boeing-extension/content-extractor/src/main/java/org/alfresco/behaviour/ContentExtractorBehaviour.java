package org.alfresco.behaviour;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.behaviour.References;
import org.alfresco.model.BoeingContentModel;
import org.alfresco.model.BoeingContentStyleModel;
import org.alfresco.util.GlobalPropertiesHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

////import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.Serializable;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import com.aspose.words.*;
import com.fasterxml.jackson.databind.ObjectMapper;

//NodeServicePolicies.onUpdateProperties
public class ContentExtractorBehaviour implements ContentServicePolicies.OnContentPropertyUpdatePolicy, NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private ContentService contentService;
    private GlobalPropertiesHandler globalPropertiesHandler;

    private BoeingContentModel boeingContentModel = new BoeingContentModel();
    private BoeingContentStyleModel boeingContentStyleModel = new BoeingContentStyleModel();


    private String referencesListAsJSON = ""; //JSON representation of Reference Object List
    private String authReferencesListAsJSON = ""; //JSON representation of Reference Object List
    private ArrayList<String> references = new ArrayList<String>();
    private ArrayList<String> authReferences = new ArrayList<String>();

    private String docTitle = "";
    private String currentSectionName = "";
    private Boolean isTitleExtracted = false;
    private ArrayList sectionNames = new ArrayList();
    private ArrayList sectionNameList = new ArrayList();
    private ArrayList refList = new ArrayList();
    private ArrayList authRefList = new ArrayList();


//    AN OPTION TO FETCH VALUES FROM alfresco-global.properties
//    @Value("${customer.name}")
//    private String customerName;


    public void init() {

        this.setAsposeLicense();

        //On Content Upload/Update Or Property Update
        policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onContentPropertyUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        //On Content Update
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        //On Property Update
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onContentPropertyUpdate(final NodeRef nodeRef, QName propertyQName, ContentData beforeValue, ContentData afterValue) {
        if (nodeService.exists(nodeRef)) {

            ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

            String myFileName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
            if ((myFileName.trim().indexOf(".docx") != -1)) {
                this.buildAsposeDocument(nodeRef);
            }
        }
    }

    @Override
    public void onUpdateNode(final NodeRef nodeRef) {
        if (nodeService.exists(nodeRef)) {
            try {
                Map<QName, Serializable> allProperties = nodeService.getProperties(nodeRef);

                /*
                System.out.println(">>>> ***** >>>>> START OF onUpdateNode() >>>> ***** >>>>> ");
                System.out.println("-------- ALL PROPERTIES : START -------");
                System.out.println(allProperties);
                System.out.println("-------- ALL PROPERTIES : END -------");
                System.out.println(">>>> ***** >>>>> END OF onUpdateNode() >>>> ***** >>>>> ");
                */

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> beforeValues, Map<QName, Serializable> afterValues) {
        if (nodeService.exists(nodeRef)) {
            try {
                /*
                System.out.println(">>>> ***** >>>>> START OF onUpdateProperties() >>>> ***** >>>>> ");
                String nodeId = nodeRef.getId();
                Serializable fileName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                System.out.println("NODE ID : "+nodeId);
                System.out.println("FILE NAME : "+fileName.toString());

                System.out.println("-------- PROPERTIES (BEFORE UPDATING) : START -------");
                System.out.println(beforeValues);
                System.out.println("-------- PROPERTIES (BEFORE UPDATING) : END -------");
                System.out.println("-------- PROPERTIES (AFTER UPDATING) : START -------");
                System.out.println(afterValues);
                System.out.println("-------- PROPERTIES (AFTER UPDATING) : END -------");

                System.out.println(">>>> ***** >>>>> END OF onUpdateProperties() >>>> ***** >>>>> ");
                */
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void extractSectionNames(final NodeRef nodeRef) {

        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setEncoding(StandardCharsets.UTF_8);
        this.sectionNames = new ArrayList();

        System.out.println(">>>> ***** >>>>> START OF extractSections() >>>> ***** >>>>> ");
        if (nodeService.exists(nodeRef)) {
            try {
                ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                InputStream is = contentReader.getContentInputStream();
                Document doc = new Document(is, loadOptions);

                for (Paragraph para : (Iterable<Paragraph>) doc.getChildNodes(NodeType.PARAGRAPH, true)) {
                    Double contentFontSize = para.getParagraphFormat().getStyle().getFont().getSize();
                    Boolean isContentBold = para.getParagraphFormat().getStyle().getFont().getBold();

                    if (isContentBold && (contentFontSize == 12.0)) {
                        String paraText = para.toString(SaveFormat.TEXT).toUpperCase();
                        paraText = paraText.replace("\n", "").replace("\r", "");
                        if (paraText.length() > 0) {
                            System.out.println(">>>" + paraText + "<<<");
                            this.sectionNames.add(paraText);
                        }
                    }
                }
            } catch (java.lang.Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(">>>> ***** >>>>> END OF extractSections() >>>> ***** >>>>> ");
    }


    public void buildAsposeDocument(final NodeRef nodeRef) {
        this.initialiseValues();
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setEncoding(StandardCharsets.UTF_8);

        if (nodeService.exists(nodeRef)) {
            try {
                ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                InputStream is = contentReader.getContentInputStream();
                Document contentDoc = new Document(is, loadOptions);
                BookmarkCollection bookmarks = contentDoc.getRange().getBookmarks();
                String previousParaText = "-";
                Boolean fetchedDocTitle = false;

                String referencesListAsJSON = "";
                String authReferencesListAsJSON = "";
                Map<String, String> referencesMap = new HashMap<String, String>();
                int referencesCount = 0;

                this.extractSectionNames(nodeRef);

                System.out.println("Number of Sections are : " + contentDoc.getSections().getCount());
                System.out.println("Document opened. Total pages are : " + contentDoc.getPageCount());

                int paraCount = 0;
                int tableCount = 0;

                for (Node node : (Iterable<Node>) contentDoc.getChildNodes(NodeType.ANY, true)) {
                    if (node instanceof Paragraph) {
                        paraCount++;
                        Paragraph paragraph = (Paragraph) node;

                        this.determineCurrentSectionName(paragraph);
//                        System.out.println("Current Section Name >> " + this.currentSectionName);


                        for (int fieldItr = 0; fieldItr < paragraph.getRange().getFields().getCount(); fieldItr++) {
                            Field field = paragraph.getRange().getFields().get(fieldItr);
                            if (field.getType() == 88) //88 is code for Hyperlink in ASPOSE
                            {
                                FieldHyperlink hyperlink = (FieldHyperlink) field;
                                String hyperlinkText = hyperlink.getResult();
                                if (this.isWriting(hyperlinkText)) {
                                    System.out.println("Paragraph found >> Reference Found >> Added to List >> " + hyperlinkText);
                                    referencesMap.put(hyperlinkText, hyperlinkText);
                                }

                                // Some hyperlinks can be local (links to bookmarks inside the document), ignore such.
                                if (hyperlink.getSubAddress() != null)
                                    continue;

                                References ref = new References(this.currentSectionName, hyperlink.getResult(), hyperlink.getAddress());
                                this.refList.add(new ObjectMapper().writeValueAsString(ref));
                                this.getAuthorityReferences(hyperlink);

//                                sectionNameList.add(currentSectionName);
//                                hyperLinkList.add(hyperlinkText);
                            }
                        }


                    } else if (node instanceof Table) {
//                        System.out.println("Table found");
                        tableCount++;

                        Table table = (Table) node;

                        // Process the table
                        for (Row row : table.getRows()) {
                            CellCollection cells = row.getCells();
                            for (int cellItr = 0; cellItr < cells.getCount(); cellItr++) {
//                                String cellText = cells.get(c).toString(SaveFormat.TEXT).trim();
//                                System.out.println(MessageFormat.format("\t\tContents of Cell:{0} = \"{1}\"", c, cellText));
                                for (int fieldItr = 0; fieldItr < cells.get(cellItr).getRange().getFields().getCount(); fieldItr++) {
                                    Field field = cells.get(cellItr).getRange().getFields().get(fieldItr);
//                                    System.out.println(">>> >>> field.Type >>> >>> " + field.getType() + " >>> "+field.getDisplayResult());

                                    if (field.getType() == 88) //88 is code for Hyperlink
                                    {
                                        FieldHyperlink hyperlink = (FieldHyperlink) field;
                                        String hyperlinkText = hyperlink.getResult();
                                        if (this.isWriting(hyperlinkText)) {
                                            System.out.println("Table found >> Reference Found >> Added to List >> " + hyperlinkText);
                                            referencesMap.put(hyperlinkText, hyperlinkText);
                                        }

                                        // Some hyperlinks can be local (links to bookmarks inside the document), ignore such.
                                        if (hyperlink.getSubAddress() != null)
                                            continue;

                                        References ref = new References(this.currentSectionName, hyperlink.getResult(), hyperlink.getAddress());
                                        this.refList.add(new ObjectMapper().writeValueAsString(ref));
                                        this.getAuthorityReferences(hyperlink);

                                    }
                                }
                            }
                        }

                    }
                }

                this.referencesListAsJSON = new ObjectMapper().writeValueAsString(this.refList);
                this.authReferencesListAsJSON = new ObjectMapper().writeValueAsString(this.authRefList);


                String[] keyArray = referencesMap.keySet().toArray(new String[0]);
                for (var mapItr = 0; mapItr < referencesMap.size(); mapItr++) {
                    this.references.add(keyArray[mapItr]);
                }

//                    System.out.println(referencesMap.keySet());
//                    System.out.println(referencesMap.size());
//                    for (var mapItr = 0; mapItr < referencesMap.size(); mapItr++) {
//                        String currentKey = keyArray[mapItr];
//                        this.references.add(keyArray[mapItr]);
//
//                        this.writings = this.writings + currentKey;
//                        if (referencesMap.size() - mapItr > 1)
//                            this.writings = this.writings + ",";
//                    }

//                System.out.println("*** *** *** WRITINGS - STRING *** *** ***");
//                System.out.println(this.writings);
                System.out.println("*** *** *** REFERENCES : ARRAY LIST *** *** ***");
                System.out.println(this.references);
                System.out.println("*** *** *** REFERENCES : AS JSON *** *** ***");
                System.out.println(this.referencesListAsJSON);
                System.out.println("*** *** *** AUTHORITY REFERENCES : ARRAY LIST  *** *** ***");
                System.out.println(this.authReferences);
                System.out.println("*** *** *** AUTHORITY REFERENCES : AS JSON *** *** ***");
                System.out.println(this.authReferencesListAsJSON);

                System.out.println("*** *** *** <><><> *** *** ***");

                this.applyWebPublishedAspect(nodeRef);


                System.out.println("Number of Paragraphs ---> " + paraCount);
                System.out.println("Number of Tables ---> " + tableCount);

            } catch (java.lang.Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void initialiseValues() {
        this.referencesListAsJSON = ""; //JSON representation of Reference Object List
        this.authReferencesListAsJSON = ""; //JSON representation of Reference Object List
        this.references = new ArrayList<String>();
        this.authReferences = new ArrayList<String>();
        this.docTitle = "";
        this.currentSectionName = "";
        this.isTitleExtracted = false;
        this.sectionNames = new ArrayList();
        this.sectionNameList = new ArrayList();
        this.refList = new ArrayList();
        this.authRefList = new ArrayList();
    }

    public void applyWebPublishedAspect(NodeRef nodeRef) {


        /*
        System.out.println("CUSTOMER NAME >>> "+this.customerName );
        System.out.println("CUSTOMER NAME >>> "+this.globalPropertiesHandler.getCustomerName() );
        System.out.println("BOEING NAMESPACE >>> " + this.globalPropertiesHandler.getBoeingNamespace());
        System.out.println("BOEING ASPECT NAME >>> " + this.globalPropertiesHandler.getBoeingAspectName());
        System.out.println("BOEING writingSetProperty >>> " + this.globalPropertiesHandler.getWritingSetProperty());
        System.out.println("BOEING referencesListProperty >>> " + this.globalPropertiesHandler.getReferencesListProperty());
        System.out.println("BOEING authorityReferencesProperty >>> " + this.globalPropertiesHandler.getAuthorityReferencesProperty());
        System.out.println("BOEING authorityReferencesListProperty >>> " + this.globalPropertiesHandler.getAuthorityReferencesListProperty());
        System.out.println("BOEING authorityReferencesSectionTitle >>> " + this.globalPropertiesHandler.getAuthorityReferencesSectionTitle());

        System.out.println("BOEING sectionFontName >>> " + this.globalPropertiesHandler.getSectionFontName());
        System.out.println("BOEING sectionFontSize >>> " + this.globalPropertiesHandler.getSectionFontSize());
        System.out.println("BOEING sectionFontBold >>> " + this.globalPropertiesHandler.getSectionFontBold());

        System.out.println("BOEING titleFontName >>> " + this.globalPropertiesHandler.getTitleFontName());
        System.out.println("BOEING titleFontSize >>> " + this.globalPropertiesHandler.getTitleFontSize());
        System.out.println("BOEING titleFontBold >>> " + this.globalPropertiesHandler.getTitleFontBold());

        System.out.println("<<<< <<<<< >>>> >>> " );

        System.out.println("BOEING NAMESPACE >>> " + this.boeingContentModel.getBoeingNamespace());
        System.out.println("BOEING ASPECT NAME >>> " + this.boeingContentModel.getBoeingAspectName());
        System.out.println("BOEING writingSetProperty >>> " + this.boeingContentModel.getWritingSetProperty());
        System.out.println("BOEING referencesListProperty >>> " + this.boeingContentModel.getReferencesListProperty());
        System.out.println("BOEING authorityReferencesProperty >>> " + this.boeingContentModel.getAuthorityReferencesProperty());
        System.out.println("BOEING authorityReferencesListProperty >>> " + this.boeingContentModel.getAuthorityReferencesListProperty());
        System.out.println("BOEING authorityReferencesSectionTitle >>> " + this.boeingContentModel.getAuthorityReferencesSectionTitle());

        System.out.println("BOEING sectionFontName >>> " + this.boeingContentStyleModel.getSectionFontName());
        System.out.println("BOEING sectionFontSize >>> " + this.boeingContentStyleModel.getSectionFontSize());
        System.out.println("BOEING sectionFontBold >>> " + this.boeingContentStyleModel.isSectionFontBold());

        System.out.println("BOEING titleFontName >>> " + this.boeingContentStyleModel.getTitleFontName());
        System.out.println("BOEING titleFontSize >>> " + this.boeingContentStyleModel.getTitleFontSize());
        System.out.println("BOEING titleFontBold >>> " + this.boeingContentStyleModel.isTitleFontBold());
        */

        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
        String nameSpace = this.boeingContentModel.getBoeingNamespace();

        QName ASPECT_BOEING_ONEPPPM = QName.createQName(nameSpace, this.boeingContentModel.getBoeingAspectName());
        QName PROP_REFERENCES = QName.createQName(nameSpace, this.boeingContentModel.getWritingSetProperty());
        QName PROP_REFERENCES_LIST = QName.createQName(nameSpace, this.boeingContentModel.getReferencesListProperty());
        QName PROP_AUTHORITY_REFERENCES = QName.createQName(nameSpace, this.boeingContentModel.getAuthorityReferencesProperty());
        QName PROP_AUTHORITY_REFERENCES_LIST = QName.createQName(nameSpace, this.boeingContentModel.getAuthorityReferencesListProperty());

        QName PROP_TEST_REPEATER = QName.createQName(nameSpace, "testRepeater"); //To be updated
        QName PROP_TEST_REPEATER_2 = QName.createQName(nameSpace, "testRepeater2"); //To be updated

        //Commenting below line as discussed with MJ.
//        aspectProperties.put(ContentModel.PROP_TITLE, this.docTitle);

        aspectProperties.put(PROP_TEST_REPEATER, this.references); //List of Strings (Repeating) for Reference Values
        aspectProperties.put(PROP_TEST_REPEATER_2, this.authReferences); //List of Strings (Repeating) for Authority Reference Values

        aspectProperties.put(PROP_REFERENCES, String.join(",", this.references)); //Comma Separated Reference Values
        aspectProperties.put(PROP_REFERENCES_LIST, this.referencesListAsJSON); // JSON representation of ArrayList of Reference Objects
        aspectProperties.put(PROP_AUTHORITY_REFERENCES, String.join(",", this.authReferences)); //Comma Separated Authority Reference Values
        aspectProperties.put(PROP_AUTHORITY_REFERENCES_LIST, this.authReferencesListAsJSON); // JSON representation of ArrayList of AuthorityReference Objects

        nodeService.addAspect(nodeRef, ASPECT_BOEING_ONEPPPM, aspectProperties);


    }

    public Boolean isWriting(String paraText) {
        return ((paraText.indexOf("POL-") > -1) || (paraText.indexOf("PRO-") > -1) || (paraText.indexOf("BPI-") > -1));
    }

    public void determineCurrentSectionName(Paragraph para) {
        if (this.isParagraghStyleMatchingWithSectionNameStyle(para)) {
            for (var k = 0; k < sectionNames.size(); k++) {
                var secName = sectionNames.get(k).toString().toUpperCase().trim();
                String paraTextString = para.getText().toString().toUpperCase().trim();

                if (paraTextString.equals(secName)) {
                    this.currentSectionName = paraTextString;
                    System.out.println("$$$ SPOTTED SECTION NAME MATCH $$$ Setting Current Section Name As : " + this.currentSectionName);
                    break;
                }
            }
        } else if ((this.isTitleExtracted == false) && this.isParagraghStyleMatchingWithTitleStyle(para)) {
            this.docTitle = para.getText().toString();
            System.out.println("$$$ SPOTTED TITLE NAME MATCH $$$ " + this.docTitle);
        }
    }

    public Boolean isParagraghStyleMatchingWithSectionNameStyle(Paragraph para) {
        Font paraFont = para.getParagraphFormat().getStyle().getFont();
        String paraContentFontName = paraFont.getName();
        Double paraContentFontSize = paraFont.getSize();
        Boolean isParaContentBold = paraFont.getBold();

        Boolean isMatching = false;
        if (paraContentFontName.equals(BoeingContentStyleModel.SECTION_FONT_NAME) &&
                paraContentFontSize.equals(BoeingContentStyleModel.SECTION_FONT_SIZE) &&
                (isParaContentBold == BoeingContentStyleModel.isSectionFontBold())) {
            isMatching = true;
        }

        return isMatching;
    }

    public Boolean isParagraghStyleMatchingWithTitleStyle(Paragraph para) {
        Font paraFont = para.getParagraphFormat().getStyle().getFont();
        String paraContentFontName = paraFont.getName();
        Double paraContentFontSize = paraFont.getSize();
        Boolean isParaContentBold = paraFont.getBold();

        Boolean isMatching = false;
        if (paraContentFontName.equals(BoeingContentStyleModel.TITLE_FONT_NAME) &&
                paraContentFontSize.equals(BoeingContentStyleModel.TITLE_FONT_SIZE) &&
                (isParaContentBold == BoeingContentStyleModel.isTitleFontBold())) {
            isMatching = true;
        }

        return isMatching;
    }

    public void getAuthorityReferences(FieldHyperlink hyperlink) {
        try {
            if (this.currentSectionName.indexOf(this.boeingContentModel.getAuthorityReferencesSectionTitle()) == 0) {
                System.out.println("*** *** *** Authority Reference *** *** *** " + hyperlink.getResult());
                References authorityRef = new References(this.currentSectionName, hyperlink.getResult(), hyperlink.getAddress());
                this.authRefList.add(new ObjectMapper().writeValueAsString(authorityRef));
                this.authReferences.add(hyperlink.getResult());
            }
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setGlobalPropertiesHandler(GlobalPropertiesHandler globalPropertiesHandler) {
        this.globalPropertiesHandler = globalPropertiesHandler;
    }

    public void setAsposeLicense() {
        // For complete examples and data files, please go to https://github.com/aspose-words/Aspose.Words-for-Java.git.
        License license = new License();
        try {
            // license.setLicense(new FileInputStream("/usr/local/tomcat/Aspose.Total.lic"));
            license.setLicense(getClass().getClassLoader().getResourceAsStream("Aspose.Total.lic"));

            System.out.println("ASPOSE TOTAL >> License set successfully.");
        } catch (Exception e) {
            System.out.println("\nThere was an error setting the license: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



    /*

    public void setAsposeLicense() {
        try {
            License license = new License();
//            license.setLicense("Aspose.Words.lic");
            license.setLicense(new FileInputStream(new File("Aspose.Words.lic")));

            System.out.println("ASPOSE WORD >> License set successfully.");

        } catch (Exception e) {
            System.out.println("\nThere was an error setting the license: " + e.getMessage());
        }
    }

    public void buildAsposeDocument(final NodeRef nodeRef) {
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setEncoding(StandardCharsets.UTF_8);

        if (nodeService.exists(nodeRef)) {
            try {
                ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                InputStream is = contentReader.getContentInputStream();
                Document contentDoc = new Document(is, loadOptions);
                BookmarkCollection bookmarks = contentDoc.getRange().getBookmarks();
                String currentSectionName = "-";
                String previousParaText = "-";
                Boolean fetchedDocTitle = false;

                String referencesListAsJSON = "";
                String authReferencesListAsJSON = "";
                Map<String, String> referencesMap = new HashMap<String, String>();
                int referencesCount = 0;

                this.extractSectionNames(nodeRef);
                ArrayList sectionNameList = new ArrayList();
                ArrayList hyperLinkList = new ArrayList();
                ArrayList refList = new ArrayList();
                ArrayList authRefList = new ArrayList();


                System.out.println("Number of Sections ---> " + contentDoc.getSections().getCount());
                System.out.println("Document opened. Total pages are " + contentDoc.getPageCount());

                for (var i = 0; i < contentDoc.getSections().getCount(); i++) {
                    Section section = contentDoc.getSections().get(i);

                    NodeCollection nodes = section.getBody().getChildNodes();

                    //                    for (Node node : nodes) {
                    //                        if (node.getNodeType() == NodeType.PARAGRAPH) {
                    //                            System.out.println("Paragraph found.");
                    //                        } else if (node.getNodeType() == NodeType.TABLE) {
                    //                            System.out.println("Table found.");
                    //                        }
                    //                    }

                    for (Node node : (Iterable<Node>) contentDoc.getChildNodes(NodeType.ANY, true)) {
                        if (node instanceof Paragraph) {
                            System.out.println("Paragraph found");

                        } else if (node instanceof Table) {
                            System.out.println("Table found");
                        }
                    }


                    //                    System.out.println("Section # "+(i+1)+" Text = "+section.getText());
                    System.out.println("Section # " + (i + 1) + " Para Count = " + section.getBody().getParagraphs().getCount());

                    ///// ******* FETCH FROM TABLE - START ******** //////
                    System.out.println("Section # " + (i + 1) + " Table Count = " + section.getBody().getTables().getCount());
                    TableCollection tables = section.getBody().getTables();

                    for (int a = 0; a < tables.getCount(); a++) {
                        System.out.println(MessageFormat.format("Start of Table {0}", a));

                        RowCollection rows = tables.get(a).getRows();

                        for (int b = 0; b < rows.getCount(); b++) {
                            System.out.println(MessageFormat.format("\tStart of Row {0}", b));

                            CellCollection cells = rows.get(b).getCells();

                            for (int c = 0; c < cells.getCount(); c++) {
                                String cellText = cells.get(c).toString(SaveFormat.TEXT).trim();
                                System.out.println(MessageFormat.format("\t\tContents of Cell:{0} = \"{1}\"", c, cellText));
                                for (int fieldItr = 0; fieldItr < cells.get(c).getRange().getFields().getCount(); fieldItr++) {
                                    Field field = cells.get(c).getRange().getFields().get(fieldItr);
                                    if (field.getType() == 88) //88 is code for Hyperlink
                                    {
                                        FieldHyperlink hyperlink = (FieldHyperlink) field;
                                        String hyperlinkText = hyperlink.getResult();

                                        System.out.println(">>> >>> INSIDE CELL >>> >>> " + hyperlinkText);
                                    }
                                }
                            }

                            System.out.println(MessageFormat.format("\tEnd of Row {0}", b));
                        }

                        System.out.println(MessageFormat.format("End of Table {0}\n", a));
                    }

                    ///// ******* FETCH FROM TABLE - END ******** //////


                    for (var j = 0; j < section.getBody().getParagraphs().getCount(); j++) {
                        Paragraph paragraph = section.getBody().getParagraphs().get(j);
                        String paraText = "" + paragraph.getText().toString();

                        if (paraText.indexOf("HYPERLINK") == -1) {
                            System.out.println(" >>> NOT A HYPERLINK >> " + paraText);

                            String paraTextString = paraText.toString().toUpperCase().trim();

                            if (fetchedDocTitle == false) {
                                if (paraTextString.indexOf(sectionNames.get(0).toString()) == 0) { //PREVIOUS
                                    this.docTitle = previousParaText;
                                    fetchedDocTitle = true;
                                } else {
                                    previousParaText = paraText;
                                }
                            }

                            for (var k = 0; k < sectionNames.size(); k++) {
                                var secName = sectionNames.get(k).toString().toUpperCase().trim();
                                if (paraText.toUpperCase().trim().equals(secName)) {
                                    currentSectionName = paraText;
                                    System.out.println("$$$ SPOTTED SECTION NAME MATCH $$$ Setting Current Section Name As : " + currentSectionName);
                                    break;
                                }
                            }
                            if (paraText.indexOf('.') != -1) {
                                if (paraText.toUpperCase().trim().split("\\.")[0].length() == 1) {
                                    currentSectionName = paraText;
                                }
                            }
                        } else {
                            System.out.println(" >>> YES, FOUND A HYPERLINK @ SECTION >>> " + currentSectionName);
                            System.out.println(" >>> PARAGRAPH TEXT >>> " + paraText);

                            for (int fieldItr = 0; fieldItr < paragraph.getRange().getFields().getCount(); fieldItr++) {
                                Field field = paragraph.getRange().getFields().get(fieldItr);
                                if (field.getType() == 88) //88 is code for Hyperlink in ASPOSE
                                {
                                    FieldHyperlink hyperlink = (FieldHyperlink) field;
                                    String hyperlinkText = hyperlink.getResult();

                                    if ((hyperlinkText.indexOf("POL-") > -1) ||
                                            (hyperlinkText.indexOf("PRO-") > -1) ||
                                            (hyperlinkText.indexOf("BPI-") > -1)) {
                                        referencesMap.put(hyperlinkText, hyperlinkText);
                                    }

                                    // Some hyperlinks can be local (links to bookmarks inside the document), ignore these.
                                    if (hyperlink.getSubAddress() != null)
                                        continue;

                                    References ref = new References(currentSectionName, hyperlink.getResult(), hyperlink.getAddress());
                                    refList.add(new ObjectMapper().writeValueAsString(ref));
                                    referencesCount++;

                                    if (currentSectionName.indexOf("Authority Reference") == 0) {
                                        System.out.println("*** *** *** Authority Reference *** *** *** " + hyperlink.getResult());
                                        References authorityRef = new References(currentSectionName, hyperlink.getResult(), hyperlink.getAddress());
                                        authRefList.add(new ObjectMapper().writeValueAsString(authorityRef));
                                        this.authReferences.add(hyperlink.getResult());
                                    }

                                    sectionNameList.add(currentSectionName);
                                    hyperLinkList.add(hyperlinkText);
                                }
                            }
                        }
                    }
                }

                this.referencesListAsJSON = new ObjectMapper().writeValueAsString(refList);
                this.authReferencesListAsJSON = new ObjectMapper().writeValueAsString(authRefList);

                System.out.println("*** *** *** <><><> *** *** ***");
                System.out.println(">>> >>> >>> referencesMap.keySet() >>> >>> >>>");
                System.out.println(referencesMap.keySet());
                System.out.println(referencesMap.size());

                String[] keyArray = referencesMap.keySet().toArray(new String[0]);
                System.out.println(keyArray);

                for (var mapItr = 0; mapItr < referencesMap.size(); mapItr++) {
                    String currentKey = keyArray[mapItr];
                    this.writings = this.writings + currentKey;
                    if (referencesMap.size() - mapItr > 1)
                        this.writings = this.writings + ",";
                }

                System.out.println(this.writings);
                System.out.println("*** *** *** <><><> *** *** ***");

                this.applyWebPublishedAspect(nodeRef);


            } catch (java.lang.Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void getDocumentTitle(NodeRef nodeRef) {
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setEncoding(StandardCharsets.UTF_8);

        if (nodeService.exists(nodeRef)) {
            try {
                ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                InputStream is = contentReader.getContentInputStream();
                Document contentDoc = new Document(is, loadOptions);

                this.docTitle = contentDoc.getFirstSection().getBody().getFirstChild().getText();
                System.out.println("*** Document Title >>> " + this.docTitle);

            } catch (java.lang.Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayList<String> getDefinedSectionNames() {

        ArrayList definedSectionNames = new ArrayList();

        definedSectionNames.add("PURPOSE");
        definedSectionNames.add("SUPERSEDED DATE");
        definedSectionNames.add("APPLIES TO");
        definedSectionNames.add("ROLES AFFECTED");
        definedSectionNames.add("AUTHORITY REFERENCE");
        definedSectionNames.add("APPROVED BY");
        definedSectionNames.add("SUMMARY OF CHANGES");
        definedSectionNames.add("1.	REQUIREMENTS");
        definedSectionNames.add("2.	RESPONSIBILITIES");
        definedSectionNames.add("3.	TERMS");
        definedSectionNames.add("4.	ACRONYMS");
        definedSectionNames.add("5.	REFERENCES");

        return definedSectionNames;
    }

    public void createRendition(NodeRef nodeRef) {
        // Names must be provided for the rendition definition and the rendering engine to use.
        QName  renditionName       = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myRendDefn");
        String renderingEngineName = ReformatRenderingEngine.NAME;

        // Create the Rendition Definition object.
        RenditionDefinition renditionDef = serviceRegistry.getRenditionService().createRenditionDefinition(renditionName, renderingEngineName);

        // Set parameters on the rendition definition.
        renditionDef.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);

    }

    */

