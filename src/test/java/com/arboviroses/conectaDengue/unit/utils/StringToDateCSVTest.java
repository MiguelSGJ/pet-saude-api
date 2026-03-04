package com.arboviroses.conectaDengue.unit.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.arboviroses.conectaDengue.Utils.StringToDateCSV;

public class StringToDateCSVTest {
    @Test
    public void test_if_convert_string_to_date_returns_correct_date() throws ParseException
    {   
        String dateStringExpected1 = "28/06/2017";
        String dateStringExpected2 = "01/06/2017";
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
        Date dateExpected1 = simpleDate.parse(dateStringExpected1);
        Date dateExpected2 = simpleDate.parse(dateStringExpected2);
        
        Date formatedDate1 = StringToDateCSV.ConvertStringToDate("06/28/2017");
        Date formatedDate2 = StringToDateCSV.ConvertStringToDate("6/28/2017");
        Date formatedDate3 = StringToDateCSV.ConvertStringToDate("06/01/2017");
        Date formatedDate4 = StringToDateCSV.ConvertStringToDate("06/1/2017");
        Date formatedDate5 = StringToDateCSV.ConvertStringToDate("6/01/2017");

        Assertions.assertEquals(dateExpected1, formatedDate1);
        Assertions.assertEquals(dateExpected1, formatedDate2);
        Assertions.assertEquals(dateExpected2, formatedDate3);
        Assertions.assertEquals(dateExpected2, formatedDate4);
        Assertions.assertEquals(dateExpected2, formatedDate5);
    }
}
