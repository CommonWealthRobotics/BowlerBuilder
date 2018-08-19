package com.neuronrobotics.bowlerbuilder

/**
 * Signifies that the receiver method is part of a public/published API and should not be modified
 * or deleted to avoid breaking the API.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class PublicAPI
