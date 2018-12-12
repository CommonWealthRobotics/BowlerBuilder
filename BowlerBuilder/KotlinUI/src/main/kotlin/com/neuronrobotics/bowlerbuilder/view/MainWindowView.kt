/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.controller.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.gitmenu.LoginManager
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.view.consoletab.ConsoleTab
import com.neuronrobotics.bowlerbuilder.view.gitmenu.GistFileSelectionView
import com.neuronrobotics.bowlerbuilder.view.gitmenu.LogInView
import com.neuronrobotics.bowlerbuilder.view.newtab.NewTabTab
import com.neuronrobotics.bowlerbuilder.view.webbrowser.WebBrowserTab
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ButtonType
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.*
import kotlin.concurrent.thread

class MainWindowView : View() {

    private val controller: MainWindowController by inject()
    private val loginManager: LoginManager by di()
    private val scriptEditorFactory: ScriptEditorFactory by di()
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
                        thread { controller.beginForceQuit() }
                    }
                }
            }

            menu("Git") {
                logInMenu = item("Log In") {
                    action { find<LogInView>().openModal() }
                    enableWhen(!loginManager.isLoggedInProperty)
                }

                logOutMenu = item("Log Out") {
                    action { loginManager.logout() }
                    enableWhen(loginManager.isLoggedInProperty)
                }

                gistsMenu = menu("My Gists")

                orgsMenu = menu("My Orgs")

                menu("My Repos")

                item("Reload Menus") { action { reloadMenus() } }

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
                item("Scratchpad") {
                    action {
                        runAsync { scriptEditorFactory.createAndOpenScratchpad() }
                    }
                }

                item("Load File from Git") {
                    action {
                        find<GistFileSelectionView>().openModal()
                    }
                }
            }
        }

        center = splitpane {
            orientation = Orientation.VERTICAL
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
        reloadMenus()
    }

    /**
     * Adds a tab to the [mainTabPane] and selects it.
     */
    fun addTab(tab: Tab) {
        mainTabPane.tabs.add(mainTabPane.tabs.size - 1, tab)
        mainTabPane.selectionModel.select(tab)
    }

    /**
     * Searches for tabs by their [Tab.content] and removes all matches.
     *
     * @param cmp The [Tab.content] to search for.
     */
    fun closeTabByContent(cmp: Node) {
        val matches = mainTabPane.tabs.filter { it.content == cmp }
        mainTabPane.tabs.removeAll(matches)
    }

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
                    controller.loadUserGists()
                } success { gists ->
                    gists.forEach { gist ->
                        menu(gist.description) {
                            runAsync {
                                controller.loadFilesInGist(gist)
                            } success { gistFiles ->
                                gistFiles.forEach { file ->
                                    item(file.filename) {
                                        action {
                                            runAsync {
                                                controller.openGistFile(file)
                                            }
                                        }
                                    }
                                }
                            }

                            action {
                                addTab(WebBrowserTab(gist.gitUrl))
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
                    controller.loadUserOrgs()
                } success { orgs ->
                    orgs.forEach { org ->
                        menu(org.name) {
                            org.repositories.forEach { repo ->
                                item(repo.name) {
                                    action {
                                        addTab(WebBrowserTab(repo.gitUrl))
                                    }
                                }
                            }

                            action {
                                addTab(WebBrowserTab(org.gitUrl))
                            }
                        }
                    }
                }
            }
        }
    }
}
