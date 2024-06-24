package com.arbr.object_model.core.types

import com.arbr.object_model.core.types.naming.NamedProperty

interface ResourceFieldValue<
        R : GeneralResource,
        RF: NamedProperty<*, *, *, *, R>,
        >
