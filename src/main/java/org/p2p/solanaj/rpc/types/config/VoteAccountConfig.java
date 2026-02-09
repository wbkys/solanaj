package org.p2p.solanaj.rpc.types.config;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class VoteAccountConfig {

    /**
     * Optional vote account public key filter.
     */
    private String votePubkey;

    /**
     * Optional commitment level.
     */
    private String commitment;

    /**
     * Optional inclusion of delinquent validators without stake.
     */
    private Boolean keepUnstakedDelinquents;

    /**
     * Optional delinquent distance threshold in slots.
     */
    private Long delinquentSlotDistance;

}
