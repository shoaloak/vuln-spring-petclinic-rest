package org.springframework.samples.petclinic.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringRowMapper implements RowMapper<String> {

    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            String columnValue = rs.getString(i);

            stringBuilder.append(columnName).append(":").append(columnValue);

            if (i < rs.getMetaData().getColumnCount()) {
                stringBuilder.append("; ");
            }
        }
        return stringBuilder.toString();
    }
}
