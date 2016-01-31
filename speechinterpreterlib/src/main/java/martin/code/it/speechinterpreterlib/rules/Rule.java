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
package martin.code.it.speechinterpreterlib.rules;

import android.os.Bundle;
import android.util.Log;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import it.code.martin.WordUtils2;
import martin.code.it.maps.MultiLinkedHashMap;
import martin.code.it.speechinterpreterlib.baseresponseparsers.BaseResponseParser;
import martin.code.it.speechinterpreterlib.exceptions.EmptyMessageSetException;
import martin.code.it.speechinterpreterlib.rules.group.Group;
import martin.code.it.speechinterpreterlib.rules.group.GroupKey;

public class Rule  {
    protected final String TAG = this.getClass().getSimpleName();

    public static final String NO_KEY="no_key";
    public static final String IS_PREQUEL="is prequel";
    public static final ArrayList<String> _NO_MSG_KEY = new ArrayList(Arrays.asList("NO KEY"));

    private static final int PREAMBLE=1;
    private static final int PROMPT=2;
    private static final int MESSAGE=3;

    private String mName,mRegex;
    private boolean mIsBrowsable=true;
    private boolean mHasPrompt =false;
    private LinkedHashMap<String,ArrayList<String>> mPreambles,mPrompts;
    private MultiLinkedHashMap<GroupKey,ArrayList<String>,ArrayList<String>> mMsgList;

    private LinkedHashMap<String,Group> mGroups;

    private Group mRootGroup;

    private Matcher mMatcher;
    private Pattern mPattern;


    public Rule(){
        mRegex="";
        mPreambles=new LinkedHashMap<>();
        mPrompts=new LinkedHashMap<>();
        mMsgList =new MultiLinkedHashMap<>();
        //mMsgList.put(GroupKey.NO_KEY_GROUPKEY, null, null);
        mGroups =new LinkedHashMap<>();
        //mGroups.put(NO_KEY,Group.NO_KEY_GROUP);

    }

    public void setName(String nameAttr) {
        this.mName= nameAttr;
    }

    public void setBrowsable(boolean browsableAttr) {
        this.mIsBrowsable = browsableAttr;
    }

    public String getName() { return mName; }

    public Boolean isBrowsable() { return mIsBrowsable; }

    private String getAMessage(int what,String key) throws EmptyMessageSetException{
        String s = null;
        try{
            switch (what){
                case PREAMBLE:
                    s = WordUtils2.getRandomStringFromArray(mPreambles.get(key));
                    break;
                case PROMPT:
                    s = WordUtils2.getRandomStringFromArray(mPrompts.get(key));
                    break;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG,e.getMessage());
            throw new EmptyMessageSetException("There's no prompt or preamble set for the rule "+mName);
        }
        return s;
    }

    //preamble management
    public String getPreamble() throws EmptyMessageSetException {
        return getAMessage(PREAMBLE,null);
    }

    public String getPreamble(String key) throws EmptyMessageSetException {
        return getAMessage(PREAMBLE,key);
    }

    public void addPreamble(String key, ArrayList<String> preamble){
        mPreambles.put(key,new ArrayList<String>(preamble));
    }

    //prompt management
    public String getPrompt() throws EmptyMessageSetException {
        return getAMessage(PROMPT,null);
    }

    public String getPrompt(String key) throws EmptyMessageSetException {
        return getAMessage(PROMPT,key);
    }

    public void addPrompt(String key, ArrayList<String> prompt){
        mHasPrompt =true;
        mPrompts.put(key,new ArrayList<String>(prompt));
    }

