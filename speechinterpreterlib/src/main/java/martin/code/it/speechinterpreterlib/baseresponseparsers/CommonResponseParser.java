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
package martin.code.it.speechinterpreterlib.baseresponseparsers;

import android.content.Context;

import com.google.code.regexp.Matcher;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.exceptions.NullGrammarException;
import martin.code.it.speechinterpreterlib.exceptions.NullRuleException;

public class CommonResponseParser  extends BaseResponseParser{
    public final static String GRAMMAR_NAME="common_grammar";

    public static final String NOT_UNDERSTOOD = "not_understood";
    public static final String COMMON_PROMPT="common_prompt";
    public static final String LEAVE_GREETING ="leave_greeting";
    public static final String QUIT="quit";

    public CommonResponseParser(Context context,String grammarName)
            throws XmlPullParserException, IOException, NullGrammarException {

        super(context,grammarName);
        mCurrentRule=mRuleMap.get(false,NOT_UNDERSTOOD);
    }


    @Override
    protected boolean processResponse() throws NullRuleException, EmptyMessageSetException {
        return super.processResponse();
    }

    @Override
    public void reset(){
        super.reset();
        mCurrentRule=mRuleMap.get(false,NOT_UNDERSTOOD);
    }

    public boolean isQuitQuery(String query){
        boolean ret=false;
        Matcher mtc=mRuleMap.getByKey2(QUIT).get(0).getMatcher(query);
        if (mtc!=null) ret=true;
        return ret;
    }




}
