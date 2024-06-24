package com.arbr.api.github.core

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/*
{
  "sha": "d499f29f317ccfeb56f8cd5477396647866500c4",
  "node_id": "MDY6Q29tbWl0MTAwMDA4Mzc6ZDQ5OWYyOWYzMTdjY2ZlYjU2ZjhjZDU0NzczOTY2NDc4NjY1MDBjNA==",
  "commit": {
    "author": {
      "name": "Isaac",
      "email": "muse_i@MuseI-Opt990.wdl.wdc.com",
      "date": "2013-11-01T22:54:45Z"
    },
    "committer": {
      "name": "Isaac",
      "email": "muse_i@MuseI-Opt990.wdl.wdc.com",
      "date": "2013-11-01T22:54:45Z"
    },
    "message": "-Fix angled brackets which technically break the XML standard",
    "tree": {
      "sha": "927a2dec1010763bddd854e87e1ba81adec7663d",
      "url": "https://api.github.com/repos/MattDMo/PythonImproved/git/trees/927a2dec1010763bddd854e87e1ba81adec7663d"
    },
    "url": "https://api.github.com/repos/MattDMo/PythonImproved/git/commits/d499f29f317ccfeb56f8cd5477396647866500c4",
    "comment_count": 0,
    "verification": {
      "verified": false,
      "reason": "unsigned",
      "signature": null,
      "payload": null
    }
  },
  "url": "https://api.github.com/repos/MattDMo/PythonImproved/commits/d499f29f317ccfeb56f8cd5477396647866500c4",
  "html_url": "https://github.com/MattDMo/PythonImproved/commit/d499f29f317ccfeb56f8cd5477396647866500c4",
  "comments_url": "https://api.github.com/repos/MattDMo/PythonImproved/commits/d499f29f317ccfeb56f8cd5477396647866500c4/comments",
  "author": null,
  "committer": null,
  "parents": [
    {
      "sha": "0907f3d7ae0ebdf360029c78841edcc9591d5331",
      "url": "https://api.github.com/repos/MattDMo/PythonImproved/commits/0907f3d7ae0ebdf360029c78841edcc9591d5331",
      "html_url": "https://github.com/MattDMo/PythonImproved/commit/0907f3d7ae0ebdf360029c78841edcc9591d5331"
    }
  ],
  "stats": {
    "total": 2,
    "additions": 1,
    "deletions": 1
  },
  "files": [
    {
      "sha": "0e1e60cd760ab5bbcbdbd041f558e0e748fe55b3",
      "filename": "PythonImproved.tmLanguage",
      "status": "modified",
      "additions": 1,
      "deletions": 1,
      "changes": 2,
      "blob_url": "https://github.com/MattDMo/PythonImproved/blob/d499f29f317ccfeb56f8cd5477396647866500c4/PythonImproved.tmLanguage",
      "raw_url": "https://github.com/MattDMo/PythonImproved/raw/d499f29f317ccfeb56f8cd5477396647866500c4/PythonImproved.tmLanguage",
      "contents_url": "https://api.github.com/repos/MattDMo/PythonImproved/contents/PythonImproved.tmLanguage?ref=d499f29f317ccfeb56f8cd5477396647866500c4",
      "patch": "@@ -16,7 +16,7 @@\n             To make this your main Python language definition (in Sublime Text 2):\n                 1) Rename Packages/Python/Python.tmLanguage to something else with a different extension (like Python.tmLanguage.backup)\n                 2) Move this file and its README.md to Packages/Python/\n-                3) Search for \"<string>PythonImproved</string>\" and change it to just say Python (if you want) - this is what is displayed in ST2's various menus\n+                3) Search for \"&lt;string&gt;PythonImproved&lt;/string&gt;\" and change it to just say Python (if you want) - this is what is displayed in ST2's various menus\n                 4) Rename this file to Python.tmLanguage (if you want)\n \n     </string>"
    }
  ]
}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubCommitInfo(
    val sha: String,
    val url: String?, // 2024-03-18 Optional for backwards compatibility
    val htmlUrl: String?, // 2024-03-18 Optional for backwards compatibility
    val commit: GitHubCommitInfoInnerCommit,
    val parents: List<GitHubCommitParentInfo>?, // 2024-03-18 Optional for backwards compatibility
    val stats: GitHubCommitInfoStats,
    val files: List<GitHubCommitInfoFile>,
)
