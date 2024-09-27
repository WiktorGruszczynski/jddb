package com.example.JDDB.lib.annotations;

import com.example.JDDB.lib.data.GeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedValue {
    GeneratorType value() default GeneratorType.SEQUENCE;
}
