package ro.pub.cs.systems.eim.practicaltest02.model;

/**
 * Created by Razvan on 21-May-18.
 */

public class Alarm {

    public int hour;
    public int min;
    public int status;

    public Alarm(Integer hour, Integer min) {
        this.hour = hour;
        this.min = min;
        this.status = 0;
    }
}
