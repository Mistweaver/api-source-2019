package com.api.demo.security.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class IpAddressUtility {
    private static Logger logger = LoggerFactory.getLogger(IpAddressUtility.class);

    public static String getIpAddressOfRequest(final HttpServletRequest request) {
        String remoteAddr = "";
        try {
            if (request != null) {
                remoteAddr = request.getHeader("X-FORWARDED-FOR");
                if (remoteAddr == null || "".equals(remoteAddr)) {
                    remoteAddr = request.getRemoteAddr();

                }
            }
        } catch (Exception e) {
            // logger.error("Failed to read incoming IP Address" + e.getMessage());
            //e.printStackTrace();
        }
        return remoteAddr;
    }
}
