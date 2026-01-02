package com.greengrocer.models;

/**
 * Represents the statistics of orders placed during a specific hour of the day.
 */
public class HourlyOrderStats {
    private int hourOfDay;
    private int orderCount;

    /**
     * Constructs a new HourlyOrderStats instance.
     *
     * @param hourOfDay  The hour of the day (0-23).
     * @param orderCount The number of orders placed during that hour.
     */
    public HourlyOrderStats(int hourOfDay, int orderCount) {
        this.hourOfDay = hourOfDay;
        this.orderCount = orderCount;
    }

    /**
     * Gets the hour of the day.
     *
     * @return The hour (0-23).
     */
    public int getHourOfDay() {
        return hourOfDay;
    }

    /**
     * Gets the order count for this hour.
     *
     * @return The number of orders.
     */
    public int getOrderCount() {
        return orderCount;
    }
}
