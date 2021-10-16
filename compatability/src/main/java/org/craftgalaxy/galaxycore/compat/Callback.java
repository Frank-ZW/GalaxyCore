package org.craftgalaxy.galaxycore.compat;

public interface Callback<V> {

    void done(V result, Throwable cause);
}
