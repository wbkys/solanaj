package org.p2p.solanaj.rpc.types.config;

import java.util.List;
import java.util.Map;

import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramAccountConfig {

    /**
     * Account data encoding, e.g. base64/jsonParsed.
     */
    private Encoding encoding = null;

    /**
     * Solana getProgramAccounts filters array.
     */
    private List<Object> filters = null;

    /**
     * Commitment level string used by Solana RPC.
     */
    private String commitment = "processed";

    /**
     * Optional slot lower bound for changed accounts.
     */
    private Long changedSinceSlot;

    /**
     * Optional data slice map with offset/length.
     */
    private Map<String, Integer> dataSlice;

    /**
     * Whether response should include context wrapper.
     */
    private Boolean withContext;

    /**
     * Optional minimum context slot.
     */
    private Long minContextSlot;

    public ProgramAccountConfig(List<Object> filters) {
        this.filters = filters;
    }

    public ProgramAccountConfig(Encoding encoding) {
        this.encoding = encoding;
    }
}