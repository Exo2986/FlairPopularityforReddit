package exomaster.flairpopularity.config;

public class ConfigEntry<T> {
    private T defaultValue;

    public T getDefaultValue() {
        return defaultValue;
    }

    private String entryName;

    public String getEntryName() {
        return entryName;
    }

    public ConfigEntry(String name, T def) {
        entryName = name;
        defaultValue = def;
    }
}