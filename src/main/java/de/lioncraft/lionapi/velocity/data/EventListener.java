package de.lioncraft.lionapi.velocity.data;

import com.velocitypowered.api.proxy.messages.ChannelMessageSource;

public interface EventListener {
    void onReceive(TransferrableObject object, ChannelMessageSource source);
}
