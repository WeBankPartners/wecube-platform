package com.webank.wecube.platform.core.utils;

import static java.util.function.Function.identity;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.webank.wecube.platform.core.commons.WecubeCoreException;

public class CollectionUtils {

    public static <K, V> Map<K, V> asMap(Collection<V> list, Function<? super V, K> keyFunction) {
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        } else {
            return list.stream().collect(Collectors.toMap(keyFunction, identity(), (oldValue, newValue) -> oldValue));
        }
    }

    public static <K, E> List<E> getOrCreateArrayList(Map<K, List<E>> map, K key) {
        List<E> element = map.get(key);
        if (element == null) {
            element = new ArrayList<>();
            map.put(key, element);
        }
        return element;
    }

    public static <T, K, V> Map<K, V> getOrCreateHashMap(Map<T, Map<K, V>> map, T key) {
        Map<K, V> element = map.get(key);
        if (element == null) {
            element = new HashMap<>();
            map.put(key, element);
        }
        return element;
    }

    public static <K, V> void putToMap(Map<K, V> targetMap, List<V> addingList, Function<? super V, K> keyMapper) {
        for (V element : addingList) {
            targetMap.put(keyMapper.apply(element), element);
        }
    }

    public static <K, V> void putToSet(Set<K> targetMap, Collection<V> addingList, Function<? super V, K> keyMapper) {
        for (V element : addingList) {
            targetMap.add(keyMapper.apply(element));
        }
    }

    public static <G, E> List<G> groupUp(List<G> groups,
                                         List<E> elements,
                                         Function<? super G, Object> keyMapperOfGroup,
                                         Function<? super G, List<E>> childrenMapperOfGroup,
                                         Function<? super E, Object> parentMapperOfElement) {
        if (isEmpty(groups)) return groups;
        if (isEmpty(elements)) return groups;
        if (keyMapperOfGroup == null)
            throw new WecubeCoreException("Key mapper of Group Object cannot be null for grouping function.");
        if (childrenMapperOfGroup == null)
            throw new WecubeCoreException("Children mapper of Group Object cannot be null for grouping function.");
        if (parentMapperOfElement == null)
            throw new WecubeCoreException("Parent mapper of Element Object cannot be null for grouping function.");
        List<G> resultGroups = new ArrayList<>(groups);
        Map<Object, G> groupMap = asMap(resultGroups, keyMapperOfGroup);

        elements.forEach(element -> {
            Object parentId = parentMapperOfElement.apply(element);
            if (parentId == null) return;
            G group = groupMap.get(parentId);
            if (group == null) return;
            List<E> children = childrenMapperOfGroup.apply(group);
            if (children == null) throw new WecubeCoreException("Children property should not be null.");
            children.add(element);
        });
        return resultGroups;
    }

    public static <E> E pickRandomOne(List<E> collection) {
        if (isEmpty(collection)) return null;
        return collection.get(new Random().nextInt(collection.size()));
    }

    public static <E> E pickLastOne(List<E> collection, Comparator<E> comparator) {
        if (isEmpty(collection)) return null;
        if (comparator != null) collection.sort(comparator);
        return collection.get(collection.size() - 1);
    }

    public static <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
        // check if the input is null, but will tolerate the data in the list can be null
        if (keys == null || values == null) {
            throw new WecubeCoreException("Each input list should not be NULL");
        }
        if (keys.size() != values.size()) {
            throw new WecubeCoreException("The input lists' size should be equal.");
        }
        Map<K, V> result = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            result.put(keys.get(i), values.get(i));
        }
        return result;
    }
}
