package org.craftgalaxy.galaxycore.proxy.runnable;

import org.craftgalaxy.galaxycore.compat.Callback;

public interface Shutdownable {

    void cancelShutdown(String var1);

    void scheduleShutdown(int var1);

    void shutdownStatus(String var1, Callback<Integer> var2);
}
