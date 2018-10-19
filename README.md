# Jisolate

[![Build Status](https://travis-ci.org/chrisgleissner/jisolate.svg?branch=master)](https://travis-ci.org/chrisgleissner/jisolate)
[![Coverage Status](https://coveralls.io/repos/chrisgleissner/jisolate/badge.svg)](https://coveralls.io/r/chrisgleissner/jisolate)

Jisolate is an API for isolating Java classes using either classloader or VM isolation. 

## Why Isolation?

Isolates are useful to load the same class multiple times, typically using different configurations.
For example, some frameworks are configured using static singletons. For tests, it is often useful 
to start multiple instances of these frameworks, each with its own configuration.  


## Classloader Isolation

Classloader isolation uses a child-first classloader combined with thread-local system properties
to ensure that non-JDK classes can be loaded multiple times in the same JVM. 

Classloader isolation is the isolation approach of choice as it is the most performant approach of ensuring isolation
and allows for an easy way to communicate results from the isolated code back to its invoker.

```java
Jisolate.classLoaderIsolation()
    .withIsolatableClass(IsolatedClass.class)
    .withIsolatableArguments("foo")
    .isolate();
```

## VM Isolation

VM isolation spawns a child VM process. This provides even better isolation than the classloader approach,
but it takes slightly longer to setup.

```java
Jisolate.jvmIsolation()
    .withMainClass(IsolatedClass.class)
    .withMainClassArguments("foo")
    .isolate();
```

#### Lifecycle control

VM isolation also allows for forcefully terminating the isolated JVM process when it's no longer needed:

```java
try (JvmIsolate isolate = Jisolate.jvmIsolation()
    .withMainClass(IsolatedClass.class)
    .withMainClassArguments("foo")
    .isolate()) {
    
    // The isolated JVM is automatically terminated on leaving this block    
}
```

## JSR-121

Jisolate is not an implementation of <a href="http://www.jcp.org/en/jsr/detail?id=121">JSR-121</a>, the Application
Isolation API Specification. Instead, it is a light-weight and pragmatic approach of providing
a best effort isolation of Java classes that is useful for many scenarios.

