package net.nosegrind.apitoolkit;


public enum RestMethod {
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE");

    private String value;

    RestMethod(String value) {
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

    public static RestMethod getEnum(String value) {
        if(value == null)
            throw new IllegalArgumentException();
        for(RestMethod v : values())
            if(value.equalsIgnoreCase(v.getValue())) return v;
        throw new IllegalArgumentException();
    }
}