/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.main

import arrow.core.Try
import arrow.core.recoverWith
import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerbuilder.controller.gitmenu.LoginManager
import com.neuronrobotics.bowlerbuilder.controller.main.BowlerEventBusLogger
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.util.cloneRepoAndGetFiles
import com.neuronrobotics.bowlerbuilder.controller.util.mapGistFileToFileOnDisk
import com.neuronrobotics.bowlerbuilder.view.consoletab.ConsoleTab
import com.neuronrobotics.bowlerbuilder.view.gitmenu.GistFileSelectionView
import com.neuronrobotics.bowlerbuilder.view.gitmenu.LogInView
import com.neuronrobotics.bowlerbuilder.view.main.event.AddCadObjectsToCurrentTabEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.CloseTabByContentEvent
import com.neuronrobotics.bowlerbuilder.view.main.event.SetCadObjectsToCurrentTabEvent
import com.neuronrobotics.bowlerbuilder.view.newtab.NewTabTab
import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditorTab
import com.neuronrobotics.bowlerbuilder.view.webbrowser.WebBrowserTab
import com.neuronrobotics.bowlerkernel.util.toImmutableList
import eu.mihosoft.vrl.v3d.CSG
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import tornadofx.*
import javax.inject.Singleton
import kotlin.concurrent.thread

@Singleton
class MainWindowView : View() {

    private val controller = getInstanceOf<MainWindowController>()
    private val loginManager = getInstanceOf<LoginManager>()
    private val scriptEditorFactory = getInstanceOf<CadScriptEditorFactory>()
    private var mainTabPane: TabPane by singleAssign()
    private var logInMenu: MenuItem by singleAssign()
    private var logOutMenu: MenuItem by singleAssign()

    private var gistsMenu: Menu by singleAssign()
    private var orgsMenu: Menu by singleAssign()

    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("Exit") {
                    action {
                        close()
                        thread { MainWindowController.beginForceQuit() }
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

                menu("My Repos")

                item("Reload Menus").action { reloadMenus() }

                item("Delete local cache") {
                    action {
                        confirmation(
                            header = "Really delete local cache?",
                            content = "This will delete all local assets and unsaved work."
                        ) {
                            if (it == ButtonType.OK) {
                                runAsync { controller.deleteLocalCache() }
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
        }

        center = splitpane(orientation = Orientation.VERTICAL)
        {
            setDividerPositions(0.9)

            mainTabPane = tabpane {
                tabs += WebBrowserTab()
                tabs += NewTabTab().apply { isClosable = false }
            }

            tabpane {
                tabs += ConsoleTab().apply { isClosable = false }
            }
        }
    }

    init {
        mainUIEventBus.register(this)
        controller.gitHub = loginManager.login()
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
        reloadMenus()
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

    /**
     * Adds a tab to the [mainTabPane] and selects it.
     */
    fun addTab(tab: Tab) {
        runLater {
            mainTabPane.tabs.add(mainTabPane.tabs.size - 1, tab)
            mainTabPane.selectionModel.select(tab)
        }
    }

    /**
     * Searches for tabs by their [Tab.content] and removes all matches.
     *
     * @param cmp The [Tab.content] to search for.
     */
    fun closeTabByContent(cmp: Node) {
        val matches = mainTabPane.tabs.filter { it.content == cmp }
        runLater { mainTabPane.tabs.removeAll(matches) }
    }

    /**
     * Adds the [cad] objects to the current CAD editor. Does nothing if there is no editor
     * selected.
     */
    fun addCadObjectsToCurrentTab(cad: ImmutableSet<CSG>) {
        val selection = mainTabPane.selectionModel.selectedItem
        if (selection is CadScriptEditorTab) {
            selection.editor.cadView.engine.addAllCSGs(cad)
        }
    }

    /**
     * Sets the [cad] objects to the current CAD editor. Does nothing if there is no editor
     * selected.
     */
    fun setCadObjectsToCurrentTab(cad: ImmutableSet<CSG>) {
        val selection = mainTabPane.selectionModel.selectedItem
        if (selection is CadScriptEditorTab) {
            selection.editor.cadView.engine.clearCSGs()
            selection.editor.cadView.engine.addAllCSGs(cad)
        }
    }

    /**
     * Returns the currently selected tab.
     */
    fun getSelectedTab() = mainTabPane.selectionModel.selectedItem

    /**
     * Reloads the git-dependent menus.
     */
    fun reloadMenus() {
        reloadGists()
        reloadOrgs()
    }

    /**
     * Reloads the gists menu.
     */
    fun reloadGists() {
        runLater {
            gistsMenu.items.clear()
            with(gistsMenu) {
                runAsync {
                    controller.gitHub.map {
                        it.myself.listGists().toImmutableList()
                    }
                } success {
                    it.map {
                        it.forEach { gist ->
                            menu(gist.description) {
                                gist.files.values.forEach { gistFile ->
                                    item(gistFile.fileName).action {
                                        thread(isDaemon = true) {
                                            mapGistFileToFileOnDisk(
                                                gist, gistFile
                                            ).recoverWith {
                                                cloneRepoAndGetFiles(
                                                    controller.credentials,
                                                    gist.gitPullUrl
                                                ).flatMap {
                                                    Try {
                                                        it.first { it.name == gistFile.fileName }
                                                    }
                                                }
                                            }.map {
                                                controller.openGistFile(gist.gitPullUrl, it)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Reloads the organizations menu.
     */
    fun reloadOrgs() {
        runLater {
            orgsMenu.items.clear()
            with(orgsMenu) {
                runAsync {
                    controller.gitHub.map {
                        it.myself.organizations
                    }
                } success {
                    it.map {
                        it.forEach { org ->
                            menu(org.login) {
                                org.repositories.values.forEach { repo ->
                                    item(repo.name).action {
                                        addTab(WebBrowserTab(repo.httpTransportUrl))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        val mainUIEventBus = EventBus.builder()
            .sendNoSubscriberEvent(false)
            .logger(BowlerEventBusLogger("MainUIEventBus"))
            .build()
    }
}
