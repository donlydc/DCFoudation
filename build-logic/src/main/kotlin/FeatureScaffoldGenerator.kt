import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

internal data class FeatureGeneratorOptions(
    val featureName: String,
    val basePackage: String,
    val navigation: Boolean = false,
    val network: Boolean = false,
    val room: Boolean = false,
    val permission: Boolean = false,
    val backgroundWork: Boolean = false
)

internal data class GeneratedFeature(
    val classPrefix: String,
    val packageName: String,
    val files: List<Path>
)

/** Pure filesystem generator kept separate from Gradle so validation and output are unit-testable. */
internal object FeatureScaffoldGenerator {
    private val identifier = Regex("[A-Za-z][A-Za-z0-9]*")
    private val packageSegment = Regex("[A-Za-z_][A-Za-z0-9_]*")
    private val kotlinKeywords = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "interface", "is", "null", "object", "package", "return", "super",
        "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while"
    )

    fun generate(
        mainSourceRoot: Path,
        testSourceRoot: Path,
        options: FeatureGeneratorOptions,
        /** Directory for `<feature>.md` (normally `architecture/features`); null skips the page. */
        architectureRoot: Path? = null
    ): GeneratedFeature {
        val classPrefix = validateFeatureName(options.featureName)
        val basePackage = validateBasePackage(options.basePackage)
        val featureSegment = classPrefix.lowercase()
        require(featureSegment !in kotlinKeywords) {
            "featureName produces reserved Kotlin package segment '$featureSegment'"
        }
        val packageName = "$basePackage.$featureSegment"
        val relativePackage = packageName.replace('.', '/')
        val mainFeatureRoot = mainSourceRoot.resolve(relativePackage)
        val testFeatureRoot = testSourceRoot.resolve(relativePackage)

        val plannedFiles = linkedMapOf<Path, String>()
        fun main(relative: String, content: String) {
            plannedFiles[mainFeatureRoot.resolve(relative)] = content
        }

        main("${classPrefix}UiState.kt", uiState(packageName, classPrefix))
        main("${classPrefix}UiAction.kt", uiAction(packageName, classPrefix, options.permission))
        if (options.permission) {
            main("${classPrefix}UiEffect.kt", uiEffect(packageName, classPrefix))
        }
        main("${classPrefix}ViewModel.kt", viewModel(packageName, classPrefix, options.permission))
        main("${classPrefix}Route.kt", route(packageName, classPrefix, options.permission))
        main("${classPrefix}Screen.kt", screen(packageName, classPrefix))
        main("${classPrefix}Preview.kt", preview(packageName, classPrefix))

        if (options.navigation) {
            main("${classPrefix}Destination.kt", destination(packageName, classPrefix))
        }
        if (options.network) {
            main("remote/${classPrefix}ApiService.kt", apiService("$packageName.remote", classPrefix))
        }
        if (options.room) {
            main("local/${classPrefix}Dao.kt", dao("$packageName.local", classPrefix))
        }
        if (options.backgroundWork) {
            main(
                "background/${classPrefix}WorkScheduler.kt",
                workScheduler("$packageName.background", classPrefix)
            )
        }
        plannedFiles[testFeatureRoot.resolve("${classPrefix}ViewModelTest.kt")] =
            viewModelTest(packageName, classPrefix, options.permission)

        if (architectureRoot != null) {
            plannedFiles[architectureRoot.resolve("$featureSegment.md")] =
                architecturePage(featureSegment, packageName, classPrefix, options)
        }

        val conflicts = plannedFiles.keys.filter(Files::exists)
        check(conflicts.isEmpty()) {
            "Feature generation stopped because these files already exist:\n" +
                conflicts.joinToString(separator = "\n") { "- $it" }
        }

        plannedFiles.forEach { (path, content) ->
            Files.createDirectories(path.parent)
            Files.writeString(
                path,
                content.trimIndent().trimEnd() + "\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
            )
        }

        return GeneratedFeature(classPrefix, packageName, plannedFiles.keys.toList())
    }

    private fun validateFeatureName(value: String): String {
        require(value.isNotBlank()) {
            "Missing featureName. Use -PfeatureName=Profile (letters and digits only)."
        }
        require(value == value.trim() && identifier.matches(value)) {
            "Invalid featureName '$value'. Use one Kotlin identifier without spaces or punctuation."
        }
        return value.replaceFirstChar { it.uppercaseChar() }
    }

    private fun validateBasePackage(value: String): String {
        require(value.isNotBlank()) {
            "Could not infer a package. Apply the plugin to an Android module or pass -PfeaturePackage=com.example.app."
        }
        val segments = value.split('.')
        require(segments.all { packageSegment.matches(it) && it !in kotlinKeywords }) {
            "Invalid base package '$value'."
        }
        return value
    }

    /**
     * Skeleton of `architecture/features/TEMPLATE.md`: same numbered headings, Status DRAFT,
     * Test status NOT_RUN. Only facts known to the generator are filled; the rest stays `TODO`.
     */
    private fun architecturePage(
        segment: String,
        packageName: String,
        name: String,
        options: FeatureGeneratorOptions
    ): String {
        val navigation = if (options.navigation) {
            "`${name}Destination` created; register it at the app navigation point (NAV-01)."
        } else {
            "none generated (TODO)"
        }
        val effect = if (options.permission) "`${name}UiEffect` (permission launch)" else "none"
        val testPath = "`${name}ViewModelTest` (generated skeleton)"
        return listOf(
            "# FEATURE: $segment",
            "Status: DRAFT",
            "Verified commit: UNKNOWN",
            "Last source audit: NOT_RUN",
            "Test status: NOT_RUN",
            "",
            "## 1. Identity and scope",
            "- Package / owning module: `$packageName`",
            "- Purpose (1 line) and non-goals: TODO",
            "- Role: TODO (reference | showcase | production feature)",
            "",
            "## 2. Presentation contract (UDF — UI-01/UI-02)",
            "- Route composable (stateful entry, collects state): `${name}Route`",
            "- Screen composable (stateless; state down, actions up): `${name}Screen`",
            "- ViewModel (`@HiltViewModel`; no `BaseViewModel`, UI-03): `${name}ViewModel`",
            "- UiState (durable, includes user messages): `${name}UiState`",
            "- UiAction (sealed; every user intent): `${name}UiAction`",
            "- UiEffect (only immediate one-shot UI integration; or `none`): $effect",
            "",
            "## 3. DI and ownership",
            "- Constructor dependencies (interfaces from which module): none yet (TODO)",
            "- Hilt module / bindings and scope (DI-01, DI-02): TODO",
            "- State owner for any persisted/shared state (CFG-02): TODO",
            "",
            "## 4. Data flow",
            "TODO: Screen -> UiAction -> ViewModel -> Repository -> DataSource -> UiState (name real symbols).",
            "- Error mapping (infrastructure exception -> `AppError` -> user message; ERR-01/INV-07): TODO",
            "- Threading (`DispatcherProvider`; THR-02): TODO",
            "",
            "## 5. Navigation entry",
            "- $navigation",
            "",
            "## 6. Design system usage",
            "- `DcTheme` and `DcLoadingState` used by the generated Screen/Preview (DS-01/DS-03); TODO others.",
            "",
            "## 7. Tests and evidence",
            "- ViewModel unit test (TST-01): $testPath; Compose UI test (TST-02): TODO",
            "- Commands: TODO",
            "- Test status: NOT_RUN",
            "",
            "## 8. Known debt / decisions",
            "- none recorded",
            "",
            "## 9. AI playbook",
            "- Read → Graph → Propose → Edit → Test → Update docs → Handover.",
            "- Add this feature to `architecture/context-map.yaml` and `architecture/module-map.md` " +
                "so it is discoverable; then run `python3 tools/check_architecture.py`.",
            "- Generator: `createFeature` scaffold (`architecture/20-feature-template.md`) for sibling features."
        ).joinToString("\n")
    }

    private fun uiState(packageName: String, name: String) = """
        package $packageName

        /** Everything [${name}Screen] needs to render. Infrastructure types do not belong here. */
        data class ${name}UiState(
            val isLoading: Boolean = false,
            /** Durable until the UI acknowledges that it has displayed the message. */
            val userMessage: String? = null
        )
    """

    private fun uiAction(packageName: String, name: String, permission: Boolean) = """
        package $packageName

        /** User intents sent from [${name}Screen] to [${name}ViewModel]. */
        sealed interface ${name}UiAction {
            data object UserMessageShown : ${name}UiAction
            ${if (permission) "data object PermissionRequestClicked : ${name}UiAction" else ""}
        }
    """

    private fun uiEffect(packageName: String, name: String) = """
        package $packageName

        /** Immediate Route-owned framework integration; durable outcomes still belong in UiState. */
        sealed interface ${name}UiEffect {
            data object LaunchPermissionRequest : ${name}UiEffect
        }
    """

    private fun viewModel(packageName: String, name: String, permission: Boolean): String {
        val imports = if (permission) """
            import kotlinx.coroutines.channels.Channel
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.receiveAsFlow
            import kotlinx.coroutines.launch
        """ else ""
        val effectFields = if (permission) """
            private val _effects = Channel<${name}UiEffect>(capacity = Channel.BUFFERED)
            val effects: Flow<${name}UiEffect> = _effects.receiveAsFlow()

        """ else ""
        val permissionBranch = if (permission) """
            ${name}UiAction.PermissionRequestClicked -> viewModelScope.launch {
                _effects.send(${name}UiEffect.LaunchPermissionRequest)
            }
        """ else ""

        return """
            package $packageName

            import androidx.lifecycle.ViewModel
            ${if (permission) "import androidx.lifecycle.viewModelScope" else ""}
            import dagger.hilt.android.lifecycle.HiltViewModel
            import javax.inject.Inject
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.StateFlow
            import kotlinx.coroutines.flow.asStateFlow
            import kotlinx.coroutines.flow.update
            $imports

            @HiltViewModel
            class ${name}ViewModel @Inject constructor() : ViewModel() {
                private val _uiState = MutableStateFlow(${name}UiState())
                val uiState: StateFlow<${name}UiState> = _uiState.asStateFlow()

                $effectFields
                fun onAction(action: ${name}UiAction) {
                    when (action) {
                        ${name}UiAction.UserMessageShown ->
                            _uiState.update { it.copy(userMessage = null) }
                        $permissionBranch
                    }
                }
            }
        """
    }

    private fun route(packageName: String, name: String, permission: Boolean): String {
        val effectImport = if (permission) "import kotlinx.coroutines.flow.collectLatest" else ""
        val permissionParameter = if (permission) "onLaunchPermissionRequest: () -> Unit,\n    " else ""
        val effectCollector = if (permission) """
            LaunchedEffect(viewModel) {
                viewModel.effects.collectLatest { effect ->
                    when (effect) {
                        ${name}UiEffect.LaunchPermissionRequest -> onLaunchPermissionRequest()
                    }
                }
            }

        """ else ""
        return """
            package $packageName

            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.foundation.layout.padding
            import androidx.compose.material3.Scaffold
            import androidx.compose.material3.SnackbarHost
            import androidx.compose.material3.SnackbarHostState
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.getValue
            import androidx.compose.runtime.remember
            import androidx.compose.ui.Modifier
            import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
            import androidx.lifecycle.compose.collectAsStateWithLifecycle
            $effectImport

            /** Stateful boundary: resolves DI, collects lifecycle-aware state, and owns UI integration. */
            @Composable
            fun ${name}Route(
                $permissionParameter modifier: Modifier = Modifier,
                viewModel: ${name}ViewModel = hiltViewModel()
            ) {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(state.userMessage) {
                    state.userMessage?.let { message ->
                        snackbarHostState.showSnackbar(message)
                        viewModel.onAction(${name}UiAction.UserMessageShown)
                    }
                }

                $effectCollector
                Scaffold(
                    modifier = modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    ${name}Screen(
                        state = state,
                        onAction = viewModel::onAction,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        """
    }

    private fun screen(packageName: String, name: String) = """
        package $packageName

        import androidx.compose.foundation.layout.Box
        import androidx.compose.foundation.layout.fillMaxSize
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.Modifier
        import com.dc.android.foundation.designsystem.components.DcLoadingState

        /** Stateless feature UI. Add feature content without resolving DI or infrastructure here. */
        @Composable
        fun ${name}Screen(
            state: ${name}UiState,
            onAction: (${name}UiAction) -> Unit,
            modifier: Modifier = Modifier
        ) {
            Box(modifier = modifier.fillMaxSize()) {
                if (state.isLoading) {
                    DcLoadingState(
                        label = "Loading $name",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    """

    private fun preview(packageName: String, name: String) = """
        package $packageName

        import androidx.compose.runtime.Composable
        import androidx.compose.ui.tooling.preview.Preview
        import com.dc.android.foundation.designsystem.theme.DcTheme

        @Preview(showBackground = true)
        @Composable
        private fun ${name}ScreenPreview() {
            DcTheme {
                ${name}Screen(
                    state = ${name}UiState(isLoading = true),
                    onAction = {}
                )
            }
        }
    """

    private fun destination(packageName: String, name: String) = """
        package $packageName

        import kotlinx.serialization.Serializable

        /** Type-safe route token. The app composition root still owns the navigation engine. */
        @Serializable
        data object ${name}Destination
    """

    private fun apiService(packageName: String, name: String) = """
        package $packageName

        /**
         * Retrofit service boundary for this feature.
         * Add only endpoints backed by confirmed product requirements; the generator invents none.
         */
        interface ${name}ApiService
    """

    private fun dao(packageName: String, name: String) = """
        package $packageName

        import androidx.room.Dao

        /**
         * Room boundary for this feature.
         * Add queries only after the feature entity and source-of-truth policy are defined.
         */
        @Dao
        interface ${name}Dao
    """

    private fun workScheduler(packageName: String, name: String) = """
        package $packageName

        /** Engine-neutral scheduling boundary; the app wires a WorkManager-backed implementation. */
        fun interface ${name}WorkScheduler {
            fun schedule()
        }
    """

    private fun viewModelTest(packageName: String, name: String, permission: Boolean): String {
        val permissionImports = if (permission) """
            import kotlinx.coroutines.async
            import kotlinx.coroutines.flow.first
            import kotlinx.coroutines.test.runTest
        """ else ""
        val permissionTest = if (permission) """

            @Test
            fun `permission action emits route-owned integration effect`() = runTest {
                val viewModel = ${name}ViewModel()
                val effect = async { viewModel.effects.first() }

                viewModel.onAction(${name}UiAction.PermissionRequestClicked)

                assertEquals(${name}UiEffect.LaunchPermissionRequest, effect.await())
            }
        """ else ""
        return """
            package $packageName

            import com.dc.android.foundation.testing.MainDispatcherRule
            $permissionImports
            import org.junit.Assert.assertEquals
            import org.junit.Rule
            import org.junit.Test

            class ${name}ViewModelTest {
                @get:Rule
                val mainDispatcherRule = MainDispatcherRule()

                @Test
                fun `initial state is stable`() {
                    val viewModel = ${name}ViewModel()

                    assertEquals(${name}UiState(), viewModel.uiState.value)
                }
                $permissionTest
            }
        """
    }
}