    //regex management
    public void setRegex(String regex){
        regex = regex.replaceAll("&lg;", "<");
        regex = regex.replaceAll("&gt;", ">");
        regex=regex.replaceAll("( *\n *)*$","");//clear new line chars from xml file
        java.util.regex.Pattern p= java.util.regex.Pattern.compile("(<\\S+?>)");
        java.util.regex.Matcher m=p.matcher(regex);
        String grN;
        while (m.find()){ //look for optional and replace results to substitute in final message
            grN=m.group();
            boolean isOptional=!grN.contains("%"); //if in the group name there is no % it means that this result is optional
            boolean isSubstitute=grN.contains("§");//if in the group name there is § it means that this result is substitutive
            grN =grN.replaceAll("%", "");
            grN=grN.replaceAll("§", "");
            grN=grN.replaceAll("<|>","");
            mGroups.put(grN,new Group(grN,isOptional,isSubstitute));
        }
        regex=regex.replaceAll("%",""); //clean the regex from special chars
        regex=regex.replaceAll("§", "");//clean the regex from special chars
        this.mRegex=regex;
      }

    public String getRegex(){
        return mRegex;
    }

    //MsgGroup management

    public void addMessage(GroupKey groupKey, ArrayList<String> msgKey, ArrayList<String> msgList){
        ArrayList<String> al=null;
        if (msgKey!=null) al=new ArrayList<>(msgKey);
        mMsgList.put(new GroupKey(groupKey),al,new ArrayList<String>(msgList));
    }

    public Bundle getMessage(GroupKey groupKey, ArrayList<String> alKey) throws EmptyMessageSetException {
        String msg=null, nextRule;
        Bundle b=new Bundle();
        //ArrayList<String> al = (mMsgList.get(groupKey, alKey)==null)?
        //      mMsgList.get(GroupKey.getNoKey(),null):mMsgList.get(groupKey, alKey);
        LinkedHashMap<ArrayList<String>,ArrayList<String>> lhm=mMsgList.getByKey1(groupKey);
        if (lhm==null) lhm=mMsgList.getByKey1(GroupKey.getNoKey());
        ArrayList<String> al= lhm.containsKey(alKey)?lhm.get(alKey):lhm.get(null);

        try {
            msg = WordUtils2.getRandomStringFromArray(al);
        } catch (IllegalStateException e) {
            Log.e(TAG, "It looks like the xml grammar does not foresee an answer for this kind of question.");
            msg = "x!$ TILT x!$";
        }catch (IllegalArgumentException e1){
            Log.e(TAG,e1.getMessage());
            throw new EmptyMessageSetException("There's no message set for the rule "+mName+
                    " or there is a mismatch in group name definition in the regex (check for special char '§').");
        }
        al=new ArrayList<>();
        Pattern p = Pattern.compile("#(.*?)#");
        Matcher m = p.matcher(msg);
        while (m.find()) { //fill an array with the groupname of all placeholder found the message
                            //will be used to replace placeholder with actual results recognized
            al.add(m.group(1));
        }
        //ship with resultbundle an array with results nameplace found in the message
        //to be replaced later on
        b.putStringArrayList(BaseResponseParser.REPLACEMENT_ARRAY, al);
        if (msg.contains("@")) {
            nextRule = msg.substring(msg.lastIndexOf("@") + 1).trim();
            b.putString(BaseResponseParser.PREQUEL_TO_MESSAGE, msg.substring(0, msg.lastIndexOf("@")));
            b.putString(BaseResponseParser.NEXT_RULE, nextRule);
            b.putBoolean(IS_PREQUEL,true);
        } else {
            b.putString(BaseResponseParser.MESSAGE, msg);
            b.putBoolean(IS_PREQUEL,false);
        }
        return b;
    }

    public LinkedHashMap<String, Group> getGroups() {
        return mGroups;
    }

    public Matcher getMatcher(String query) {
        mPattern = Pattern.compile(this.getRegex(), 66);
        mMatcher = mPattern.matcher(query);
        Boolean found = mMatcher.matches();
        if (found) {
            return mMatcher;
        } else return null;
    }


    public boolean hasPrompt() {
        return mHasPrompt;
    }

    public boolean setRootGroup(Group group){
        boolean ret=false;
        if ((mRootGroup==null)&&(group!=null)){
            mRootGroup=group;
            ret=true;
        }
        return ret;
    }

    public Group getRootGroup(){
        return mRootGroup;
    }

}
