package com.arbr.graphql.lang.common

import graphql.language.OperationDefinition

enum class GraphQlOperationValue {
    QUERY, MUTATION, SUBSCRIPTION;

    companion object {
        fun fromLangValue(
            operation: OperationDefinition.Operation
        ): GraphQlOperationValue {
            return when (operation) {
                OperationDefinition.Operation.QUERY -> QUERY
                OperationDefinition.Operation.MUTATION -> MUTATION
                OperationDefinition.Operation.SUBSCRIPTION -> SUBSCRIPTION
            }
        }

    }
}
