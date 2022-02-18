package org.edmcouncil.spec.ontoviewer.core.ontology.loader.mapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.CommonBaseIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class VersionIriMapper extends CommonBaseIRIMapper {
  
  private static final Logger LOG = LoggerFactory.getLogger(VersionIriMapper.class);
  
  private Path dirPath;
  private Map<IRI, IRI> irisMap = new HashMap<>();
  
  public VersionIriMapper(Path dirPath) {
    super(IRI.create(dirPath.toUri()));
    this.dirPath = dirPath;
  }

  public Map<IRI, IRI> getIriMap() {
    return irisMap;
  }
  
  public void mapOntologyVersion(AutoIRIMapper autoIRIMapper) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
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
    
    for (IRI iri : autoIRIMapper.getOntologyIRIs()) {
      Document doc = builder.parse(autoIRIMapper.getDocumentIRI(iri).toString());
      Object result = expr.evaluate(doc, XPathConstants.NODESET);
      NodeList nodes = (NodeList) result;
      for (int i = 0; i < nodes.getLength(); i++) {
        var iriKey = IRI.create(nodes.item(i).getNodeValue());
        var iriValue = autoIRIMapper.getDocumentIRI(iri);
        super.addMapping(iriKey, iriValue.toURI().toString());
        irisMap.put(iriKey, iri);
      }
    }
  }

  public void mapOntologyFileVersion(Path path) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
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
    
    Document doc = builder.parse(path.toFile());
    Object result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList) result;
    for (int i = 0; i < nodes.getLength(); i++) {
      super.addMapping(IRI.create(nodes.item(i).getNodeValue()), path.toAbsolutePath().toString());
    }
  }
}