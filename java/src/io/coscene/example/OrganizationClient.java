/*
 * Copyright 2024 coScene
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.coscene.example;

import build.buf.gen.coscene.openapi.dataplatform.v1alpha1.resources.Organization;
import build.buf.gen.coscene.openapi.dataplatform.v1alpha1.services.GetOrganizationRequest;
import build.buf.gen.coscene.openapi.dataplatform.v1alpha1.services.OrganizationServiceGrpc;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrganizationClient {
    private static final Logger logger = Logger.getLogger(OrganizationClient.class.getName());

    private final OrganizationServiceGrpc.OrganizationServiceBlockingStub blockingStub;
    private final ManagedChannel channel;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public OrganizationClient(CallCredentials callCredentials, ManagedChannel channel) {
        this.channel = channel;

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = OrganizationServiceGrpc.newBlockingStub(channel).withCallCredentials(callCredentials);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Say hello to server.
     */
    public void getOrganization(String name) {
        logger.info("Will try to greet " + name + " ...");
        GetOrganizationRequest request = GetOrganizationRequest.newBuilder().setName(name).build();
        Organization response;
        try {
            response = blockingStub.getOrganization(request);
            logger.info("Organization: " + JsonFormat.printer().print(response));
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        String serverHost = "openapi.api.coscene.dev";
        int port = 443;
        // username is "apikey" and password is the api key
        String username = "apikey";
        String apiKey = "YjYzYzEwOGE1M2I4MGZmNDM1ODk5ZGQ1MGY5ZGFhMjMwOTJmMmRhZjFjM2ZiNTk1YTllYzE5MTYzYTUzYjE3NA==";

        AuthCallCredentials callCredentials = new AuthCallCredentials(username, apiKey);
        ManagedChannel managedChannel = Grpc.newChannelBuilderForAddress(serverHost, port, TlsChannelCredentials.create())
                .build();
        OrganizationClient client = new OrganizationClient(callCredentials, managedChannel);
        try {
            client.getOrganization("organizations/current");
        } finally {
            try {
                client.shutdown();
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "shutdown interrupted");
            }
        }
    }
}

