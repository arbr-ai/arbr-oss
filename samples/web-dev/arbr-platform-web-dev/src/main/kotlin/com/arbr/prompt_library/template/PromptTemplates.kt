package com.arbr.prompt_library.template

import java.io.File

object PromptTemplates {

    // TODO: Missing?
    val iterativeCodeEditDiffInstructions =
        File("src/main/resources/prompts/iterative_code_edit_diff_instr.md")
            .readText()
            .trim()

}
