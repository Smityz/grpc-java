package io.grpc.examples.loadbalance;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;

public class ExampleNameResolverProvider extends NameResolverProvider {
    public static final String exampleScheme = "example";

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        return new ExampleNameResolver(targetUri.toString());
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Override
    // gRPC choose the first NameResolverProvider that supports the target URI scheme.
    public String getDefaultScheme() {
        return exampleScheme;
    }
}
