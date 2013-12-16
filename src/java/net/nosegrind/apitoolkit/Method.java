package net.nosegrind.apitoolkit;


public enum Method {
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE");

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