package com.example.mycourseschedule.Helper;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Converters {


    @TypeConverter
public static List<String> fromString(String value) {
    if (value == null || value.isEmpty()) {
        return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(value.split(",")));
}

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    @TypeConverter
    public static Date fromTimestamp(Long value) { return value == null ? null : new Date(value); }
    @TypeConverter
    public static Long dateToTimestamp(Date date) { return date == null ? null : date.getTime(); }


}
