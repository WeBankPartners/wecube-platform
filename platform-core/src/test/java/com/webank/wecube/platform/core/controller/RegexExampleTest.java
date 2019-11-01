package com.webank.wecube.platform.core.controller;

import java.util.regex.Pattern;

public class RegexExampleTest {
    public static void main(String args[]) {
//        System.out.println("by character classes and quantifiers ...");
//        System.out.println(Pattern.matches("[789]{1}[0-9]{9}", "9953038949"));//true
//        System.out.println(Pattern.matches("[789][0-9]{9}", "9953038949"));//true
//
//        System.out.println(Pattern.matches("[789][0-9]{9}", "9953038949"));//false (11 characters)
//        System.out.println(Pattern.matches("[789][0-9]{9}", "6953038949"));//false (starts from 6)
//        System.out.println(Pattern.matches("[789][0-9]{9}", "8853038949"));//true
//
//        System.out.println("by metacharacters ...");
//        System.out.println(Pattern.matches("[789]{1}\\d{9}", "8853038949"));//true
//        System.out.println(Pattern.matches("[789]{1}\\d{9}", "3853038949"));//false (starts from 3)

        System.out.println(Pattern.matches("[a-z0-9][a-z0-9-]{1,61}[a-z0-9]", "service-management"));
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9-]{1,61}[a-z0-9]", "Service-management"));
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9-]{1,61}[a-z0-9]", "service_management"));
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9-]{1,61}[a-z0-9]", "service.management"));
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz012345678901") + "64");
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz01234567890"));
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]", "xyz"));
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9-]{1,61}[a-z0-9]", "a0"));
        System.out.println(Pattern.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]", "service.management"));
    }
}
