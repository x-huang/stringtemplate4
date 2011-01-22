/*
 [The "BSD license"]
 Copyright (c) 2009 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.stringtemplate.v4.test;

import org.junit.Test;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.ErrorBuffer;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;
import org.stringtemplate.v4.misc.STRuntimeMessage;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCoreBasics extends BaseTest {
    @Test public void testNullAttr() throws Exception {
        String template = "hi <name>!";
        ST st = new ST(template);
        String expected =
            "hi !";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testAttr() throws Exception {
        String template = "hi <name>!";
        ST st = new ST(template);
        st.add("name", "Ter");
        String expected = "hi Ter!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testSetUnknownAttr() throws Exception {
        String templates =
            "t() ::= <<hi <name>!>>\n";
        ErrorBuffer errors = new ErrorBuffer();
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
		group.setListener(errors);
        ST st = group.getInstanceOf("t");
        String result = null;
		try {
			st.add("name", "Ter");
		}
		catch (IllegalArgumentException iae) {
			result = iae.getMessage();
		}
        String expected = "no such attribute: name";
        assertEquals(expected, result);
    }

    @Test public void testMultiAttr() throws Exception {
        String template = "hi <name>!";
        ST st = new ST(template);
        st.add("name", "Ter");
        st.add("name", "Tom");
        String expected =
            "hi TerTom!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testAttrIsList() throws Exception {
        String template = "hi <name>!";
        ST st = new ST(template);
        List names = new ArrayList() {{add("Ter"); add("Tom");}};
        st.add("name", names);
        st.add("name", "Sumana"); // shouldn't alter my version of names list!
        String expected =
            "hi TerTomSumana!";  // ST sees 3 names
        String result = st.render();
        assertEquals(expected, result);

        assertTrue(names.size() == 2); // my names list is still just 2
    }

    @Test public void testAttrIsArray() throws Exception {
        String template = "hi <name>!";
        ST st = new ST(template);
        String[] names = new String[] {"Ter", "Tom"};
        st.add("name", names);
        st.add("name", "Sumana"); // shouldn't alter my version of names list!
        String expected =
            "hi TerTomSumana!";  // ST sees 3 names
        String result = st.render();
        assertEquals(expected, result);
    }

	@Test public void testProp() throws Exception {
		String template = "<u.id>: <u.name>"; // checks field and method getter
		ST st = new ST(template);
		st.add("u", new User(1, "parrt"));
		String expected = "1: parrt";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testPropWithNoAttr() throws Exception {
		String template = "<foo.a>: <ick>"; // checks field and method getter
		ST st = new ST(template);
		st.add("foo", new HashMap() {{put("a","b");}});
		String expected = "b: ";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testSTProp() throws Exception {
		String template = "<t.x>"; // get x attr of template t
		ST st = new ST(template);
		ST t = new ST("<x>");
		t.add("x", "Ter");
		st.add("t", t);
		String expected = "Ter";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testBooleanISProp() throws Exception {
		String template = "<t.manager>"; // call isManager
		ST st = new ST(template);
		st.add("t", new User(32, "Ter"));
		String expected = "true";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testBooleanHASProp() throws Exception {
		String template = "<t.parkingSpot>"; // call hasParkingSpot
		ST st = new ST(template);
		st.add("t", new User(32, "Ter"));
		String expected = "true";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testNullAttrProp() throws Exception {
		String template = "<u.id>: <u.name>";
		ST st = new ST(template);
		String expected = ": ";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testNoSuchProp() throws Exception {
		ErrorBufferAllErrors errors = new ErrorBufferAllErrors();
		String template = "<u.qqq>";
		STGroup group = new STGroup();
		group.setListener(errors);
		ST st = new ST(group, template);
		st.add("u", new User(1, "parrt"));
		String expected = "";
		String result = st.render();
		assertEquals(expected, result);
		STRuntimeMessage msg = (STRuntimeMessage)errors.errors.get(0);
		STNoSuchPropertyException e = (STNoSuchPropertyException)msg.cause;
		assertEquals("org.stringtemplate.v4.test.BaseTest$User.qqq", e.propertyName);
	}

	@Test public void testNullIndirectProp() throws Exception {
		ErrorBufferAllErrors errors = new ErrorBufferAllErrors();
		STGroup group = new STGroup();
		group.setListener(errors);
		String template = "<u.(qqq)>";
		ST st = new ST(group, template);
		st.add("u", new User(1, "parrt"));
		st.add("qqq", null);
		String expected = "";
		String result = st.render();
		assertEquals(expected, result);
		STRuntimeMessage msg = (STRuntimeMessage)errors.errors.get(0);
		STNoSuchPropertyException e = (STNoSuchPropertyException)msg.cause;
		assertEquals("org.stringtemplate.v4.test.BaseTest$User.null", e.propertyName);
	}

	@Test public void testPropConvertsToString() throws Exception {
		ErrorBufferAllErrors errors = new ErrorBufferAllErrors();
		STGroup group = new STGroup();
		group.setListener(errors);
		String template = "<u.(name)>";
		ST st = new ST(group, template);
		st.add("u", new User(1, "parrt"));
		st.add("name", 100);
		String expected = "";
		String result = st.render();
		assertEquals(expected, result);
		STRuntimeMessage msg = (STRuntimeMessage)errors.errors.get(0);
		STNoSuchPropertyException e = (STNoSuchPropertyException)msg.cause;
		assertEquals("org.stringtemplate.v4.test.BaseTest$User.100", e.propertyName);
	}

    @Test public void testInclude() throws Exception {
        String template = "load <box()>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "kewl"+newline+"daddy");
        String expected =
            "load kewl"+newline+"daddy;";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testIncludeWithArg() throws Exception {
        String template = "load <box(\"arg\")>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "x", "kewl <x> daddy");
		st.impl.dump();
        st.add("name", "Ter");
        String expected = "load kewl arg daddy;";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testIncludeWithArg2() throws Exception {
        String template = "load <box(\"arg\", foo())>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "x,y", "kewl <x> <y> daddy");
        st.impl.nativeGroup.defineTemplate("foo", "blech");
        st.add("name", "Ter");
        String expected = "load kewl arg blech daddy;";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testIncludeWithNestedArgs() throws Exception {
        String template = "load <box(foo(\"arg\"))>;";
        ST st = new ST(template);
        st.impl.nativeGroup.defineTemplate("box", "y", "kewl <y> daddy");
        st.impl.nativeGroup.defineTemplate("foo", "x", "blech <x>");
        st.add("name", "Ter");
        String expected = "load kewl blech arg daddy;";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testDefineTemplate() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("inc", "x", "<x>+1");
        group.defineTemplate("test", "name", "hi <name>!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected =
            "hi TerTomSumana!";
        String result = st.render();
        assertEquals(expected, result);
    }

	@Test public void testMap() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("inc", "x", "[<x>]");
		group.defineTemplate("test", "name", "hi <name:inc()>!");
		ST st = group.getInstanceOf("test");
		st.add("name", "Ter");
		st.add("name", "Tom");
		st.add("name", "Sumana");
		String expected =
			"hi [Ter][Tom][Sumana]!";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testIndirectMap() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("inc", "x", "[<x>]");
		group.defineTemplate("test", "t,name", "<name:(t)()>!");
		ST st = group.getInstanceOf("test");
		st.add("t", "inc");
		st.add("name", "Ter");
		st.add("name", "Tom");
		st.add("name", "Sumana");
		String expected =
			"[Ter][Tom][Sumana]!";
		String result = st.render();
		assertEquals(expected, result);
	}

    @Test public void testMapWithExprAsTemplateName() throws Exception {
        String templates =
            "d ::= [\"foo\":\"bold\"]\n" +
            "test(name) ::= \"<name:(d.foo)()>\"\n" +
            "bold(x) ::= <<*<x>*>>\n";
        writeFile(tmpdir, "t.stg", templates);
        STGroup group = new STGroupFile(tmpdir+"/"+"t.stg");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected = "*Ter**Tom**Sumana*";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testParallelMap() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "names,phones", "hi <names,phones:{n,p | <n>:<p>;}>");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("phones", "x5001");
        st.add("phones", "x5002");
        st.add("phones", "x5003");
        String expected =
            "hi Ter:x5001;Tom:x5002;Sumana:x5003;";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testParallelMapWith3Versus2Elements() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("test", "names,phones", "hi <names,phones:{n,p | <n>:<p>;}>");
        ST st = group.getInstanceOf("test");
        st.add("names", "Ter");
        st.add("names", "Tom");
        st.add("names", "Sumana");
        st.add("phones", "x5001");
        st.add("phones", "x5002");
        String expected =
            "hi Ter:x5001;Tom:x5002;Sumana:;";
        String result = st.render();
        assertEquals(expected, result);
    }

	@Test public void testParallelMapThenMap() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("bold", "x", "[<x>]");
		group.defineTemplate("test", "names,phones",
							 "hi <names,phones:{n,p | <n>:<p>;}:bold()>");
		ST st = group.getInstanceOf("test");
		st.add("names", "Ter");
		st.add("names", "Tom");
		st.add("names", "Sumana");
		st.add("phones", "x5001");
		st.add("phones", "x5002");
		String expected =
			"hi [Ter:x5001;][Tom:x5002;][Sumana:;]";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testMapThenParallelMap() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("bold", "x", "[<x>]");
		group.defineTemplate("test", "names,phones",
							 "hi <[names:bold()],phones:{n,p | <n>:<p>;}>");
		ST st = group.getInstanceOf("test");
		st.add("names", "Ter");
		st.add("names", "Tom");
		st.add("names", "Sumana");
		st.add("phones", "x5001");
		st.add("phones", "x5002");
		String expected =
			"hi [Ter]:x5001;[Tom]:x5002;[Sumana]:;";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testMapIndexes() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("inc", "x,i", "<i>:<x>");
		group.defineTemplate("test", "name", "<name:{n|<inc(n,i)>}; separator=\", \">");
		ST st = group.getInstanceOf("test");
		st.add("name", "Ter");
		st.add("name", "Tom");
		st.add("name", null); // don't count this one
		st.add("name", "Sumana");
		String expected =
			"1:Ter, 2:Tom, 3:Sumana";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testMapIndexes2() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("test", "name", "<name:{n | <i>:<n>}; separator=\", \">");
		ST st = group.getInstanceOf("test");
		st.add("name", "Ter");
		st.add("name", "Tom");
		st.add("name", null); // don't count this one. still can't apply subtemplate to null value
		st.add("name", "Sumana");
		String expected =
			"1:Ter, 2:Tom, 3:Sumana";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testMapSingleValue() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("a", "x", "[<x>]");
		group.defineTemplate("test", "name", "hi <name:a()>!");
		ST st = group.getInstanceOf("test");
		st.add("name", "Ter");
		String expected = "hi [Ter]!";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testMapNullValue() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("a", "x", "[<x>]");
		group.defineTemplate("test", "name", "hi <name:a()>!");
		ST st = group.getInstanceOf("test");
		String expected = "hi !";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testMapNullValueInList() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("test", "name", "<name; separator=\", \">");
		ST st = group.getInstanceOf("test");
		st.add("name", "Ter");
		st.add("name", "Tom");
		st.add("name", null); // don't print this one
		st.add("name", "Sumana");
		String expected =
			"Ter, Tom, Sumana";
		String result = st.render();
		assertEquals(expected, result);
	}

    @Test public void testRepeatedMap() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("a", "x", "[<x>]");
        group.defineTemplate("b", "x", "(<x>)");
        group.defineTemplate("test", "name", "hi <name:a():b()>!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected =
            "hi ([Ter])([Tom])([Sumana])!";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testRoundRobinMap() throws Exception {
        STGroup group = new STGroup();
        group.defineTemplate("a", "x", "[<x>]");
        group.defineTemplate("b", "x", "(<x>)");
        group.defineTemplate("test", "name", "hi <name:a(),b()>!");
        ST st = group.getInstanceOf("test");
        st.add("name", "Ter");
        st.add("name", "Tom");
        st.add("name", "Sumana");
        String expected =
            "hi [Ter](Tom)[Sumana]!";
        String result = st.render();
        assertEquals(expected, result);
    }

	@Test public void testTrueCond() throws Exception {
		String template = "<if(name)>works<endif>";
		ST st = new ST(template);
		st.add("name", "Ter");
		String expected = "works";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testEmptyIFTemplate() throws Exception {
		String template = "<if(x)>fail<elseif(name)><endif>";
		ST st = new ST(template);
		st.add("name", "Ter");
		String expected = "";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testCondParens() throws Exception {
		String template = "<if(!(x||y)&&!z)>works<endif>";
		ST st = new ST(template);
		String expected = "works";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testFalseCond() throws Exception {
		String template = "<if(name)>works<endif>";
		ST st = new ST(template);
		String expected = "";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testFalseCond2() throws Exception {
		String template = "<if(name)>works<endif>";
		ST st = new ST(template);
		st.add("name", null);
		String expected = "";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testFalseCondWithFormalArgs() throws Exception {
		// insert of indent instr was not working; ok now
		String dir = getRandomDir();
		String groupFile =
			"a(scope) ::= <<" +newline+
			"foo" +newline+
			"    <if(scope)>oops<endif>" +newline+
			"bar" +newline+
			">>";
		writeFile(dir, "group.stg", groupFile);
		STGroupFile group = new STGroupFile(dir+"/group.stg");
		ST st = group.getInstanceOf("a");
		st.impl.dump();
		String expected = "foo" +newline+
			"bar";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testElseIf2() throws Exception {
		String template =
			"<if(x)>fail1<elseif(y)>fail2<elseif(z)>works<else>fail3<endif>";
		ST st = new ST(template);
		st.add("z", "blort");
		String expected = "works";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testElseIf3() throws Exception {
		String template =
			"<if(x)><elseif(y)><elseif(z)>works<else><endif>";
		ST st = new ST(template);
		st.add("z", "blort");
		String expected = "works";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testNotTrueCond() throws Exception {
		String template = "<if(!name)>works<endif>";
		ST st = new ST(template);
        st.add("name", "Ter");
        String expected = "";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testNotFalseCond() throws Exception {
        String template = "<if(!name)>works<endif>";
        ST st = new ST(template);
        String expected = "works";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testTrueCondWithElse() throws Exception {
        String template = "<if(name)>works<else>fail<endif>";
        ST st = new ST(template);
        st.add("name", "Ter");
        String expected = "works";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testFalseCondWithElse() throws Exception {
        String template = "<if(name)>fail<else>works<endif>";
        ST st = new ST(template);
        String expected = "works";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testElseIf() throws Exception {
        String template = "<if(name)>fail<elseif(id)>works<else>fail<endif>";
        ST st = new ST(template);
        st.add("id", "2DF3DF");
        String expected = "works";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testElseIfNoElseAllFalse() throws Exception {
        String template = "<if(name)>fail<elseif(id)>fail<endif>";
        ST st = new ST(template);
        String expected = "";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testElseIfAllExprFalse() throws Exception {
        String template = "<if(name)>fail<elseif(id)>fail<else>works<endif>";
        ST st = new ST(template);
        String expected = "works";
        String result = st.render();
        assertEquals(expected, result);
    }

	@Test public void testOr() throws Exception {
		String template = "<if(name||notThere)>works<else>fail<endif>";
		ST st = new ST(template);
		st.add("name", "Ter");
		String expected = "works";
		String result = st.render();
		assertEquals(expected, result);
	}

    @Test public void testMapConditionAndEscapeInside() throws Exception {
        String template = "<if(m.name)>works \\\\<endif>";
        ST st = new ST(template);
        Map m = new HashMap();
        m.put("name", "Ter");
        st.add("m", m);
        String expected = "works \\";
        String result = st.render();
        assertEquals(expected, result);
    }

	@Test public void testAnd() throws Exception {
		String template = "<if(name&&notThere)>fail<else>works<endif>";
		ST st = new ST(template);
		st.add("name", "Ter");
		String expected = "works";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testAndNot() throws Exception {
		String template = "<if(name&&!notThere)>works<else>fail<endif>";
		ST st = new ST(template);
		st.add("name", "Ter");
		String expected = "works";
		String result = st.render();
		assertEquals(expected, result);
	}

    @Test public void testCharLiterals() throws Exception {
        ST st = new ST(
                "Foo <\\n><\\n><\\t> bar\n"
                );
        StringWriter sw = new StringWriter();
        st.write(new AutoIndentWriter(sw,"\n")); // force \n as newline
        String result = sw.toString();
        String expecting ="Foo \n\n\t bar\n";     // expect \n in output
        assertEquals(expecting, result);

        st = new ST(
                "Foo <\\n><\\t> bar" +newline);
        sw = new StringWriter();
        st.write(new AutoIndentWriter(sw,"\n")); // force \n as newline
        expecting ="Foo \n\t bar\n";     // expect \n in output
        result = sw.toString();
        assertEquals(expecting, result);

        st = new ST(
                "Foo<\\ >bar<\\n>");
        sw = new StringWriter();
        st.write(new AutoIndentWriter(sw,"\n")); // force \n as newline
        result = sw.toString();
        expecting ="Foo bar\n"; // forced \n
        assertEquals(expecting, result);
    }

    @Test public void testUnicodeLiterals() throws Exception {
        ST st = new ST(
                "Foo <\\uFEA5><\\n><\\u00C2> bar\n"
                );
        String expecting ="Foo \ufea5"+newline+"\u00C2 bar"+newline;
        String result = st.render();
        assertEquals(expecting, result);

        st = new ST(
                "Foo <\\uFEA5><\\n><\\u00C2> bar" +newline);
        expecting ="Foo \ufea5"+newline+"\u00C2 bar"+newline;
        result = st.render();
        assertEquals(expecting, result);

        st = new ST(
                "Foo<\\ >bar<\\n>");
        expecting ="Foo bar"+newline;
        result = st.render();
        assertEquals(expecting, result);
    }

    @Test public void testSubtemplateExpr() throws Exception {
        String template = "<{name\n}>";
        ST st = new ST(template);
        String expected =
            "name"+newline;
        String result = st.render();
        assertEquals(expected, result);
    }

	@Test public void testSeparator() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
		ST st = group.getInstanceOf("test");
		st.add("names", "Ter");
		st.add("names", "Tom");
		String expected =
			"case Ter, case Tom";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void testSeparatorInList() throws Exception {
		STGroup group = new STGroup();
		group.defineTemplate("test", "names", "<names:{n | case <n>}; separator=\", \">");
		ST st = group.getInstanceOf("test");
		st.add("names", new ArrayList<String>() {{add("Ter"); add("Tom");}});
		String expected =
			"case Ter, case Tom";
		String result = st.render();
		assertEquals(expected, result);
	}

	@Test public void playing() throws Exception {
		String template = "<a:t(x,y),u()>";
		ST st = new ST(template);
		st.impl.dump();
	}

}
