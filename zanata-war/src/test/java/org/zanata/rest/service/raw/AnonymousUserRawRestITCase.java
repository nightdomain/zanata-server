/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.rest.service.raw;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.rest.InvalidApiKeyUtil;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.ResourceRequestEnvironment;

public class AnonymousUserRawRestITCase extends RestTest {

    private final String invalidAPI = "InvalidAPIKEY";

    //NOTE: keep in sync with RestLimitingSynchronousDispatcher.API_KEY_ABSENCE_WARNING
    private final String API_KEY_ABSENCE_WARNING =
        "You must have a valid API key. You can create one by logging in to Zanata and visiting the settings page.";

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void doGETProjectsWithWrongAPI() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects"), "GET",
            getUnAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getEntity(String.class).toString(),
                    is(InvalidApiKeyUtil.getMessage(ADMIN, invalidAPI)));

                assertThat(response.getStatus(),
                    is(Status.UNAUTHORIZED.getStatusCode()));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void doGETProjectsWithCorrectAPI() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects"), "GET",
            getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                    MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                    is(Status.OK.getStatusCode()));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void doGETProjectsWithAnonymous() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                    MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                    is(Status.OK.getStatusCode()));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void doPOSTProjectsWithAnonymous() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects"), "POST") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                    MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getEntity(String.class).toString(),
                    is(InvalidApiKeyUtil.getMessage(
                        API_KEY_ABSENCE_WARNING)));

                assertThat(response.getStatus(),
                    is(Status.UNAUTHORIZED.getStatusCode()));
            }
        }.run();
    }

    private ResourceRequestEnvironment getUnAuthorizedEnvironment() {
        return new ResourceRequestEnvironment() {
            @Override
            public Map<String, Object> getDefaultHeaders() {
                return new HashMap<String, Object>() {
                    {
                        put("X-Auth-User", ADMIN);
                        put("X-Auth-Token", invalidAPI);
                    }
                };
            }
        };
    }
}
