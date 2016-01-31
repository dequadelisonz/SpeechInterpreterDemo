/*****************************************************************
 * Copyright 30/12/15 Paolo Martinello
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************/
package martin.code.it.speechinterpreterdemo01;

import android.content.Context;
import android.util.Log;

import com.fathzer.soft.javaluator.DoubleEvaluator;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import martin.code.it.speechinterpreterlib.answeringmachine.AnsweringMachine;
import martin.code.it.speechinterpreterlib.baseresponseparsers.BaseResponseParser;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.exceptions.NullGrammarException;
import martin.code.it.speechinterpreterlib.exceptions.NullRuleException;
import martin.code.it.speechinterpreterlib.exceptions.ResultNotAvailableException;

public class CalculatorResponseParser extends BaseResponseParser {

    public final static String GRAMMAR_NAME = "calculator_grammar";

    public static final String FIRST = "first";
    public static final String SECOND = "second";
    public static final String OPERATOR = "operator";
    public static final String ZERO_DIVIDE = "zero_divide";


    public CalculatorResponseParser(Context context, String grammarName)
            throws XmlPullParserException, IOException, NullGrammarException {

        super(context, grammarName);
    }


    @Override
    protected boolean processResponse() throws EmptyMessageSetException, NullRuleException {
        //super.processResponse();
        String operator = null;
        try {
            operator = getResultFromMsgGroupName(OPERATOR);

            operator = operator.replaceAll("by", "");
            operator = operator.trim();
            operator = operator.replaceAll("plus|addition|sum", "+");
            operator = operator.replaceAll("minus|subtraction", "-");
            operator = operator.replaceAll("divided|division", "/");
            operator = operator.replaceAll("multiplied|multiplication|product", "*");
            String first = getResultFromMsgGroupName(FIRST);
            first = first.replaceAll(",", ".");
            String second = getResultFromMsgGroupName(SECOND);
            second = second.replaceAll(",", ".");
            String expression = first + operator + second;
            DoubleEvaluator interpreter = new DoubleEvaluator();
            Boolean notZeroDivide = !((operator.equalsIgnoreCase("/")) && second.matches("^0+.?0*?$")); //second.startsWith("0"));

            if (notZeroDivide) {
                Double result = interpreter.evaluate(expression);
                result = Math.round(result * 100.0) / 100.0;
                String resultStr = result.toString();
                resultStr = resultStr.replace(".", " dot ");
                this.setDiagResponse(resultStr);
                this.setDiagEnding(AnsweringMachine.END_OF_SPEAK);
            } else {
                runRule(ZERO_DIVIDE);
            }
        } catch (ResultNotAvailableException e) {
            Log.e(TAG, "Something went wrong with results collection, check your grammar XML file," +
                    "and correct flags in group names in the regex");
            this.setDiagResponse("Mmh, something's wrong with my software, pls contact the creator of the app where I'm living.");
            this.setDiagEnding(AnsweringMachine.END_OF_SPEAK);
        }
        return true;
    }

}
