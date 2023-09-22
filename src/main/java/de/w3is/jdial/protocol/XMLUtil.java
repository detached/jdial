/*
 * Copyright (C) 2018 Simon Weis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.w3is.jdial.protocol;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author Simon Weis
 */
class XMLUtil {

    private static final Logger LOGGER = Logger.getLogger(XMLUtil.class.getName());

    private static final String PREVENT_XXE_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    static Document getDocumentFromStream(InputStream inputStream) throws IOException, ParserConfigurationException, SAXException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#introduction
        try {
            documentBuilderFactory.setFeature(PREVENT_XXE_FEATURE, true);
            documentBuilderFactory.setXIncludeAware(false);
        } catch (ParserConfigurationException e) {
            LOGGER.info("Were not able to activate feature " + PREVENT_XXE_FEATURE);
        }

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);

        document.getDocumentElement().normalize();

        return document;
    }

    static String getTextFromSub(Document element, String tagName) {

        NodeList elementsByTagName = element.getElementsByTagName(tagName);

        if (elementsByTagName.getLength() >= 1) {
            return elementsByTagName.item(0).getTextContent();
        }

        return "";
    }
}
