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
package martin.code.it.speechinterpreterlib.rules.group;

import java.util.ArrayList;
import java.util.Collection;

public class GroupKey  extends ArrayList<Group> {

    private static GroupKey NO_KEY_GROUPKEY;


    public GroupKey() {
        super();

    }

    /**
    private GroupKey(boolean b){
        super();
    }
     **/

    public GroupKey(Collection<Group> collection) {
        super(collection);
    }

    public static GroupKey getNoKey(){
        if ((NO_KEY_GROUPKEY==null)||NO_KEY_GROUPKEY.isEmpty()){
            NO_KEY_GROUPKEY=new GroupKey();
            NO_KEY_GROUPKEY.add(Group.NO_KEY_GROUP);
        }
        return NO_KEY_GROUPKEY;
    }

}
