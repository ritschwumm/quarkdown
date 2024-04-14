package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
import eu.iamgio.quarkdown.function.library.LibraryRegistrant
import eu.iamgio.quarkdown.function.library.loader.MultiFunctionLibraryLoader
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for functions called from a Quarkdown source.
 * For independent function call tests see [StandaloneFunctionTest].
 */
class FunctionNodeExpansionTest {
    private lateinit var context: MutableContext
    private lateinit var expander: FunctionCallNodeExpander

    @Suppress("MemberVisibilityCanBePrivate")
    fun sum(
        a: Number,
        b: Number,
    ) = NumberValue(a.toFloat() + b.toFloat())

    @FunctionName("customfunction")
    fun myFunction(x: String) = StringValue(x)

    @Suppress("MemberVisibilityCanBePrivate")
    fun echoEnum(value: Aligned.Alignment) = StringValue(value.name)

    @Suppress("MemberVisibilityCanBePrivate")
    fun resourceContent(path: String) = StringValue(javaClass.getResourceAsStream("/function/$path")!!.reader().readText())

    @Suppress("MemberVisibilityCanBePrivate")
    fun setAndEchoDocumentName(
        @Injected context: Context,
        name: String,
    ): StringValue {
        context.documentInfo.name = name
        return StringValue(context.documentInfo.name!!)
    }

    @BeforeTest
    fun setup() {
        context = MutableContext()

        val library =
            MultiFunctionLibraryLoader("lib").load(
                setOf(
                    ::sum,
                    ::myFunction,
                    ::echoEnum,
                    ::resourceContent,
                    ::setAndEchoDocumentName,
                ),
            )

        LibraryRegistrant(context).registerAll(listOf(library))
        expander = FunctionCallNodeExpander(context)
    }

    @Test
    fun `sum expansion`() {
        val node =
            FunctionCallNode(
                "sum",
                listOf(
                    FunctionCallArgument(DynamicInputValue("2")),
                    FunctionCallArgument(DynamicInputValue("3")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertEquals(Text("5"), node.children.first())
    }

    @Test
    fun `sum expansion, failing`() {
        val node =
            FunctionCallNode(
                "sum",
                listOf(
                    FunctionCallArgument(DynamicInputValue("2")),
                    FunctionCallArgument(DynamicInputValue("a")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Text>(this)
            assertTrue("sum(" in text) // Error message
        }
    }

    @Test
    fun `custom function name`() {
        val node =
            FunctionCallNode(
                "customfunction",
                listOf(
                    FunctionCallArgument(DynamicInputValue("abc")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertEquals(Text("abc"), node.children.first())
    }

    @Test
    fun `custom function name, failing`() {
        val node =
            FunctionCallNode(
                "myFunction",
                listOf(
                    FunctionCallArgument(DynamicInputValue("abc")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Text>(this)
            println(text)
            assertTrue("reference" in text) // Unresolved reference error.
        }
    }

    @Test
    fun `resource content expansion, failing`() {
        val node =
            FunctionCallNode(
                "resourceContent",
                listOf(
                    FunctionCallArgument(DynamicInputValue("non-existant-resource")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Text>(this)
            assertTrue("error" in text)
        }
    }

    @Test
    fun `resource content expansion`() {
        val node =
            FunctionCallNode(
                "resourceContent",
                listOf(
                    FunctionCallArgument(DynamicInputValue("hello.txt")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertEquals(Text("Hello Quarkdown!"), node.children.first())
    }

    @Test
    fun `enum lookup, failing`() {
        val node =
            FunctionCallNode(
                "echoEnum",
                listOf(
                    FunctionCallArgument(DynamicInputValue("non-existant-value")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())

        expander.expandAll()

        assertEquals(1, node.children.size)

        with(node.children.first()) {
            assertIs<Text>(this)
            assertTrue("No such element" in text)
        }
    }

    @Test
    fun `context injection`() {
        val node =
            FunctionCallNode(
                "setAndEchoDocumentName",
                listOf(
                    FunctionCallArgument(DynamicInputValue("New name")),
                ),
            )

        context.register(node)

        assertTrue(node.children.isEmpty())
        assertNull(context.documentInfo.name)

        expander.expandAll()

        assertEquals(1, node.children.size)
        assertEquals("New name", context.documentInfo.name)
        assertEquals(Text("New name"), node.children.first())
    }
}