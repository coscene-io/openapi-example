// Copyright 2024 coScene
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package main

import (
	openDpsV1alpha1Connect "buf.build/gen/go/coscene-io/coscene-openapi/connectrpc/go/coscene/openapi/dataplatform/v1alpha1/services/servicesconnect"
	"buf.build/gen/go/coscene-io/coscene-openapi/protocolbuffers/go/coscene/openapi/dataplatform/v1alpha1/services"
	"connectrpc.com/connect"
	"context"
	"encoding/base64"
	"google.golang.org/protobuf/encoding/protojson"
	"log"
	"net/http"
)

func newOrganizationClient(address string) openDpsV1alpha1Connect.OrganizationServiceClient {
	httpClient := http.DefaultClient

	return openDpsV1alpha1Connect.NewOrganizationServiceClient(httpClient, address)
}

func main() {
	serverUrl := "https://openapi.coscene.cn"
	// username is "apikey" and apikey is from coscene API console.
	username := "apikey"
	apikey := "Your API Key from coscene API Console"

	client := newOrganizationClient(serverUrl)
	req := connect.NewRequest(&services.GetOrganizationRequest{
		Name: "organizations/current",
	})
	req.Header().Set("Authorization", getBasicToken(username, apikey))

	res, err := client.GetOrganization(context.Background(), req)
	if err != nil {
		log.Fatal(err)
	}

	marshal, err := protojson.Marshal(res.Msg)
	if err != nil {
		log.Fatal(err)
	}

	log.Println(string(marshal))
}

func getBasicToken(username, apikey string) string {
	return "Basic " + base64.StdEncoding.EncodeToString([]byte(username+":"+apikey))
}
