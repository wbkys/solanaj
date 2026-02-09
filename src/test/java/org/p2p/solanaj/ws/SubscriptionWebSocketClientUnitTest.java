package org.p2p.solanaj.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import okhttp3.WebSocket;

public class SubscriptionWebSocketClientUnitTest {

    @Test
    public void endpointConversionPreservesPortPathAndQuery() throws Exception {
        Method converter = SubscriptionWebSocketClient.class.getDeclaredMethod("toWebSocketEndpoint", URI.class);
        converter.setAccessible(true);

        String converted = (String) converter.invoke(
                null,
                new URI("https://rpc.example.com:9443/custom/path?api-key=abc123")
        );

        assertEquals("wss://rpc.example.com:9443/custom/path?api-key=abc123", converted);
    }

    @Test
    public void endpointConversionSupportsHttpScheme() throws Exception {
        Method converter = SubscriptionWebSocketClient.class.getDeclaredMethod("toWebSocketEndpoint", URI.class);
        converter.setAccessible(true);

        String converted = (String) converter.invoke(null, new URI("http://localhost:8899"));

        assertEquals("ws://localhost:8899", converted);
    }

    @Test
    public void unsubscribeUsesCorrectRpcMethodForActiveLogsSubscription() throws Exception {
        SubscriptionWebSocketClient client = SubscriptionWebSocketClient.getExactPathInstance("ws://127.0.0.1:1");
        try {
            CompletableFuture<Long> future = client.logsSubscribe("So11111111111111111111111111111111111111112", data -> {
            });
            assertTrue(!future.isDone());

            Map<?, ?> pending = getField(client, "subscriptions", Map.class);
            String requestId = pending.keySet().iterator().next().toString();
            invokeHandleMessage(client, "{\"jsonrpc\":\"2.0\",\"result\":42,\"id\":\"" + requestId + "\"}");

            // Let initial connection attempt fail first, then override connection state for deterministic testing.
            Thread.sleep(200);

            AtomicBoolean isConnected = getField(client, "isConnected", AtomicBoolean.class);
            isConnected.set(true);

            WebSocket webSocket = Mockito.mock(WebSocket.class);
            setField(client, "webSocket", webSocket);

            client.unsubscribe(42L);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(webSocket, atLeastOnce()).send(payloadCaptor.capture());
            String payload = payloadCaptor.getValue();

            assertTrue(payload.contains("\"method\":\"logsUnsubscribe\""));
            assertNull(client.getSubscriptionId("So11111111111111111111111111111111111111112"));
        } finally {
            client.close();
        }
    }

    @Test
    public void resubscribeAllRequeuesActiveAndPendingSubscriptions() throws Exception {
        SubscriptionWebSocketClient client = SubscriptionWebSocketClient.getExactPathInstance("ws://127.0.0.1:1");
        try {
            client.logsSubscribe("So11111111111111111111111111111111111111112", data -> {
            });
            client.accountSubscribe("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", data -> {
            });

            invokeHandleMessage(client, "{\"jsonrpc\":\"2.0\",\"result\":99,\"id\":\"1\"}");

            Map<?, ?> pendingBefore = getField(client, "subscriptions", Map.class);
            Map<?, ?> activeBefore = getField(client, "activeSubscriptions", Map.class);
            assertEquals(1, pendingBefore.size());
            assertEquals(1, activeBefore.size());

            invokeResubscribeAll(client);

            Map<?, ?> pendingAfter = getField(client, "subscriptions", Map.class);
            Map<?, ?> activeAfter = getField(client, "activeSubscriptions", Map.class);
            assertEquals(2, pendingAfter.size());
            assertEquals(0, activeAfter.size());
        } finally {
            client.close();
        }
    }

    private void invokeHandleMessage(SubscriptionWebSocketClient client, String message) throws Exception {
        Method method = SubscriptionWebSocketClient.class.getDeclaredMethod("handleMessage", String.class);
        method.setAccessible(true);
        method.invoke(client, message);
    }

    private void invokeResubscribeAll(SubscriptionWebSocketClient client) throws Exception {
        Method method = SubscriptionWebSocketClient.class.getDeclaredMethod("resubscribeAll");
        method.setAccessible(true);
        method.invoke(client);
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(SubscriptionWebSocketClient client, String fieldName, Class<T> type) throws Exception {
        Field field = SubscriptionWebSocketClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(client);
    }

    private void setField(SubscriptionWebSocketClient client, String fieldName, Object value) throws Exception {
        Field field = SubscriptionWebSocketClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(client, value);
    }
}
