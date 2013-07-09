package net.nosegrind.restrpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Variable{
    String paramType();
    String name();
    String description();
    boolean required();
    Param[] value();
}
