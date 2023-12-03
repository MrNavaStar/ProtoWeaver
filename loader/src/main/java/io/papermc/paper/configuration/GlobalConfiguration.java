package io.papermc.paper.configuration;

public class GlobalConfiguration {
    public static GlobalConfiguration get() {
        return new GlobalConfiguration();
    }

    public Proxies proxies;

    public class Proxies {
        public Velocity velocity;
        public class Velocity {
            public String secret = "";
        }
    }
}