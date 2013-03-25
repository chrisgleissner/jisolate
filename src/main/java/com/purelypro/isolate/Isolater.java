package com.purelypro.isolate;

public interface Isolater {

    Isolate isolate();

    <T> T isolate(Class<T> t);
}
