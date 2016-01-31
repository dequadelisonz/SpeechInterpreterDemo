/**
 * **************************************************************
 * Copyright 2015 Paolo Martinello; created on 17/07/15
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************
 */
package martin.code.it.speechinterpreterdemo01;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import martin.code.it.speechinterpreterlib.baseresponseparsers.BaseResponseParser;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.exceptions.NullGrammarException;
import martin.code.it.speechinterpreterlib.exceptions.NullRuleException;


public class ConversationResponseParser extends BaseResponseParser {
    public final static String GRAMMAR_NAME = "conversation_grammar";


    public ConversationResponseParser(Context context, String grammarName) throws XmlPullParserException, IOException, NullGrammarException {
        super(context, grammarName);
    }

    @Override
    protected boolean processResponse() throws NullRuleException, EmptyMessageSetException {
    return super.processResponse();
    }

}
