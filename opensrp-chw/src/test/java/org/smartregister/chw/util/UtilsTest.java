package org.smartregister.chw.util;

import android.app.Activity;
import android.os.Environment;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.model.ReferralTypeModel;

import java.util.Arrays;
import java.util.List;

import static org.smartregister.chw.util.Utils.formatDateForVisual;
import static org.smartregister.chw.util.Utils.getClientName;
import static org.smartregister.chw.util.Utils.getFormattedDateFromTimeStamp;

public class UtilsTest extends BaseUnitTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void firstCharacterUppercase_empty() {
        Assert.assertEquals("", Utils.firstCharacterUppercase(""));
    }

    @Test
    public void firstCharacterUppercase_with_one_character() {
        Assert.assertEquals("A", Utils.firstCharacterUppercase("a"));
    }

    @Test
    public void firstCharacterUppercase_with_two_word() {
        Assert.assertEquals("A b", Utils.firstCharacterUppercase("a b"));
    }

    @Test
    public void testConvertDpToPixel() {
        Assert.assertEquals(20.0, Utils.convertDpToPixel(20f, RuntimeEnvironment.application), 0);
    }

    @Test
    public void testTableColConcatEmpty() {
        Assert.assertEquals("", ChildDBConstants.tableColConcat("", ""));
    }

    @Test
    public void testTableColConcatValidInput() {
        Assert.assertEquals("table.col", ChildDBConstants.tableColConcat("table", "col"));
    }

    @Test
    public void testGetDownloadUrl() {
        String downloadUrl = BuildConfig.guidebooks_url + RuntimeEnvironment.application.getResources().getConfiguration().locale + "/fileName";
        Assert.assertEquals(downloadUrl, DownloadGuideBooksUtils.getDownloadUrl("fileName", RuntimeEnvironment.application));
    }

    @Test
    public void testHasExternalDisk() {
        Boolean canWrite = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        Assert.assertEquals(canWrite, FileUtils.hasExternalDisk());
    }

    @Test
    public void testFormatDateForVisual() {
        String date = "2020-06-23";
        String inputFormat = "yyyy-MM-dd";
        String formattedDate = formatDateForVisual(date, inputFormat);
        Assert.assertEquals(formattedDate, "23 Jun 2020");
    }

    @Test
    public void testGetClientName() {
        String name = getClientName("first_name", "middle_name", "last_name");
        if (ChwApplication.getApplicationFlavor().hasSurname())
            Assert.assertEquals("first_name middle_name last_name", name);
        else
            Assert.assertEquals("first_name middle_name", name);

    }

    @Test
    public void testGetDateTimeFromTimeStamp() {
        Assert.assertEquals("01 Dec 2020", getFormattedDateFromTimeStamp(Long.valueOf("1606780800000"), "dd MMM yyyy"));
        Assert.assertEquals("2020-12-02", getFormattedDateFromTimeStamp(Long.valueOf("1606889233342"), "yyyy-MM-dd"));

    }

    @Test
    public void testGetWFHZScore() {
        double score = org.smartregister.chw.util.Utils.getWFHZScore("Male", "70", "70");
        Assert.assertNotEquals(100.0, score, 0.0);
    }

    @Test
    public void testToCSV() {
        String csv = org.smartregister.chw.util.Utils.toCSV(Arrays.asList("foo", "bar", "baz"));
        Assert.assertEquals("foo, bar, baz ",csv);
    }

    @Test
    public void testGetCommonReferralTypes() {
        Activity activity = Mockito.mock(Activity.class);
        @NotNull List<ReferralTypeModel> referralTypeModels = org.smartregister.chw.util.Utils.getCommonReferralTypes(activity);
        Assert.assertEquals(4, referralTypeModels.size());
    }
}
