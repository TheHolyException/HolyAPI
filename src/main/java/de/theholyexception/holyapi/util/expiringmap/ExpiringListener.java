package de.theholyexception.holyapi.util.expiringmap;

public abstract class ExpiringListener {

        private long timeStamp;
        private final Object key;
        private final Object value;

        protected ExpiringListener(long timeStamp, Object key, Object value) {
                this.timeStamp = timeStamp;
                this.key = key;
                this.value = value;
        }
        abstract void expire();

        protected long getTimeStamp() {
                return timeStamp;
        }

        public Object getKey() {
                return key;
        }

        public Object getValue() {
                return value;
        }

        public void setTimeStamp(long timeStamp) {
                this.timeStamp = timeStamp;
        }
}
