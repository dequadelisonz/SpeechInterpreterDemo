## INTRODUCTION
SpeechInterpreterLib (**SIL**) is a Java library for Android that allows you to manage the dialogue between user and an Android app through a natural language.

You can :
* issue commands
* ask information
* receive notices or warnings
* .... or simply talk!

Dialogues can be managed both in text and in voice format (ongoing development at the moment).
The speech recognition takes place via the rules defined in a series of XML file defined as **grammars**, each grammar is associated with a class extended from the **BaseResponseParser** class that performs most of the work for the recognition of the phrase input and provides available output required to perform the wanted task. 
In any grammar, as well as the recognition of the sentence input to generate an output message, you can make sure to retrieve specific data that can later be used for further processing.
For example, if the user imputes a phrase like **"My name is Jack"** you can retrieve the word **"Jack"** in order to be used as a variable in the code of the app. Or in a sentence like **"Navigate to Rome"** the word **"Rome"** can be used as the destination in the calculation of a navigation route, and so on!

SIL is also able to handle inputs not understandable taking care to repeat the input request, just as if a person were to ask an 'other to repeat what has been said!

-----------------------------

##<a name="working-principle"></a>WORKING PRINCIPLE
When the user generates an input, SIL interprets the phrase by the following logic:

![diagram](http://s20.postimg.org/rzvztw3xp/SIL_working_principle.png)

The mechanism used by the parser to parse the sentence is based on **regular expressions** of Java.
>This is what makes SIL so flexible in recognizing and analysing the input sentence, and this is why it is important to master the fine art of regular expressions.
>There are many helpful tutorials on the net and to practice there are many on-line tester, such as [Regex101](http://regex101.com/) which I found to be a great gym, or [Visual Regex Tester](http://www.ocpsoft.org/tutorials/regular-expressions/java-visual-regex-tester/) of ocpsoft!

Ahead in this guide we will see more details about this.

XML files grammars are placed in the **/assets** folder of the app project, while classes derived from BaseResponseParser are normally included in their packages in the **/java** folder.
![assets folder](http://s20.postimg.org/457kjidil/assets_folder.png)

----------------------------------------

##<a name="basic-structure-of-the-xml-grammar"></a>BASIC STRUCTURE OF THE XML GRAMMAR
As said before, every XML grammar is tied to a Response Parser class which processes and manage the results retrieved from user input. For this reason it is convenient to foresee a couple grammar/parser for each specific domain we are thinking to work with.
For example, if our app has to be able to answer to questions related to weather and send messages, it will be convenient to foresee two different grammar/parser units each specific for every task.

Every grammar basically follows this structure:
```
<root>
	<rule name="ruleA">
		<prompt>
			<item>A first prompt</item>
			<item>A second prompt</item>
			<item> ....other prompts...</item>
		</prompt>
		<preamble>
			<item>A first preamble</item>
			<item>A second preamble</item>
			<item> ....other preambles...</item>
		</preamble>
			<regex>....</regex>
		<msg>
			<item>A first message</item>
			<item>A second message</item>
			<item>...other messages...</item>
		<msg>	
	</rule>
	<rule name="ruleB">
	.......
	</rule>
	<rule name="ruleC">
	.......
	</rule>
	......
<root>
```
Every grammar XML file starts and ends with a ```<root>``` tag.
Every XML file can contain multiple rules which nedd to have a **name** attribute.
>Each rule  shall have an unique name identifier through **all** the grammar files, to avoid mismatch in recognition.

 Each rule can be found by SIL based on the input of the user or it can be launched from the app to specifically ask for an input.
Every rule shall basically contain:
* a ```<prompt>``` tag which defines a set of prompt sentences (each of which is included in an ```<item>``` tag)  to ask the user an input, in the case that the rule was run from the app. The prompt sentence is chosen randomly between the ones in the ```<item>``` tags.
* a ```<preamble>``` tag, similar to ```<prompt>``` but in this case the set of sentences are used to re-ask an input to the user if something has not been understood.
* a ```<regex>``` tag which defines the regular expression used to parse the input.
* a ```<msg>``` tag which defines a set of output messages which can be optionally used by the parser class to give back an output sentence. The message sentence is chosen randomly between the ones in the ```<item>``` tags.

Ahead in this guide we will see further attributes.

------------------------------------------------

##<a name="first-example"></a>A FIRST SIMPLE EXAMPLE
In this example we will see how to create a simple grammar to give an answer to a simple question, and at the same time to retrieve the **name** of the user to be used as data to process.

###<a name="the-grammar-file1"></a>The grammar file
```
<root>
    <rule name="ask_name">
        <preamble>
            <item>I don't think I understood what you mean...</item>
        </preamble>
        <prompt>
            <item> What's your name?</item>
            <item> Could you tell me your name?</item>
        </prompt>
        <regex>My name is (?&lt;name§&gt;.*)</regex>
        <msg>
            <item>Nice to meet you #name#!</item>
            <item>Welcome #name#!</item>
        </msg>
    </rule>
</root>
```
here the grammar file is composed by only one rule named "**ask_name**".
Inside the ```<prompt>``` tag there are two sentences used to prompt the user. SIL chooses randomly one of the "n" sentences available in the set.
Similarly an other sentence is chosen randomly by SIL between the items available in the ```<msg>``` set.
The sentence set in the `<preamble>` tag is used in case that user input was not understood, re-prompt the question.
Here you can notice that there is a string ```#name#``` inside the body of the messages. SIL will replace this place-holder with the content of the group ```<name>``` parsed with the regex.
>In the ```regex``` tag above there's ```&lt;``` and ```&gt;``` around the string **name**: they stay respectively for ```<``` and ```>```. So the real string passed to the regex engine will be ```<name>``` rather than ```&lt;name&gt;```.  This is to avoid mismatch with XML tags of the grammar file while SIL is parsing it. Perhaps in the future the XML parser will be improved to avoid this strange writing.

The tag ```<regex>``` contains the regular expression to match with the user input. In this case the expression is very simple and at the same time limited: it can only match sentences in the form of ```My name is John``` or ```My name is Max```. All the other syntaxes will be not recognized and will make run the common response to say that the input was not understood. Of course the regex mechanism is very powerful and allows to compose more complex regex syntax to recognize a wider range of input sentences.
The group ```(?<name>.*)``` is supposed to catch the user name in the input sentence. More ahead we will see how to manage it.
>Note the symbol "**§**" after the regex name: this is used to declare that this group name is use as substitutive: in other words the result that will be get with this group will replace the placeholder in the message sentence. Without this symbol the result catch by the group **name** will be used as key to get the proper output message from the rule-s set.

###<a name="the-parser-class"></a>The parser class
```
public class AsknameResponseParser extends BaseResponseParser {
    public final static String GRAMMAR_NAME = "askname_grammar.xml";

    public AsknameResponseParser(Context context, String grammarName) throws XmlPullParserException, IOException, NullGrammarException {
        super(context, grammarName);
    }

    @Override
    protected boolean processResponse() throws NullRuleException, EmptyMessageSetException {
        return super.processResponse();
    }

}
```
The basic structure of the parser class is very simple: the two only method that need to be overridden are the constructor and the ```processResponse()``` methods. 
The constructor is supposed to throw exceptions if something goes wrong when the grammar file is read from the assets folder, there is a problem with XML parsing or simply the grammar file does not exist.
Also, a final static field shall be initialized:

``` public final static String GRAMMAR_NAME = "some-grammar-name_grammar"; ```

where the value of the string equals the XML grammar file name. This field is important because from here is taken the XML filename during the Response Parser registration process at the AnsweringMachine initialization.
For basic conversation purposes without need of further processing that's all!

###<a name="including-parser-classes-in-the-app-code"></a>Including parser classes in the app code
```
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
    private Bundle mResultBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mAnsweringMachine = AnsweringMachine.getDefault(this);
            mAnsweringMachine.registerResponseParser(new AsknameResponseParser(this, AsknameResponseParser.GRAMMAR_NAME));
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
            mResultBundle=mAnsweringMachine.answer(mQuestionEdt.getText().toString());
            mResult = mResultBundle.getString(AnsweringMachine.DIALOGUE_RESULT) + " <BR><BR>";
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
            mResultBundle=mAnsweringMachine.runRule("ask_name");
            mResult=mResultBundle.getString(AnsweringMachine.DIALOGUE_RESULT) + " <BR><BR>";
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


```
Here's the MainActivity code used to setup a conversation environment. At this stage the conversation is purely text-based.
The basic interface is made by an EditText to input the questions and a TextView to show the conversation, plus a couple of buttons to enter our questions and init a conversation. It is a very simple part and I will not give further explanation on this. In the github project you will find also the XML layout file of the activity.
The most interesting thing to explain is that the response to our questions is given by an **AnsweringMachine** singleton class:

```
...
private AnsweringMachine mAnsweringMachine;
...
        try {
            mAnsweringMachine = AnsweringMachine.getDefault(this);
            mAnsweringMachine.registerResponseParser(new AsknameResponseParser(this, AsknameResponseParser.GRAMMAR_NAME));

        } catch (XmlPullParserException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (NullGrammarException e) {
            Log.e(TAG, e.getMessage());
        }
...
```
the field **mAnsweringMachine** is firstly declared and then assigned through the static method **AnsweringMachine.getDefault(this)**. This method takes the parameter "**this**" to pass the Activity context to **SIL** in order to make possible to hook the required resources such as the XML grammar files in the assets folder.
After getting our answering machine we need to register all the custom response parsers we are using in our app with this method:
```
mAnsweringMachine.registerResponseParser(...);
```
The **registerResponseParser(...)** takes as parameter a new instance of the custom Response Parser (AsknameResponseParser class) . The custom response parser classes are extended from **BaseResponseParser** class and their constructors take a parameter for activity context `this` and the static String field which declares the grammar file name ([see here](#the-parser-class)).

The answer to the user input comes from the method ```answer()``` of the AnsweringMachine class.

```
mResultBundle=mAnsweringMachine.answer(mQuestionEdt.getText().toString());
mResult = mResultBundle.getString(AnsweringMachine.DIALOGUE_RESULT) + " <BR><BR>";
```

It takes the user input as String parameter and returns a Bundle with the answer message and a flag to determine if the answer is definitive or if it is a prompt for a further input from the user. This will be helpful when the dialogue is managed through speech recognition.
The answer can be extracted from the Bundle with the known method `getString()` using as key the flag `AnsweringMachine.DIALOGUE_RESULT`.

The conversation is start when user clicks the button `mStartBtn` which calls this AnsweringMachine method
```
mResultBundle=mAnsweringMachine.runRule("ask_name");
mResult=mResultBundle.getString(AnsweringMachine.DIALOGUE_RESULT) + " <BR><BR>";
```
Also in this case the method `runRule()` returns a Bundle object containing the result parsed by SIL.

###<a name="retrieving-the-name-of-the-user"></a>Retrieving the name of the user to be used as data to process
In this example the result is automatically processed by SIL: the expected result is the name of the user and the rule grammar regex contains a result group "**name**". This result is parsed and automatically inserted in the output message at the placeholder ```#name#```.
>As mentioned before, the group name in the regex contains the special char "**§**" to tell that the result collected in this group will replace the placeholder in the result message.

It is possible also to retrieve programmatically the value of the result group "**name**" inside the response parser to perform some other operation inside our App (for example store the user name for future usages).

To do this, the method processResponse in our custom AsknameResponseParser class becomes:

```
    @Override
    protected boolean processResponse() throws NullRuleException, EmptyMessageSetException {
        Boolean ret= super.processResponse();
        try {
            String name= getResultFromMsgGroupName("name");
            Toast.makeText(mContext,"I got your name: you are "+name,Toast.LENGTH_SHORT);
        } catch (ResultNotAvailableException e) {
            e.printStackTrace();
        }
        return ret;
    }
```
The method ```getResultFromMsgGroupName()``` does the trick. It takes as parameter a String which is the result group name as it is declared in ```<regex>``` tag in the XML grammar file. Here we use the result to show a Toast message, but we can use it for whatever other purpose we like.

--------------------------------------------------

##<a name="second-example"></a>SECOND EXAMPLE: A SIMPLE DIALOGUE CALCULATOR
Now we'll see a further step in dialogue management: a grammar and a Recognition Parser to accept input for simple arithmetic calculations such as "what is 2 + 3" or "calculate 4 / 2".
###<a name="the-grammar-file2"></a>The grammar file
```
<root>
    <rule name="calculator_inquiry">
        <prompt>
            <item>calculation that you want to run?</item>
            <item>What should I calculate?</item>
        </prompt>
        <regex>(what is|figures|It calculates how much is|calculate how much is|calculate)(?&lt;first%&gt;[ a-z0-9,\\.]*)?(?&lt;operator%&gt; multiplied| multiplied by| divided| divided by| plus| minus)(?&lt;second%&gt;[ a-z0-9,\\.]*)?</regex>
        <msg>
            <item>I can not figure it!</item>
            <item>For now I can perform addition, subtraction, multiplication and division!
            </item>
        </msg>
    </rule>
    <rule name="first" browsable="false">
        <prompt>
            <item>what is the first value?</item>
        </prompt>
        <regex>(?:the first (?:value )?is )?(?&lt;first%&gt;[a-z0-9,\\.]*)</regex>
    </rule>
    <rule name="second" browsable="false">
        <prompt>
            <item>what is the second value?</item>
        </prompt>
        <regex>(?:the second (?:value )?is )?(?&lt;second%&gt;[a-z0-9,\\.]*)</regex>
    </rule>
    <rule name="operator" browsable="false">
        <prompt>
            <item>which operation you want to do?</item>
            <item>which operation you want to do? For now I can perform addition, subtraction, multiplication and division!
            </item>
        </prompt>
        <regex>(?:perform |calculates |make )?(?:the )?(?&lt;operator%&gt;?:(sum|addition|subtraction|multiplication|multiplication by|product|product by|division|division by)|(?:\w*))</regex>
    </rule>
    <rule name="zero_divide" browsable="false">
        <msg>
            <item>I'm sorry but the result does not exist!</item>
            <item>Division by zero has no result!</item>
            <item>Sorry, but in this part of the universe it does not make sense!</item>
        </msg>
    </rule>
</root>
```

The entry point is the main rule **calculator_inquiry**. The regex seems quite complicated but at the same time is also very flexible. It foresees three result groups:
* **first** is the group supposed to collect the first term of the operation. Here the input is free but has to made by numbers or letter.
* **operator** is supposed to collect which kind of operation we'd like to do. Here the input has to fall between a closed group of allowed operations (sum, subtraction, multiplication, division).
* **second** similar to first but for the second term.

We also have a set of prompts in case the rule is run from the app and not found through the user input, and a set of messages to answer in case that something is not understood by SIL.

All the result groups **are not optional**: they are all required to complete the evaluation of the mathematical expression. If the user asks something like "calculate 2 plus" without specifying the second term, SIL will have to ask for the missing input data.
To force asking for the missing input, the special char "**%**" is appended at the end of the group names:

    ?<first%>

At the same time one rule for each result group has to be foreseen to letting know SIL how to ask for missing data. The name of these rules has to equal the result group name (without any special char like "**%**" or "**§**"), to allow SIL to find them.

```
<rule name="first" browsable="false">
	<prompt>
	        <item>what is the first value?</item>
        </prompt>
        <regex>(?:the first (?:value )?is )?(?&lt;first%&gt;[a-z0-9,\\.]*)</regex>
</rule>
```

If the user makes a question like "calculate 2 plus" then SIL will ask "what is the first value?" and the following user input will be evaluated with the regex declared in the rule "**first**".
>If SIL does not find any rule named like a non-optional result group, an **Exception** is thrown.

At the end of the XML grammar file a further rule "**zero_divide**" is declared. This ruled is run from the method `processResponse()` of the **CalculatorResponseParser** class after checking if the opearation is a division and if the second term is equal to zero.

The rules "**first**", "**operator**","**second**" and "**zero_divide**" have in their starting tag the declaration of the attribute "**browsable**":

    browsable="false"

This attribute tells to SIL that the related rule has not to be taken into consideration when parsing all XML grammars to find the right one to answer. This because does not make sense to use them as entry point but only to ask further input after a conversation has already been initiated and they could mess up the user input recognition. Moreover the recognition is uselessly slower.

###<a name="the-parser-class2"></a>The parser class
```
    @Override
    protected boolean processResponse() throws EmptyMessageSetException, NullRuleException {
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
            Boolean notZeroDivide = !((operator.equalsIgnoreCase("/")) && second.matches("^0+.?0*?$"));
            if (notZeroDivide) {
                Double result = interpreter.evaluate(expression);
                result = Math.round(result * 100.0) / 100.0;
                String resultStr = result.toString();
                //resultStr = resultStr.replace(".", " dot ");//to use in case of TTS output
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
```

After collecting all the result groups ("**first**","**operator**" and "**second**") and making some clean-up operation the expression string is composed and a Java expression evaluator is declared (see [here](http://javaluator.sourceforge.net/en/doc/javadoc/com/fathzer/soft/javaluator/package-summary.html) for details).
>In this simplified version of the calculator parser, the input of the first and second term can only be done in numerical format. (i.e. "2" and not "TWO"). I created a parser to convert from numbers from digit to words [here](https://github.com/dequadelisonz/WordUtils2) for Italian spoken language.

Then a zero-divide check is performed before launching the evaluator.

After getting the result in the string `resultStr`  and some further cleanup the final response that will be returned to the user through the instance of **AnsweringMachined** declared in the **MainActivity**.
This done by calling method `setDiagResponse()` which takes as String parameter the result string just processed.
It is also necessary to tell SIL how to end the dialogue through the method `setDiagEnding()` which takes as parameter String as follow
```
    public static final String END_OF_PROMPT
    public static final String END_OF_SPEAK
```
    
In a pure text application like this is not so important which value to set, but it will be important when using SIL with speech recognition, in order to stop recognition after SIL answer (END_OF_SPEAK) or to continue recognition after a prompt (END_OF_PROMPT).

Here's the code snippet to add in **MainActivity** `onCreate()` method, to register the `CalculatorResponseParser` class:

    mAnsweringMachine.registerResponseParser(new CalculatorResponseParser(this, CalculatorResponseParser.GRAMMAR_NAME));

###<a name="the-processresponse-method"></a>The processResponse() method
As explained before, this is the method to override to process the results parsed by SIL.
It is not necessary to call the super class method, but it is helpful if you need to have a pre-processing to get an answer message chosen by mean of result groups collected, as seen in the ([AsknameResponseParser](#a-first-simple-example)). 

This method shall return a boolean value:

 - if true the AnsweringMachine and all the parsers are reset and SIL is ready to start a new conversation with a new user input;
 - If false the AnsweringMachine is not going to be reset and it is possible to run further rules from within the  `processResponse()` method, by using the method `runRule()` in order to get a further input.

--------------------------------------------------

##<a name="third-example"></a>THIRD EXAMPLE: A CONVERSATION GRAMMAR
In this example we'll see how to manage a simple conversation flow with the only implementation of the XML grammar file, without any specific programmatic development.

###<a name="the-grammar-file3"></a>The grammar file
```
<root>
    <rule name="greeting">
        <regex>(?&lt;greet§&gt;Hello|Hi|Good morning|good afternoon|good evening|good night)</regex>
        <msg>
            <item>#greet#!@ask_status</item>
        </msg>
    </rule>

    <rule name="greeting2">
        <regex>(Hello|Hi)?[ ,]*(what's up|how are you( doing)?)( today)?\??</regex>
        <msg>
            <item>Fine thanks! @ask_status</item>
            <item>Well but a little bit annoyed! @chat</item>
        </msg>
    </rule>
    <rule name="i_feel">
        <regex>.*((?&lt;!how can )i feel)(?&lt;how_feel&gt;.*)</regex>
        <msg>
            <item>Tell me more about this feeling</item>
            <item>Do you often feel #how_feel# ?</item>
        </msg>
    </rule>
    <rule name="how_can_i">
        <regex>.*(how can i)(?&lt;how_can_i&gt;.*)</regex>
        <msg>
            <item>Did you never try?</item>
            <item>Why you shouldn't be able to #how_can_i# ?</item>
            <item>Actually you are creating a barrier when you ask if you can #how_can_i#</item>
        </msg>
    </rule>
    <rule name="ask_status" browsable="false">
        <preamble>
            <item>I didn't get it...</item>
            <item>I don't think I understood what you said...</item>
        </preamble>
        <prompt>
            <item>How are you?</item>
            <item>How are you doing today?</item>
        </prompt>
        <regex>(?&lt;how&gt;(quite )?fine|well|good|not bad|bad|good shape|bad shape|.*?)( thanks| thank you)?!?</regex>
        <msggroup  key="how">
            <msg>
                <item>It's funny to hear that you are #how# @chat</item>
            </msg>
            <msg key="fine">
                <item>Nice to hear you are fine!</item>
                <item>OK this is a good news!</item>
                <item>Good!@chat</item>
                <item>Very good!</item>
                <item>Happy to know this!</item>
            </msg>
            <msg key="well">
                <item>Good to hear you are well!</item>
            </msg>
            <msg key="good">
                <item>Nice to hear you are good!</item>
            </msg>
            <msg key="not bad">
                <item>Nice to hear you are not bad!</item>
            </msg>
            <msg key="bad">
                <item>Why are you bad! @chat</item>
            </msg>
            <msg key="good shape">
                <item>I'm glad you are in good shape!</item>
            </msg>
            <msg key="bad shape">
                <item>Why the hell you are in bad shape? @chat</item>
            </msg>
        </msggroup>
    </rule>

    <rule name="chat" browsable="false">
        <preamble>
            <item>I didn't get it...</item>
            <item>I don't think I understood what you said...</item>
        </preamble>
        <prompt>
            <item>Do you want to chat about something?</item>
            <item>Do you want to ask me something?</item>
        </prompt>
        <regex>(?&lt;yesno&gt;yes|no)?(.*?(?&lt;what&gt;m[ae]n|wom[ae]n|sex|life|animals?)|(.*my name is (?&lt;name§&gt;.*))|(?:.*))
        </regex>
        <msggroup>
            <msg>
                <item>I'm sorry but I'm still not ready to talk about this matter!</item>
                <item>Mmmh...I don't like too much this argument. Let's talk about something else!
                </item>
            </msg>
        </msggroup>
        <msggroup key="yesno,what"> <!-->in case of the input is "yes, what about men?"<-->
            <msg key="yes,man">
                <item>@men</item>
            </msg>
            <msg key="yes,men">
                <item>@men</item>
            </msg>
            <msg key="yes,woman">
                <item>@women</item>
            </msg>
            <msg key="yes,women">
                <item>@women</item>
            </msg>
            <msg key="yes,sex">
                <item>@sex</item>
            </msg>
            <msg key="yes,life">
                <item>@life</item>
            </msg>
            <msg key="yes,animals">
                <item>@animals</item>
            </msg>
            <msg>
                <item>I'm sorry but I'm still not ready to talk about this matter!</item>
                <item>Mmmh...I don't like too much this argument. Let's talk about something else!
                </item>
            </msg>
        </msggroup>
        <msggroup key="what"> <!-->in case of the input is "what about men?"<-->
            <msg key="man">
                <item>@men</item>
            </msg>
            <msg key="men">
                <item>@men</item>
            </msg>
            <msg key="woman">
                <item>@women</item>
            </msg>
            <msg key="women">
                <item>@women</item>
            </msg>
            <msg key="sex">
                <item>@sex</item>
            </msg>
            <msg key="life">
                <item>@life</item>
            </msg>
            <msg key="animals">
                <item>@animals</item>
            </msg>
            <msg>
                <item>I'm sorry but I'm still not ready to talk about this matter!</item>
                <item>Mmmh...I don't like too much this argument. Let's talk about something else!
                </item>
            </msg>
        </msggroup>
        <msggroup key="yesno">
            <msg key="yes">
                <item>Ok let's talk!</item>
            </msg>
            <msg key="no">
                <item>OK, see you next time</item>
            </msg>
        </msggroup>
        <msggroup key="yesno,name">
            <msg key="yes,name">
                <item>Nice to meet you #name#!</item>
            </msg>
            <msg key="no,name">
                <item>Welcome #name#!</item>
            </msg>
        </msggroup>
    </rule>

    <rule name="men" browsable="false">
        <msg>
            <item>Men are mythological beings. Half men and half idiot.</item>
            <item>Men are like the Bluetooth connection, when you are next to them, remain connected, but when you are away, look for new devices.
            </item>
            <item>Every man loves two women: one is created from his imagination, the other is not yet born.
            </item>
            <item>Coco Chanel said that a man can wear what he wants. Will always be a woman accessory.
            </item>
            <item>Men are like horoscopes. They tell you what to do. And usually always wrong.
            </item>
        </msg>
    </rule>

    <rule name="women" browsable="false">
        <msg>
            <item>The worst thing you can do to a woman is close to a room with a thousand hats and even a mirror.</item>
            <item>The five minutes of a woman are like dog years: they have to multiply by seven!</item>
            <item>On the sixth day God created man. Then he created woman. Then he created the man again, that the other had escaped.</item>
            <item>The man will have discovered fire, but women discovered how to play.</item>
            <item>Women remember everything. What they do not remember him invent.</item>
        </msg>
    </rule>
    <rule name="sex" browsable="false">
        <msg>
            <item>Do you like sex?</item>
        </msg>
    </rule>
    <rule name="life" browsable="false">
        <msg>
            <item>Do you like life?</item>
        </msg>
    </rule>
    <rule name="animals" browsable="false">
        <msg>
            <item>Do you like animals?</item>
        </msg>
    </rule>
</root>
```

The entry points are the grammars "**greeting**", "**greeting2**", "**i_feel**" and "**how_can_i**". All the others are sub-grammars used in the flow of the conversation because it all have the attribute `browsable="false"`.
So let's analyze the entry point rules:
```
    <rule name="greeting">
        <regex>(?&lt;greet§&gt;Hello|Hi|Good morning|good afternoon|good evening|good night)</regex>
        <msg>
            <item>#greet#!@ask_status</item>
        </msg>
    </rule>
```
The regex has a result group `?<greet§>` which can make start this rule if the user inputs a sentence that matches with this regex, such as "Hi" or "Good morning" or so on.
The result group name has the special char "**§**" which means that the result collected ("Hi" or "Good morning") will be inserted in the placeholder `#greet#` in the message sentence `#greet#!@ask_status`.
The other thing to notice is the string `@ask_status`: this string makes the output message to act as entry point for an other non-browsable rule called "**ask_status**" by putting the special char **§**:
```
<rule name="ask_status" browsable="false">
	<preamble>
		<item>I didn't get it...</item>
		<item>I don't think I understood what you said...</item>
	</preamble>
	<prompt>
		<item>How are you?</item>
		<item>How are you doing today?</item>
	</prompt>
	<regex>(?&lt;how&gt;(quite )?fine|well|good|not bad|bad|good shape|bad shape|.*?)( thanks| thank you)?!?</regex>
	<msggroup  key="how">
		<msg>
			<item>It's funny to hear that you are #how#! @chat</item>
		</msg>
		<msg key="fine">
			<item>Nice to hear you are fine!</item>
			<item>OK this is a good news!</item>
			<item>Good!@chat</item>
			<item>Very good!</item>
			<item>Happy to know this!</item>
		</msg>
		<msg key="well">
			<item>Good to hear you are well!</item>
		</msg>
		<msg key="good">
			<item>Nice to hear you are good!</item>
		</msg>
		<msg key="not bad">
			<item>Nice to hear you are not bad!</item>
		</msg>
		<msg key="bad">
			<item>Why are you bad! @chat</item>
		</msg>
		<msg key="good shape">
			<item>I'm glad you are in good shape!</item>
		</msg>
		<msg key="bad shape">
			<item>Why the hell you are in bad shape? @chat</item>
		</msg>
	</msggroup>
</rule>
```
So the flow of the conversation will be something like:

 - User: "**Good morning**"
 - SIL: "**Good morning! How are you?**"

In the reply of SIL, the part "**Good morning**" is taken from the user input, while the part "**How are you?**" is taken from the prompt sentence set of the rule "**ask_status**".

The regex of the rule "**ask_status**" has a result group `?<how>` which is supposed to collect the status of the user, between a defined set of words ( `fine|well|good|not bad|bad|good shape|bad shape` ) or undefined ( `.*` ).
The name of the result group `?<how>` and its content is then used by SIL as key to select a set of output message, with the help of tag `<msggroup>`.
This tag has the attribute `key` which is declared in this case as `="how"` and this means that if SIL get a valid match for this result group it will select a message from this `<msggroup>`.
Also the tag `<msg>` has an attribute `key` to which it is assigned the value assumed by result group "**how**". We have in fact message sets declared with `key="fine"` or `key="bad"`.

To clarify with an example, the conversation could go ahead like this:

 - User: "**Fine thanks!**"
 - SIL: "**Nice to hear you are fine!**"

or

 - User: "**Good shape**"
 - SIL: "**I'm glad you are in good shape!**"

if the result for group "**how**" is not defined such as "**sad**" or "**hungry**" then SIL will select message sentence without key (noKey set):

 - User: "**Hungry**"
 - SIL: "**It's funny to hear you are hungry! Do you want to chat about something?**"

Here the part "**Do you want to chat about something?**" comes from the prompt set of the rule "**chat**" in fact the message chosen by SIL is "**It's funny to hear that you are #how#! @chat**":

```
<rule name="chat" browsable="false">
        <preamble>
            <item>I didn't get it...</item>
            <item>I don't think I understood what you said...</item>
        </preamble>
        <prompt>
            <item>Do you want to chat about something?</item>
            <item>Do you want to ask me something?</item>
        </prompt>
        <regex>(?&lt;yesno&gt;yes|no)?(.*?(?&lt;what&gt;m[ae]n|wom[ae]n|sex|life|animals?)|(.*my name is (?&lt;name§&gt;.*))|(?:.*))
        </regex>
        <msggroup>
            <msg>
                <item>I'm sorry but I'm still not ready to talk about this matter!</item>
                <item>Mmmh...I don't like too much this argument. Let's talk about something else!
                </item>
            </msg>
        </msggroup>
        <msggroup key="yesno,what"> <!-->in case of the input is "yes, what about men?"<-->
            <msg key="yes,man">
                <item>@men</item>
            </msg>
            <msg key="yes,men">
                <item>@men</item>
            </msg>
            <msg key="yes,woman">
                <item>@women</item>
            </msg>
            <msg key="yes,women">
                <item>@women</item>
            </msg>
            <msg key="yes,sex">
                <item>@sex</item>
            </msg>
            <msg key="yes,life">
                <item>@life</item>
            </msg>
            <msg key="yes,animals">
                <item>@animals</item>
            </msg>
            <msg>
                <item>I'm sorry but I'm still not ready to talk about this matter!</item>
                <item>Mmmh...I don't like too much this argument. Let's talk about something else!
                </item>
            </msg>
        </msggroup>
        <msggroup key="what"> <!-->in case of the input is "what about men?"<-->
            <msg key="man">
                <item>@men</item>
            </msg>
            <msg key="men">
                <item>@men</item>
            </msg>
            <msg key="woman">
                <item>@women</item>
            </msg>
            <msg key="women">
                <item>@women</item>
            </msg>
            <msg key="sex">
                <item>@sex</item>
            </msg>
            <msg key="life">
                <item>@life</item>
            </msg>
            <msg key="animals">
                <item>@animals</item>
            </msg>
        </msggroup>
        <msggroup key="yesno">
            <msg key="yes">
                <item>Ok let's talk!</item>
            </msg>
            <msg key="no">
                <item>OK, see you next time</item>
            </msg>
        </msggroup>
        <msggroup key="yesno,name">
            <msg key="yes,name">
                <item>Nice to meet you #name#!</item>
            </msg>
            <msg key="no,name">
                <item>Welcome #name#!</item>
            </msg>
        </msggroup>
    </rule>
```
this rule is similar but more structured, the `key` attribute in tags `<msggroup>` and `<msg>` are composed by more words because in the regex there are more result groups.
Let's continue our example conversation:

 - SIL: "**It's funny to hear you are hungry! Do you want to chat about something?**"
 - User:"**yes, what about animals?**"
 - SIL: "**do you like animals?**"

The user sentence pilots SIL through the `<msggroup>` with `key="yesno,what"` like the result groups where there is a match ("yes" and "animals") and then through the `<msg>` with `key="yes,animals"`. The message extracted is "**@animals**" which leads to an other rule to continue conversation.
Or conversation could be like this:

 - SIL: "**It's funny to hear you are hungry! Do you want to chat about something?**"
 - User:"**yes, my name is John**"
 - SIL: "**nice to meet you John**"

SIL select `<msggroup>` with `key="yesno,name"` and then `<msg>` with `key="yes,name"`.  Here becomes more clear the meaning of char "**§**": without this char the content of result group "**name**" ("John") will be taken literally as key to select the output message, but it's impossible to code a msg for all the possible names that the user can input!

Or an other case could be:

 - SIL: "**It's funny to hear you are hungry! Do you want to chat about something?**"
 - User:"**what about food**"
 - SIL: "**Mmmh...I don't like too much this argument. Let's talk about something else!**"

this reply was selected because "**food**" is not included in the regex and there is no reply for it.

_____________________

##<a name="common-response-parser"></a>THE CommonRersponseParser: A SPECIAL CASE OF RESPONSE PARSER
Every app using SIL shall define a custom XML grammar for  the CommonResponseParser which is embedded in SIL and takes care of starting, finishing, quitting messages in every conversation. Also it comprises a rule to manage situation where SIL is not capable to parse the user input and so it shall reply with some "not understood" message.
###<a name="the-grammar-file3"></a>The grammar file
```
<root>
    <rule name="common_prompt" browsable="false">
        <prompt>
            <item>Tell me!</item>
            <item>What's up?</item>
            <item>Here I am!</item>
            <item>What can I do for you?</item>
            <item>Ugh! What do you want? I'm busy!</item>
        </prompt>
        <regex>.*</regex>
    </rule>

    <rule name="leave_greeting" browsable="false">
        <msg>
            <item>See you soon!</item>
            <item>Ciao!</item>
            <item>See you!</item>
            <item>Bye!</item>
        </msg>
    </rule>
    
    <rule name="not_understood" browsable="false">
        <msg>
            <item>I'm sorry but I do not understand!</item>
            <item>I can not understand, try to repeat better!</item>
            <item>I'm afraid I do not understand ...</item>
        </msg>
    </rule>
    
    <rule name="quit">
        <regex>quit</regex>
        <msg>
            <item>ok! I'll forget about it!</item>
        </msg>
    </rule>
</root>
```
here's  the basic configuration of this grammar. It is required to set prompt and messages for four rules:

 - "**common_prompt**" - this is the rule in charge to manage the beginning of a conversation, with suitable prompts to begin the conversation. A set of opening prompts shall be set to open the conversation.
 - "**leave_greeting**" - this is the rule in charge to manage the ending of a conversation.  A set of closing messages shall be set to close the conversation.
 - "**not_understood**" - this is the rule in charge to manage situation where SIL has to tell the user that it didn't understood the user input.  A set of messages to tell the input was not understood shall be set to inform the user.
 - "**quit**" - this is the rule in charge to manage the quit in the middle of a conversation. Here shall be set the right regex with a suitable keyword to quit conversation in every moment. **It is very important to properly set the regex of this rule in a way that it cannot be confused with other regex of other rule: the quitting message from the user shall match only with this rule and not with others.**

> The rule names of this grammar shall **never be changed** because they are internally managed by the SIL code. For implementation in new apps the only tags to modify are `<prompt>`, `<msg>` and the tag `<regex>` only for rule "**quit**".
> Also, there is no need to implement a custom Response Parser as far as it is already embedded in SIL as class named `CommonResponseParser`.
_____________________

##<a name="to_be_done"></a>TO BE DONE
The next implementation ongoing regards the integration of SIL with Google ASR engine so to transpose the conversation from written text to spoken speech. Keep following this project on GitHub to get further updates.
_____________________

##<a name="conclusions"></a>CONCLUSIONS
As we saw SIL can be a quite powerful and flexible interpreter to parse spoken language.
A good part of its power is due to the mechanism of the regex that allows us to capture many variations and nuances, that's why it is very important to master the regex syntax.
Also the `<msggroup>` and `<msg>` tags system with their `key` attribute allows good flexibility in guiding SIL to give always the proper response to the user, without playing at all with Java code.
