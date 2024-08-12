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

import java.io.IOException;
import java.io.Serializable;
import java.io.InputStream;
import java.util.*;
import java.nio.charset.StandardCharsets;

import com.aspose.words.*;
import com.fasterxml.jackson.databind.ObjectMapper;



//NodeServicePolicies.onUpdateProperties
public class ContentExtractorBehaviour implements ContentServicePolicies.OnContentPropertyUpdatePolicy, NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private ContentService contentService;

    private String referencesListAsJSON = ""; //JSON representation of Reference Object List
    private String authReferencesListAsJSON = "";  //JSON representation of Reference Object List
    private ArrayList<String> authReferences = new ArrayList<String>();

    private String docTitle = "";




    public void init() {
        policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onContentPropertyUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onContentPropertyUpdate(final NodeRef nodeRef, QName propertyQName, ContentData beforeValue, ContentData afterValue) {
        if (nodeService.exists(nodeRef)) {
            ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            try {
                System.out.println(">>>> ***** >>>>> START OF CONTENT UPLOAD OR UPDATE >>>> ***** >>>>> ");
                System.out.println(">>>> ***** >>>>> Uploaded " + contentReader.getContentInputStream().readAllBytes().length + " bytes... >>>> ***** >>>>> ");

                String fileName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
                String nodeId = nodeRef.getId();

//              if((fileName.trim().indexOf("doclib") == -1) && (fileName.trim().indexOf("pdf") == -1)) {
                if((fileName.trim().indexOf(".docx") != -1)) {
                    System.out.println("FILE NAME : "+fileName+" >>> NODE ID : "+nodeId+" >>> INDEX-OF('doclib') : "+fileName.trim().indexOf("doclib"));
                    System.out.println(">>> INVOKING buildAsposeDocument() FOR >>> "+fileName);

                    this.buildAsposeDocument(nodeRef);
                }


                System.out.println(">>>> ***** >>>>> END OF CONTENT UPLOAD OR UPDATE >>>> ***** >>>>> ");

            } catch (IOException e) {
                throw new RuntimeException(e);
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

    public void buildAsposeDocument(final NodeRef nodeRef){
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

                ArrayList sectionNames = this.getDefinedSectionNames();
                ArrayList sectionNameList = new ArrayList();
                ArrayList hyperLinkList = new ArrayList();
                ArrayList refList = new ArrayList();
                ArrayList authRefList = new ArrayList();

//                ArrayList sectionNames = new ArrayList();
//                sectionNames.add("PURPOSE");
//                sectionNames.add("SUPERSEDED DATE");
//                sectionNames.add("APPLIES TO");
//                sectionNames.add("ROLES AFFECTED");
//                sectionNames.add("AUTHORITY REFERENCE");
//                sectionNames.add("APPROVED BY");
//                sectionNames.add("SUMMARY OF CHANGES");
//                sectionNames.add("1.	REQUIREMENTS");
//                sectionNames.add("2.	RESPONSIBILITIES");
//                sectionNames.add("3.	TERMS");
//                sectionNames.add("4.	ACRONYMS");
//                sectionNames.add("5.	REFERENCES");

                System.out.println("Number of Sections ---> "+contentDoc.getSections().getCount());
                System.out.println("Document opened. Total pages are " + contentDoc.getPageCount());

                for (var i=0; i<contentDoc.getSections().getCount(); i++) {
                    Section section = contentDoc.getSections().get(i);
//                    System.out.println("Section # "+(i+1)+" Text = "+section.getText());
                    System.out.println("Section # "+(i+1)+ " Para Count = "+section.getBody().getParagraphs().getCount());

                    for(var j=0; j<section.getBody().getParagraphs().getCount(); j++)
                    {
                        Paragraph paragraph = section.getBody().getParagraphs().get(j);
                        String paraText = ""+paragraph.getText().toString();

                        if(paraText.indexOf("HYPERLINK") == -1)
                        {
                            System.out.println(" >>> NOT A HYPERLINK >> "+paraText);

                            String paraTextString = paraText.toString().toUpperCase().trim();
                            Boolean isParaTextASectionName = false;

                            if(fetchedDocTitle == false){
                                if(paraTextString.indexOf(sectionNames.get(0).toString()) == 0){  //PREVIOUS
                                    this.docTitle = previousParaText;
                                    fetchedDocTitle = true;
                                }else{
                                    previousParaText = paraText;
                                }
                            }

                            for(var k=0; k<sectionNames.size(); k++)
                            {
                                var secName = sectionNames.get(k).toString().toUpperCase().trim();
                                if(paraText.toUpperCase().trim().equals(secName))
                                {
                                    currentSectionName = paraText;
                                    System.out.println("$$$ SPOTTED SECTION NAME MATCH $$$ Setting Current Section Name As : "+currentSectionName);
                                    break;
                                }
                            }
                            if(paraText.indexOf('.') != -1)
                            {
                                if(paraText.toUpperCase().trim().split("\\.")[0].length() == 1)
                                {
                                    currentSectionName = paraText;
                                }
                            }
                        }
                        else
                        {
                            System.out.println(" >>> YES, FOUND A HYPERLINK @ SECTION >>> "+ currentSectionName);
                            System.out.println(" >>> PARAGRAPH TEXT >>> "+ paraText);

                            for(int fieldItr = 0; fieldItr < paragraph.getRange().getFields().getCount(); fieldItr++){
                                Field field = paragraph.getRange().getFields().get(fieldItr);
                                if (field.getType() == 88) //88 is code for Hyperlink
                                {
                                    FieldHyperlink hyperlink = (FieldHyperlink) field;
                                    String hyperlinkText = hyperlink.getResult();

                                    if( (hyperlinkText.indexOf("POL-") > -1) ||
                                        (hyperlinkText.indexOf("PRO-") > -1) ||
                                        (hyperlinkText.indexOf("BPI-") > -1) )
                                    {
                                        myMap.put(hyperlinkText, hyperlinkText);
                                    }

                                    // Some hyperlinks can be local (links to bookmarks inside the document), ignore these.
                                    if (hyperlink.getSubAddress() != null)
                                        continue;

                                    References ref = new References(currentSectionName, hyperlink.getResult(), hyperlink.getAddress());
                                    refList.add(new ObjectMapper().writeValueAsString(ref));
                                    referencesCount++;

                                    if(currentSectionName.indexOf("Authority Reference") == 0)
                                    {
                                        System.out.println("*** *** *** Authority Reference *** *** *** "+hyperlink.getResult());
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

                String writings = "";
                String[] keyArray = myMap.keySet().toArray(new String[0]);
                System.out.println(keyArray);

                for(var mapItr=0; mapItr<myMap.size(); mapItr++){
                    String currentKey = keyArray[mapItr];
                    writings = writings + currentKey ;
                    if(myMap.size() - mapItr > 1)
                        writings = writings+",";
                }

                System.out.println(writings);
                System.out.println("*** *** *** <><><> *** *** ***");

                this.applyWebPublishedAspect(nodeRef, writings);



            } catch (java.lang.Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void getDocumentTitle(){

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

    public void applyWebPublishedAspect(NodeRef nodeRef, String writings) {
        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();

        aspectProperties.put(ContentModel.PROP_TITLE, this.docTitle);
        aspectProperties.put(BoeingContentModel.PROP_WRITING_SET, writings); //Comma Separated Reference Values
        aspectProperties.put(BoeingContentModel.PROP_REFERENCES_LIST, this.referencesListAsJSON); // JSON representation of ArrayList of Reference Objects
        aspectProperties.put(BoeingContentModel.PROP_AUTHORITY_REFERENCES, String.join(",", this.authReferences)); //Comma Separated Authority Reference Values
        aspectProperties.put(BoeingContentModel.PROP_AUTHORITY_REFERENCES_LIST, this.authReferencesListAsJSON); // JSON representation of ArrayList of AuthorityReference Objects

        nodeService.addAspect(nodeRef, BoeingContentModel.ASPECT_BOEING_ONEPPPM, aspectProperties);
    }


//    public void createRendition(NodeRef nodeRef) {
//        // Names must be provided for the rendition definition and the rendering engine to use.
//        QName  renditionName       = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myRendDefn");
//        String renderingEngineName = ReformatRenderingEngine.NAME;
//
//        // Create the Rendition Definition object.
//        RenditionDefinition renditionDef = serviceRegistry.getRenditionService().createRenditionDefinition(renditionName, renderingEngineName);
//
//        // Set parameters on the rendition definition.
//        renditionDef.setParameterValue(AbstractRenderingEngine.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);
//
//    }




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
