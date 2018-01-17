package rfid.view;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;

public class ReadXMLFile 
{
	
	NodeList nList;
	String sFileName = "db.xml";
	
	class Book
	{
	    public String Tag; 
	    public String Name;  
	}
	
	public ReadXMLFile()
	{
		readXML();
	}
	
	public void readXML()
	{
	   try 
	    {
	
			File fXmlFile = new File(sFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
		
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
			nList = doc.getElementsByTagName("book");
			
			System.out.println("----------------------------");
			
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
			
				Node nNode = nList.item(temp);
			
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
			
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
				
					Element eElement = (Element) nNode;
				
					System.out.println("id: " + eElement.getElementsByTagName("tag").item(0).getTextContent());
					System.out.println("title: " + eElement.getElementsByTagName("title").item(0).getTextContent());
				
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public ArrayList<Book> getBooks()
	{
		readXML();
		
		ArrayList<Book> retVal = new ArrayList<Book>();
		
		for (int temp = 0; temp < nList.getLength(); temp++) 
		{
			Book aBook = new Book();
			
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				Element eElement = (Element) nNode;
				aBook.Tag = eElement.getElementsByTagName("tag").item(0).getTextContent();
				aBook.Name = eElement.getElementsByTagName("title").item(0).getTextContent();
			}
			
			retVal.add(aBook);
		}
		
		return retVal;
	}
	
	public String getNamebyTag(String tag)
	{
		String retVal = "";
		
		for (int temp = 0; temp < nList.getLength(); temp++) 
		{
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				Element eElement = (Element) nNode;
				String cmp = eElement.getElementsByTagName("tag").item(0).getTextContent();
				
				if (cmp.equals(tag))
					retVal = eElement.getElementsByTagName("title").item(0).getTextContent();
			}
		}
		
		return retVal;		
	}
	
	public void addNodeToXML(String stag, String title)
	{
		try
		{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

			File is = new File(sFileName);
			Document doc = docBuilder.parse(is);        
			
			Node root = doc.getFirstChild();
			Element book = doc.createElement("book");
			
			Element eTag = doc.createElement("tag");
			eTag.appendChild(doc.createTextNode(stag));
			book.appendChild(eTag);
			
			Element eName = doc.createElement("title");
			eName.appendChild(doc.createTextNode(title));
			book.appendChild(eName);
			
			root.appendChild(book);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			Result output = new StreamResult(new File("db.xml"));
			Source input = new DOMSource(doc);

			transformer.transform(input, output);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
	
	