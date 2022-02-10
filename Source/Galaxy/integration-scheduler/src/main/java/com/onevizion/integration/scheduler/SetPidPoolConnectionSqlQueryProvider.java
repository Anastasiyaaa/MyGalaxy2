/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.zaxxer.hikari.OnBorrowConnectionSqlQueryProvider;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class SetPidPoolConnectionSqlQueryProvider implements OnBorrowConnectionSqlQueryProvider{
    private static final String CONFIG_SESSION_PID_ONLY = "begin pkg_sec.config_session(null,{pid:-null},null,null,0,null); pkg_sec.set_java(0); end;";

    @Autowired
    private ConnectionProperties connectionProperties;

    @Override
    public String getSqlQuery() {
        Map<String, String> params = new HashMap<>();

        if (connectionProperties.getProgramId() != null) {
            params.put("pid", connectionProperties.getProgramId().toString());
        }

        StringSubstitutor stringSubstitutor = new StringSubstitutor(params,"{","}");
        return stringSubstitutor.replace(CONFIG_SESSION_PID_ONLY);
    }
}
