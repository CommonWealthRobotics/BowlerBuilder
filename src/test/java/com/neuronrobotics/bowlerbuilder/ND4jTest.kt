package com.neuronrobotics.bowlerbuilder

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.nd4j.linalg.factory.Nd4j

class ND4jTest {

    @Test
    @Tag("ExcludedTest")
    fun basicTest() {
        val tens = Nd4j.zeros(3, 5).addi(10)
        println(tens)
    }

}
