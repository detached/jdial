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

/**
 * @author Simon Weis
 */
class XMLUtil {

    static String getTextFromSub(Document element, String tagName) {

        NodeList elementsByTagName = element.getElementsByTagName(tagName);

        if (elementsByTagName.getLength() >= 1) {
            return elementsByTagName.item(0).getTextContent();
        }

        return "";
    }
}