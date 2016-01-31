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
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.google.code.regexp.Matcher;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

import code.martin.it.eventbuswrapper.EventbusObject;
import martin.code.it.maps.MultiLinkedHashMap;
import martin.code.it.speechinterpreterlib.answeringmachine.AnsweringMachine;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.exceptions.NullGrammarException;
import martin.code.it.speechinterpreterlib.exceptions.NullRuleException;
import martin.code.it.speechinterpreterlib.exceptions.ResultNotAvailableException;
import martin.code.it.speechinterpreterlib.rules.Rule;
import martin.code.it.speechinterpreterlib.rules.group.Group;
import martin.code.it.speechinterpreterlib.rules.group.GroupKey;

public class BaseResponseParser  extends EventbusObject implements IResponseParser {

    //tags const declaration
    private final String RULE_TAG = "rule";
    private final String PREAMBLE_TAG = "preamble";
    private final String PROMPT_TAG = "prompt";
    private final String REGEX_TAG = "regex";
    private final String MSG_TAG = "msg";
    private final String MSGGROUP_TAG = "msggroup";
    private final String ITEM_TAG = "item";

    //attribute const declaration
    private final String NAME_ATTR = "name";
    private final String BROWSBLE_ATTR = "browsable";
    private final String KEY_ATTR = "key";

    public static final String MESSAGE = "message";
    public static final String PREQUEL_TO_MESSAGE = "prequel_to_message";
    public static final String NEXT_RULE = "next_rule";
    public static final String REPLACEMENT_ARRAY = "REPLACEMENT_ARRAY";

    protected final Context mContext;
    private final String mGrammarName;

    protected String mQuery;

    protected MultiLinkedHashMap<Boolean, String, Rule> mRuleMap;
    protected LinkedHashMap<String,Group> mGroupMap;
    protected Rule mCurrentRule;
    private AnsweringMachine mAnsweringMachine;
    protected  MultiLinkedHashMap<String,Group,String> mResults;
    protected LinkedHashMap<String,Group> mCurrentRuleGroups;
    protected Queue<Group> mResultToGetFifo;
    protected Bundle mResultBdl;
    private String mDiagResponse;
    private String mDiagEnding;

    public BaseResponseParser(Context context, String grammarName)
            throws XmlPullParserException, IOException, NullGrammarException {
        super();
        Log.v(TAG, "Startup");
        this.mContext = context;
        this.mGrammarName = grammarName.replaceAll("(?i).xml", "") + ".xml";
        mRuleMap = new MultiLinkedHashMap<>();
        mGroupMap=new LinkedHashMap<>();
        mResults=new MultiLinkedHashMap<>();
        mCurrentRuleGroups =new LinkedHashMap<>();
        mResultToGetFifo=new LinkedList<Group>();
        mResultBdl=new Bundle();
        readGrammar(this.mGrammarName);
        setDiagResponse("");
        setDiagEnding(AnsweringMachine.END_OF_SPEAK);
    }

