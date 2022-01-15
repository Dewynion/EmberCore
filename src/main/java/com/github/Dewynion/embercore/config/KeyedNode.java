package com.github.Dewynion.embercore.config;

import java.util.*;

public final class KeyedNode<T> {
    private KeyedNode parent;
    private String key;
    private T value;
    private Map<String, KeyedNode<T>> children;

    KeyedNode(T value) {
        this("", value);
    }

    KeyedNode(String key, T value) {
        this(null, key, value);
    }

    KeyedNode(KeyedNode parent, String key, T value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        children = new HashMap<>();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public T value() {
        return value;
    }

    public T get(T defaultValue, String... key) {
        try {
            return getChild(key).value;
        } catch (NullPointerException ex) {
            return defaultValue;
        }
    }

    // TODO actually create a child

    public KeyedNode<T> createChild(String... key) {
        int index = 0;
        KeyedNode<T> current = this;
        while (index < key.length) {
            current = current.getChild(key[index++]);
            if (current == null)
                break;
        }
        return current;
    }

    public KeyedNode<T> getChild(String... key) {
        int index = 0;
        KeyedNode<T> current = this;
        while (index < key.length) {
            current = current.getChild(key[index++]);
            if (current == null)
                break;
        }
        return current;
    }

    public KeyedNode<T> getChild(String key) throws NullPointerException {
        try {
            return children.get(key);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public Map<String, Object> getChildren() {
        return Collections.unmodifiableMap(children);
    }
}
