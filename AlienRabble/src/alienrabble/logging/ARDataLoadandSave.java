package alienrabble.logging;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ARDataLoadandSave {

	private String name;

	public ARDataLoadandSave(String name){
		this.name = name;
	}
	
	public void test() 
	throws 	javax.xml.parsers.ParserConfigurationException,
    		javax.xml.transform.TransformerException,
    		javax.xml.transform.TransformerConfigurationException, IOException{

		DocumentBuilderFactory factory
		 = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation impl = builder.getDOMImplementation();
		
		Document doc = impl.createDocument(null,null,null);
		Element e1 = doc.createElement("alienrabbledata");
		doc.appendChild(e1);
		
		Element e2 = doc.createElement("participant");
		e1.appendChild(e2);
		
		e2.setAttribute("name","caspar");
		e2.setAttribute("dob","29-07-1974");
		
		
		
		// transform the Document into a String
		DOMSource domSource = new DOMSource(doc);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		//transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
		transformer.setOutputProperty
		   ("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		java.io.FileWriter fw = new java.io.FileWriter(name);
		
		StreamResult sr = new StreamResult(fw);
		transformer.transform(domSource, sr);
		fw.close();
	}
	
}
