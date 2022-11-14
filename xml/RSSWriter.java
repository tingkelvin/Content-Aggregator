package xml;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class RSSWriter {
    private static String XML_BLOCK = "\n";
    private static String XML_INDENT = "\t";

    public static String write(RSSFeed rssfeed, String xmlfile) throws Exception {
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        StringWriter xmlContent = new StringWriter();
        XMLEventWriter writer = output.createXMLEventWriter(xmlContent);
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent endSection = eventFactory.createDTD(XML_BLOCK);
        XMLEvent tabSection = eventFactory.createDTD(XML_INDENT);

        StartDocument startDocument = eventFactory.createStartDocument();
        writer.add(startDocument);
        writer.add(endSection);
        StartElement rssStart = eventFactory.createStartElement("", "",
                "feed");
        writer.add(rssStart);
        writer.add(eventFactory.createAttribute("xml:lang", "en-US"));
        writer.add(eventFactory.createAttribute("xmlns", "http://www.w3.org/2005/Atom"));
        writer.add(endSection);
        RSSHeader header = rssfeed.getHeader();
        createNode(writer, "title", header.getTitle());
        createNode(writer, "subtitle", header.getSubtitle());
        createNode(writer, "link", header.getLink());
        createNode(writer, "updated", header.getUpdated());

        RSSAuthor author = rssfeed.getAuthor();
        writer.add(tabSection);
        writer.add(eventFactory.createStartElement("", "", "author"));
        writer.add(endSection);
        createNode(writer, "name", author.getAuthor());
        if (!author.getEmail().equals("")) {
            createNode(writer, "email", author.getEmail());
        }
        createNode(writer, "id", header.getID());
        writer.add(tabSection);
        writer.add(eventFactory.createEndElement("", "", "entry"));
        writer.add(endSection);
        Iterator<RSSEntry> iterator = rssfeed.getEntries().iterator();
        while (iterator.hasNext()) {
            RSSEntry entry = iterator.next();
            writer.add(tabSection);
            writer.add(eventFactory.createStartElement("", "", "entry"));
            writer.add(endSection);
            createNode(writer, "title", entry.getTitle());
            createNode(writer, "link", entry.getLink());
            createNode(writer, "id", entry.getID());
            createNode(writer, "updated", entry.getUpdated());
            createNode(writer, "summary", entry.getSummary());

            writer.add(tabSection);
            writer.add(eventFactory.createEndElement("", "", "entry"));
            writer.add(endSection);
        }

        writer.add(eventFactory.createEndElement("", "", "feed"));

        writer.close();
        return xmlContent.toString();
    }

    private static void createNode(XMLEventWriter eventWriter, String name, String value)
            throws XMLStreamException {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent endSection = eventFactory.createDTD(XML_BLOCK);
        XMLEvent tabSection = eventFactory.createDTD(XML_INDENT);

        StartElement sElement = eventFactory.createStartElement("", "", name);
        eventWriter.add(tabSection);
        eventWriter.add(tabSection);
        eventWriter.add(sElement);

        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);

        EndElement eElement = eventFactory.createEndElement("", "", name);
        eventWriter.add(eElement);
        eventWriter.add(endSection);
    }
}