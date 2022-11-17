package io.grpc.examples.loadbalance;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExampleNameResolver extends NameResolver {

    private Listener2 listener;

    private final String authority;

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

        List<EquivalentAddressGroup> equivalentAddressGroup = addresses.stream()
                .map(this::toSocketAddress)
                .map(address -> Arrays.asList(address))
                .map(this::addrToEquivalentAddressGroup)
                .collect(Collectors.toList());

        ResolutionResult resolutionResult = ResolutionResult.newBuilder()
                .setAddresses(equivalentAddressGroup)
                .build();

        this.listener.onResult(resolutionResult);
    }

    private SocketAddress toSocketAddress(InetSocketAddress address) {
        return new InetSocketAddress(address.getHostName(), address.getPort());
    }

    private EquivalentAddressGroup addrToEquivalentAddressGroup(List<SocketAddress> addrList){
        return new EquivalentAddressGroup(addrList);
    }
}