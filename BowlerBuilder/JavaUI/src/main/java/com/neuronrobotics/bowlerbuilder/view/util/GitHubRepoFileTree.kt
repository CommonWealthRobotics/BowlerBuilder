package com.neuronrobotics.bowlerbuilder.view.util

import javafx.beans.value.ChangeListener
import javafx.scene.control.TreeItem
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository

object GitHubRepoFileTree {

    /**
     * Generate a [TreeItem] for a [GHRepository] containing the repo name and all of its contents.
     */
    @JvmStatic
    fun getTreeItemForRepo(repo: GHRepository): TreeItem<String> {
        val repoItem = TreeItem(repo.name)

        // Temporary item so repoItem has a dropdown arrow
        repoItem.children.add(TreeItem())

        repoItem.expandedProperty().addListener { _, _, new ->
            if (new) {
                repo.getDirectoryContent(".").map {
                    getTreeItemForRepoContent(repo, it, null, null)
                }.let { repoItem.children.setAll(it) }
            }
        }

        return repoItem
    }

    private fun getTreeItemForRepoContent(
        rootRepo: GHRepository,
        content: GHContent,
        parentItem: TreeItem<String>?,
        parentContent: GHContent?
    ): TreeItem<String> {
        val contentItem = TreeItem(content.name)

        if (content.isDirectory) {
            // Temporary item so contentItem has a dropdown arrow
            contentItem.children.add(TreeItem())

            var listener: ChangeListener<Boolean>? = null

            listener = ChangeListener { _, _, new ->
                if (new) {
                    val directoryContent = content.listDirectoryContent().asList()
                    if (parentItem != null &&
                            parentContent != null &&
                            parentContent.listDirectoryContent().asList().size == 1) {
                        parentItem.value = "${parentItem.value}.${content.name}"
                        directoryContent.map {
                            getTreeItemForRepoContent(rootRepo, it, parentItem, parentContent)
                        }.let {
                            parentItem.children.setAll(it)
                        }
                    } else {
                        directoryContent.map {
                            getTreeItemForRepoContent(rootRepo, it, contentItem, content)
                        }.let { contentItem.children.setAll(it) }
                    }

                    /**
                     * Remove the listener so it won't re-expand incorrectly. If the user wants
                     * to reload the files, they should reload the git menus.
                     */
                    contentItem.expandedProperty().removeListener(listener)
                }
            }

            contentItem.expandedProperty().addListener(listener)
        }

        return contentItem
    }
}
