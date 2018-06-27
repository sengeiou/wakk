package com.ubtrobot.diagnosis;

public class Diagnosis {

    private String partId;
    private boolean faulty;
    private String fault;
    private String cause;

    private Diagnosis() {
    }

    public String getPartId() {
        return partId;
    }

    public boolean isFaulty() {
        return faulty;
    }

    public String getFault() {
        return fault;
    }

    public String getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "Diagnosis{" +
                "partId='" + partId + '\'' +
                ", faulty=" + faulty +
                ", fault='" + fault + '\'' +
                ", cause='" + cause + '\'' +
                '}';
    }

    public static class Builder {

        private String partId;
        private boolean faulty;
        private String fault;
        private String cause;

        public Builder setPartId(String partId) {
            this.partId = partId;
            return this;
        }

        public Builder setFaulty(boolean faulty) {
            this.faulty = faulty;
            return this;
        }

        public Builder setFault(String fault) {
            this.fault = fault;
            return this;
        }

        public Builder setCause(String cause) {
            this.cause = cause;
            return this;
        }

        public Diagnosis build() {
            Diagnosis diagnosis = new Diagnosis();
            diagnosis.partId = partId == null ? "" : partId;
            diagnosis.faulty = faulty;
            diagnosis.fault = fault == null ? "" : fault;
            diagnosis.cause = cause == null ? "" : cause;

            return diagnosis;
        }
    }
}
