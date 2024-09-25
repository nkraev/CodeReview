package com.adyen.android.assignment

import com.adyen.android.assignment.money.Bill
import com.adyen.android.assignment.money.Change
import com.adyen.android.assignment.money.Coin
import org.junit.Test
import org.junit.Assert.assertEquals

class CashRegisterTest {

    @Test
    fun noChangeTransaction() {
        val registerChange = Change().apply {
            add(Bill.FIVE_EURO, 2)
            add(Coin.TWO_EURO, 5)
            add(Coin.ONE_EURO, 10)
        }

        val cashRegister = CashRegister(registerChange)

        val amountPaid = Change().apply {
            add(Bill.FIVE_EURO, 1)
        }

        val result = cashRegister.performTransaction(500L, amountPaid)
        val expectedChange = Change.none() // No change expected

        assertEquals(expectedChange, result)
    }

    @Test(expected = CashRegister.TransactionException::class)
    fun insufficientFunds() {
        val registerChange = Change().apply {
            add(Bill.FIVE_EURO, 1)
            add(Coin.TWO_EURO, 5)
        }

        val cashRegister = CashRegister(registerChange)

        val amountPaid = Change().apply {
            add(Coin.TWO_EURO, 1) // Paying only 2 EUR
        }

        // Price is 5 EUR, so this should throw an exception due to insufficient funds
        cashRegister.performTransaction(500L, amountPaid)
    }

    @Test(expected = CashRegister.TransactionException::class)
    fun cannotProvideExactChange() {
        val registerChange = Change().apply {
            add(Bill.TWENTY_EURO, 1)
            add(Coin.TWO_EURO, 2) // Only 2x 2 EUR coins available
        }

        val cashRegister = CashRegister(registerChange)

        val amountPaid = Change().apply {
            add(Bill.TWENTY_EURO, 1) // Paying 20 EUR
        }

        // Price is 15 EUR, and we don't have enough 1 EUR coins to make 5 EUR change, so this should throw an exception
        cashRegister.performTransaction(1500L, amountPaid)
    }

    @Test
    fun provideChangeWithLimitedCoins() {
        val registerChange = Change().apply {
            add(Bill.TWENTY_EURO, 1)
            add(Coin.TWO_EURO, 1) // Only 1x 2 EUR coin
            add(Coin.ONE_EURO, 1) // Only 1x 1 EUR coin
        }

        val cashRegister = CashRegister(registerChange)

        val amountPaid = Change().apply {
            add(Bill.TEN_EURO, 1) // Paying 10 EUR
        }

        // Price is 7 EUR, so expecting 2 EUR + 1 EUR as change (total: 3 EUR)
        val result = cashRegister.performTransaction(700L, amountPaid)

        val expectedChange = Change().apply {
            add(Coin.TWO_EURO, 1)
            add(Coin.ONE_EURO, 1)
        }

        assertEquals(expectedChange, result)
    }

    // fun part ---
    @Test
    fun combinedChange() {
        val registerChange = Change().apply {
            add(Bill.TWENTY_EURO, 1)
            add(Coin.TWO_EURO, 5)
            add(Coin.ONE_EURO, 10)
            add(Coin.FIFTY_CENT, 10)
            add(Coin.TEN_CENT, 20)
        }

        val cashRegister = CashRegister(registerChange)

        val amountPaid = Change().apply {
            add(Bill.TEN_EURO, 1) // Paying 10 EUR
        }

        // Price is 7.30 EUR, expecting 2 EUR, 50 Cents, 10 Cents, and 10 Cents as change (total: 2.70 EUR)
        val result = cashRegister.performTransaction(730L, amountPaid)

        val expectedChange = Change().apply {
            add(Coin.TWO_EURO, 1)
            add(Coin.FIFTY_CENT, 1)
            add(Coin.TEN_CENT, 2)
        }

        assertEquals(expectedChange, result)
    }
}

