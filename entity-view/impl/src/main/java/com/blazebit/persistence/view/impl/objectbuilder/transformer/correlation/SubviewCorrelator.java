/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.metamodel.MappingConstructorImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class SubviewCorrelator implements Correlator {

    private final ManagedViewTypeImplementor<?> managedViewType;
    private final MappingConstructorImpl<?> mappingConstructor;
    private final EntityViewManagerImpl evm;
    private final String viewName;

    public SubviewCorrelator(ManagedViewTypeImplementor<?> managedViewType, MappingConstructorImpl<?> mappingConstructor, EntityViewManagerImpl evm, String viewName) {
        this.managedViewType = managedViewType;
        this.mappingConstructor = mappingConstructor;
        this.evm = evm;
        this.viewName = viewName;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void finish(FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration entityViewConfiguration, int tupleOffset, String correlationRoot) {
        // TODO: actually we need a new entity view configuration for this
        ObjectBuilder builder = evm.createObjectBuilder(managedViewType, mappingConstructor, viewName, correlationRoot, criteriaBuilder, entityViewConfiguration, tupleOffset, false);
        criteriaBuilder.selectNew(builder);
    }
}
