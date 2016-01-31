package martin.code.it.speechinterpreterdemo01;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import martin.code.it.speechinterpreterlib.answeringmachine.AnsweringMachine;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.exceptions.NullGrammarException;
import martin.code.it.speechinterpreterlib.exceptions.NullRuleException;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String ERROR_MESSAGE_RESULT = "Something went wrong during interpretation of your question....pls contact the creator of the app where I'm living. <BR><BR>";
    protected final String TAG = this.getClass().getSimpleName();
    private final String userPrompt = "<b><font color = \"#ff3399\">You said:</font></b>  ";
    private final String hartwinPrompt = "<b><font color = \"#009900\">Hartwin said:</font></b>  ";
    private AnsweringMachine mAnsweringMachine;
    private EditText mQuestionEdt;
    private TextView mAnswerTxv;
    private ScrollView mAnswerScv;
    private ImageButton mAskBtn, mStartBtn;
    private String mResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mAnsweringMachine = AnsweringMachine.getDefault(this);
            mAnsweringMachine.registerResponseParser(new AsknameResponseParser(this, AsknameResponseParser.GRAMMAR_NAME));
            mAnsweringMachine.registerResponseParser(new ConversationResponseParser(this, ConversationResponseParser.GRAMMAR_NAME));
            mAnsweringMachine.registerResponseParser(new CalculatorResponseParser(this, CalculatorResponseParser.GRAMMAR_NAME));
        } catch (XmlPullParserException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (NullGrammarException e) {
            Log.e(TAG, e.getMessage());
        }


        mQuestionEdt = (EditText) findViewById(R.id.questionEdt);

        mAnswerTxv = (TextView) findViewById(R.id.answerTxv);
        mAnswerTxv.setMovementMethod(new ScrollingMovementMethod());

        mAnswerScv = (ScrollView) findViewById(R.id.answerScv);
        mAnswerScv.fullScroll(View.FOCUS_DOWN);

        mAskBtn = (ImageButton) findViewById(R.id.askBtn);
        mAskBtn.setOnClickListener(this);

        mStartBtn = (ImageButton) findViewById(R.id.startBtn);
        mStartBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        String question;
        if (view.equals(mAskBtn)) {
            question = mQuestionEdt.getText().toString() + " <BR><BR>";
            mAnswerTxv.append(Html.fromHtml(userPrompt + question));
            mAnswerScv.smoothScrollTo(0, mAnswerTxv.getBottom());
            try {
                mResult = mAnsweringMachine.answer(mQuestionEdt.getText().toString()).getString(AnsweringMachine.DIALOGUE_RESULT) + " <BR><BR>";
            } catch (EmptyMessageSetException e) {
                errorProcedure(e);
            } catch (NullRuleException e) {
                errorProcedure(e);
            }
            mAnswerTxv.append(Html.fromHtml(hartwinPrompt + mResult));
            mAnswerScv.smoothScrollTo(0, mAnswerTxv.getBottom());
            mQuestionEdt.setText("");
        } else if (view.equals(mStartBtn)) {
            try {
                mResult = mAnsweringMachine.runRule("ask_name").getString(AnsweringMachine.DIALOGUE_RESULT) + " <BR><BR>";
            } catch (NullRuleException e) {
                errorProcedure(e);
            } catch (EmptyMessageSetException e) {
                errorProcedure(e);
            }
            mAnswerTxv.append(Html.fromHtml(hartwinPrompt + mResult));
        }

    }

    @NonNull
    private void errorProcedure(Exception e) {
        Log.e(TAG, e.getMessage());
        mResult = ERROR_MESSAGE_RESULT;
        mAnsweringMachine.reset();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
