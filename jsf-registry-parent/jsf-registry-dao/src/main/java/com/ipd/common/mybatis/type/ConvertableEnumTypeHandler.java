/**
 * Copyright 2004-2048 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.common.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * 负责处理枚举类型和jdbc类型的转换<br>
 * 
 * LIMIT：对应的jdbc类型为byte/short/int
 * 
 * @param <S>
 *            具体的枚举类型
 * @param <T>
 *            具体的jdbc类型：byte/short/int
 */
public class ConvertableEnumTypeHandler<S, T> extends BaseTypeHandler<S> {
    private final ConvertableContext<S, T> convertableContext;
    private final Delegator<T> delegator;

    @SuppressWarnings("unchecked")
    public ConvertableEnumTypeHandler(Class<S> convertableClass) {
        super();
        this.convertableContext = ConvertableContext.build(convertableClass);

        Class<T> valueType = convertableContext.getValueType();
        Delegator<?> delegator;
        if (valueType == Byte.class || valueType == byte.class) {
            delegator = new ByteDelegator();
        } else if (valueType == Short.class || valueType == short.class) {
            delegator = new ShortDelegator();
        } else if (valueType == Integer.class || valueType == int.class) {
            delegator = new IntegerDelegator();
        } else {
            throw new RuntimeException(
                    "Convertable enum's value type should be byte/short/int.");
        }
        this.delegator = (Delegator<T>) delegator;

    }

    final public void setNonNullParameter(PreparedStatement ps, int i,
            S parameter, JdbcType jdbcType) throws SQLException {
        T v;
        try {
            v = convertableContext.value(parameter);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }
        if (v == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            delegator.setValue(ps, i, v);
        }
    }

    final public S getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        T v = delegator.getValue(rs, columnName);
        if (rs.wasNull()) {
            return null;
        }
        try {
            return convertableContext.of(v);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    final public S getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        T v = delegator.getValue(rs, columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        try {
            return convertableContext.of(v);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    final public S getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        T v = delegator.getValue(cs, columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        try {
            return convertableContext.of(v);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    static abstract class Delegator<T> {
        protected abstract void setValue(PreparedStatement ps, int i, T value)
                throws SQLException;

        protected abstract T getValue(ResultSet rs, String columnName)
                throws SQLException;

        protected abstract T getValue(ResultSet rs, int columnIndex)
                throws SQLException;

        protected abstract T getValue(CallableStatement cs, int columnIndex)
                throws SQLException;
    }

    static class ByteDelegator extends Delegator<Byte> {
        @Override
        protected void setValue(PreparedStatement ps, int i, Byte value)
                throws SQLException {
            ps.setByte(i, value);
        }

        @Override
        protected Byte getValue(ResultSet rs, String columnName)
                throws SQLException {
            return rs.getByte(columnName);
        }

        @Override
        protected Byte getValue(CallableStatement cs, int columnIndex)
                throws SQLException {
            return cs.getByte(columnIndex);
        }

        @Override
        protected Byte getValue(ResultSet rs, int columnIndex)
                throws SQLException {
            return rs.getByte(columnIndex);
        }
    }

    static class IntegerDelegator extends Delegator<Integer> {
        @Override
        protected void setValue(PreparedStatement ps, int i, Integer value)
                throws SQLException {
            ps.setInt(i, value);
        }

        @Override
        protected Integer getValue(ResultSet rs, String columnName)
                throws SQLException {
            return rs.getInt(columnName);
        }

        @Override
        protected Integer getValue(CallableStatement cs, int columnIndex)
                throws SQLException {
            return cs.getInt(columnIndex);
        }

        @Override
        protected Integer getValue(ResultSet rs, int columnIndex)
                throws SQLException {
            return rs.getInt(columnIndex);
        }
    }

    static class ShortDelegator extends Delegator<Short> {
        @Override
        protected void setValue(PreparedStatement ps, int i, Short value)
                throws SQLException {
            ps.setShort(i, value);
        }

        @Override
        protected Short getValue(ResultSet rs, String columnName)
                throws SQLException {
            return rs.getShort(columnName);
        }

        @Override
        protected Short getValue(CallableStatement cs, int columnIndex)
                throws SQLException {
            return cs.getShort(columnIndex);
        }

        @Override
        protected Short getValue(ResultSet rs, int columnIndex)
                throws SQLException {
            return rs.getShort(columnIndex);
        }
    }
}
