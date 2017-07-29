package utils;

public class TimeDifferenceCalculator {

    public TimeDifferenceCalculator() {
    }

    public String calculateAndFormat(long timestampMillis) {
        long diff_number = System.currentTimeMillis() / 1000L - timestampMillis;
        String seconds_text = " SECOND";
        if(diff_number > 1) {
            seconds_text += "S";
        }
        String diff_text = String.valueOf(diff_number) + seconds_text;

        if(isBetween(diff_number, 61, 3600)) {
            long temp_diff = (diff_number / (60)) % 60;
            String temp_text = " MINUTE";
            if(temp_diff > 1) {
                temp_text += "S";
            }
            diff_text = String.valueOf(temp_diff) + temp_text;
        }
        else if(isBetween(diff_number, (long)3601, (long)86400)) {
            long temp_diff = (diff_number / (60*60)) % 24;
            String temp_text = " HOUR";
            if(temp_diff > 1) {
                temp_text += "S";
            }
            diff_text = String.valueOf(temp_diff) + temp_text;
        }
        else if(isBetween(diff_number, 86401, 604800)) {
            long temp_diff = diff_number / (60*60*24);
            String temp_text = " DAY";
            if(temp_diff > 1) {
                temp_text += "S";
            }
            diff_text = String.valueOf(temp_diff) + temp_text;
        }
        else if(diff_number > 604801) {
            long temp_diff = diff_number / (60*60*24*7);
            String temp_text = " WEEK";
            if(temp_diff > 1) {
                temp_text += "S";
            }
            diff_text = String.valueOf(temp_diff) + temp_text;
        }

        return diff_text + " AGO";
    }

    private boolean isBetween(long a, long lower, long upper) {
        return a <= upper && a >= lower;
    }
}
