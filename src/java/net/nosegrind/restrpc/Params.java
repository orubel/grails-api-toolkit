package net.nosegrind.restrpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Params{
    String paramType();
    String name();
    String belongsTo() default "";
    String description();
    String[] roles() default {""};
    boolean required() default true;
    String mockData() default "";
    Param[] values() default {};
}
