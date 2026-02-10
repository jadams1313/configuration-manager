package com.github.configmanager;

import com.github.configmanager.annotations.FieldNameMappings;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for field name mapping functionality.
 */
class FieldNameMappingsTest {

    private final FieldNameMappings mapper = new FieldNameMappings();

    @Test
    void testSimpleCamelCase() {
        assertEquals("db_host", mapper.nameMapping("dbHost"));
        assertEquals("cache_ttl", mapper.nameMapping("cacheTtl"));
    }

    @Test
    void testMultipleUpperCase() {
        assertEquals("max_connections", mapper.nameMapping("maxConnections"));
        assertEquals("url_parser", mapper.nameMapping("URLParser"));
    }

    @Test
    void testSingleWord() {
        assertEquals("username", mapper.nameMapping("username"));
        assertEquals("password", mapper.nameMapping("password"));
    }

    @Test
    void testNullAndEmpty() {
        assertNull(mapper.nameMapping(null));
        assertEquals("", mapper.nameMapping(""));
    }

    @Test
    void testUpperCaseFirst() {
        assertEquals("database", mapper.nameMapping("Database"));
        assertEquals("my_class", mapper.nameMapping("MyClass"));
    }

    @Test
    void testConsecutiveUpperCase() {
        assertEquals("h_t_t_p_client", mapper.nameMapping("HTTPClient"));
        assertEquals("x_m_l_parser", mapper.nameMapping("XMLParser"));
    }

    @Test
    void testMixedCase() {
        assertEquals("my_d_b_connection", mapper.nameMapping("myDBConnection"));
        assertEquals("get_u_r_l", mapper.nameMapping("getURL"));
    }
}