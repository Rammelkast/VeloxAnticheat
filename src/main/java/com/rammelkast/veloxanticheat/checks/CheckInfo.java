package com.rammelkast.veloxanticheat.checks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckInfo {

	String name() default "Unnamed";
	
	char type() default '?';

}
