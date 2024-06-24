package com.arbr.core_web_dev.workflow.input.base

interface ProjectInfoBearer {
    val projectFullName: String
    val baseBranch: String?
}
