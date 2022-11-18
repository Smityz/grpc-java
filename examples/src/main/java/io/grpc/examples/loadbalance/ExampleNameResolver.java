package io.grpc.examples.loadbalance;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExampleNameResolver extends NameResolver {

    private Listener2 listener;

    private final String authority;

    // authority is the string from the target URI passed to gRPC
    public ExampleNameResolver(String authority) {
        this.authority = authority;
    }

    @Override
    public String getServiceAuthority() {
        return authority;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void start(Listener2 listener) {
        this.listener = listener;
        this.resolve();
    }

    @Override
    public void refresh() {
        this.resolve();
    }

    private void resolve() {
        List<InetSocketAddress> addresses = new ArrayList<>();
        for (int i = 0; i < LoadBalanceServer.serverCount; i++) {
            addresses.add(new InetSocketAddress("localhost", LoadBalanceServer.startPort + i));
        }
        try {
            List<EquivalentAddressGroup> equivalentAddressGroup = addresses.stream()
                    // convert to socket address
                    .map(this::toSocketAddress)
                    // every socket address is a single EquivalentAddressGroup, so they can be accessed randomly
                    .map(address -> Arrays.asList(address))
                    .map(this::addrToEquivalentAddressGroup)
                    .collect(Collectors.toList());

            ResolutionResult resolutionResult = ResolutionResult.newBuilder()
                    .setAddresses(equivalentAddressGroup)
                    .build();

            this.listener.onResult(resolutionResult);

        } catch (Exception e){
            // when error occurs, notify listener
            this.listener.onError(Status.UNAVAILABLE.withDescription("Unable to resolve host ").withCause(e));
        }
    }

    private SocketAddress toSocketAddress(InetSocketAddress address) {
        return new InetSocketAddress(address.getHostName(), address.getPort());
    }

    private EquivalentAddressGroup addrToEquivalentAddressGroup(List<SocketAddress> addrList) {
        return new EquivalentAddressGroup(addrList);
    }
}