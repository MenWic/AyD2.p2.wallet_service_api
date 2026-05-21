package ayd2.p2b.wallet_service_api.unit.common;

import ayd2.p2b.wallet_service_api.common.util.TextNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextNormalizerTest {

    @Test
    void should_trim_required_value() {
        assertThat(TextNormalizer.trimRequired("  hello  ", "field")).isEqualTo("hello");
    }

    @Test
    void should_throw_when_required_value_is_null() {
        assertThatThrownBy(() -> TextNormalizer.trimRequired(null, "field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field");
    }

    @Test
    void should_throw_when_required_value_is_blank() {
        assertThatThrownBy(() -> TextNormalizer.trimRequired("   ", "fieldName"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fieldName");
    }

    @Test
    void should_return_null_for_optional_blank_value() {
        assertThat(TextNormalizer.trimOptional("   ")).isNull();
        assertThat(TextNormalizer.trimOptional(null)).isNull();
    }

    @Test
    void should_trim_optional_non_blank_value() {
        assertThat(TextNormalizer.trimOptional("  value  ")).isEqualTo("value");
    }

    @Test
    void should_lower_trim_required_value() {
        assertThat(TextNormalizer.lowerTrimRequired("  HELLO  ", "field")).isEqualTo("hello");
    }
}
