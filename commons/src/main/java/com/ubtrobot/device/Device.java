package com.ubtrobot.device;

public class Device {

    private final String id;
    private final String name;
    private String description;

    protected Device(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Device(GenericBuilder<?> builder) {
        id = builder.id;
        name = builder.name;
        description = builder.description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (id != null ? !id.equals(device.id) : device.id != null) return false;
        if (name != null ? !name.equals(device.name) : device.name != null) return false;
        return description != null ? description.equals(device.description) : device.description == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Builder extends GenericBuilder<Builder> {

        public Builder(String id, String name) {
            super(id, name);
        }
    }

    protected static class GenericBuilder<T extends GenericBuilder<T>> {

        private final String id;
        private final String name;
        private String description;

        public GenericBuilder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public T setDescription(String description) {
            this.description = description;
            return (T) this;
        }

        public Device build() {
            Device device = new Device(id, name);
            device.description = description;
            return device;
        }
    }
}