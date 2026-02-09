package org.p2p.solanaj.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.LatestBlockhash;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.Supply;
import org.p2p.solanaj.rpc.types.config.Commitment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RpcApiOverloadIntegrationTest {

    private RpcClient client;
    private static final PublicKey SYSVAR_PROGRAM_ID = new PublicKey("Sysvar1111111111111111111111111111111111111");

    @BeforeEach
    public void setup() throws InterruptedException {
        client = new RpcClient(Cluster.MAINNET);
        Thread.sleep(200L);
    }

    @Test
    public void getProgramAccountsOverloadSupportsWithContext() throws RpcException {
        List<ProgramAccount> accounts = client.getApi().getProgramAccounts(
                SYSVAR_PROGRAM_ID,
                Commitment.CONFIRMED,
                true,
                null
        );

        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
    }

    @Test
    public void getProgramAccountsWithContextFlagReturnsAccounts() throws RpcException {
        List<ProgramAccount> result = client.getApi().getProgramAccounts(
                SYSVAR_PROGRAM_ID,
                Commitment.CONFIRMED,
                true,
                null
        );

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void getLatestBlockhashOverloadWorks() throws RpcException {
        long currentSlot = client.getApi().getSlot();
        long minContextSlot = Math.max(0L, currentSlot - 100L);

        LatestBlockhash latestBlockhash = client.getApi().getLatestBlockhash(Commitment.CONFIRMED, minContextSlot);

        assertNotNull(latestBlockhash);
        assertNotNull(latestBlockhash.getValue());
        assertNotNull(latestBlockhash.getValue().getBlockhash());
    }

    @Test
    public void getSupplyOverloadWorks() throws RpcException {
        Supply supply = client.getApi().getSupply(Commitment.CONFIRMED, true);

        assertNotNull(supply);
        assertNotNull(supply.getValue());
        assertTrue(supply.getValue().getTotal() > 0);
    }

    @Test
    public void getTransactionOverloadWorks() throws RpcException {
        String transactionSignature =
                "25XzdvPoirNY8kFALxVZXWbdU6LmMitNceHciYRSNV4S5zjUPjeJaWHCP9dingewmrsrcoKtAP57JXyVXtSsV6Bv";

        ConfirmedTransaction tx = client.getApi().getTransaction(transactionSignature, Commitment.CONFIRMED, 0);

        assertNotNull(tx);
        assertNotNull(tx.getTransaction());
    }
}
