package com.montunosoftware.pillpopper.android.util;

import org.junit.Assert;
import org.junit.Test;

import com.montunosoftware.pillpopper.android.util.UIUtils;

/**
 * Created by M1032896 on 5/2/2018.
 */

public class UIUtilsTest {

    @Test
    public void shouldNotAllowWebLink(){
        Assert.assertSame(false, UIUtils.isValidInput("http:// note"));
    }

    @Test
    public void shouldNotAllowInvalidCharacters(){
        Assert.assertSame(false,UIUtils.isValidInput("'-&$% note"));
    }


    @Test
    public void shouldNotAllowHtmlTags(){
        Assert.assertSame(false,UIUtils.isValidInput("<> [] note"));
    }


    @Test
    public void shouldAllowNormalTest(){
        Assert.assertSame(true,UIUtils.isValidInput("Hello world!!"));
    }
}
