/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Metamodel;

/**
 * A base interface for builders that support basic query functionality.
 * This interface is shared between normal query builders and subquery builders.
 *
 * @param <T> The query result type
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0
 */
public interface CommonQueryBuilder<T, X extends CommonQueryBuilder<T, X>> {

    /**
     * Returns the result type of this query.
     *
     * @return The result type of this query
     */
    public Class<T> getResultType();

    /**
     * Returns the query string for the built query.
     *
     * @return The query string
     */
    public String getQueryString();

    /**
     * Returns the JPA {@link Metamodel} of the persistence unit which is used by this query builder.
     *
     * @return The JPA metamodel
     */
    public Metamodel getMetamodel();

    /**
     * The criteria builder factory that created this or it's parent builder.
     * 
     * @return The criteria builder factory
     * @since 1.0.5
     */
    public CriteriaBuilderFactory getCriteriaBuilderFactory();

    /**
     * Sets the given value as the value for the parameter with the given name.
     *
     * @param name The name of the parameter which should be set
     * @param value The value of the parameter that should be set
     * @return The query builder for chaining calls
     */
    public X setParameter(String name, Object value);

    /**
     * Sets the given {@link Calendar} value as the value for the parameter with the given name.
     *
     * @param name The name of the parameter which should be set
     * @param value The value of the parameter that should be set
     * @param temporalType The temporal type of the value
     * @return The query builder for chaining calls
     */
    public X setParameter(String name, Calendar value, TemporalType temporalType);

    /**
     * Sets the given {@link Date} value as the value for the parameter with the given name.
     *
     * @param name The name of the parameter which should be set
     * @param value The value of the parameter that should be set
     * @param temporalType The temporal type of the value
     * @return The query builder for chaining calls
     */
    public X setParameter(String name, Date value, TemporalType temporalType);

    /**
     * Returns true if a parameter with the given name is registered, otherwise false.
     *
     * @param name The name of the parameter that should be checked
     * @return True if the parameter is registered, otherwise false
     */
    public boolean containsParameter(String name);

    /**
     * Returns true if a parameter with the given name is registered and a value has been set, otherwise false.
     *
     * @param name The name of the parameter that should be checked
     * @return True if the parameter is registered and a value has been set, otherwise false
     */
    public boolean isParameterSet(String name);

    /**
     * Returns the parameter object representing the parameter with the given name if
     * {@link QueryBuilder#containsParameter(java.lang.String) } returns true, otherwise null.
     *
     * @param name The name of the parameter that should be returned
     * @return The parameter object if the parameter is registered, otherwise null
     */
    public Parameter<?> getParameter(String name);

    /**
     * Returns a set of all registered parameters.
     *
     * @return The set of registered parameters
     */
    public Set<? extends Parameter<?>> getParameters();

    /**
     * Returns the set value for the parameter with the given name. If no value has been set, or the parameter does not exist, null is
     * returned.
     *
     * @param name The name of the parameter for which the value should be returned
     * @return The value of the parameter or null if no value has been set or the parameter does not exist
     */
    public Object getParameterValue(String name);

    /**
     * Uses the keyset which the keyset builder constructed to filter out rows that come after the keyset.
     * Based on the order by expressions, the keyset builder should receive reference values for every used expression.
     * The constructed keyset will be filtered out so this is like a "lower than" filter.
     * 
     * @return The keyset builder for specifing the keyset
     */
    public KeysetBuilder<X> beforeKeyset();

    /**
     * Like {@link QueryBuilder#beforeKeyset()} but maps the reference values by position instead of by expression.
     * The order of the reference values has to match the order of the order by expressions.
     * 
     * @param values The reference values
     * @return The query builder for chaining calls
     */
    public X beforeKeyset(Serializable... values);

    /**
     * Like {@link QueryBuilder#beforeKeyset(java.io.Serializable...)} but uses the given keyset as reference values.
     * The order of the tuple values has to match the order of the order by expressions.
     * 
     * @param keyset The reference keyset
     * @return The query builder for chaining calls
     */
    public X beforeKeyset(Keyset keyset);

    /**
     * Uses the keyset which the keyset builder constructed to filter out rows that come before the keyset.
     * Based on the order by expressions, the keyset builder should receive reference values for every used expression.
     * The constructed keyset will be filtered out so this is like a "greater than" filter.
     * 
     * @return The keyset builder for specifing the keyset
     */
    public KeysetBuilder<X> afterKeyset();

    /**
     * Like {@link QueryBuilder#afterKeyset()} but maps the reference values by position instead of by expression.
     * The order of the reference values has to match the order of the order by expressions.
     * 
     * @param values The reference values
     * @return The query builder for chaining calls
     */
    public X afterKeyset(Serializable... values);

    /**
     * Like {@link QueryBuilder#afterKeyset(java.io.Serializable...)} but uses the given keyset as reference values.
     * The order of the tuple values has to match the order of the order by expressions.
     * 
     * @param keyset The reference keyset
     * @return The query builder for chaining calls
     */
    public X afterKeyset(Keyset keyset);
}