    private void readGrammar(String grammarName)
            throws XmlPullParserException, IOException, NullGrammarException {
        InputStream is = null;
        try {
            AssetManager am = mContext.getAssets();
            String text = "";
            XmlPullParser parser;
            Rule rule = null;
            is = am.open(grammarName);
            parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(is, "utf8");
            int eventType = parser.getEventType();
            String tagName;
            String tempKey = null;
            GroupKey tempGroupKey = GroupKey.getNoKey();
            ArrayList<String> tempArrayKey = null;
            ArrayList<String> tempList = new ArrayList<>();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase(RULE_TAG)) {
                            rule = new Rule();
                            String nameAttr = parser.getAttributeValue(null, NAME_ATTR);
                            if (nameAttr != null) {
                                rule.setName(nameAttr);
                            } else throw new IllegalStateException("A rule in grammar file " +
                                    grammarName + " has no attribute name defined");
                            String browsableAttr = parser.getAttributeValue(null, BROWSBLE_ATTR);
                            if (browsableAttr != null) {
                                rule.setBrowsable(Boolean.parseBoolean(browsableAttr));
                            }
                            mRuleMap.put(rule.isBrowsable(),rule.getName(), rule);

                        } else if (tagName.equalsIgnoreCase(PROMPT_TAG)) {
                            tempKey = parser.getAttributeValue(null, KEY_ATTR);

                        } else if (tagName.equalsIgnoreCase(PREAMBLE_TAG)) {
                            tempKey = parser.getAttributeValue(null, KEY_ATTR);

                        } else if (tagName.equalsIgnoreCase(MSG_TAG)) {
                            String key = parser.getAttributeValue(null, KEY_ATTR);
                            if (key != null) { //if attribute is not empty or void
                                tempArrayKey = new ArrayList<>(Arrays.asList(key.split(",")));
                            } else tempArrayKey = null;

                        } else if (tagName.equalsIgnoreCase(MSGGROUP_TAG)) {
                            String key = parser.getAttributeValue(null, KEY_ATTR);
                            ArrayList<String> al = null;
                            if (key != null) { //if attribute is not empty or void
                                tempGroupKey=new GroupKey();
                                al = new ArrayList<>(Arrays.asList(key.split(",")));
                                for (String s : al) {
                                    Group g = rule.getGroups().get(s);
                                    if (g != null) tempGroupKey.add(g);
                                }
                            } else tempGroupKey=GroupKey.getNoKey();

                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase(RULE_TAG)) {
                            if (rule.isBrowsable() && (rule.getRegex().equalsIgnoreCase(".*")))
                                throw new
                                        IllegalStateException("Rule with generic regex '.*' cannot be 'browsable' state to true.\n" +
                                        "				Check rule definition in " + grammarName + " xml file.");
                        } else if (tagName.equalsIgnoreCase(REGEX_TAG)) {
                            rule.setRegex(text);
                            mGroupMap.putAll(rule.getGroups());
                        } else if (tagName.equalsIgnoreCase(ITEM_TAG)) {
                            tempList.add(text);
                        } else if (tagName.equalsIgnoreCase(PROMPT_TAG)) {
                            rule.addPrompt(tempKey, tempList);
                            tempList.clear();
                            tempKey = null;
                        } else if (tagName.equalsIgnoreCase(PREAMBLE_TAG)) {
                            rule.addPreamble(tempKey, tempList);
                            tempList.clear();
                            tempKey = null;
                        } else if (tagName.equalsIgnoreCase(MSG_TAG)) {
                            rule.addMessage(tempGroupKey, tempArrayKey, tempList);
                            tempArrayKey = null;
                            tempList.clear();
                        } else if (tagName.equalsIgnoreCase(MSGGROUP_TAG)) {
                            tempGroupKey=GroupKey.getNoKey();
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }

        } finally {
            if (is != null) {
                is.close();
            } else {
                NullGrammarException Nge = new NullGrammarException("Not valid grammar found");
                throw Nge;
            }
        }

