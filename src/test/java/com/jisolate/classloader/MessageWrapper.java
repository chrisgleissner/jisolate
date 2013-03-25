package com.jisolate.classloader;

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
