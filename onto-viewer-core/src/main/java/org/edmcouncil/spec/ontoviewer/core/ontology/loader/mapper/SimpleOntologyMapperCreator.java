package org.edmcouncil.spec.ontoviewer.core.ontology.loader.mapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SimpleOntologyMapperCreator {

  public static SimpleIRIMapper create(IRI ontologyIRI, IRI documentIRI) {
    return new SimpleIRIMapper(ontologyIRI, documentIRI);
  }

  public static Set<SimpleIRIMapper> createAboutMapper(IRI documentIri)
      throws XPathException, IOException, ParserConfigurationException, SAXException {
    return createAboutMapper(new File(documentIri.toURI()));
  }

  public static Set<SimpleIRIMapper> createAboutMapper(File f) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {

    IRI documentIRI = IRI.create(f);
    Set<SimpleIRIMapper> resultSet = new LinkedHashSet<>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
    xpath.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String prefix) {
        return prefix.equals("owl") ? "http://www.w3.org/2002/07/owl#" : prefix.equals("rdf") ? "http://www.w3.org/1999/02/22-rdf-syntax-ns#" : null;
      }
      
      @Override
      public Iterator<String> getPrefixes(String val) {
        return null;
      }
      
      @Override
      public String getPrefix(String uri) {
        return null;
      }
    });
    //XPath for versionIRI
    XPathExpression expr = xpath.compile("//owl:Ontology/@rdf:about");
    
    Document doc = builder.parse(f);
    Object result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList) result;
    for (int i = 0; i < nodes.getLength(); i++) {
      SimpleIRIMapper imap = new SimpleIRIMapper(IRI.create(nodes.item(i).getNodeValue()), documentIRI);
      resultSet.add(imap);
    }
    
    

     return resultSet;
  }
    public static Set<SimpleIRIMapper> createVersionMapper(File f) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {

    IRI documentIRI = IRI.create(f);
    Set<SimpleIRIMapper> resultSet = new LinkedHashSet<>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
    xpath.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String prefix) {
        return prefix.equals("owl") ? "http://www.w3.org/2002/07/owl#" : prefix.equals("rdf") ? "http://www.w3.org/1999/02/22-rdf-syntax-ns#" : null;
      }
      
      @Override
      public Iterator<String> getPrefixes(String val) {
        return null;
      }
      
      @Override
      public String getPrefix(String uri) {
        return null;
      }
    });
    //XPath for versionIRI
    XPathExpression expr = xpath.compile("//owl:Ontology/owl:versionIRI/@rdf:resource");
    
    Document doc = builder.parse(f);
    Object result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList) result;
    for (int i = 0; i < nodes.getLength(); i++) {
      SimpleIRIMapper imap = new SimpleIRIMapper(IRI.create(nodes.item(i).getNodeValue()), documentIRI);
      resultSet.add(imap);
    }
    
    return resultSet;
  }

}
