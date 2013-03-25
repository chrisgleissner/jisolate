package com.jisolate;

/**
 * Factory of {@code Isolate}s.
 */
public interface Isolater {

    Isolate isolate();

    <T> T isolate(Class<T> t);
}
