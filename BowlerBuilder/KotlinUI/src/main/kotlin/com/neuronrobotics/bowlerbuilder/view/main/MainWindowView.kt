/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.view.main

import arrow.core.extensions.`try`.monadThrow.bindingCatch
import arrow.core.handleError
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerbuilder.controller.gitmenu.LoginManager
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.controller.util.cloneAssetRepo
import com.neuronrobotics.bowlerbuilder.view.ConsoleTab
import com.neuronrobotics.bowlerbuilder.view.ReportIssueView
import com.neuronrobotics.bowlerbuilder.view.cad.CadView
import com.neuronrobotics.bowlerbuilder.view.gitmenu.GistFileSelectionView
import com.neuronrobotics.bowlerbuilder.view.gitmenu.LogInView
import com.neuronrobotics.bowlerbuilder.view.main.event.AddCadObjectsToCurrentTabEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.CadViewExplodedEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.CloseTabByContentEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.SetCadObjectsToCurrentTabEvent
import com.neuronrobotics.bowlerbuilder.view.newtab.NewTabTab
import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditorTab
import com.neuronrobotics.bowlerbuilder.view.webbrowser.WebBrowserTab
import eu.mihosoft.vrl.v3d.CSG
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import org.greenrobot.eventbus.Subscribe
import org.octogonapus.ktguava.collections.toImmutableList
import tornadofx.*
import java.io.FileNotFoundException
import javax.inject.Singleton
import kotlin.concurrent.thread

@SuppressWarnings("TooManyFunctions", "LargeClass")
@Singleton
class MainWindowView : View() {

    private val controller = getInstanceOf<MainWindowController>()
    private val loginManager = getInstanceOf<LoginManager>()
    private val scriptEditorFactory = getInstanceOf<CadScriptEditorFactory>()
    private var mainTabPane: TabPane by singleAssign()
    private var bottomTabPane: TabPane by singleAssign()
    private var logInMenu: MenuItem by singleAssign()
    private var logOutMenu: MenuItem by singleAssign()

    private var gistsMenu: Menu by singleAssign()
    private var orgsMenu: Menu by singleAssign()
    private var reposMenu: Menu by singleAssign()

    override val root = borderpane {
        setPrefSize(800.0, 600.0)
        usePrefHeight = true
        usePrefWidth = true

        top = menubar {
            menu("File") {
                item("Exit") {
                    action {
                        close()
                        thread(isDaemon = true) { MainWindowController.beginForceQuit() }
                    }
                }
            }

            menu("Git") {
                logInMenu = item("Log In") {
                    action { LogInView().openModal() }
                    enableWhen(!loginManager.isLoggedInProperty)
                }

                logOutMenu = item("Log Out") {
                    action { loginManager.logout() }
                    enableWhen(loginManager.isLoggedInProperty)
                }

                gistsMenu = menu("My Gists")

                orgsMenu = menu("My Orgs")

                reposMenu = menu("My Repos")

                item("Reload Menus").action { reloadMenus() }

                item("Delete local cache") {
                    action {
                        confirmation(
                            header = "Really delete local cache?",
                            content = "This will delete all local assets and unsaved work."
                        ) {
                            if (it == ButtonType.OK) {
                                runAsync { controller.deleteGitCache() }
                            }
                        }
                    }
                }
            }

            menu("3D CAD") {
                item("Scratchpad").action {
                    runAsync { scriptEditorFactory.createAndOpenScratchpad() }
                }

                item("Load File from Git").action {
                    GistFileSelectionView.create().openModal()
                }
            }

            menu("Help") {
                item("Report Issue").action {
                    ReportIssueView().openModal()
                }
            }
        }

        center = splitpane(orientation = Orientation.VERTICAL) {
            setDividerPositions(0.8)
            mainTabPane = tabpane {}

            vbox {
                bottomTabPane = tabpane {
                    vgrow = Priority.ALWAYS
                }

                hbox {
                    padding = Insets(0.0, 0.0, 0.0, 2.0)
                    spacing = 5.0

                    this += RunningActionsView()
                    this += RunningScriptsView()
                }
            }
        }
    }