        for (Rule r:mRuleMap.valuesByArrayList()) {
            //check if in the group map every rule has a corresponding group
            if (!mGroupMap.containsKey(r.getName()))
                //if not present add it to allow rule iteration if rule was explicitly run and user input does not match regex
                mGroupMap.put(r.getName(),new Group(r.getName(),false,false));

            r.setRootGroup(mGroupMap.get(r.getName()));
        }
    }

    public final Boolean findRuleByQuery(String query) {
        Boolean result = false;
        Matcher mtc;
        LinkedHashMap<String,Rule> lhm=mRuleMap.getByKey1(true);
        if (lhm!=null) {
            for (Rule currentRule : lhm.values()) {
                mtc = currentRule.getMatcher(query);
                if (mtc != null) {
                    mAnsweringMachine.setCurrentResponseParser(this);
                    mCurrentRule = currentRule;
                    mQuery = query;
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public final Boolean findRuleByName(String ruleName) {
        boolean result = mRuleMap.get(true,ruleName) != null;
        result|=mRuleMap.get(false,ruleName)!=null;
        if (result) {
            mAnsweringMachine.setCurrentResponseParser(this);
        }
        return result;
    }

    public void reset() {
        /*
        if (mAnsweringMachine != null) {
            this.mAnsweringMachine.reset();
        }
        */
        mResultToGetFifo.clear();
        mResults.clear();
        mQuery = "";
    }




    public final Bundle runRule(String ruleName) throws NullRuleException, EmptyMessageSetException {
        ArrayList<Rule> al=mRuleMap.getByKey2(ruleName);
        if ((al!=null)&&(!al.isEmpty())) {
            return runRule(al.get(0));
        } else{
            throw new NullRuleException("The rule named "+ruleName+
                    " has not been found. Check carefully the name string passed as parameter to method runRule()" +
                    "and if rule exists in the XML files");
        }
    }

    public final Bundle runRule(Rule ruleToRun) throws EmptyMessageSetException {
        if (ruleToRun != null) {
            //tell to AnsweringMachine that this is the ResponseParser in charge of reply
            mAnsweringMachine.setCurrentResponseParser(this);
            if (ruleToRun.hasPrompt()) { //extract prompt string if rule has regex to parse the user query...
                String prompt = "";
                if (mResultBdl != null)
                    prompt = mResultBdl.getString(PREQUEL_TO_MESSAGE) == null ? "" : mResultBdl.getString(PREQUEL_TO_MESSAGE);
                setDiagResponse(prompt.trim() + " " + ruleToRun.getPrompt());
                setDiagEnding(AnsweringMachine.END_OF_PROMPT);
                //set the rule to run for the next pass
                mCurrentRule = ruleToRun;
            } else { //....or extract msg string if there is only some message to tell
                String msg=ruleToRun.getMessage(null,null).getString(MESSAGE);
                setDiagResponse(msg);
                setDiagEnding(AnsweringMachine.END_OF_SPEAK);
                mAnsweringMachine.reset(); //reset the parser after the msg, nothing more to do at the moment
            }
            return mResultBdl;
        } else {
            return null;
        }
    }

    @Override
    public final Bundle answer() throws IllegalStateException, EmptyMessageSetException {

        if (mCurrentRule != null) {
            //mCurrentRule == null shall never happen....otherwise throw exception because the method answer() shall run only
            //after the mCurrentRule has been defined
            String currentResult = "";
            Matcher mtc = mCurrentRule.getMatcher(mQuery); //get the matcher from the rule
            if (mtc != null) { //if matcher==null, the current rule is not going to be removed from the FiFo queue to attempt again to get the result
                mResultToGetFifo.remove(mCurrentRule.getRootGroup());//if matcher was not null, question was understood and at the moment shall not be repeated anymore
                mCurrentRuleGroups = mCurrentRule.getGroups();
                for (Group group : mCurrentRuleGroups.values()) { //parse through named groups to seek for results
                    if (mResults.get(mCurrentRule.getName(),group) == null) { //if result is already present jump over
                        currentResult = mtc.group(group.getName()); //get the current named group content
                        if ((currentResult == null) || (currentResult.equalsIgnoreCase(""))) {
                            //if the expected result is null or empty then the related rule has to run
                            boolean b = group.isOptional();
                            if ((!mResultToGetFifo.contains(group)) && !b) {
                                // check if it is already present to avoid duplicates and if the rule is not optional
                                mResultToGetFifo.add(group);
                            }
                        } else {
                            //otherwise if there is a result store it and remove its key from the FiFo queue
                            Log.v(TAG, "result for group name " + group
                                    + " found!");
                            mResults.put(mCurrentRule.getName(),group, currentResult.trim());
                            mResultToGetFifo.remove(group);
                        }
                    }
                }//end of for iteration
            } else {
                if (!mResultToGetFifo.contains(mGroupMap.get(mCurrentRule.getName()))) {
                    // if the matcher is null, it means that the question was not understood, so shall be repeated
                    // check also if the rule to be run is not already present in the FiFo queue
                    mResultToGetFifo.add(mGroupMap.get(mCurrentRule.getName()));
                    mResultBdl.putString(PREQUEL_TO_MESSAGE, mCurrentRule.getPreamble());
                }
            }
            if (mResultToGetFifo.isEmpty()) { //if results FiFo is empty then we collected all expected results and can process the final response
                //setMsgKey();
                try {
                    if (processResponse())
                        mAnsweringMachine.reset(); //if process() method returns true everything went fine and parser can be reset
                } catch (NullRuleException e) {
                    Log.e(TAG,e.getMessage());
                }
                //in fact during response processing something could be wrong or not as expected, so further rule could be run from there
                //otherwise further rule has to run to get all expected results
            } else {
                String ruleName=mResultToGetFifo.element().getName();//extract the first rule's name to run to complete results collection
                ArrayList<Rule> alR=mRuleMap.getByKey2(ruleName);//extract the rule from the map using the name
                if ((!alR.isEmpty())&&(alR.get(0)!=null)) { //check the result is not empty or null
                    mResultBdl = runRule(mRuleMap.getByKey2(ruleName).get(0));
                } else throw new IllegalStateException("It seems there is no rule defined in the XML grammar related to result group '"+
                        ruleName+
                        "' or this result it has been declared as non-optional with symbol '%' as suffix in the rule's regex");
            }

        } else
            throw new IllegalStateException("There was a problem with the rules parsing process");
        return mResultBdl;
    }


    //to be overridden by descendant class to get real response after
    //all the mResults required by the selected rule are collected
    protected boolean processResponse() throws NullRuleException, EmptyMessageSetException {
        Boolean mProcessed;
        GroupKey groupKey;
        ArrayList<String> alKey;
        LinkedHashMap<Group,String> Lhm1=mResults.getByKey1(mCurrentRule.getName());
        if (Lhm1!=null){
            groupKey= new GroupKey(Lhm1.keySet());//get the keyset of linkedhashmap related to results of currentrule
            alKey=new ArrayList<>();//get the valuesByArrayList of linkedhashmap related to results of currentrule
            for (Group gg:Lhm1.keySet()){
                if (gg.isSubstitute()) alKey.add(gg.getName());
                else alKey.add(Lhm1.get(gg));
            }
        } else {
            groupKey=GroupKey.getNoKey();
            alKey=null;
        }
        mResultBdl=mCurrentRule.getMessage(groupKey,alKey);//get the suitable message from the current rule messages set
        boolean isPrequel=mResultBdl.getBoolean(Rule.IS_PREQUEL);//check if the result is a prequel to launch an other rule
        ArrayList<String> al = mResultBdl.getStringArrayList(BaseResponseParser.REPLACEMENT_ARRAY);

        if (isPrequel) {
            String p = mResultBdl.getString(PREQUEL_TO_MESSAGE);
            if (al != null) {
                String nm= mCurrentRule.getName();
                Group gr;
                for (String ss : al) { //each groupname placeholder found in the REPLACEMENT_ARRAY will be replaced with actual result
                    gr=mGroupMap.get(ss);
                    p = p.replace("#"+ss+"#", mResults.get(nm,gr));
                }
            }
            mResultBdl.putString(PREQUEL_TO_MESSAGE, p);
            runRule((mResultBdl.getString(BaseResponseParser.NEXT_RULE)));
            mProcessed = false;
        } else {
            String s = mResultBdl.getString(BaseResponseParser.MESSAGE);
            if (al != null) {
                String nm= mCurrentRule.getName();
                String r;
                Group gr;
                for (String ss : al) {
                    gr=mGroupMap.get(ss);
                    r=mResults.get(nm,gr);
                    s = s.replace("#"+ss+"#", r);
                }
            }
            setDiagResponse(s);
            setDiagEnding(AnsweringMachine.END_OF_SPEAK);
            mProcessed = true;
        }
        return mProcessed;
    }

    public final void setQuery(String query) {
        this.mQuery = query;
    }

    public final void setAnsweringMachine(AnsweringMachine mAnsweringMachine) {
        this.mAnsweringMachine = mAnsweringMachine;
    }

    public final String getGrammarName() {
        return mGrammarName;
    }

    protected final String getDiagResponse() {
        return new String(mDiagResponse);
    }


    protected final void setDiagResponse(String diagResponse) {
        this.mDiagResponse = new String(diagResponse);
        mResultBdl.putString(AnsweringMachine.DIALOGUE_RESULT, mDiagResponse);
    }


    protected final String getDiagEnding() {
        return new String(mDiagEnding);
    }


    protected final void setDiagEnding(String mDiagEnding) {
        this.mDiagEnding = new String(mDiagEnding);
        mResultBdl.putString(AnsweringMachine.DIALOGUE_ENDING, mDiagEnding);
    }

    public final String getResultFromMsgGroupName(String name) throws ResultNotAvailableException {
        String res= null;
        try {
            res = mResults.getByKey2(mGroupMap.get(name)).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new ResultNotAvailableException("No results have been found by name '"+name+
                    "'. Check if group name in rule's regex has properly been defined as non-optional" +
                    " with flag %");
        }
        return res;
    }
}
