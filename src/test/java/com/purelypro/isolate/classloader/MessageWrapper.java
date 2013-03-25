package com.purelypro.isolate.classloader;

public class MessageWrapper {

    private final String message;

    public MessageWrapper(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
