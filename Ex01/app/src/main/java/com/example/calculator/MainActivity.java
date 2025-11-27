package com.example.calculator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class    MainActivity extends AppCompatActivity {

    private TextView tvOperand1, tvOperand2, textResult;
    private boolean activeFirst = true; // đang nhập operand1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOperand1 = findViewById(R.id.tvOperand1);
        tvOperand2 = findViewById(R.id.tvOperand2);
        textResult = findViewById(R.id.textResult);

        int[] numIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        };

        View.OnClickListener numListener = v -> {
            String toAdd = ((Button)v).getText().toString();
            appendToActiveOperand(toAdd);
        };

        for (int id : numIds) {
            findViewById(id).setOnClickListener(numListener);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> backspaceActive());
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            tvOperand1.setText("0");
            tvOperand2.setText("");
            textResult.setText("Result will show here");
            activeFirst = true;
            highlightActive();
        });
        findViewById(R.id.btnSwitch).setOnClickListener(v -> {
            activeFirst = !activeFirst;
            highlightActive();
        });

        findViewById(R.id.btnAdd).setOnClickListener(v -> performOperation(R.id.btnAdd));
        findViewById(R.id.btnSub).setOnClickListener(v -> performOperation(R.id.btnSub));
        findViewById(R.id.btnMul).setOnClickListener(v -> performOperation(R.id.btnMul));
        findViewById(R.id.btnDiv).setOnClickListener(v -> performOperation(R.id.btnDiv));

        tvOperand1.setText("0");
        tvOperand2.setText("");
        highlightActive();
    }

    private void highlightActive() {
        tvOperand1.setText(stripMarker(tvOperand1.getText().toString()));
        tvOperand2.setText(stripMarker(tvOperand2.getText().toString()));

        if (activeFirst) {
            tvOperand1.setText("> " + stripLeadingZero(tvOperand1.getText().toString()));
        } else {
            tvOperand2.setText("> " + stripLeadingZero(tvOperand2.getText().toString()));
        }
    }

    private String stripMarker(String s) {
        if (s.startsWith("> ")) return s.substring(2);
        return s;
    }

    private void appendToActiveOperand(String toAdd) {
        TextView target = activeFirst ? tvOperand1 : tvOperand2;
        String cur = stripMarker(target.getText().toString());

        if (toAdd.equals(".") && cur.contains(".")) return;

        if (cur.equals("0") && !toAdd.equals(".")) {
            cur = toAdd;
        } else {
            cur = cur + toAdd;
        }

        target.setText((activeFirst ? "> " : "") + cur);
    }

    private void backspaceActive() {
        TextView target = activeFirst ? tvOperand1 : tvOperand2;
        String cur = stripMarker(target.getText().toString());
        if (cur.length() == 0) return;

        cur = cur.substring(0, cur.length() - 1);
        if (cur.length() == 0) cur = activeFirst ? "0" : "";

        target.setText((activeFirst ? "> " : "") + cur);
    }

    private void performOperation(int opId) {
        String s1 = stripMarker(tvOperand1.getText().toString()).trim();
        String s2 = stripMarker(tvOperand2.getText().toString()).trim();

        if (s1.isEmpty() || s2.isEmpty()) {
            Toast.makeText(this, "Missing operand!", Toast.LENGTH_SHORT).show();
            return;
        }

        double a = Double.parseDouble(s1);
        double b = Double.parseDouble(s2);
        double res = 0;
        String opSymbol = "";

        if (opId == R.id.btnAdd) {
            res = a + b; opSymbol = "+";
        } else if (opId == R.id.btnSub) {
            res = a - b; opSymbol = "-";
        } else if (opId == R.id.btnMul) {
            res = a * b; opSymbol = "×";
        } else if (opId == R.id.btnDiv) {
            if (b == 0) {
                Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show();
                return;
            }
            res = a / b; opSymbol = "÷";
        }

        textResult.setText(a + " " + opSymbol + " " + b + " = " + res);
    }

    private String stripLeadingZero(String s) {
        if (s.startsWith("0") && s.length() > 1 && !s.startsWith("0.")) {
            int i = 0;
            while (i < s.length() - 1 && s.charAt(i) == '0' && s.charAt(i + 1) != '.') i++;
            return s.substring(i);
        }
        return s;
    }
}
