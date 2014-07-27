/* ****************************************************************************
 * Copyright 2014 Owen Rubel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/
package net.nosegrind.apitoolkit;

/*
 * Method		Idempotent	Safe
 * OPTIONS		yes			yes
 * HEAD			yes			yes
 * 
 * GET			yes			yes
 * PUT			yes			no
 * POST			no			no
 * DELETE		yes			no
 * PATCH		no			no
 * TRACE		no			yes
 */

public enum Method {
	OPTIONS("OPTIONS"),
	HEAD("HEAD"),
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	PATCH("PATCH"),
	TRACE("TRACE");

    private String value;

    Method(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public String getKey() {
        return name();
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static Method getEnum(String value) {
        if(value == null)
            throw new IllegalArgumentException();
        for(Method v : values())
            if(value.equalsIgnoreCase(v.getValue())) return v;
        throw new IllegalArgumentException();
    }
}