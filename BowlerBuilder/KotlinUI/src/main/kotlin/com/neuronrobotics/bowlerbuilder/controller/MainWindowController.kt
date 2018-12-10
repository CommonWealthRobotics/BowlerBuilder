/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.model.Gist
import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.model.Organization
import com.neuronrobotics.bowlerbuilder.model.Repository
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.kinematicschef.util.toImmutableList
import tornadofx.*

class MainWindowController : Controller() {

    private val scriptEditorFactory: ScriptEditorFactory by di()

    fun loadUserGists(): ImmutableList<Gist> {
        return ScriptingEngine.getGithub()
            .myself
            .listGists()
            .map {
                Gist(
                    gitUrl = it.gitPushUrl,
                    description = it.description
                )
            }.toImmutableList()
    }

    fun loadFilesInGist(gist: Gist): ImmutableList<GistFile> {
        return ScriptingEngine.filesInGit(gist.gitUrl)
            .map {
                GistFile(gist, it)
            }.toImmutableList()
    }

    fun openGistFile(file: GistFile) {
        scriptEditorFactory.createAndOpenScriptEditor(file)
    }

    fun loadUserOrgs(): ImmutableList<Organization> {
        return ScriptingEngine.getGithub()
            .myOrganizations
            .map {
                Organization(
                    gitUrl = it.value.htmlUrl,
                    name = it.key,
                    repositories = it.value.repositories
                        .map {
                            Repository(
                                gitUrl = it.value.gitTransportUrl,
                                name = it.key
                            )
                        }.toImmutableList()
                )
            }.toImmutableList()
    }
}
