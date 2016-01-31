/*****************************************************************
 * Copyright 07/12/15 Paolo Martinello
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
package martin.code.it.speechinterpreterlib.answeringmachine;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;

import code.martin.it.eventbuswrapper.EventbusObject;
import martin.code.it.speechinterpreterlib.baseresponseparsers.CommonResponseParser;
import martin.code.it.speechinterpreterlib.baseresponseparsers.IResponseParser;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.exceptions.NullGrammarException;
import martin.code.it.speechinterpreterlib.exceptions.NullRuleException;

public class AnsweringMachine extends EventbusObject {

    // keys definition
    public static final String DIALOGUE_ENDING = "dialogue_ending";
    public static final String DIALOGUE_RESULT = "dialogue_result";

    //valuesByArrayList definition
    public static final String END_OF_PROMPT = "end_of_prompt";
    public static final String END_OF_SPEAK = "end_of_speak";

    private IResponseParser mCurrentResponseParser, mCommonResponseParser;
    private LinkedHashMap<String, IResponseParser> mResponseParserCollection;

    private static AnsweringMachine mIstance;
    private Boolean isConversationRunning = false;
    private Bundle mBundleResult;
    private Context mContext;

    private AnsweringMachine(Context context) throws XmlPullParserException, IOException, NullGrammarException {
        super();
        Log.v(TAG, "Startup");
        this.mContext = context;
        try {
            mCommonResponseParser = new CommonResponseParser(mContext, CommonResponseParser.GRAMMAR_NAME);
            mCurrentResponseParser = mCommonResponseParser;
            mResponseParserCollection = new LinkedHashMap<String, IResponseParser>();
            registerResponseParser(mCommonResponseParser);
        } catch (NullGrammarException e) {
            //Log.e(TAG,e.getMessage());
            throw new NullGrammarException("Grammar for CommonResponseParser has not been found. Please write it and insert in assets folder of your app");
        }

    }

    public static AnsweringMachine getDefault(Context context) throws XmlPullParserException, IOException, NullGrammarException {
        if (mIstance == null) {
            mIstance = new AnsweringMachine(context);
        }
        return mIstance;
    }

    public IResponseParser registerResponseParser(IResponseParser rp) {
        rp.setAnsweringMachine(this);
        rp.reset();
        return mResponseParserCollection.put(rp.getGrammarName(), rp);
    }

    public IResponseParser unRegisterResponseParser(String rpn) {
        return mResponseParserCollection.remove(rpn);
    }


    public Bundle answer(String query) throws EmptyMessageSetException, NullRuleException {
        query = query.replaceAll("[!?]", ""); // question and esclamation marks make trip the regex engine....
        if (!isConversationRunning) {
            for (IResponseParser responseParser : mResponseParserCollection.values()) {
                if (responseParser.findRuleByQuery(query)) {
                    isConversationRunning = true;
                    break;
                }
            }
            ;
        } else mCurrentResponseParser.setQuery(query);
        if (!((CommonResponseParser) mCommonResponseParser).isQuitQuery(query)) {
            mBundleResult = mCurrentResponseParser.answer();
        } else {
            runQuitSequence();
        }
        return mBundleResult;
    }


    public Bundle runRule(String ruleName, String... params) throws NullRuleException, EmptyMessageSetException {
        if (!isConversationRunning) {
            for (IResponseParser responseParser : mResponseParserCollection.values()) {
                if (responseParser.findRuleByName(ruleName)) {
                    isConversationRunning = true;
                    break;
                }
            }
            ;
        }
        mBundleResult = mCurrentResponseParser.runRule(ruleName);
        return mBundleResult;
    }

    public Bundle runStartingPrompt() throws NullRuleException, EmptyMessageSetException {
        Bundle b = runRule(CommonResponseParser.COMMON_PROMPT);
        reset();
        ((CommonResponseParser)mCommonResponseParser).reset();
        return b;
    }

    public Bundle runGreetAnnounce() throws NullRuleException, EmptyMessageSetException {
        Bundle b = runRule(CommonResponseParser.LEAVE_GREETING);
        reset();
        ((CommonResponseParser)mCommonResponseParser).reset();
        return b;
    }

    public Bundle runNotUnderstood() throws NullRuleException, EmptyMessageSetException {
        reset();
        ((CommonResponseParser)mCommonResponseParser).reset();
        Bundle b = runRule(CommonResponseParser.NOT_UNDERSTOOD);
        return b;
    }

    public Bundle runQuitSequence() throws EmptyMessageSetException, NullRuleException {
        reset();
        ((CommonResponseParser)mCommonResponseParser).reset();
        Bundle b = runRule(CommonResponseParser.QUIT);
        return b;
    }

    public void setCurrentResponseParser(IResponseParser baseResponseParser) {
        mCurrentResponseParser = baseResponseParser;

    }

    public void reset() {
        isConversationRunning = false;
        mCurrentResponseParser.reset();
        mCurrentResponseParser = mCommonResponseParser;
    }


}