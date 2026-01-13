package com.saha.androidfm.di

import com.saha.androidfm.data.network.MyApi
import com.saha.androidfm.data.repo.Repo
import com.saha.androidfm.data.repo.RepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepoModule {

    @Provides
    fun provideMyApiRepo(
        myApi: MyApi
    ): Repo {
        return RepoImpl(myApi)
    }
}