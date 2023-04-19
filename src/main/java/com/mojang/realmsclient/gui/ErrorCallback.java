package com.mojang.realmsclient.gui;

import net.minecraft.network.chat.Component;

public interface ErrorCallback
{
    void error(Component pError);

default void error(String pError)
    {
        this.error(Component.literal(pError));
    }
}
