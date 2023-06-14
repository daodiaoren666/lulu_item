package com.lulu.usercenter.model.enums;

/**
 * 队伍状态枚举类
 */
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");
    private int value;
    private String text;
   public static TeamStatusEnum getEumByValue(Integer value){
        if(value==null){
            return null;
        }
       TeamStatusEnum[] values = TeamStatusEnum.values();
       for (TeamStatusEnum teamStatusEnums : values) {
           if(teamStatusEnums.getValue()==value){
               return teamStatusEnums;
           }
       }
       return null;
   }
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}
