package net.exemine.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiValueMapTest {

    private MultiValueMap<String, Integer> multiValueMap;

    @BeforeEach
    public void setUp() {
        multiValueMap = new MultiValueMap<>();
    }

    @Test
    public void testPutAndGet() {
        multiValueMap.put("key1", 1);
        multiValueMap.put("key1", 2);
        multiValueMap.put("key2", 3);

        assertEquals(2, multiValueMap.get("key1").size());
        assertTrue(multiValueMap.get("key1").contains(1));
        assertTrue(multiValueMap.get("key1").contains(2));

        assertEquals(1, multiValueMap.get("key2").size());
        assertTrue(multiValueMap.get("key2").contains(3));
    }

    @Test
    public void testRemove() {
        multiValueMap.put("key1", 1);
        multiValueMap.put("key1", 2);
        multiValueMap.put("key2", 3);

        multiValueMap.remove("key1", 1);
        assertEquals(1, multiValueMap.get("key1").size());
        assertTrue(multiValueMap.get("key1").contains(2));

        multiValueMap.remove("key2", 3);
        assertTrue(multiValueMap.get("key2").isEmpty());
    }

    @Test
    public void testEmptyGet() {
        assertTrue(multiValueMap.get("nonexistent").isEmpty());
    }

    @Test
    public void testCustomMap() {
        MultiValueMap<Integer, String> multiValueMap = new MultiValueMap<>(new LinkedHashMap<>());

        multiValueMap.put(1, "apple");
        multiValueMap.put(2, "banana");
        multiValueMap.put(3, "cherry");

        assertEquals("apple", multiValueMap.get(1).get(0));
        assertEquals("banana", multiValueMap.get(2).get(0));
        assertEquals("cherry", multiValueMap.get(3).get(0));
    }

    @Test
    public void testCustomListSupplier() {
        MultiValueMap<String, Integer> linkedListMultiValueMap = new MultiValueMap<>(LinkedList::new);

        linkedListMultiValueMap.put("key1", 1);
        linkedListMultiValueMap.put("key1", 2);

        assertTrue(linkedListMultiValueMap.get("key1") instanceof LinkedList);
    }
}