package eu.thermz.java;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author riccardo
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface CSVOrder {
	int value();
}
