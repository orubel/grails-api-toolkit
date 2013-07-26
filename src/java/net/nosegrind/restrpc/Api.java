package net.nosegrind.restrpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Api{
	RestMethod method();
    String description();
    Params[] values() default {};
    Params[] returns() default {};
    ErrorCode[] errors() default {};
}