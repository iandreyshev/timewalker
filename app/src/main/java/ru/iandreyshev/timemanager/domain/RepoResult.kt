package ru.iandreyshev.timemanager.domain

sealed class RepoResult<out T : Any> {
    class Success<out T : Any>(val data: T) : RepoResult<T>()
    class Error(val error: RepoError) : RepoResult<Nothing>()
}
