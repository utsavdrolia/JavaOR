package org.crowdcache.approxcache.objreccache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by utsav on 2/9/16.
 */
public class LRUCache<K extends Object, V extends Object> extends LinkedHashMap<K, V>
{
    Integer size;
    public LRUCache(Integer size)
    {
        super(size, 1.1f, false);
        this.size = size;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> entry)
    {
        return size() > size;
    }
}
