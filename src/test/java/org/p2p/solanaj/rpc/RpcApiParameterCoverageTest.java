package org.p2p.solanaj.rpc;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.types.Block;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.LatestBlockhash;
import org.p2p.solanaj.rpc.types.Supply;
import org.p2p.solanaj.rpc.types.TokenAccountInfo;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig;
import org.p2p.solanaj.rpc.types.config.SimulateTransactionConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

public class RpcApiParameterCoverageTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArgumentCaptor<List<Object>> listCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
    }

    @Test
    public void latestBlockhashSupportsCommitmentAndMinContextSlotConfig() throws RpcException {
        RpcClient client = Mockito.mock(RpcClient.class);
        RpcApi api = new RpcApi(client);
        Mockito.when(client.call(eq("getLatestBlockhash"), anyList(), eq(LatestBlockhash.class))).thenReturn(null);

        api.getLatestBlockhash(Commitment.CONFIRMED, 42L);

        ArgumentCaptor<List<Object>> paramsCaptor = listCaptor();
        Mockito.verify(client).call(eq("getLatestBlockhash"), paramsCaptor.capture(), eq(LatestBlockhash.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) paramsCaptor.getValue().get(0);
        assertEquals("confirmed", config.get("commitment"));
        assertEquals(42L, config.get("minContextSlot"));
    }

    @Test
    public void getTransactionUsesCommitmentValueStringAndDefaultVersion() throws RpcException {
        RpcClient client = Mockito.mock(RpcClient.class);
        RpcApi api = new RpcApi(client);
        Mockito.when(client.call(eq("getTransaction"), anyList(), eq(ConfirmedTransaction.class))).thenReturn(null);

        api.getTransaction("sig-1", Commitment.CONFIRMED);

        ArgumentCaptor<List<Object>> paramsCaptor = listCaptor();
        Mockito.verify(client).call(eq("getTransaction"), paramsCaptor.capture(), eq(ConfirmedTransaction.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) paramsCaptor.getValue().get(1);
        assertEquals("confirmed", config.get("commitment"));
        assertEquals(0, config.get("maxSupportedTransactionVersion"));
    }

    @Test
    public void getBlockPassesThroughAllBlockConfigOptions() throws RpcException {
        RpcClient client = Mockito.mock(RpcClient.class);
        RpcApi api = new RpcApi(client);
        Mockito.when(client.call(eq("getBlock"), anyList(), eq(Block.class))).thenReturn(null);

        api.getBlock(99, Commitment.FINALIZED, "json", "none", false, 0);

        ArgumentCaptor<List<Object>> paramsCaptor = listCaptor();
        Mockito.verify(client).call(eq("getBlock"), paramsCaptor.capture(), eq(Block.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) paramsCaptor.getValue().get(1);
        assertEquals("finalized", config.get("commitment"));
        assertEquals("none", config.get("transactionDetails"));
        assertEquals(false, config.get("rewards"));
    }

    @Test
    public void getSupplySupportsExcludeNonCirculatingAccountsListOption() throws RpcException {
        RpcClient client = Mockito.mock(RpcClient.class);
        RpcApi api = new RpcApi(client);
        Mockito.when(client.call(eq("getSupply"), anyList(), eq(Supply.class))).thenReturn(null);

        api.getSupply(Commitment.PROCESSED, true);

        ArgumentCaptor<List<Object>> paramsCaptor = listCaptor();
        Mockito.verify(client).call(eq("getSupply"), paramsCaptor.capture(), eq(Supply.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) paramsCaptor.getValue().get(0);
        assertEquals("processed", config.get("commitment"));
        assertEquals(true, config.get("excludeNonCirculatingAccountsList"));
    }

    @Test
    public void simulateTransactionSupportsDirectConfigOverload() throws RpcException {
        RpcClient client = Mockito.mock(RpcClient.class);
        RpcApi api = new RpcApi(client);
        SimulateTransactionConfig config = new SimulateTransactionConfig(RpcSendTransactionConfig.Encoding.base64);
        config.setReplaceRecentBlockhash(true);
        Mockito.when(client.call(eq("simulateTransaction"), anyList(), eq(org.p2p.solanaj.rpc.types.SimulatedTransaction.class)))
                .thenReturn(null);

        api.simulateTransaction("base64-tx", config);

        ArgumentCaptor<List<Object>> paramsCaptor = listCaptor();
        Mockito.verify(client).call(eq("simulateTransaction"), paramsCaptor.capture(), eq(org.p2p.solanaj.rpc.types.SimulatedTransaction.class));
        assertEquals("base64-tx", paramsCaptor.getValue().get(0));
        assertSame(config, paramsCaptor.getValue().get(1));
    }

    @Test
    public void tokenAccountsByOwnerSupportsMinContextSlotOption() throws RpcException {
        RpcClient client = Mockito.mock(RpcClient.class);
        RpcApi api = new RpcApi(client);
        Mockito.when(client.call(eq("getTokenAccountsByOwner"), anyList(), eq(TokenAccountInfo.class))).thenReturn(null);

        api.getTokenAccountsByOwner(
                new PublicKey("AoUnMozL1ZF4TYyVJkoxQWfjgKKtu8QUK9L4wFdEJick"),
                Map.of("mint", new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")),
                Map.of("commitment", Commitment.FINALIZED, "minContextSlot", 123L)
        );

        ArgumentCaptor<List<Object>> paramsCaptor = listCaptor();
        Mockito.verify(client).call(eq("getTokenAccountsByOwner"), paramsCaptor.capture(), eq(TokenAccountInfo.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) paramsCaptor.getValue().get(2);
        assertEquals("finalized", config.get("commitment"));
        assertEquals(123L, config.get("minContextSlot"));
    }

    @Test
    public void isBlockhashValidUsesCommitmentStringValue() throws RpcException {
        RpcClient client = Mockito.mock(RpcClient.class);
        RpcApi api = new RpcApi(client);
        Mockito.when(client.call(eq("isBlockhashValid"), anyList(), eq(Map.class))).thenReturn(Map.of("value", true));

        boolean valid = api.isBlockhashValid("hash-abc", Commitment.CONFIRMED, 999L);

        assertTrue(valid);
        ArgumentCaptor<List<Object>> paramsCaptor = listCaptor();
        Mockito.verify(client).call(eq("isBlockhashValid"), paramsCaptor.capture(), eq(Map.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) paramsCaptor.getValue().get(1);
        assertEquals("confirmed", config.get("commitment"));
        assertEquals(999L, config.get("minContextSlot"));
    }
}
