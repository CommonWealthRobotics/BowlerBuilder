package com.neuronrobotics.bowlerbuilder.view.util

import com.neuronrobotics.bowlerbuilder.FxUtil
import javafx.beans.value.ChangeListener
import javafx.scene.control.TreeItem
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository

object GitHubRepoFileTree {

    @JvmStatic
    fun getTreeItemForRepo(repo: GHRepository): TreeItem<String> {
        val repoItem = TreeItem(repo.name)

        // Temporary item so repoItem has a dropdown arrow
        repoItem.children.add(TreeItem())

        repoItem.expandedProperty().addListener { _, _, new ->
            if (new) {
                FxUtil.runFXAndWait {
                    repo.getDirectoryContent(".").map {
                        getTreeItemForRepoContent(repo, it, null)
                    }.let { repoItem.children.setAll(it) }
                }
            }
        }

        return repoItem
    }

    private fun getTreeItemForRepoContent(rootRepo: GHRepository, content: GHContent, parentItem: TreeItem<String>?): TreeItem<String> {
        val contentItem = TreeItem(content.name)

        if (content.isDirectory) {
            // Temporary item so contentItem has a dropdown arrow
            contentItem.children.add(TreeItem())

            var listener: ChangeListener<Boolean>? = null

            listener = ChangeListener { _, _, new ->
                if (new) {
                    FxUtil.runFXAndWait {
                        val directoryContent = content.listDirectoryContent().asList()
                        if (parentItem != null && directoryContent.size == 1 && directoryContent.first().isDirectory) {
                            parentItem.value = "${parentItem.value}.${content.name}"
                            parentItem.children.setAll(getTreeItemForRepoContent(rootRepo, directoryContent.first(), parentItem))
                        } else {
                            directoryContent.map {
                                getTreeItemForRepoContent(rootRepo, it, contentItem)
                            }.let { contentItem.children.setAll(it) }
                        }

                        /**
                         * Remove the listener so it won't re-expand incorrectly. If the user wants
                         * to reload the files, they should reload the git menus.
                         */
                        contentItem.expandedProperty().removeListener(listener)
                    }
                }
            }

            contentItem.expandedProperty().addListener(listener)
        }

        return contentItem
    }
}
