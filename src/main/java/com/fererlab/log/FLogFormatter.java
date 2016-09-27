package com.fererlab.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class FLogFormatter extends SimpleFormatter {

    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder stringBuilder = new StringBuilder();
        // date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(record.getMillis());
        // add logger key
        stringBuilder.append("[LOG] ");
        // current date time
        stringBuilder.append("[");
        stringBuilder.append(simpleDateFormat.format(date));
        stringBuilder.append("] ");
        // log level
        stringBuilder.append("[");
        stringBuilder.append(record.getLevel());
        stringBuilder.append("] ");
        // current thread
        stringBuilder.append("[");
        stringBuilder.append(Thread.currentThread().getId());
        stringBuilder.append("] ");
        // get caller
        stringBuilder.append("[");
        stringBuilder.append(record.getSourceClassName());
        stringBuilder.append(" : ");
        stringBuilder.append(record.getSourceMethodName());
        stringBuilder.append("] ");
        // format message
        stringBuilder.append(record.getMessage());
        // add new line
        stringBuilder.append(System.lineSeparator());
        // return log message
        return stringBuilder.toString();
    }

    @Override
    public synchronized String formatMessage(LogRecord record) {
        return format(record);
    }

}
