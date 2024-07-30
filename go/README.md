# Go examples

You can generate the SDK in [buf](https://buf.build/coscene-io/coscene-openapi/sdks) and use the SDK in your Go
project.

## Usage

Add the following to your `go.mod` file:

```go
go get buf.build/gen/go/coscene-io/coscene-openapi/connectrpc/go@latest
go get buf.build/gen/go/coscene-io/coscene-openapi/protocolbuffers/go@latest
```

## Go version

Right now, we use go 1.22, if you want to use older versions, you may need to downgrade the go version and the
dependencies version in the `go.mod` file.

## Other usage

You can read the docs of connect-rpc in [connect](https://connectrpc.com/docs/go/getting-started).
