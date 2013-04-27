package org.hibnet.jst.test;

public interface Cache {

    public void set(long key, Object value);

    public Object get(long key);
}
