package net.nosegrind.restrpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String paramType();
    String name();
    String belongsTo() default "";
    String description();
    boolean required() default true;
    String mockData() default "";
    String[] roles() default {};
    String requiredRole() default "";
}