    init {
        MainWindowController.mainUIEventBus.register(this)
        controller.gitHub = loginManager.login()

        // Clone the asset repo before anything else loads if the user is logged in
        controller.gitHub.map {
            cloneAssetRepo()
        }

        runLater { mainTabPane.tabs += NewTabTab().apply { isClosable = false } }
        addTab(WebBrowserTab())
        addBottomTab(ConsoleTab().apply { isClosable = false })

        reloadMenus()
//        addTab(
//            Tab(
//                "",
//                CreatureConfigurationView(
//                    (ScriptingEngine.gitScriptRun(
//                        "https://gist.github.com/65ac76aeb898d2c00867b7b8397367e9.git",
//                        "HephaestusWorkCell_copy.xml",
//                        null
//                    ) as MobileBaseLoader).base
//                ).root
//            )
//        )
    }

    @Subscribe
    fun onAddTabEvent(event: AddTabEvent) = addTab(event.tab)

    @Subscribe
    fun onCloseTabByContentEvent(event: CloseTabByContentEvent) =
        closeTabByContent(event.tabContent)

    @Subscribe
    fun onAddCadObjectsToCurrentTabEvent(event: AddCadObjectsToCurrentTabEvent) =
        addCadObjectsToCurrentTab(event.cad)

    @Subscribe
    fun onSetCadObjectsToCurrentTabEvent(event: SetCadObjectsToCurrentTabEvent) =
        setCadObjectsToCurrentTab(event.cad)

    @Subscribe
    fun onCadViewExplodedEvent(event: CadViewExplodedEvent) {
        val selection = mainTabPane.selectionModel.selectedItem
        if (selection is CadScriptEditorTab) {
            val newCadView = CadView()
            newCadView.engine.addAllCSGs(selection.editor.cadView.engine.getCsgMap().keys)
            selection.editor.cadView.replaceWith(newCadView)
            selection.editor.setRegenerateRoot(newCadView)
        }
    }

    /**
     * Adds a tab to the [mainTabPane] and selects it.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun addTab(tab: Tab) {
        runLater {
            mainTabPane.tabs.add(mainTabPane.tabs.size - 1, tab)
            mainTabPane.selectionModel.select(tab)
        }
    }

    /**
     * Adds a tab to the [bottomTabPane] and selects it.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun addBottomTab(tab: Tab) {
        runLater {
            bottomTabPane.tabs.add(tab)
            bottomTabPane.selectionModel.select(tab)
        }
    }

    /**
     * Searches for tabs by their [Tab.content] and all of the content's children and removes all
     * matches.
     *
     * @param cmp The [Tab.content] to search for.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun closeTabByContent(cmp: Node) {
        fun removeMatches(tabPane: TabPane) {
            fun findMatchInChildren(node: Node): Boolean =
                node == cmp || node.getChildList()?.fold(false) { acc, elem ->
                    acc || findMatchInChildren(elem)
                } ?: false

            val matches = tabPane.tabs.filter { findMatchInChildren(it.content) }
            runLater { tabPane.tabs.removeAll(matches) }
        }

        removeMatches(mainTabPane)
        removeMatches(bottomTabPane)
    }

    /**
     * Adds the [cad] objects to the current CAD editor. If there is no CAD viewer open, then a
     * new one is opened.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun addCadObjectsToCurrentTab(cad: ImmutableSet<CSG>) {
        when (val selection = mainTabPane.selectionModel.selectedItem) {
            is CadScriptEditorTab -> selection.editor.cadView.engine.addAllCSGs(cad)
            else -> addTab(Tab("CAD", CadView().let {
                it.engine.addAllCSGs(cad)
                it.root
            }))
        }
    }

    /**
     * Sets the [cad] objects to the current CAD editor. If there is no CAD viewer open, then a
     * new one is opened.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setCadObjectsToCurrentTab(cad: ImmutableSet<CSG>) {
        when (val selection = mainTabPane.selectionModel.selectedItem) {
            is CadScriptEditorTab -> {
                selection.editor.cadView.engine.clearCSGs()
                selection.editor.cadView.engine.addAllCSGs(cad)
            }

            else -> addTab(Tab("CAD", CadView().let {
                it.engine.clearCSGs()
                it.engine.addAllCSGs(cad)
                it.root
            }))
        }
    }

    /**
     * Returns the currently selected tab.
     */
    @Suppress("unused")
    fun getSelectedTab(): Tab = mainTabPane.selectionModel.selectedItem

