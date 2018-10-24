/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.util

/**
 * Adapted from Arrow-kt's Either. This is reduced in scope and adds [handle].
 */
sealed class Verified<out E, out S> {

    internal abstract val isError: Boolean
    internal abstract val isSuccess: Boolean

    fun isError(): Boolean = isError
    fun isSuccess(): Boolean = isSuccess

    inline fun <C> fold(
        crossinline mapSuccess: (S) -> C,
        crossinline mapError: (E) -> C
    ): C =
            when (this) {
                is Success<E, S> -> mapSuccess(successVal)
                is Error<E, S> -> mapError(errorVal)
            }

    inline fun handle(
        crossinline handleSuccess: (S) -> Unit,
        crossinline handleError: (E) -> Unit
    ): Unit =
            when (this) {
                is Success<E, S> -> handleSuccess(successVal)
                is Error<E, S> -> handleError(errorVal)
            }

    fun getSuccess(): S? =
            when (this) {
                is Success<E, S> -> successVal
                else -> null
            }

    fun getError(): E? =
            when (this) {
                is Error<E, S> -> errorVal
                else -> null
            }

    data class Error<out E, out S> @PublishedApi internal constructor(val errorVal: E) : Verified<E, S>() {
        override val isError: Boolean
            get() = true
        override val isSuccess: Boolean
            get() = false
    }

    data class Success<out E, out S> @PublishedApi internal constructor(val successVal: S) : Verified<E, S>() {
        override val isError: Boolean
            get() = false
        override val isSuccess: Boolean
            get() = true
    }

    companion object {
        fun <E> error(error: E): Verified<E, Nothing> = Error(error)
        fun <S> success(success: S): Verified<Nothing, S> = Success(success)
    }
}
