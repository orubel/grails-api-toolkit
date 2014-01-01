package net.nosegrind.apitoolkit;

/*
 * Method	Idempotent	Safe
 * OPTIONS	yes			yes
 * GET		yes			yes
 * HEAD		yes			yes
 * PUT		yes			no
 * POST		no			no
 * DELETE	yes			no
 * PATCH	no			no - See more at: http://restcookbook.com/HTTP%20Methods/idempotency/#sthash.90mexIwP.dpuf
 */

public enum Method {
	OPTIONS("OPTIONS"),
	GET("GET"),
	HEAD("HEAD"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	PATCH("PATCH");

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