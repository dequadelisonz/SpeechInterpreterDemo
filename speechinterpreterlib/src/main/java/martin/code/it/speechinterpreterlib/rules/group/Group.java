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

import java.util.Arrays;

public class Group  {
    public final static Group NO_KEY_GROUP= new Group("no_key", true,false);

    private final String name;
    private final boolean isOptional;
    private final boolean isSubstitute;

    public Group(String name, boolean isOptional, boolean isSubstitute) {
        this.name = name;
        this.isOptional=isOptional;
        this.isSubstitute = isSubstitute;
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public boolean isSubstitute() {
        return isSubstitute;
    }


    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Group) {
            Group that = (Group) o;
            result = (this.isOptional == that.isOptional &&
                    this.isSubstitute == that.isSubstitute &&
                    this.name.equals(that.name));
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash= Arrays.hashCode(new Object[]{new Boolean(isOptional), new Boolean(isSubstitute)});
        hash+=Arrays.hashCode(new Object[]{new String(name)});
        return hash;
    }


}
