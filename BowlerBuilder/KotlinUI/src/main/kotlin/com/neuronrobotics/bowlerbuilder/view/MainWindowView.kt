/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.LoggerUtilities
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
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.*
import java.util.Timer
import java.util.TimerTask
import java.util.logging.Level

class MainWindowView : View() {

    private val controller: MainWindowController by inject()
    private val loginManager: LoginManager by di()
    private val scriptEditorFactory: ScriptEditorFactory by di()
    private var mainTabPane: TabPane by singleAssign()
    private var logInMenu: MenuItem by singleAssign()
    private var logOutMenu: MenuItem by singleAssign()

    private var gistsMenu: Menu by singleAssign()

    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("Exit") {
                    action {
                        close()
                        beginForceQuit()
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

                menu("My Orgs")

                menu("My Repos")

                item("Reload Menus")

                item("Delete local cache")
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
        reloadGists()
    }

    fun addTab(tab: Tab) {
        mainTabPane.tabs.add(mainTabPane.tabs.size - 1, tab)
        mainTabPane.selectionModel.select(tab)
    }

    fun closeTabByContent(cmp: Node) {
        val matches = mainTabPane.tabs.filter { it.content == cmp }
        mainTabPane.tabs.removeAll(matches)
    }

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

    private fun beginForceQuit() {
        // Need to make sure the VM exits; sometimes a rouge thread is running
        // Wait 10 seconds before killing the VM
        val timer = Timer(true)
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    LOGGER.severe {
                        "Still alive for some reason. Printing threads and killing VM..."
                    }

                    val threads = Thread.getAllStackTraces().keys
                    val threadString = StringBuilder()
                    threads.forEach { item -> threadString.append(item).append("\n") }
                    LOGGER.log(Level.FINE, threadString.toString())

                    Runtime.getRuntime().exit(1) // Abnormal exit
                }
            },
            10000
        )
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(MainWindowView::class.java.simpleName)
    }
}
