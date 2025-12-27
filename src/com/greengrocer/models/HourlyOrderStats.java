package com.greengrocer.models;

public class HourlyOrderStats {
    private int hourOfDay;
    private int orderCount;

    public HourlyOrderStats(int hourOfDay, int orderCount) {
        this.hourOfDay = hourOfDay;
        this.orderCount = orderCount;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public int getOrderCount() {
        return orderCount;
    }
}
