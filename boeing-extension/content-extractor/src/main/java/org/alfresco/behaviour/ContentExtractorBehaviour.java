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

import java.io.IOException;
import java.io.Serializable;
import java.io.InputStream;
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

    private String referencesListAsJSON = ""; //JSON representation of Reference Object List
    private String authReferencesListAsJSON = ""; //JSON representation of Reference Object List
    private ArrayList<String> references = new ArrayList<String>();
    private ArrayList<String> authReferences = new ArrayList<String>();

    private String docTitle = "";
    private String currentSectionName = "";

    private Boolean isTitleExtracted = false;

    private String writings = "";
    private ArrayList sectionNames = new ArrayList();
    private ArrayList sectionNameList = new ArrayList();
    private ArrayList hyperLinkList = new ArrayList();
    private ArrayList refList = new ArrayList();
    private ArrayList authRefList = new ArrayList();


    public void init() {
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
                //                this.testAsposeMethods(nodeRef);
                //                this.buildAsposeDocument(nodeRef);
                this.buildAsposeDocument_newLogic(nodeRef);
            }


            /*
            COMMENTED TEMPORARILY TO TEST THE CONTENT EXTRACTION LOGIC OF TABLE - START

            try {
                System.out.println(">>>> ***** >>>>> START OF CONTENT UPLOAD OR UPDATE >>>> ***** >>>>> ");
                System.out.println(">>>> ***** >>>>> Uploaded " + contentReader.getContentInputStream().readAllBytes().length + " bytes... >>>> ***** >>>>> ");

                String fileName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
                String nodeId = nodeRef.getId();

                if((fileName.trim().indexOf(".docx") != -1)) {
                    System.out.println("FILE NAME : "+fileName+" >>> NODE ID : "+nodeId+" >>> INDEX-OF('doclib') : "+fileName.trim().indexOf("doclib"));
                    System.out.println(">>> INVOKING buildAsposeDocument() FOR >>> "+fileName);

                    this.buildAsposeDocument(nodeRef);
                }


                System.out.println(">>>> ***** >>>>> END OF CONTENT UPLOAD OR UPDATE >>>> ***** >>>>> ");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            COMMENTED TEMPORARILY TO TEST THE CONTENT EXTRACTION LOGIC OF TABLE - END
            */

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


    public void buildAsposeDocument_newLogic(final NodeRef nodeRef) {
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
                Map<String, String> myMap = new HashMap<String, String>();
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
                                    myMap.put(hyperlinkText, hyperlinkText);
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
                                            myMap.put(hyperlinkText, hyperlinkText);
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

                System.out.println("AUTHORITY REFERENCE LIST >>> "+String.join(",", this.authReferences));
                System.out.println("AUTHORITY REFERENCE LIST AS JSON >>> "+this.authReferencesListAsJSON);
                System.out.println("*** *** *** <><><> *** *** ***");


                System.out.println("*** *** *** <><><> *** *** ***");
                System.out.println(">>> >>> >>> myMap.keySet() >>> >>> >>>");
                System.out.println(myMap.keySet());
                System.out.println(myMap.size());


                String[] keyArray = myMap.keySet().toArray(new String[0]);
                System.out.println(keyArray);

                for (var mapItr = 0; mapItr < myMap.size(); mapItr++) {
                    String currentKey = keyArray[mapItr];
                    this.writings = this.writings + currentKey;
                    if (myMap.size() - mapItr > 1)
                        this.writings = this.writings + ",";
                }

                System.out.println(this.writings);
                System.out.println("*** *** *** <><><> *** *** ***");

                this.applyWebPublishedAspect(nodeRef);


                System.out.println("Number of Paragraphs ---> " + paraCount);
                System.out.println("Number of Tables ---> " + tableCount);

            } catch (java.lang.Exception e) {
                throw new RuntimeException(e);
            }
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
                Map<String, String> myMap = new HashMap<String, String>();
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
                                        myMap.put(hyperlinkText, hyperlinkText);
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
                System.out.println(">>> >>> >>> myMap.keySet() >>> >>> >>>");
                System.out.println(myMap.keySet());
                System.out.println(myMap.size());

                String[] keyArray = myMap.keySet().toArray(new String[0]);
                System.out.println(keyArray);

                for (var mapItr = 0; mapItr < myMap.size(); mapItr++) {
                    String currentKey = keyArray[mapItr];
                    this.writings = this.writings + currentKey;
                    if (myMap.size() - mapItr > 1)
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

    /*public void getDocumentTitle(NodeRef nodeRef) {
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

    public void applyWebPublishedAspect(NodeRef nodeRef) {
        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();

        aspectProperties.put(ContentModel.PROP_TITLE, this.docTitle);
        aspectProperties.put(BoeingContentModel.PROP_WRITING_SET, this.writings); //Comma Separated Reference Values
        aspectProperties.put(BoeingContentModel.PROP_REFERENCES_LIST, this.referencesListAsJSON); // JSON representation of ArrayList of Reference Objects
        aspectProperties.put(BoeingContentModel.PROP_AUTHORITY_REFERENCES, String.join(",", this.authReferences)); //Comma Separated Authority Reference Values
        aspectProperties.put(BoeingContentModel.PROP_AUTHORITY_REFERENCES_LIST, this.authReferencesListAsJSON); // JSON representation of ArrayList of AuthorityReference Objects

        nodeService.addAspect(nodeRef, BoeingContentModel.ASPECT_BOEING_ONEPPPM, aspectProperties);
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

    public void getAuthorityReferences(FieldHyperlink hyperlink){
        try {
            if (this.currentSectionName.indexOf(BoeingContentModel.AUTHORITY_REFERENCE) == 0) {
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

}
