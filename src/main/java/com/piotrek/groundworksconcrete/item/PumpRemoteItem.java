package com.piotrek.groundworksconcrete.item;

import net.minecraft.world.item.Item;

/**
 * Concrete boom handheld controller. Pair by right-clicking the pump truck.
 * The pairing is stored on the server-side pump entity, not trusted from
 * unvalidated item NBT or client-originated entity IDs.
 */
public final class PumpRemoteItem extends Item {
    public PumpRemoteItem(Properties properties){super(properties);}
}
