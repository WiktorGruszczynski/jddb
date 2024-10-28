package jddb.data.annotations;

import jddb.data.enums.GeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedValue {
    GeneratorType value() default GeneratorType.SEQUENCE;
}
