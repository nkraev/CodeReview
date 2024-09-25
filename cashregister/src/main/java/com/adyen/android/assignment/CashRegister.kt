package com.adyen.android.assignment

import com.adyen.android.assignment.money.Change

/**
 * The CashRegister class holds the logic for performing transactions.
 *
 * @param change The change that the CashRegister is holding.
 */
class CashRegister(private val change: Change) {
    /**
     * Performs a transaction for a product/products with a certain price and a given amount.
     *
     * @param price The price of the product(s).
     * @param amountPaid The amount paid by the shopper.
     *
     * @return The change for the transaction.
     *
     * @throws TransactionException If the transaction cannot be performed.
     */
    fun performTransaction(price: Long, amountPaid: Change): Change {

        // Well, reliable solution is quite slow for this task, time complexity is O(price-amountPaid)*change.elemnts.size
        // which means in real project I would think of as much as possible
        // probable cases and check them before going to my current solution.
        // Cases like try to give change with one monetaryElement or just go from bigger to smaller monetaryElement

        val totalPaid = amountPaid.total
        val changeDue = totalPaid - price

        if (changeDue < 0) {
            throw TransactionException("Insufficient funds provided by the customer")
        }

        if (changeDue == 0L) {
            updateRegister(amountPaid, Change.none())
            return Change.none()
        }

        val maxAmount = changeDue.toInt()
        val bestShots = IntArray(maxAmount + 1) { Int.MAX_VALUE } // dp[i] is the minimum number of coins needed to make amount i
        bestShots[0] = 0 // No coins are needed to make 0 amount

        // Iterate over amounts from 1 to changeDue
        for (amount in 1..maxAmount) {
            // For each amount, try every monetaryElement
            for (monetaryElement in change.getElements()) {
                val minorValue = monetaryElement.minorValue
                if (amount >= minorValue) {
                    val previousAmount = amount - minorValue
                    if (bestShots[previousAmount] != Int.MAX_VALUE) {
                        bestShots[amount] = minOf(bestShots[amount], bestShots[previousAmount] + 1)
                    }
                }
            }
        }

        if (bestShots[maxAmount] == Int.MAX_VALUE) {
            throw TransactionException("Cannot provide exact change with the monetaryElement available.")
        }

        // Backtrack
        val changeToGive = Change.none()
        var amount = maxAmount
        while (amount > 0) {
            for (monetaryElement in change.getElements()) {
                val minorValue = monetaryElement.minorValue
                if (amount >= minorValue && bestShots[amount] == bestShots[amount - minorValue] + 1) {
                    changeToGive.add(monetaryElement, 1)
                    amount -= minorValue
                    break
                }
            }
        }

        updateRegister(amountPaid, changeToGive)
        return changeToGive
    }

    private fun updateRegister(amountPaid: Change, changeGiven: Change) {
        amountPaid.getElements().forEach { element ->
            change.add(element, amountPaid.getCount(element))
        }
        changeGiven.getElements().forEach { element ->
            change.remove(element, changeGiven.getCount(element))
        }
    }

    class TransactionException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
