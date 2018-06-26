package com.ubtrobot.diagnosis;

public class Part {

    private String id;
    private String name;
    private String description;

    private Part() {
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

        Part part = (Part) o;

        if (id != null ? !id.equals(part.id) : part.id != null) return false;
        if (name != null ? !name.equals(part.name) : part.name != null) return false;
        return description != null ? description.equals(part.description) : part.description == null;
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
        return "Part{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Builder {

        private String id;
        private String name;
        private String description;

        public Builder(String id) {
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

        public Part build() {
            Part part = new Part();
            part.id = id == null ? "" : id;
            part.name = name == null ? "" : name;
            part.description = description == null ? "" : description;
            return part;
        }
    }
}
