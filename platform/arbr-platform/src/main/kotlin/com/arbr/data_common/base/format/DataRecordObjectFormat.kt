package com.arbr.data_common.base.format

import com.arbr.data_common.spec.model.DataRecordFormat

sealed interface DataRecordObjectFormat {
    val dataRecordFormat: DataRecordFormat

    data object FileJson: DataRecordObjectFormat {
        override val dataRecordFormat: DataRecordFormat = DataRecordFormat.FILE_JSON
    }
    data object FileCsv: DataRecordObjectFormat {
        override val dataRecordFormat: DataRecordFormat = DataRecordFormat.FILE_CSV
    }
    data object FileYaml: DataRecordObjectFormat {
        override val dataRecordFormat: DataRecordFormat = DataRecordFormat.FILE_YAML
    }
    data object FileBinary: DataRecordObjectFormat {
        override val dataRecordFormat: DataRecordFormat = DataRecordFormat.FILE_BINARY
    }
    data object FilePlaintext: DataRecordObjectFormat {
        override val dataRecordFormat: DataRecordFormat = DataRecordFormat.FILE_PLAINTEXT
    }
    data object FileByteBuffer: DataRecordObjectFormat {
        override val dataRecordFormat: DataRecordFormat = DataRecordFormat.FILE_BYTE_BUFFER
    }

    data object DatabaseRecord: DataRecordObjectFormat {
        override val dataRecordFormat: DataRecordFormat = DataRecordFormat.DATABASE_RECORD
    }
}


