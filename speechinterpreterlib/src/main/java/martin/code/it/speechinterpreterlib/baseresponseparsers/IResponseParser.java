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

import android.os.Bundle;

import martin.code.it.speechinterpreterlib.answeringmachine.AnsweringMachine;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.exceptions.NullRuleException;
import martin.code.it.speechinterpreterlib.rules.Rule;

public interface IResponseParser {
    public Bundle answer() throws EmptyMessageSetException;
    public void reset();
    public void setAnsweringMachine(AnsweringMachine mAnsweringMachine);
    public String getGrammarName();
    public Boolean findRuleByQuery(String query);
    public Boolean findRuleByName(String ruleName);
    public void setQuery(String query);
    public Bundle runRule(String ruleName) throws NullRuleException, EmptyMessageSetException;
    public Bundle runRule(Rule ruleToRun) throws EmptyMessageSetException;
}
