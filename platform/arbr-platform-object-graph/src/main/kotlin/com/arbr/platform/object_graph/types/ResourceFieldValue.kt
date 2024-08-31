package com.arbr.platform.object_graph.types

import com.arbr.platform.object_graph.types.naming.NamedProperty

interface ResourceFieldValue<
        R : GeneralResource,
        RF: NamedProperty<*, *, *, *, R>,
        >
