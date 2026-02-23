package concurrency;

import java.util.function.Supplier;

public class StableMyCustom<T> {

        T value = null;

        private StableMyCustom() {
        }

        public static <T> StableMyCustom<T> of() {
            return new StableMyCustom<>();
        }

        // concurrent access to set operation can cause errors. Use synchronized for same instance
        public synchronized T orElseSet(Supplier<T> lambda) {
            // first set operation and value is null
            if (value == null) {
                value = lambda.get(); // set operation
                return value;
            }
            // return rest but not set operation executed
            return value;
        }
    }