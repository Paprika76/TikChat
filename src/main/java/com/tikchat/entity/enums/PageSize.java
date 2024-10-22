package com.tikchat.entity.enums;

public enum PageSize {
    SIZE15(15),
    SIZE20(20),
    SIZE10(10),
    SIZE5(5),
    SIZE7(7),
    SIZE8(8),
    SIZE9(9),
    ;

    PageSize(Integer size) {
        this.size = size;
    }

    private Integer size;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
