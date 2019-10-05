package com.ruoyi.lijun;

import com.ruoyi.lijun.utils.FSS;

public class Test {
    @org.junit.Test
    public void test1(){
        System.err.println("https://127.0.0.1:9999/app/aaaaa".replace("127.0.0.1:9999", FSS.DOMAIN_NAME));
    }
}
