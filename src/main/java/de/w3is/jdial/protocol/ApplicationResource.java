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

import java.io.IOException;
import java.net.URL;

import de.w3is.jdial.model.Application;
import de.w3is.jdial.model.DialContent;
import de.w3is.jdial.protocol.model.ApplicationResourceException;

/**
 * @author Simon Weis
 */
public interface ApplicationResource {

    Application getApplication(String applicationName) throws IOException;

    URL startApplication(String applicationName) throws IOException, ApplicationResourceException;

    URL startApplication(String applicationName, DialContent dialContent) throws IOException, ApplicationResourceException;

    void stopApplication(URL instanceUrl) throws IOException, ApplicationResourceException;

    void hideApplication(URL instanceURL) throws IOException, ApplicationResourceException;
}
