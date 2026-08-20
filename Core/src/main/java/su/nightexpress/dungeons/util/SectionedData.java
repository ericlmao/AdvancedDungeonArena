package su.nightexpress.dungeons.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.util.NumberUtil;
import su.nightexpress.dungeons.nightcore.util.Numbers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class SectionedData {

    private static final String DEF_SECTION_DELIMITER = ";";
    private static final String DEF_GLOBAL_DELIMITER = " ";

    private final List<List<String>> data;

    public SectionedData(@NonNull List<List<String>> data) {
        this.data = data;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    @NonNull
    public static SectionedData deserialize(@NonNull String source) {
        List<List<String>> data = Arrays.stream(source.split(DEF_GLOBAL_DELIMITER)).map(section -> Arrays.asList(section.split(DEF_SECTION_DELIMITER))).toList();

        return new SectionedData(data);
    }

    @NonNull
    public String serialize() {
        return this.data.stream().map(values -> String.join(DEF_SECTION_DELIMITER, values)).collect(Collectors.joining(DEF_GLOBAL_DELIMITER));
    }

    @NonNull
    public Optional<List<String>> section(int index) {
        return index >= this.data.size() ? Optional.empty() : Optional.of(this.data.get(index));
    }

    public int sectionLength(int index) {
        return this.section(index).map(List::size).orElse(0);
    }

    @Nullable
    public String getAt(int section, int index) {
        return this.section(section).map(list -> index >= list.size() ? null : list.get(index)).orElse(null);
    }

    @NonNull
    public Optional<String> at(int section, int index) {
        return Optional.ofNullable(this.getAt(section, index));
    }

    @NonNull
    public String getString(int section, int index, @NonNull String fallback) {
        return this.at(section, index).orElse(fallback);
    }

    public int getInt(int section, int index, int fallback) {
        return this.at(section, index).map(str -> Numbers.getAnyInteger(str, fallback)).orElse(fallback);
    }

    public double getDouble(int section, int index, double fallback) {
        return this.at(section, index).map(str -> NumberUtil.getDoubleAbs(str, fallback)).orElse(fallback);
    }

    public static class Builder {

        private final List<List<String>> data;

        Builder() {
            this.data = new ArrayList<>();
        }

        @NonNull
        public SectionedData build() {
            return new SectionedData(this.data);
        }

        @NonNull
        public String serialize() {
            return this.build().serialize();
        }

        @NonNull
        public Builder section(double... values) {
            this.data.add(DoubleStream.of(values).boxed().map(String::valueOf).toList());
            return this;
        }

        @NonNull
        public Builder section(int... values) {
            this.data.add(IntStream.of(values).boxed().map(String::valueOf).toList());
            return this;
        }

        @NonNull
        public Builder section(@NonNull String... values) {
            this.data.add(Arrays.asList(values));
            return this;
        }
    }
}
