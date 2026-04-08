package net.stirdrem.toglide;


/*
Duck interface to allow mixin to add accessible fields and methods
 */

import net.stirdrem.toglide.items.GliderItem;

public interface PlayerEntityDuck {

    boolean toglide$isGliding();

    void toglide$setIsGliding(boolean isPlayerGliding);

    boolean toglide$isActivatingGlider();

    void toglide$setIsActivatingGlider(boolean isPlayerActivatingGlider);

    GliderItem toglide$getActiveGlider();

    void toglide$setActiveGlider(GliderItem item);
}