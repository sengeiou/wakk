package com.ubtrobot.light;

public class LightingEffect {

    private String id;
    private String name;
    private String description;
    private Class<? extends DisplayOption> optionClass;

    private LightingEffect(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends DisplayOption> getOptionClass() {
        return optionClass;
    }

    @Override
    public String toString() {
        return "LightingEffect{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", optionClass=" + optionClass +
                '}';
    }

    public static class Builder {

        private String id;
        private String name;
        private String description;
        private Class<? extends DisplayOption> optionClass;

        public Builder(String id) {
            if (id == null) {
                throw new IllegalArgumentException("Argument id is null.");
            }

            this.id = id;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setOptionClass(Class<? extends DisplayOption> optionClass) {
            this.optionClass = optionClass;
            return this;
        }

        public LightingEffect build() {
            LightingEffect effect = new LightingEffect(id);
            effect.name = name == null ? "" : name;
            effect.description = description == null ? "" : description;
            effect.optionClass = optionClass == null ? DisplayOption.class : optionClass;
            return effect;
        }
    }
}