import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FeatureScaffoldGeneratorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `minimal feature creates only the canonical files`() {
        val root = temporaryFolder.root.toPath()

        val result = FeatureScaffoldGenerator.generate(
            mainSourceRoot = root.resolve("src/main/java"),
            testSourceRoot = root.resolve("src/test/java"),
            options = FeatureGeneratorOptions("Profile", "com.example.app")
        )

        assertEquals("Profile", result.classPrefix)
        assertEquals("com.example.app.profile", result.packageName)
        assertEquals(6, result.files.count { it.startsWith(root.resolve("src/main")) })
        assertEquals(1, result.files.count { it.startsWith(root.resolve("src/test")) })
        assertFalse(result.files.any { it.fileName.toString() == "ProfileUiEffect.kt" })
        assertFalse(result.files.any { "remote" in it.toString() || "local" in it.toString() })
        assertTrue(result.files.all(Files::exists))
        val screen = result.files.single { it.fileName.toString() == "ProfileScreen.kt" }
        assertTrue(Files.readString(screen).contains("label = \"Loading Profile\""))
    }

    @Test
    fun `selected capabilities create their explicit integration boundaries`() {
        val root = temporaryFolder.root.toPath()

        val result = FeatureScaffoldGenerator.generate(
            mainSourceRoot = root.resolve("main"),
            testSourceRoot = root.resolve("test"),
            options = FeatureGeneratorOptions(
                featureName = "Orders",
                basePackage = "com.example",
                navigation = true,
                network = true,
                room = true,
                permission = true,
                backgroundWork = true
            )
        )
        val names = result.files.map { it.fileName.toString() }.toSet()

        assertTrue("OrdersDestination.kt" in names)
        assertTrue("OrdersApiService.kt" in names)
        assertTrue("OrdersDao.kt" in names)
        assertTrue("OrdersUiEffect.kt" in names)
        assertTrue("OrdersWorkScheduler.kt" in names)
        val viewModel = result.files.single { it.fileName.toString() == "OrdersViewModel.kt" }
        assertTrue(Files.readString(viewModel).contains("LaunchPermissionRequest"))
    }

    @Test
    fun `invalid name fails before creating directories`() {
        val root = temporaryFolder.root.toPath()

        val error = runCatching {
            FeatureScaffoldGenerator.generate(
                mainSourceRoot = root.resolve("main"),
                testSourceRoot = root.resolve("test"),
                options = FeatureGeneratorOptions("bad-name", "com.example")
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertFalse(Files.exists(root.resolve("main")))
        assertFalse(Files.exists(root.resolve("test")))
    }

    @Test
    fun `existing file aborts without overwrite`() {
        val root = temporaryFolder.root.toPath()
        val options = FeatureGeneratorOptions("Profile", "com.example")
        val first = FeatureScaffoldGenerator.generate(root.resolve("main"), root.resolve("test"), options)
        val stateFile = first.files.single { it.fileName.toString() == "ProfileUiState.kt" }
        Files.writeString(stateFile, "owned content")

        val error = runCatching {
            FeatureScaffoldGenerator.generate(root.resolve("main"), root.resolve("test"), options)
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertEquals("owned content", Files.readString(stateFile))
    }
}
