package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentNumber: String = "0"
    private var currentResult: BigDecimal = BigDecimal.ZERO
    private var pendingOperation: String? = null
    private var isNewNumberStarting: Boolean = true
    private var operationHistory: StringBuilder = StringBuilder()
    private var currentExpression: StringBuilder = StringBuilder()
    private var lastInputWasOperation: Boolean = false
    private var isCalculationComplete: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        binding.currentResult.text = "0"
        binding.pastResult.text = ""
        currentExpression.append("0")
    }

    private fun setupClickListeners() {
        binding.zeroButton.setOnClickListener { onNumberClick("0") }
        binding.oneButton.setOnClickListener { onNumberClick("1") }
        binding.twoButton.setOnClickListener { onNumberClick("2") }
        binding.threeButton.setOnClickListener { onNumberClick("3") }
        binding.fourButton.setOnClickListener { onNumberClick("4") }
        binding.fiveButton.setOnClickListener { onNumberClick("5") }
        binding.sixButton.setOnClickListener { onNumberClick("6") }
        binding.sevenButton.setOnClickListener { onNumberClick("7") }
        binding.eightButton.setOnClickListener { onNumberClick("8") }
        binding.nineButton.setOnClickListener { onNumberClick("9") }

        binding.plusButton.setOnClickListener { onOperationClick("+") }
        binding.minusButton.setOnClickListener { onOperationClick("-") }
        binding.multiplyButton.setOnClickListener { onOperationClick("x") }
        binding.divisionButton.setOnClickListener { onOperationClick("/") }
        binding.percentButton.setOnClickListener { onPercentClick() }

        binding.acButton.setOnClickListener { onACClick() }
        binding.arrowButton.setOnClickListener { onBackspaceClick() }
        binding.decimalButton.setOnClickListener { onDecimalClick() }
        binding.equalsButton.setOnClickListener { onEqualsClick() }
        binding.plusMinusButton.setOnClickListener { onPlusMinusClick() }
    }

    private fun onNumberClick(number: String) {
        if (isCalculationComplete) {
            onACClick()
        }

        if (isNewNumberStarting) {
            currentNumber = number
            if (lastInputWasOperation) {
                currentExpression.append(number)
            } else {
                val lastOpIndex = findLastOperationIndex(currentExpression.toString())
                if (lastOpIndex >= 0) {
                    currentExpression.delete(lastOpIndex + 2, currentExpression.length)
                    currentExpression.append(number)
                } else {
                    currentExpression.clear()
                    currentExpression.append(number)
                }
            }
            isNewNumberStarting = false
        } else {
            if (currentNumber == "0" && number != ".") {
                currentNumber = number
                if (currentExpression.isNotEmpty() && currentExpression[currentExpression.length - 1] == '0') {
                    currentExpression.setCharAt(currentExpression.length - 1, number[0])
                }
            } else {
                currentNumber += number
                currentExpression.append(number)
            }
        }

        binding.currentResult.text = currentExpression.toString()
        lastInputWasOperation = false
    }

    private fun findLastOperationIndex(expression: String): Int {
        val operations = listOf(" + ", " - ", " x ", " / ")
        var lastIndex = -1
        for (op in operations) {
            val index = expression.lastIndexOf(op)
            if (index > lastIndex) {
                lastIndex = index + 1
            }
        }
        return lastIndex
    }

    private fun onOperationClick(operation: String) {
        isCalculationComplete = false

        if (pendingOperation != null && !lastInputWasOperation) {
            performPendingOperation()
        } else if (operationHistory.isEmpty() && !lastInputWasOperation) {
            currentResult = BigDecimal(currentNumber)
            operationHistory.append(currentNumber)
        }

        if (lastInputWasOperation && operationHistory.isNotEmpty()) {
            val lastSpaceIndex = operationHistory.lastIndexOf(" ")
            if (lastSpaceIndex >= 0 && lastSpaceIndex < operationHistory.length - 1) {
                operationHistory.replace(lastSpaceIndex + 1, lastSpaceIndex + 2, operation)
            }
            if (currentExpression.length >= 3) {
                currentExpression.delete(currentExpression.length - 3, currentExpression.length)
                currentExpression.append(" $operation ")
            }
        } else {
            operationHistory.append(" $operation ")
            currentExpression.append(" $operation ")
        }

        pendingOperation = operation
        isNewNumberStarting = true
        lastInputWasOperation = true

        binding.pastResult.text = operationHistory.toString()
        binding.currentResult.text = currentExpression.toString()
    }

    private fun performPendingOperation() {
        if (pendingOperation == null) return

        val secondOperand = BigDecimal(currentNumber)

        when (pendingOperation) {
            "+" -> currentResult = currentResult.add(secondOperand)
            "-" -> currentResult = currentResult.subtract(secondOperand)
            "x" -> currentResult = currentResult.multiply(secondOperand)
            "/" -> {
                if (secondOperand == BigDecimal.ZERO) {
                    binding.currentResult.text = "Error"
                    return
                }
                currentResult = currentResult.divide(secondOperand, 10, RoundingMode.HALF_UP)
            }
            "%" -> currentResult = currentResult.multiply(secondOperand).divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
        }

        if (!lastInputWasOperation) {
            operationHistory.append(currentNumber)
        }
    }

    private fun onEqualsClick() {
        if (pendingOperation == null) return

        performPendingOperation()

        binding.pastResult.text = currentExpression.toString()
        binding.currentResult.text = currentResult.stripTrailingZeros().toPlainString()

        pendingOperation = null
        isNewNumberStarting = true
        currentNumber = currentResult.stripTrailingZeros().toPlainString()
        currentExpression.clear()
        currentExpression.append(currentNumber)
        isCalculationComplete = true
        lastInputWasOperation = false
    }

    private fun onACClick() {
        currentNumber = "0"
        currentResult = BigDecimal.ZERO
        pendingOperation = null
        isNewNumberStarting = true
        operationHistory = StringBuilder()
        currentExpression = StringBuilder()
        currentExpression.append("0")
        lastInputWasOperation = false
        isCalculationComplete = false
        binding.currentResult.text = "0"
        binding.pastResult.text = ""
    }

    private fun onBackspaceClick() {
        if (lastInputWasOperation) {
            if (operationHistory.isNotEmpty() && operationHistory.endsWith(" ")) {
                val lastSpaceBeforeOp = operationHistory.lastIndexOf(" ", operationHistory.length - 3)
                if (lastSpaceBeforeOp >= 0) {
                    operationHistory.delete(lastSpaceBeforeOp + 1, operationHistory.length)
                    binding.pastResult.text = operationHistory.toString()

                    if (currentExpression.length >= 3) {
                        currentExpression.delete(currentExpression.length - 3, currentExpression.length)
                    }

                    val lastSpaceInHistory = operationHistory.lastIndexOf(" ")
                    currentNumber = if (lastSpaceInHistory >= 0) {
                        operationHistory.substring(lastSpaceInHistory + 1)
                    } else {
                        operationHistory.toString()
                    }

                    binding.currentResult.text = currentExpression.toString()
                    lastInputWasOperation = false
                    pendingOperation = null
                }
            }
            return
        }

        if (currentNumber.length > 1) {
            currentNumber = currentNumber.substring(0, currentNumber.length - 1)
            if (currentExpression.isNotEmpty()) {
                currentExpression.deleteCharAt(currentExpression.length - 1)
            }
        } else {
            currentNumber = "0"
            if (currentExpression.isEmpty()) {
                currentExpression.append("0")
            } else {
                val lastOpIndex = findLastOperationIndex(currentExpression.toString())
                if (lastOpIndex >= 0) {
                    currentExpression.delete(lastOpIndex + 2, currentExpression.length)
                    currentExpression.append("0")
                } else {
                    currentExpression.clear()
                    currentExpression.append("0")
                }
            }
            isNewNumberStarting = true
        }
        binding.currentResult.text = currentExpression.toString()
    }

    private fun onDecimalClick() {
        if (isCalculationComplete) {
            onACClick()
        }

        if (isNewNumberStarting) {
            currentNumber = "0."
            if (lastInputWasOperation) {
                currentExpression.append("0.")
            } else {
                val lastOpIndex = findLastOperationIndex(currentExpression.toString())
                if (lastOpIndex >= 0) {
                    currentExpression.delete(lastOpIndex + 2, currentExpression.length)
                    currentExpression.append("0.")
                } else {
                    currentExpression.clear()
                    currentExpression.append("0.")
                }
            }
            isNewNumberStarting = false
        } else if (!currentNumber.contains(".")) {
            currentNumber += "."
            currentExpression.append(".")
        }
        binding.currentResult.text = currentExpression.toString()
        lastInputWasOperation = false
    }

    private fun onPlusMinusClick() {
        if (currentNumber == "0") return

        currentNumber = if (currentNumber.startsWith("-")) {
            currentNumber.substring(1)
        } else {
            "-$currentNumber"
        }

        val lastOpIndex = findLastOperationIndex(currentExpression.toString())
        if (lastOpIndex >= 0) {
            currentExpression.delete(lastOpIndex + 1, currentExpression.length)
            currentExpression.append(currentNumber)
        } else {
            currentExpression.clear()
            currentExpression.append(currentNumber)
        }

        binding.currentResult.text = currentExpression.toString()
        lastInputWasOperation = false
    }

    private fun onPercentClick() {
        if (currentNumber == "0") return

        val value = BigDecimal(currentNumber)
        val result = value.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
        currentNumber = result.stripTrailingZeros().toPlainString()

        val lastOpIndex = findLastOperationIndex(currentExpression.toString())
        if (lastOpIndex >= 0) {
            currentExpression.delete(lastOpIndex + 2, currentExpression.length)
            currentExpression.append(currentNumber)
        } else {
            currentExpression.clear()
            currentExpression.append(currentNumber)
        }

        binding.currentResult.text = currentExpression.toString()
        lastInputWasOperation = false
    }
}
