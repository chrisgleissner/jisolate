package com.purelypro.isolate.classloader;

public class IsolatableClass {

    public MessageWrapper getMessageWrapper(String message) {
        return new MessageWrapper(message);
    }

    public String ping(String s) {
        return "pong " + s;
    }

    public String staticPing(String s) {
        return "staticPong " + s;
    }
}
