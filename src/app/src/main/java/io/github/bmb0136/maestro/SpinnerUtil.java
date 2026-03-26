package io.github.bmb0136.maestro;

import javafx.scene.control.SpinnerValueFactory;

public final class SpinnerUtil {
    private SpinnerUtil() {}

    public static <T> SpinnerValueFactory<Object> toObjectValueFactory(SpinnerValueFactory<T> base) {
        SpinnerValueFactory<Object> factory = new SpinnerValueFactory<>() {
            @Override
            public void decrement(int steps) {
                base.decrement(steps);
                setValue(base.getValue());
            }

            @Override
            public void increment(int steps) {
                base.increment(steps);
                setValue(base.getValue());
            }
        };
        factory.setValue(base.getValue());
        return factory;
    }
}
