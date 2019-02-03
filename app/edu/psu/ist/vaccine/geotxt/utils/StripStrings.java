/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.psu.ist.vaccine.geotxt.utils;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Anuj Jaiswal
 */
public class StripStrings {

    public static String strip(String input){
        String delims = ".-:,\\\"\'=? \n";
        return StringUtils.strip(input, delims);
    }

    public static void main(String args[]){
        System.out.println(StripStrings.strip("_http://bit.ly/4myVgG/..."));
    }
}
