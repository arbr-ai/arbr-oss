package com.arbr.types.homotopy.util

import com.arbr.types.homotopy.functional.Ingestor
import com.arbr.types.homotopy.functional.Producer
import com.arbr.types.homotopy.functional.Reducer

class StringReducer<T> private constructor(
    producer: Producer<String>,
    preOrderVisitor: Ingestor<T> = Ingestor { },
    postOrderVisitor: Ingestor<T> = Ingestor { },
) : Reducer<T, String>(
    producer, preOrderVisitor, postOrderVisitor
) {
    companion object {
        class StringBox(
            var linePrefix: String = ""
        )

        fun <T> with(
            preOrderAppender: Appendable.(T, StringBox) -> Unit,
            postOrderAppender: Appendable.(T, StringBox) -> Unit,
        ): StringReducer<T> {
            val stringBuilder = StringBuilder()
            val stringBox = StringBox()
            return StringReducer(
                { stringBuilder.toString() },
                {
                    preOrderAppender(stringBuilder, it, stringBox)
                },
                {
                    postOrderAppender(stringBuilder, it, stringBox)
                },
            )
        }
    }
}
