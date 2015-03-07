package at.rseiler.spbee.core.util;

import at.rseiler.spbee.core.pojo.AnnotationValueInfo;
import org.junit.Test;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import java.util.Arrays;
import java.util.List;

import static at.rseiler.spbee.core.pojo.AnnotationValueInfo.Kind.DECLARED_TYPE;
import static at.rseiler.spbee.core.pojo.AnnotationValueInfo.Kind.ELEMENT;
import static at.rseiler.spbee.core.pojo.AnnotationValueInfo.Kind.LIST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ElementConverterTest {

    @Test
    public void testConvert1() throws Exception {
        // @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {SQLSyntaxErrorException.class, HsqlException.class})

        Element isolationElement = mock(Element.class);
        when(isolationElement.toString()).thenReturn("READ_COMMITTED");

        AnnotationValueInfo annotationValueInfo = ElementConverter.convert("isolation", isolationElement, isolationElement.toString());

        assertThat(annotationValueInfo.getName(), is("isolation"));
        assertThat(annotationValueInfo.getType(), is("READ_COMMITTED"));
        assertThat(annotationValueInfo.getKind(), is(ELEMENT));
        assertEquals("READ_COMMITTED", annotationValueInfo.getValue());
    }

    @Test
    public void testConvert2() throws Exception {
        AnnotationValue rollbackElement1 = mock(AnnotationValue.class);
        DeclaredType declaredType1 = mock(DeclaredType.class);
        when(declaredType1.toString()).thenReturn("java.sql.SQLSyntaxErrorException");
        when(rollbackElement1.getValue()).thenReturn(declaredType1);
        when(rollbackElement1.toString()).thenReturn("java.sql.SQLSyntaxErrorException.class");

        AnnotationValue rollbackElement2 = mock(AnnotationValue.class);
        DeclaredType declaredType2 = mock(DeclaredType.class);
        when(declaredType2.toString()).thenReturn("org.hsqldb.HsqlException");
        when(rollbackElement2.getValue()).thenReturn(declaredType2);
        when(rollbackElement2.toString()).thenReturn("org.hsqldb.HsqlException.class");

        List<AnnotationValue> rollbackForList = Arrays.asList(
                rollbackElement1,
                rollbackElement2
        );

        AnnotationValueInfo annotationValueInfo = ElementConverter.convert("rollbackFor", rollbackForList, rollbackForList.toString());

        assertThat(annotationValueInfo.getName(), is("rollbackFor"));
        assertThat(annotationValueInfo.getKind(), is(LIST));

        List<AnnotationValueInfo> list = (List<AnnotationValueInfo>) annotationValueInfo.getValue();
        assertThat(list.get(0).getValue(), is("java.sql.SQLSyntaxErrorException"));
        assertThat(list.get(0).getType(), is("java.sql.SQLSyntaxErrorException.class"));
        assertThat(list.get(0).getKind(), is(DECLARED_TYPE));
        assertThat(list.get(1).getValue(), is("org.hsqldb.HsqlException"));
        assertThat(list.get(1).getType(), is("org.hsqldb.HsqlException.class"));
        assertThat(list.get(1).getKind(), is(DECLARED_TYPE));
    }
}