    /**
     * Reloads the git-dependent menus.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun reloadMenus() {
        reloadGists()
        reloadOrgs()
        reloadRepos()
    }

    /**
     * Reloads the gists menu.
     */
    @SuppressWarnings("LabeledExpression")
    fun reloadGists() {
        thread(isDaemon = true) {
            controller.ideAction("Reload Gists") {
                bindingCatch {
                    val (gitHub) = controller.gitHub
                    val gists = gitHub.myself.listGists().toImmutableList()
                    val items = gists.map { gist ->
                        Menu(gist.description).apply {
                            gist.files.values.forEach { gistFile ->
                                item(gistFile.fileName).action {
                                    thread(isDaemon = true) {
                                        controller.openGistFile(gist, gistFile)
                                    }
                                }
                            }
                        }
                    }

                    runLater {
                        gistsMenu.items.clear()
                        gistsMenu.items.addAll(items)
                    }
                }.handleError {
                    // FileNotFoundException is typically a failure to log in
                    val exceptionString = when (it) {
                        is FileNotFoundException -> it.localizedMessage
                        else -> Throwables.getStackTraceAsString(it)
                    }

                    LOGGER.warning {
                        """
                        |Could not reload gists:
                        |$exceptionString
                        """.trimMargin()
                    }
                }
            }
        }
    }

    /**
     * Reloads the organizations menu.
     */
    @SuppressWarnings("LabeledExpression")
    fun reloadOrgs() {
        thread(isDaemon = true) {
            controller.ideAction("Reload Orgs") {
                bindingCatch {
                    val (gitHub) = controller.gitHub
                    val orgs = gitHub.myself.organizations
                    val items = orgs.map { org ->
                        Menu(org.login).apply {
                            org.repositories.values.forEach { repo ->
                                item(repo.name).action {
                                    addTab(WebBrowserTab(repo.httpTransportUrl))
                                }
                            }
                        }
                    }

                    runLater {
                        orgsMenu.items.clear()
                        orgsMenu.items.addAll(items)
                    }
                }.handleError {
                    // FileNotFoundException is typically a failure to log in
                    val exceptionString = when (it) {
                        is FileNotFoundException -> it.localizedMessage
                        else -> Throwables.getStackTraceAsString(it)
                    }

                    LOGGER.warning {
                        """
                        |Could not reload organizations:
                        |$exceptionString
                        """.trimMargin()
                    }
                }
            }
        }
    }

    /**
     * Reloads the repositories menu.
     */
    @SuppressWarnings("LabeledExpression")
    fun reloadRepos() {
        thread(isDaemon = true) {
            controller.ideAction("Reload Repos") {
                bindingCatch {
                    val (gitHub) = controller.gitHub
                    val repos = gitHub.myself.listRepositories().toImmutableList()
                    val items = repos.map { repo ->
                        MenuItem(repo.name).apply {
                            action {
                                addTab(WebBrowserTab(repo.httpTransportUrl))
                            }
                        }
                    }

                    runLater {
                        reposMenu.items.clear()
                        reposMenu.items.addAll(items)
                    }
                }.handleError {
                    // FileNotFoundException is typically a failure to log in
                    val exceptionString = when (it) {
                        is FileNotFoundException -> it.localizedMessage
                        else -> Throwables.getStackTraceAsString(it)
                    }

                    LOGGER.warning {
                        """
                        |Could not reload repositories:
                        |$exceptionString
                        """.trimMargin()
                    }
                }
            }
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(MainWindowView::class.java.simpleName)
    }
}
