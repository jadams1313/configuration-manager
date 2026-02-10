package com.github.configmanger;

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
        assertEquals("db_host", mapper.snakeCaseNameMapping("dbHost"));
        assertEquals("cache_ttl", mapper.snakeCaseNameMapping("cacheTtl"));
    }

    @Test
    void testMultipleUpperCase() {
        assertEquals("max_connections", mapper.snakeCaseNameMapping("maxConnections"));
        assertEquals("url_parser", mapper.snakeCaseNameMapping("URLParser"));
    }

    @Test
    void testSingleWord() {
        assertEquals("username", mapper.snakeCaseNameMapping("username"));
        assertEquals("password", mapper.snakeCaseNameMapping("password"));
    }

    @Test
    void testNullAndEmpty() {
        assertNull(mapper.snakeCaseNameMapping(null));
        assertEquals("", mapper.snakeCaseNameMapping(""));
    }

    @Test
    void testUpperCaseFirst() {
        assertEquals("database", mapper.snakeCaseNameMapping("Database"));
        assertEquals("my_class", mapper.snakeCaseNameMapping("MyClass"));
    }

    @Test
    void testConsecutiveUpperCase() {
        assertEquals("h_t_t_p_client", mapper.snakeCaseNameMapping("HTTPClient"));
        assertEquals("x_m_l_parser", mapper.snakeCaseNameMapping("XMLParser"));
    }

    @Test
    void testMixedCase() {
        assertEquals("my_d_b_connection", mapper.snakeCaseNameMapping("myDBConnection"));
        assertEquals("get_u_r_l", mapper.snakeCaseNameMapping("getURL"));
    }
}