package com.webank.wecube.platform.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyValue<K,V> {
    private K key;
    private V value;

    public static <K,V> KeyValue<K,V> of(K key, V value) {
        return new KeyValue<>(key,value);
    }
}
