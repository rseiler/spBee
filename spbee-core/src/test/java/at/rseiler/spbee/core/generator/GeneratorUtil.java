package at.rseiler.spbee.core.generator;

import at.rseiler.spbee.core.annotation.MappingConstructor;
import at.rseiler.spbee.core.annotation.ReturnNull;
import at.rseiler.spbee.core.annotation.RowMapper;
import at.rseiler.spbee.core.annotation.StoredProcedure;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GeneratorUtil {

    private GeneratorUtil() {
    }

    static StoredProcedure getStoredProcedure(String value) {
        StoredProcedure storedProcedure = mock(StoredProcedure.class);
        when(storedProcedure.value()).thenReturn(value);
        return storedProcedure;
    }

    static RowMapper getRowMapper(Class value) {
        RowMapper rowMapper = mock(RowMapper.class);
        when(rowMapper.value()).thenReturn(value);
        return rowMapper;
    }

    static ReturnNull getReturnNull() {
        return mock(ReturnNull.class);
    }

    static MappingConstructor getMappingConstructor(String value) {
        MappingConstructor mappingConstructor = mock(MappingConstructor.class);
        when(mappingConstructor.value()).thenReturn(value);
        return mappingConstructor;
    }

    static void assertContains(String text, String... searchStrings) {
        for (String searchString : searchStrings) {
            assertTrue("Text doesn't contain search string: " + searchString + "\n" + text, text.contains(searchString));
        }
    }

}
