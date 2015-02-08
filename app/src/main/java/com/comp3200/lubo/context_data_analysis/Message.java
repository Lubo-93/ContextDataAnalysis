package com.comp3200.lubo.context_data_analysis;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lubo on 19.1.2015.
 *
 * A custom message class
 */
public class Message implements Comparable<Message> {

    // Which class or activity issued the message
    private String mIssuer;
    // The contents of the message
    private String mContent;
    // Any extra information
    private String mExtra;
    // Instance of calendar used to get the current date and time
    private Calendar mCalendar;
    // Date object used only to sort messages
    private Date mDate;
    // The date the message was created in a dd-mm format
    private String mDayMonth;
    // The time the message was created in a hh-mm-ss.mss format
    private String mTime;

    public Message(String issuer, String content){
        mCalendar = Calendar.getInstance();
        setDate(mCalendar);
        mIssuer = issuer;
        mContent = content;
    }

    // Set the dat and time of the message
    private void setDate(Calendar calendar){
        // Get the current data as a java.util.Date object
        mDate = calendar.getTime();
        // Get the current day and month
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        // Convert it to a string of the desired format
        mDayMonth = (day + "-" + month);
        // Get the current time
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int mins = calendar.get(Calendar.MINUTE);
        int secs = calendar.get(Calendar.SECOND);
        int milis = calendar.get(Calendar.MILLISECOND);
        // Convert it to a string of the desired format
        mTime = (hours + ":" + mins + ":" + secs + "." + milis);
    }

    // Compare messages by date of creation
    @Override
    public int compareTo(Message o) {
        return getDate().compareTo(o.getDate());
    }

    // Getters and setters
    public String getIssuer() {
        return mIssuer;
    }

    public String getContent() {
        return mContent;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra){
        mExtra = extra;
    }

    public Date getDate() {
        return mDate;
    }

    public String getDayMonth() {
        return mDayMonth;
    }

    public String getTime() {
        return mTime;
    }


}
