# Copyright 2024 coScene
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import base64
import logging
import sys

import grpc
from coscene.openapi.dataplatform.v1alpha1.services import organization_pb2_grpc, organization_pb2
from google.protobuf import json_format

_log = logging.getLogger(__name__)
# customize the log level and output format
logging.basicConfig(stream=sys.stdout, level=logging.DEBUG)


class BasicTokenAuthMetadataPlugin(grpc.AuthMetadataPlugin):
    def __init__(self, access_token: str):
        self._access_token = access_token

    def __call__(self, context, callback):
        metadata = (("authorization", self._access_token),)
        callback(metadata, None)


class CustomInterceptor(grpc.UnaryUnaryClientInterceptor):
    def intercept_unary_unary(self, continuation, client_call_details, request):
        request_size = request.ByteSize()
        _log.debug(f"Sending request of size: {request_size} bytes")

        response_future = continuation(client_call_details, request)
        if grpc.StatusCode.OK == response_future.code():
            response_size = response_future.result().ByteSize()
            _log.debug(f"Received response of size: {response_size} bytes")
        return response_future


class GrpcClient:
    def __init__(self, api_key: str, server_url: str):
        self._api_key = api_key
        self._server_url = server_url

        self._channel = self.__create_client_channel(self.__get_address(),
                                                     self.__get_basic_token("apikey", self._api_key))

    def __get_address(self):
        url = self._server_url.removeprefix("https://").removeprefix("http://")
        return url

    def __get_basic_token(self, username: str, password: str):
        userpass = username + ":" + password
        encoded_u = base64.b64encode(userpass.encode()).decode()
        return "Basic %s" % encoded_u

    def __create_client_channel(self, addr: str, token: str):
        call_credentials = grpc.metadata_call_credentials(BasicTokenAuthMetadataPlugin(token), name="basic_token_auth")
        channel_credential = grpc.ssl_channel_credentials()
        composite_credentials = grpc.composite_channel_credentials(
            channel_credential,
            call_credentials,
        )

        # User can add more custom interceptors here
        interceptors = [CustomInterceptor()]
        channel = grpc.secure_channel(addr, composite_credentials)
        intercept_channel = grpc.intercept_channel(channel, *interceptors)
        return intercept_channel

    def get_organization(self):
        try:
            stub = organization_pb2_grpc.OrganizationServiceStub(self._channel)
            req = organization_pb2.GetOrganizationRequest(name="organizations/current")
            res = stub.GetOrganization(req, timeout=10)
            result = json_format.MessageToDict(res)

            _log.info("==> Get the organization {org_name}".format(org_name=result))
        except grpc.RpcError as rpc_error:
            _log.error("Get the organization failure: %s", rpc_error)
            raise RuntimeError("Failed to get organization")


if __name__ == "__main__":
    server_url = "https://openapi.coscene.cn"
    api_key = "Your API Key generated from the console"

    grpc_client = GrpcClient(api_key, server_url)
    grpc_client.get_organization()
