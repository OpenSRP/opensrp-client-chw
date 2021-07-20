package org.smartregister.chw.util;

import com.mapbox.mapboxsdk.log.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtils {

    public static String changeDateFormat(String date){
        String[] splitDob = date.split("T");
        String inputFormat = "yyyy-MM-dd";
        String OutPutFormat = "dd-MM-yyyy";
        String convertedDate = formatDate(splitDob[0], inputFormat, OutPutFormat);
        return convertedDate;
    }

    public static String formatDate(String dateToFormat, String inputFormat, String outputFormat) {
        try {
            Logger.e("DATE", "Input Date Date is " + dateToFormat);
            String convertedDate = new SimpleDateFormat(outputFormat)
                    .format(new SimpleDateFormat(inputFormat)
                            .parse(dateToFormat));
            Logger.e("DATE", "Output Date is " + convertedDate);
            //Update Date
            return convertedDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
