package org.craftgalaxy.galaxycore.client.login;

import org.craftgalaxy.galaxycore.client.data.ClientData;
import org.craftgalaxy.galaxycore.client.login.impl.AuthenticateTask;
import org.craftgalaxy.galaxycore.client.login.impl.BotCheckTask;
import org.craftgalaxy.galaxycore.client.login.impl.PasswordSetTask;
import org.craftgalaxy.galaxycore.compat.login.LoginTask;

public abstract class PlayerLoginTask implements LoginTask<ClientData> {

    public enum LoginTasks {

        BOT_CHECK(new BotCheckTask()),
        PASSWORD_SET(new PasswordSetTask()),
        AUTHENTICATE(new AuthenticateTask());

        public static LoginTasks[] VALUES = values();
        private final PlayerLoginTask task;

        private LoginTasks(PlayerLoginTask task) {
            this.task = task;
        }

        public PlayerLoginTask getTask() {
            return this.task;
        }
    }
}
