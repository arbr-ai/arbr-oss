package com.arbr.core_web_dev.functions

import com.arbr.platform.object_graph.common.ObjectModel

//inline fun <V, reified U : ObjectModel.ObjectValue<V, *, *, U>> typeOf(): ObjectModel.ObjectType<V, *, *, U> {
//    TODO()
//}

fun <V: W, W, U : ObjectModel.ObjectValue<W, *, *, U>> ObjectModel.ObjectValue<V, *, *, *>.into(
    targetType: ObjectModel.ObjectType<W, *, *, U>,
): U {
    return this.map(targetType) {
        it
    }
}

///**
// * Cast a value into a value of the same material type.
// */
//inline fun <V : Any, reified U : ObjectModel.ObjectValue<V, *, *, U>> ObjectModel.ObjectValue<V, *, *, *>.into(): U {
//    val targetType: ObjectModel.ObjectType<V, *, *, U> = typeOf<V, U>()
//    return into(targetType)
//}
//
///**
// * Cast a value into a value with the nullable version of the same material type.
// */
//@JvmName("toNullable")
//inline fun <V, reified U : ObjectModel.ObjectValue<V?, *, *, U>> ObjectModel.ObjectValue<V, *, *, *>.into(): U {
//    val targetType = typeOf<V?, U>()
//    return targetType.initialize(
//        kind,
//        value,
//        generatorInfo,
//    )
//}
