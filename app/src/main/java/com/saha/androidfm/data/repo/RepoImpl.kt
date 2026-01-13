package com.saha.androidfm.data.repo

import com.saha.androidfm.data.network.MyApi
import javax.inject.Inject

class RepoImpl @Inject constructor(private val api: MyApi) : Repo