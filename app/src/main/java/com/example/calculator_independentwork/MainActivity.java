package com.example.calculator_independentwork;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import android.view.Window;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Objects;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private TextView display;
    private StringBuilder currentInput = new StringBuilder();
    private boolean hasDot = false;
    private boolean isCleared = true;
    private char lastInput = '\0';
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(android.R.color.black));

        display = findViewById(R.id.display);

        View.OnClickListener listener = view -> {
            MaterialButton button = (MaterialButton) view;
            String buttonText = button.getText().toString();

            button.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            handler.postDelayed(() -> button.setBackgroundColor(242424), 100);

            switch (buttonText) {
                case "AC":
                case "C":
                    toggleClearState();
                    break;
                case "\u232b":
                    backspace();
                    break;
                case "%":
                    calculatePercentage();
                    break;
                case ".":
                    addDot();
                    break;
                case "00":
                case "0":
                    appendDoubleZero(buttonText);
                    break;
                case "=":
                    calculateResult();
                    break;
                default:
                    appendInput(buttonText);
                    break;
            }
        };

        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
                R.id.buttonAC, R.id.buttonBackspace, R.id.buttonPercent,
                R.id.buttonDivide, R.id.buttonMultiply, R.id.buttonMinus,
                R.id.buttonPlus, R.id.buttonDot, R.id.buttonEqual, R.id.button00
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void toggleClearState() {
        if (currentInput.length() > 0) {
            clearInput();
            MaterialButton buttonAC = findViewById(R.id.buttonAC);
            buttonAC.setText("AC");
            isCleared = true;
        } else if (!isCleared) {
            MaterialButton buttonAC = findViewById(R.id.buttonAC);
            buttonAC.setText("AC");
        }
    }

    private void clearInput() {
        currentInput.setLength(0);
        hasDot = false;
        display.setText("0");
        lastInput = '\0';
    }

    private void backspace() {
        if (currentInput.length() > 0) {
            String[] currentInputString = currentInput.toString().split("[*/+\\-]");
//            Log.d("Natija: ", currentInputString[currentInputString.length - 1]);
            String lastPart = currentInputString.length > 0 ? currentInputString[currentInputString.length - 1] : "";
            if (lastPart.indexOf(".") != -1) {
                hasDot = true;
            }
            if (lastPart.endsWith(".")) {
                hasDot = false;
            }
            currentInput.deleteCharAt(currentInput.length() - 1);
            lastInput = currentInput.length() > 0 ? currentInput.charAt(currentInput.length() - 1) : '\0';
            display.setText(currentInput.length() > 0 ? currentInput.toString() : "0");
            if (currentInput.length() < 1) {
                MaterialButton buttonAC = findViewById(R.id.buttonAC);
                buttonAC.setText("AC");
            }
        }
    }

    private void calculatePercentage() {
        if (currentInput.length() > 0 && !isOperator(lastInput)) {
            try {
                double value = Double.parseDouble(currentInput.toString());
                currentInput.setLength(0);
                currentInput.append(formatResult(value / 100));
                display.setText(currentInput.toString());
                hasDot = true;
                lastInput = '\0';
            } catch (NumberFormatException e) {
                display.setText("Hisoblashda xatolik!");
            }
        }
    }

    private void addDot() {
        if (!hasDot) {
            if (currentInput.length() == 0 || isOperator(lastInput)) {
                currentInput.append("0");
            }

            currentInput.append(".");
            hasDot = true;
            lastInput = '.';
            display.setText(currentInput.toString());
        } else {
            display.setText(currentInput.toString());
        }
    }


    private void appendDoubleZero(String input) {
        String[] currentInputString = currentInput.toString().split("[*/+\\-]");
        String lastPart = currentInputString.length > 0 ? currentInputString[currentInputString.length - 1] : "";

        if (isOperator(lastInput) && Objects.equals(input, "0") ) {
            currentInput.append(input + '.');
            display.setText(currentInput.toString());
            hasDot = true;
            lastInput = '.';
        }
        else if (lastPart.length() > 0) {
            if (hasDot || (lastInput != '0' && !isOperator(lastInput))) {
                currentInput.append(input);
                display.setText(currentInput.toString());
                lastInput = '0';
            }
        }
    }


    private void appendInput(String input) {
        char inputChar = input.charAt(0);

        if ((lastInput == '.' && isOperator(inputChar)) || (isOperator(inputChar) && (currentInput.length() == 0 || isOperator(lastInput)))) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            lastInput = inputChar;
            currentInput.append(lastInput);
            display.setText(currentInput.length() > 0 ? currentInput.toString() : "0");
            return;
        }

        if (isOperator(inputChar)) {
            hasDot = false;
        }

        if (currentInput.toString().equals("0") && !input.equals(".")) {
            currentInput.setLength(0);
        }

        currentInput.append(input);
        lastInput = inputChar;
        display.setText(currentInput.toString());

        MaterialButton buttonAC = findViewById(R.id.buttonAC);
        buttonAC.setText("C");
        isCleared = false;
    }


    private void calculateResult() {
        if (currentInput.length() > 0 && !isOperator(lastInput)) {
            try {
                String expression = currentInput.toString().replace("\u00F7", "/").replace("\u00D7", "*").replace('\u2212', '-').replace('\u002B', '+');

                Expression exp = new ExpressionBuilder(expression).build();

                double result = exp.evaluate();

                currentInput.setLength(0);
                currentInput.append(formatResult(result));
                display.setText(currentInput.toString());
                lastInput = '\0';
                hasDot = currentInput.toString().contains(".");
            } catch (Exception e) {
                display.setText(e.toString().indexOf("by zero") != -1 ? "Nolga bo'lish mumkin emas!" : "Hisoblashda xatolik!");
            }
        }
    }


    private String formatResult(double result) {
        if (result == (int) result) {
            return String.valueOf((int) result);
        } else {
            return String.valueOf(result);
        }
    }


    private boolean isOperator(char c) {
        return c == '\u002B' || c == '\u2212' || c == '\u00D7' || c == '\u00F7';
    }
}